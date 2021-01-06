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
import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.NEA2Orchestrator;
import pl.a2s.ms.core.orch.Orchestrator;
import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.Deme.HistoryItem;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.orch.nea2.NEA2State;
import pl.a2s.ms.core.util.Pair;
import lombok.extern.java.Log;
import lombok.val;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.a2s.ms.core.util.ArraysUtil.last;
import static java.lang.String.format;

@Log
public class Ola19CaseIAnalyser implements Analyser {

    @Override
    public boolean supports(Orchestrator orch) {
        if (orch instanceof HgsOrchestrator && ((HgsOrchestrator) orch).getState().getLbaState() != null) {
            return true;
        } else if (orch instanceof NEA2Orchestrator && ((NEA2Orchestrator) orch).getState().getLbaState() != null) {
            return true;
        }
        return false;
    }

    @Override
    public void analyse(State state) {
        final boolean isHgs = state instanceof HgsState;
        final boolean isNea = state instanceof NEA2State;
        if (!isHgs && !isNea) {
            throw new IllegalStateException("Only NEA2 and HGS orchestrators are supported!");
        }
        val hgsState = isHgs ? (HgsState) state : null;
        val neaState = isNea ? (NEA2State) state : null;
        val lbaState = isHgs ? hgsState.getLbaState() : neaState.getLbaState();
        {
            int evalsGlobal = 0;
            int evalsLocal = 0;
            if (isHgs) {
                for (final Level lvl: hgsState.getHgsDemes()) {
                    for (final Deme deme: lvl.getDemes()) {
                        for (final HistoryItem hi: deme.getHistory()) {
                            evalsGlobal += hi.getPopulation().getSize();
                        }
                    }
                }
            } else if (isNea) {
                evalsGlobal += neaState.getRuns().stream()
                        .mapToLong(run -> run.getSampledIndividuals().size())
                        .sum();
                evalsGlobal += neaState.getRuns().stream()
                        .flatMap(run -> run.getCmaState().stream())
                        .flatMap(cmaState -> cmaState.getPopulations().stream())
                        .mapToLong(Population::getSize)
                        .sum();
            }
            for (final Pair<Deme, EvoAlg> pair: lbaState.getDemesAndAlgs()) {
                final Deme deme = pair.getFirst();
                evalsLocal += deme.getPopulation().getSize();
                for (final HistoryItem hi: deme.getHistory()) {
                    evalsLocal += hi.getPopulation().getSize();
                }
            }
            log.info(format("Number of evaluations global/local/total: %d %d %d", evalsGlobal, evalsLocal, evalsGlobal+evalsLocal));
        }
        {
            long count = 0;
            if (isHgs) {
                count = last(hgsState.getHgsDemes()).getDemes().size();
            } else if (isNea) {
                count = neaState.getRuns().stream()
                        .mapToLong(run -> run.getCmaState().size())
                        .sum();
            }
            log.info(format("Number of last level demes after global phase: %d", count));
        }{
            final int countClusters = lbaState.getClusters().size();
            final int countReducedClusters = lbaState.getReducedClusters().size();
            log.info(format("Clusters before and after reduction: %d -> %d", countClusters, countReducedClusters));
        }
        {
            val sb = new StringBuilder();
            int i = 0;
            sb.append(format("CLUSTERS BEFORE REDUCTION: %d", lbaState.getClusters().size()));
            for (val cluster: lbaState.getClusters()) {
                sb.append(format("\n\n\nCluster %d", i));
                for (val ind: cluster.getIndividuals()) {
                    sb.append("\n");
                    String sep = "";
                    for (final double coord: ind.getPoint()) {
                        sb.append(sep).append(coord);
                        sep = " ";
                    }
                    for (final double coord: ind.getObjectives()) {
                        sb.append(sep).append(coord);
                    }
                }
                i++;
            }
            log.info(sb.toString());
        }
        {
            val sb = new StringBuilder();
            int i = 0;
            sb.append(format("CLUSTERS AFTER REDUCTION: %d", lbaState.getReducedClusters().size()));
            for (val cluster: lbaState.getReducedClusters()) {
                sb.append(format("\n\n\nReduced Cluster %d", i));
                for (val ind: cluster.getIndividuals()) {
                    sb.append("\n");
                    String sep = "";
                    for (final double coord: ind.getPoint()) {
                        sb.append(sep).append(coord);
                        sep = " ";
                    }
                    for (final double coord: ind.getObjectives()) {
                        sb.append(sep).append(coord);
                    }
                }
                i++;
            }
            log.info(sb.toString());
        }
        {
            val demesAndAlgs = lbaState.getDemesAndAlgs();
            val sb = new StringBuilder();
            sb.append(format("LBA DEMES: %d", demesAndAlgs.size()));
            int i = 0;
            for (val pair: demesAndAlgs) {
                val deme = pair.getFirst();
                sb.append(format("\n\n\nLBA DEME %d; Population size: %d", i, deme.getPopulation().getSize()));
//                if (pair.getSecond() instanceof MWEA) {
//                    final MWEA mwea = (MWEA) pair.getSecond();
//                    sb.append(format(" MWEA with std %f", mwea.getMutationStd()));
//                }
                val populations = Stream.concat(
                        deme.getHistory().stream().map(HistoryItem::getPopulation),
                        Stream.of(deme.getPopulation())
                    ).collect(Collectors.toList());
                int j = 0;
                for (val population: populations) {
//                    sb.append(format("\nDeme %d Step %d", i, j));
                    for (val ind: population.getIndividuals()) {
                        sb.append("\n");
                        String sep = "";
                        for (final double coord: ind.getPoint()) {
                            sb.append(sep).append(coord);
                            sep = " ";
                        }
                        for (final double coord: ind.getObjectives()) {
                            sb.append(sep).append(coord);
                        }
                    }
                    sb.append("\n");
                    j++;
                }
                i++;
            }
            log.info(sb.toString());
        }
    }

}
