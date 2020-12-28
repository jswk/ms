/*
 * Copyright 2021 A2S
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.a2s.ms.core.orch;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import lombok.val;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.util.FastMath;
import pl.a2s.ms.core.analysis.Analyser;
import pl.a2s.ms.core.clu.Cluster;
import pl.a2s.ms.core.clu.Ellipsoid;
import pl.a2s.ms.core.clu.NearestBetterClusterer;
import pl.a2s.ms.core.cmaes.CMAESOptimizer;
import pl.a2s.ms.core.conf.FitnessExtractorEnabledOrchestrator;
import pl.a2s.ms.core.conf.OrchestratorConfigurer;
import pl.a2s.ms.core.conf.RandEnabledOrchestrator;
import pl.a2s.ms.core.ie.FitnessExtractor;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.ind.SimpleIndividual;
import pl.a2s.ms.core.obj.ObjectiveCalculator;
import pl.a2s.ms.core.orch.hgs.LbaState;
import pl.a2s.ms.core.orch.nea2.CMAState;
import pl.a2s.ms.core.orch.nea2.NEA2RunState;
import pl.a2s.ms.core.orch.nea2.NEA2State;
import pl.a2s.ms.core.util.ArraysUtil;
import pl.a2s.ms.core.util.RandomSampleGenerator;
import pl.a2s.ms.core.util.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Log
public class NEA2Orchestrator implements Orchestrator,
                                         BenchmarkEnabledOrchestrator,
                                         RandEnabledOrchestrator,
                                         FitnessExtractorEnabledOrchestrator,
                                         LbaEnabledOrchestrator {

    @Getter @Setter private Random rand = new Random();
    @Getter @Setter private ObjectiveCalculator objectiveCalculator;
    @Getter @Setter private IndividualEvaluator individualEvaluator;
    @Getter @Setter private FitnessExtractor fitnessExtractor;

    @Getter @Setter private Analyser analyser;

    @Getter public NEA2State state = new NEA2State();

    private final Supplier<String> budgetReporter = () -> format("Budget %d/%d evals", individualEvaluator.getEvaluationCount(), state.getBudget());

    private final LbaExecutor lbaExecutor;

    public NEA2Orchestrator(OrchestratorConfigurer oc) {
        oc.configure(this);
        lbaExecutor = new LbaExecutor(rand, objectiveCalculator, individualEvaluator);
    }

    @Override
    public void setDomain(Range[] domain) {
        state.setDomain(domain);
    }

    @Override
    public Range[] getDomain() {
        return state.getDomain();
    }

    @Override
    public void setLbaState(LbaState lbaState) {
        state.setLbaState(lbaState);
    }

    private void nextRun() {
        // Alg. 6: NEA2 (Preuss2015)
        // 1. distribute an evenly spread sample over the search space
        final Range[] domain = state.getDomain();
        final int dimCount = domain.length;
        final int sampleSize = 50 * dimCount;
        final double initialSigma = state.getInitialSigma();
        final double stopFitness = state.getStopFitness();
        final int lambda = 4 + (int) FastMath.floor(3 * FastMath.log(dimCount));
        final List<Individual> inds = new ArrayList<>(sampleSize);

        final NEA2RunState runState = new NEA2RunState();
        state.getRuns().add(runState);
        runState.setSampledIndividuals(inds);

        {
            final double[][] sample = new RandomSampleGenerator(rand).uniformSample(sampleSize, domain);
            final Population population = new Population(sample.length);
            final Individual[] individuals = population.getIndividuals();
            for (int i = 0; i < sample.length; i++) {
                individuals[i] = new SimpleIndividual(sample[i]);
                inds.add(individuals[i]);
            }
            log.info(format("Sampled %d individuals", sample.length));
            individualEvaluator.evaluate(population);
            if (budgetExceeded()) {
                log.info(format("%s exceeded", budgetReporter.get()));
                return;
            }
        }

        final double[] lB = new double[dimCount];
        final double[] uB = new double[dimCount];
        for (int i = 0; i < dimCount; i++) {
            lB[i] = domain[i].getStart();
            uB[i] = domain[i].getEnd();
        }

        // 2. apply NBC; separate sample into populations according to clusters
        final NearestBetterClusterer nbc = NearestBetterClusterer.createForParameters(dimCount, inds.size());
        final List<Cluster> clusters = nbc.clusterize(inds);
        runState.setNbcClusters(clusters);
        log.info(format("Resulting clusters: %d", clusters.size()));

        // 3. forall the populations do
        for (int i = 0; i < clusters.size(); i++) {
            val idStr = String.format("Cluster %d", i);
            log.info(format("%s: initializing", idStr));
            val cluster = clusters.get(i);
            // 4.   run a CMA-ES until any CMA-ES stop criterion is hit
            final CMAESOptimizer optimizer = new CMAESOptimizer(stopFitness, rand, individualEvaluator, fitnessExtractor);
            optimizer.initialize(
                    new CMAESOptimizer.PopulationSize(lambda),
                    new CMAESOptimizer.Sigma(ArraysUtil.constant(domain.length, initialSigma)),
                    new CMAESOptimizer.FirstPopulation(cluster.getIndividuals().toArray(new Individual[0])),
                    new SimpleBounds(lB, uB),
                    GoalType.MINIMIZE
                    );

            final CMAState cmaState = new CMAState();
            cmaState.setOptimizer(optimizer);
            runState.getCmaState().add(cmaState);

            while (true) {
                log.info(format("%s: step %d", idStr, optimizer.getIterations()));
                final Population curr = optimizer.runOneStep();
                if (curr != null) {
                    cmaState.getPopulations().add(curr);
                }
                if (budgetExceeded()) {
                    log.info(format("%s: %s exceeded", idStr, budgetReporter.get()));
                    return;
                }
                if (curr == null) {
                    log.info(format("%s: CMA-ES stopping condition hit", idStr));
                    break;
                }
            }
        }
    }

    public void run() {
        if (!analyser.supports(this)) {
            throw new RuntimeException("Analyser incompatible with orchestrator");
        }
        // 5. if !termination then (termination based on budget being used up)
        while (!budgetExceeded()) {
            // 6. goto step 1
            log.info(format("Run %d", state.getRuns().size()));
            nextRun();
        }
        if (state.getLbaState() != null && lbaExecutor != null) {
            state.getLbaState().setClusters(computeClusters());
            lbaExecutor.run(state.getLbaState());
//            runLbaPhase();
        }
        analyser.analyse(state);
    }

    private boolean budgetExceeded() {
        return individualEvaluator.getEvaluationCount() >= state.getBudget();
    }

    private List<Cluster> computeClusters() {
        return state.getRuns().stream()
                .flatMap(run -> run.getCmaState().stream())
                .filter(cmaState -> !cmaState.getPopulations().isEmpty())
                .map(cmaState -> cmaState.getPopulations().get(cmaState.getPopulations().size() - 1))
                .map(population -> {
                    final Cluster cluster = new Cluster();
                    cluster.getIndividuals().addAll(Arrays.asList(population.getIndividuals()));
                    return cluster;
                }).collect(Collectors.toList());
    }


    private List<Cluster> computeClustersWithCmaEllipsoids() {
        final List<Ellipsoid> ellipsoids = new ArrayList<>();
        state.getRuns().stream()
            .flatMap(run -> run.getCmaState().stream())
            .forEach(cmaState -> {
                final CMAESOptimizer cmaesOptimizer = cmaState.getOptimizer();
                try {
                    ellipsoids.add(Ellipsoid.builder()
                        .mean(cmaesOptimizer.getXmean())
                        .invCov(MatrixUtils.inverse(cmaesOptimizer.getC()))
                        .sigma(cmaesOptimizer.getSigma())
                        .build());
                } catch (final Exception ex) {
                    // ignore, the ellipsoid doesn't exist in this case
                }
            });
        final List<Cluster> clusters = new ArrayList<>();
        state.getRuns().stream().flatMap(run -> run.getCmaState().stream())
            .map(CMAState::getPopulations)
            .map(pops -> pops.stream()
                .flatMap(pop -> Arrays.stream(pop.getIndividuals()))
                .collect(Collectors.toList())
            ).forEach(inds -> {
                final Cluster cluster = new Cluster();
                final List<Individual> cluInds = cluster.getIndividuals();
                for (final Individual ind: inds) {
                    final RealMatrix point = MatrixUtils.createColumnRealMatrix(ind.getPoint());
                    for (final Ellipsoid el: ellipsoids) {
                        if (el.mahalanobisDistance(point) <= 1) {
                            cluInds.add(ind);
                        }
                    }
                }
                if (cluInds.size() > 0) {
                    clusters.add(cluster);
                }
            });
        return clusters;
    }

}
