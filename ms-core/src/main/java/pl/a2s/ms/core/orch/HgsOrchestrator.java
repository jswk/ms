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
import pl.a2s.ms.core.analysis.Analyser;
import pl.a2s.ms.core.analysis.NullAnalyser;
import pl.a2s.ms.core.archive.Archive;
import pl.a2s.ms.core.archive.RankingArchive;
import pl.a2s.ms.core.clu.*;
import pl.a2s.ms.core.cmaes.CMAES;
import pl.a2s.ms.core.conf.FitnessExtractorEnabledOrchestrator;
import pl.a2s.ms.core.conf.OrchestratorConfigurer;
import pl.a2s.ms.core.conf.RandEnabledOrchestrator;
import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.ea.MWEA;
import pl.a2s.ms.core.ie.FitnessExtractor;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.ie.ParallelIndividualEvaluator;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.lsc.LocalStopCondition;
import pl.a2s.ms.core.obj.ObjectiveCalculator;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.LbaState;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.sprout.generator.Sprouter;
import pl.a2s.ms.core.sprout.reducer.SproutReducer;
import pl.a2s.ms.core.util.ArraysUtil;
import pl.a2s.ms.core.util.Pair;
import pl.a2s.ms.core.util.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Log
public class HgsOrchestrator implements Orchestrator,
                                        BenchmarkEnabledOrchestrator,
                                        RandEnabledOrchestrator,
                                        FitnessExtractorEnabledOrchestrator,
                                        LbaEnabledOrchestrator {
    @Getter @Setter public Random rand = new Random();
    @Getter public final HgsState state = new HgsState();
    @Getter @Setter public ObjectiveCalculator objectiveCalculator;
    @Getter @Setter public FitnessExtractor fitnessExtractor;
    @Getter @Setter public IndividualEvaluator individualEvaluator;
    @Getter @Setter public Analyser analyser = new NullAnalyser();

    private final LbaExecutor lbaExecutor;

    public HgsOrchestrator(OrchestratorConfigurer oc) {
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

    public void run() {
        if (!analyser.supports(this)) {
            throw new RuntimeException("Analyser incompatible with orchestrator");
        }

        try {
            runHgs();
            final LbaState lbaState = state.getLbaState();
            if (lbaState != null && lbaExecutor != null) {
                final Archive archive = state.getArchive();
                if (archive instanceof RankingArchive) {
                    final RankingArchive ra = (RankingArchive) archive;
                    ra.setMaxRank(-1);
                }
                lbaState.setClusters(computeClusters());
                lbaExecutor.run(lbaState);
                if (lbaState.isRunPostInverted()) {
                    runPostInvertedLbaPhase();
                }
            }
        } finally {
            analyser.analyse(state);
        }
    }

    private void runHgs() {
        individualEvaluator.evaluate(state.getHgsDemes()[0].getDemes().get(0).getPopulation());
        while (!state.getGlobalStopCondition().shouldStop(state)) {
            log.info(format("Starting epoch %d", state.getEpoch()));
            StringBuilder format = new StringBuilder("Demes:");
            final Object[] formatArgs = new Object[state.getHgsDemes().length*2];
            for (int i = 0; i < state.getHgsDemes().length; i++) {
                format.append(" %d/%d");
                formatArgs[2*i] = (int) state.getHgsDemes()[i].getDemes().stream().filter(deme -> !deme.isStopped()).count();
                formatArgs[2*i+1] = state.getHgsDemes()[i].getDemes().size();
            }
            log.info(format(format.toString(), formatArgs));
            runOneStepOfHgs();
            state.setEpoch(state.getEpoch()+1);
        }
    }

    private void runOneStepOfHgs() {
        final Archive archive = state.getArchive();
        // run metaepochs
        for (final Level level: state.getHgsDemes()) {
            setPrecisionIfPossible(level);
            final EvoAlg alg = level.getEvoAlg();
            for (final Deme deme: level.getDemes()) {
                if (!deme.isStopped()) {
                    final Population population = alg.apply(level, deme, state);
                    if (population != null) {
                        individualEvaluator.evaluate(population);
                        deme.getHistory().add(Deme.HistoryItem.builder()
                                .epoch(state.getEpoch())
                                .population(deme.getPopulation())
                                .build());
                        deme.setPopulation(population);

                        if (archive.isEnabled() && level.isArchived()) {
                            archive.addAllFrom(deme);
                        }
                    }
                }
            }
        }
        if (archive.isEnabled()) {
            log.info("Archive size " + archive.getIndividuals().size()
                    + ", actual max rank " + archive.getActualMaxRank());
        }
        // check stopping conditions
        for (final Level level: state.getHgsDemes()) {
            setPrecisionIfPossible(level);
            final LocalStopCondition lsc = level.getStopCondition();
            for (final Deme deme: level.getDemes()) {
                if (!deme.isStopped() && lsc.shouldStop(deme, state)) {
                    deme.setStopped(true);
                }
            }
        }
        // create sprouts
        if ((state.getEpoch() + 1) % state.getMetaepochLength() == 0) {
            final int levelCount = state.getHgsDemes().length;
            // better to go leaf -> root, as the just created sprouts won't be considered for sprouting then
            for (int levelIndex = levelCount - 2; levelIndex >= 0; levelIndex--) {
                final Level level = state.getHgsDemes()[levelIndex];
                final Level nextLevel = state.getHgsDemes()[levelIndex+1];
                final Sprouter sprouter = level.getSprouter();
                final SproutReducer sproutReducer = level.getSproutReducer();
                final List<Pair<Individual,Deme>> sprouts = new ArrayList<>();

                setPrecisionIfPossible(level);

                for (final Deme deme: level.getDemes()) {
                    if (!deme.isStopped()) {
                        sprouts.addAll(sprouter.createSprouts(level, nextLevel, deme, state));
                    }
                }
                final List<Pair<Individual, Deme>> reducedSprouts = sproutReducer.reduce(sprouts, state, levelIndex);
                setPrecisionIfPossible(nextLevel);
                logSprouting(levelIndex+1, reducedSprouts);
                nextLevel.getDemes().addAll(reducedSprouts.stream()
                        .map(Pair::getSecond)
                        .map(sprout -> {
                            individualEvaluator.evaluate(sprout.getPopulation());
                            return sprout;
                        })
                        .collect(Collectors.toList()));
                for (final Pair<Individual, Deme> pair: reducedSprouts) {
                    final Deme deme = pair.getSecond();
                    final Deme parent = deme.getParent();
                    final List<Deme> children = parent.getHistory().get(parent.getHistory().size()-1).getChildren();
                    deme.setName(parent.getName()+"-"+parent.getChildren().size());
                    children.add(deme);
                    parent.getChildren().add(deme);
                }
            }
        }
    }

    private void setPrecisionIfPossible(Level level) {
        if (individualEvaluator instanceof ParallelIndividualEvaluator && level.getPrecision() > 0) {
            ((ParallelIndividualEvaluator) individualEvaluator).setPrecision(level.getPrecision());
        }
    }

    private void logSprouting(int levelIndex, List<Pair<Individual, Deme>> reducedSprouts) {
        final StringBuilder sb = new StringBuilder("Sprouting to level ").append(levelIndex).append(" from:");
        for (final Individual ind: reducedSprouts.stream().map(Pair::getFirst).collect(Collectors.toList())) {
            sb.append("\n");
            String sep = "";
            for (final double obj: ind.getObjectives()) {
                sb.append(sep).append(obj);
                sep = " ";
            }
            for (final double obj: ind.getPoint()) {
                sb.append(sep).append(obj);
            }
        }
        log.info(sb.toString());
    }


    private void doRunLbaPhase() {
        final LbaState lbaState = state.getLbaState();
        final List<Pair<Deme, EvoAlg>> lbaDemes = lbaState.getDemesAndAlgs();
        final Archive archive = state.getArchive();
        while (lbaDemes.stream().map(Pair::getFirst).anyMatch(deme -> !deme.isStopped())) {
            log.info(String.format("Starting epoch %d", lbaState.getEpoch()));
            for (final Pair<Deme, EvoAlg> pair: lbaDemes) {
                final Deme deme = pair.getFirst();
                final EvoAlg evoAlg = pair.getSecond();
                if (!deme.isStopped()) {
                    final Population population = evoAlg.apply(null, deme, state);
                    individualEvaluator.evaluate(population);
                    deme.getHistory().add(Deme.HistoryItem.builder()
                            .epoch(lbaState.getEpoch())
                            .population(deme.getPopulation())
                            .build());
                    deme.setPopulation(population);
                    if (archive.isEnabled()) {
                        archive.addAllFrom(deme);
                    }
                }
            }
            if (archive.isEnabled()) {
                log.info("Archive size " + archive.getIndividuals().size()
                        + ", actual max rank " + archive.getActualMaxRank());
            }
            // check stopping conditions
            final LocalStopCondition lsc = lbaState.getStopCondition();
            for (final Pair<Deme, EvoAlg> pair: lbaDemes) {
                final Deme deme = pair.getFirst();
                if (!deme.isStopped() && lsc.shouldStop(deme, state)) {
                    deme.setStopped(true);
                }
            }
            log.info(format(
                    "Demes: %d/%d",
                    lbaDemes.stream()
                        .map(Pair::getFirst)
                        .filter(deme -> !deme.isStopped())
                        .count(),
                    lbaDemes.size()));
            lbaState.setEpoch(lbaState.getEpoch()+1);
        }
    }

    private void runPostInvertedLbaPhase() {
        log.info("Running post-LBA phase with inverted fitness");
        final LbaState lbaState = state.getLbaState();
        val lbaDemes = lbaState.getDemesAndAlgs();
        for (val lbaPair : lbaDemes) {
            lbaPair.getFirst().setStopped(false);
            final EvoAlg ea = lbaPair.getSecond();
            if (!(ea instanceof MWEA)) {
                throw new IllegalStateException("Not MWEA algorithm in LBA");
            }
            final MWEA mwea = (MWEA) ea;
            mwea.getMwPolicy().setKeepOriginalFitness(true);
        }
        lbaState.setEpoch(0);
        doRunLbaPhase();
    }

    private List<Cluster> computeClusters() {
        List<Cluster> clusters;
        if (objectiveCalculator.getObjectiveCount() > 1) {
            final Archive archive = state.getArchive();
            if (archive.isEnabled()) {
                final ArchiveClusterer clusterer = new ArchiveClusterer(archive);
                clusters = clusterer.createClusters();
            } else {
                final DemeToClusterConverter conv = new DemeToClusterConverter();
                final List<Deme> demes = ArraysUtil.last(state.getHgsDemes()).getDemes();
                clusters = demes.stream().map(conv::convert).collect(Collectors.toList());
            }
        } else if (ArraysUtil.last(state.getHgsDemes()).getEvoAlg() instanceof CMAES) {
            final CMADemeToClusterConverter conv = new CMADemeToClusterConverter();
            clusters = conv.convert(state);
        } else {
            final DemeToClusterConverterSO conv = new DemeToClusterConverterSO();
            final List<Deme> demes = ArraysUtil.last(state.getHgsDemes()).getDemes();
            clusters = demes.stream().map(conv::convert).collect(Collectors.toList());
        }
        return clusters;
    }
}
