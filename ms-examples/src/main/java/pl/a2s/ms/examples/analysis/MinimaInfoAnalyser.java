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

import pl.a2s.ms.core.analysis.Analyser;
import pl.a2s.ms.core.analysis.MinimaInfoProvider;
import pl.a2s.ms.core.analysis.MinimumInfo;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.NEA2Orchestrator;
import pl.a2s.ms.core.orch.Orchestrator;
import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.nea2.NEA2State;
import pl.a2s.ms.core.util.ArraysUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.val;
import org.apache.commons.math3.util.MathArrays;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.math3.util.MathArrays.distance;

@Log
@RequiredArgsConstructor
public class MinimaInfoAnalyser implements Analyser {

    private final MinimaInfoProvider minimaInfoProvider;
    private final IndividualEvaluator ie;

//    @Setter
//    private MetricsCollector metricsCollector;

    @Override
    public boolean supports(Orchestrator orch) {
        return orch instanceof HgsOrchestrator || orch instanceof NEA2Orchestrator;
    }

    @Override
    public void analyse(State state) {
        val minimaInfo = minimaInfoProvider.getMinimaInfo(state.getDomain());

        val covered = new HashSet<MinimumInfo>();
        double bestObjective = Double.MAX_VALUE;
        val bestDistance = ArraysUtil.constant(minimaInfo.size(), Double.MAX_VALUE);
        val bestDistanceHistory = ArraysUtil.constant(minimaInfo.size(), -1);
        val minimumCoveredBy = new ArrayList<Set<Integer>>();
        for (int i = 0; i < minimaInfo.size(); i++) {
            minimumCoveredBy.add(new HashSet<>());
        }

        val histories = extractHistories(state);

        val historyCovering = new ArrayList<Set<MinimumInfo>>();
        for (int i = 0; i < histories.size(); i++) {
            historyCovering.add(new HashSet<>());
        }

        for (int history_i = 0; history_i < histories.size(); history_i++) {
            val history = histories.get(history_i);
            if (!history.isEmpty()) {
//            for (val population: history) {
                val population = history.get(history.size()-1);
                for (final Individual ind: population.getIndividuals()) {
                    for (int i = 0; i < minimaInfo.size(); i++) {
                        final MinimumInfo info = minimaInfo.get(i);
                        if (MathArrays.distance(info.getPoint(), ind.getPoint()) <= info.getDistance()
                                &&
                            ind.getObjectives()[0] < info.getTolerance()
                        ) {
                            covered.add(info);
                            minimumCoveredBy.get(i).add(history_i);
                            historyCovering.get(history_i).add(info);
                        }
                        final double dist = distance(info.getPoint(), ind.getPoint());
                        if (dist < bestDistance[i]) {
                            bestDistance[i] = dist;
                            bestDistanceHistory[i] = history_i;
                        }
                    }
                    bestObjective = Math.min(ind.getObjectives()[0], bestObjective);
                }
            }
        }
        final long minCov = minimumCoveredBy.stream().filter(e -> e.size() > 0).count();
        final long minCovMoreThanOnce = minimumCoveredBy.stream().filter(e -> e.size() > 1).count();
        final long historiesInMoreThanOneMin = historyCovering.stream().filter(e -> e.size() > 1).count();

        log.info(format("mins total: %d", minimaInfo.size()));
        log.info(format("min: %d - %f; minCovMoreThanOnce: %d; percent: %f", minCov, 1. * minCov / minimaInfo.size(), minCovMoreThanOnce, 1. * minCovMoreThanOnce / minCov));
        log.info(format("his: %d; hisInMoreThanOneMin: %d; percent: %f", histories.size(), historiesInMoreThanOneMin, 1. * historiesInMoreThanOneMin / histories.size()));

//        if (metricsCollector != null) {
//            metricsCollector.collect(1. * minCov / minimaInfo.size(), 1. * minCovMoreThanOnce / minCov, histories.size(), 1. * historiesInMoreThanOneMin / histories.size());
//        }
        /*

        final StringBuilder bestDistanceSb = new StringBuilder();
        {
            String sep = "";
            for (final double bd: bestDistance) {
                bestDistanceSb.append(sep);
                bestDistanceSb.append(format("%e", bd));
                sep = ", ";
            }
        }
        log.info(format("eval: %d; fitness: %e; covered: %d/%d", ie.getEvaluationCount(), bestObjective , covered.size(), minimaInfo.size()));
        */
    }

    private List<List<Population>> extractHistories(State state) {
        if (state instanceof HgsState) {
            val hgsState = (HgsState) state;
            // get the last level of the tree
            return hgsState.getHgsDemes()[hgsState.getHgsDemes().length-1].getDemes().stream()
                // extract lists of HistoryItems and extract only Populations
                .map(deme -> deme.getHistory().stream()
                        .map(hi -> hi.getPopulation())
                        .collect(Collectors.toList())
                        )
                .collect(Collectors.toList());
        } else if (state instanceof NEA2State) {
            val neaState = (NEA2State) state;
            // get all runs
            return neaState.getRuns().stream()
                // flat map all the CmaStates from all the runs
                .flatMap(runState -> runState.getCmaState().stream())
                // extract populations
                .map(cmaState -> cmaState.getPopulations())
                .collect(Collectors.toList());
        }
        throw new IllegalStateException("Whoops! Something went very wrong");
    }

}
