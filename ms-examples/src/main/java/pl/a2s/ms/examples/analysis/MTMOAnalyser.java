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

package pl.a2s.ms.examples.analysis;

import pl.a2s.ms.examples.analysis.MetricsCollector.AnalyserResult;
import pl.a2s.ms.core.analysis.Analyser;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.Orchestrator;
import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.Level;
import lombok.Data;
import lombok.Setter;
import lombok.extern.java.Log;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.FastMath;

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.math3.util.MathArrays.distance1;

@Log
public class MTMOAnalyser implements Analyser {

    final DoubleFunction<Double> INVERSE_PARAM_TRANSFORM = (a) -> FastMath.pow(10, a - 1.);
    final int OBJ_COUNT = 2;

    @Setter
    private MetricsCollector metricsCollector;

    @Override
    public boolean supports(Orchestrator orch) {
        if (!(orch instanceof HgsOrchestrator)) {
            return false;
        }
        return ((HgsOrchestrator) orch).objectiveCalculator.getObjectiveCount() == 2;
    }

    @Override
    public void analyse(State state) {
        final HgsState hgsState = (HgsState) state;

        val level1 = new ResultForLevel();
        val level2 = new ResultForLevel();
        val level3 = new ResultForLevel();

        if (hgsState.getHgsDemes().length > 0) getResultForLevel(level1, hgsState.getHgsDemes()[0]);
        if (hgsState.getHgsDemes().length > 1) getResultForLevel(level2, hgsState.getHgsDemes()[1]);
        if (hgsState.getHgsDemes().length > 2) getResultForLevel(level3, hgsState.getHgsDemes()[2]);

        if (hgsState.getLbaState() != null) {
            val lbaState = hgsState.getLbaState();
            lbaState.getDemesAndAlgs().stream().map(pair -> pair.getFirst()).forEach(deme -> {
                try {
                    logDemePopulations(deme);
                } catch (final Exception e) {
                    log.log(java.util.logging.Level.WARNING, "Error logging a LBA deme", e);
                }
            });
        }

        metricsCollector.collect(AnalyserResult.builder()
                .evalsCountLevel1(level1.evalsCount)
                .evalsCountLevel2(level2.evalsCount)
                .evalsCountLevel3(level3.evalsCount)
                .demesCountLevel1(level1.demesCount)
                .demesCountLevel2(level2.demesCount)
                .demesCountLevel3(level3.demesCount)
                .bestFitness0Level1(level1.bestFitness0)
                .bestFitness0Level2(level2.bestFitness0)
                .bestFitness0Level3(level3.bestFitness0)
                .bestFitness1Level1(level1.bestFitness1)
                .bestFitness1Level2(level2.bestFitness1)
                .bestFitness1Level3(level3.bestFitness1)
                .bestDistToSolutionLevel1(level1.bestDistToSolution)
                .bestDistToSolutionLevel2(level2.bestDistToSolution)
                .bestDistToSolutionLevel3(level3.bestDistToSolution)
                .build());

        log.info("\nevalsCountLevel1 evalsCountLevel2 evalsCountLevel3 "+
                 "demesCountLevel1 demesCountLevel2 demesCountLevel3 "+
                 "bestFitness0Level1 bestFitness0Level2 bestFitness0Level3 "+
                 "bestFitness1Level1 bestFitness1Level2 bestFitness1Level3 "+
                 "bestDistToSolutionLevel1 bestDistToSolutionLevel2 bestDistToSolutionLevel3\n"+
                 String.format("%d %d %d %d %d %d %f %f %f %f %f %f %f %f %f",
                               level1.evalsCount, level2.evalsCount, level3.evalsCount,
                               level1.demesCount, level2.demesCount, level3.demesCount,
                               level1.bestFitness0, level2.bestFitness0, level3.bestFitness0,
                               level1.bestFitness1, level2.bestFitness1, level3.bestFitness1,
                               level1.bestDistToSolution, level2.bestDistToSolution, level3.bestDistToSolution));
    }

    private final static double[] BEST_SOLUTION_POINT = new double[] {
            FastMath.log10(1.0) + 1,
            FastMath.log10(2.0) + 1,
            FastMath.log10(10.0) + 1,
            FastMath.log10(3.0) + 1
    };

    private void getResultForLevel(ResultForLevel result, Level level) {
        long evalsCount = 0;
        final long demesCount = level.getDemes().size();
        final double[] bestFitness = new double[] { Double.NaN, Double.NaN };
        double bestDistToSolution = Double.NaN;

        final Individual[] indBestFitness =  new Individual[] { null, null };
        for (val deme: level.getDemes()) {
            logDemePopulations(deme);

            Individual[] prevInds = new Individual[0];
            for (val pop: extractPopulations(deme)) {
                for (val ind: pop.getIndividuals()) {
                    if (!ArrayUtils.contains(prevInds, ind)) {
                        evalsCount++;
                    }
                    for (int objNum = 0; objNum < OBJ_COUNT; objNum++) {
                        if (indBestFitness[objNum] == null || ind.getObjectives()[objNum] < indBestFitness[objNum].getObjectives()[objNum]) {
                            indBestFitness[objNum] = ind;
                        }
                    }
                    final double dist = distance1(BEST_SOLUTION_POINT, ind.getPoint());
                    if (Double.isNaN(bestDistToSolution) || dist < bestDistToSolution) {
                        bestDistToSolution = dist;
                    }

                }
                prevInds = pop.getIndividuals();
            }
        }

        for (int objNum = 0; objNum < OBJ_COUNT; objNum++) {
            if (indBestFitness[objNum] != null) {
                bestFitness[objNum] = indBestFitness[objNum].getObjectives()[objNum];
            }
        }

        result.setEvalsCount(evalsCount);
        result.setDemesCount(demesCount);
        result.setBestFitness0(bestFitness[0]);
        result.setBestFitness1(bestFitness[1]);
        result.setBestDistToSolution(bestDistToSolution);
    }

    @Data
    private static class ResultForLevel {
        private long evalsCount = 0;
        private long demesCount = 0;
        private double bestFitness0 = Double.NaN;
        private double bestFitness1 = Double.NaN;
        private double bestDistToSolution = Double.NaN;
    }

    private List<Population> extractPopulations(Deme deme) {
        return Stream.concat(
               deme.getHistory().stream().map(hi -> hi.getPopulation()),
               Stream.of(deme.getPopulation())).collect(Collectors.toList());
    }

    private void logDemePopulations(Deme deme) {
        val sb = new StringBuilder();

        val populations = extractPopulations(deme);
        sb.append("Deme: ").append(deme.getName()).append(", epochs: ").append(populations.size()).append("\n");

        int count = 0;
        for (val population : populations) {
            sb.append("Epoch: ").append(count++).append("\n");
            for (val individual : population.getIndividuals()) {
                val point = individual.getPoint();
                sb.append(String.format("%f %f %f %f %e %e",
                        INVERSE_PARAM_TRANSFORM.apply(point[0]),
                        INVERSE_PARAM_TRANSFORM.apply(point[1]),
                        INVERSE_PARAM_TRANSFORM.apply(point[2]),
                        INVERSE_PARAM_TRANSFORM.apply(point[3]),
                        individual.getObjectives()[0],
                        individual.getObjectives()[1]));
                sb.append("\n");
            }
        }
        log.info(sb.toString());
    }

}
