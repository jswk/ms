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
import pl.a2s.ms.examples.obj.GaussCShape2DMultimodalLandscape1;
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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;
import lombok.val;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.a2s.ms.core.util.ArraysUtil.last;
import static java.lang.String.format;

@Log
public class Ola19CaseIIAnalyser implements Analyser {

    @Setter
    private GaussCShape2DMultimodalLandscape1 benchmark;

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
                        .mapToLong(population -> population.getSize())
                        .sum();
            }
            for (final Pair<Deme, EvoAlg> pair: lbaState.getDemesAndAlgs()) {
                final Deme deme = pair.getFirst();
                evalsLocal += deme.getPopulation().getSize();
                for (final HistoryItem hi: deme.getHistory()) {
                    evalsLocal += hi.getPopulation().getSize();
                }
            }
            res.evalsGlobalStat.addValue(evalsGlobal);
            res.evalsLocalStat.addValue(evalsLocal);
            res.evalsTotalStat.addValue(evalsGlobal+evalsLocal);
            log.info(format("Number of evaluations global/local/total: %d %d %d", evalsGlobal, evalsLocal, evalsGlobal+evalsLocal));
        }
        {
            long count = 0;
            if (isHgs) {
                count = last(hgsState.getHgsDemes()).getDemes().size();
            } else if (isNea) {
                count = neaState.getRuns().stream()
                        .flatMap(run -> run.getCmaState().stream())
                        .count();
            }
            log.info(format("Number of last level demes after global phase: %d", count));
        }{
            final int countClusters = lbaState.getClusters().size();
            final int countReducedClusters = lbaState.getReducedClusters().size();
            res.clustersAfterGlobalPhaseStat.addValue(countClusters);
            res.clustersAfterReductionPhaseStat.addValue(countReducedClusters);
            log.info(format("Clusters before and after reduction: %d -> %d", countClusters, countReducedClusters));
        }
        {
            val sb = new StringBuilder();
            final int i = 0;
            sb.append(format("CLUSTERS BEFORE REDUCTION: %d", lbaState.getClusters().size()));
//            for (val cluster: lbaState.getClusters()) {
//                sb.append(format("\n\n\nCluster %d", i));
//                for (val ind: cluster.getIndividuals()) {
//                    sb.append("\n");
//                    String sep = "";
//                    for (final double coord: ind.getPoint()) {
//                        sb.append(sep).append(coord);
//                        sep = " ";
//                    }
//                    for (final double coord: ind.getObjectives()) {
//                        sb.append(sep).append(coord);
//                    }
//                }
//                i++;
//            }
            log.info(sb.toString());
        }
        {
            val sb = new StringBuilder();
            final int i = 0;
            sb.append(format("CLUSTERS AFTER REDUCTION: %d", lbaState.getReducedClusters().size()));
//            for (val cluster: lbaState.getReducedClusters()) {
//                sb.append(format("\n\n\nReduced Cluster %d", i));
//                for (val ind: cluster.getIndividuals()) {
//                    sb.append("\n");
//                    String sep = "";
//                    for (final double coord: ind.getPoint()) {
//                        sb.append(sep).append(coord);
//                        sep = " ";
//                    }
//                    for (final double coord: ind.getObjectives()) {
//                        sb.append(sep).append(coord);
//                    }
//                }
//                i++;
//            }
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
                        Stream.of(deme.getPopulation()),
                        deme.getHistory().stream().map(hi -> hi.getPopulation())
                    ).collect(Collectors.toList());
                int j = 0;
                for (val population: populations) {
                    sb.append(format("\nDeme %d Step %d", i, j));
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
                    j++;
                }
                i++;
            }
            log.info(sb.toString());
        }
        {
            val minimalRegions = benchmark.getMinimalRegions();
            val demes = lbaState.getDemesAndAlgs().stream()
                    .map(demeAndAlg -> demeAndAlg.getFirst())
                    .filter(deme -> deme.getHistory().size() > 0)
                    .collect(Collectors.toList());
            val regionsAndCoveringAndTouchingDemes = minimalRegions.stream()
                    .map(r -> Pair.of(r, Pair.of(new ArrayList<Deme>(), new ArrayList<Deme>())))
                    .collect(Collectors.toList());
            int demesTouchingMultipleRegionsCount = 0;
            for (val deme: demes) {
                val demePoints = deme.getHistory().stream()
                        .flatMap(hi -> Arrays.stream(hi.getPopulation().getIndividuals()))
                        .map(ind -> ind.getPoint())
                        .collect(Collectors.toList());
                int touchedRegions = 0;
                for (val regionAndCoveringAndTouchedDemes: regionsAndCoveringAndTouchingDemes) {
                    boolean anyEllipsoidCovered = false;
                    boolean allElipsoidsCovered = true;
                    for (val el: regionAndCoveringAndTouchedDemes.getFirst().getEllipsoids()) {
                        if (demePoints.stream().anyMatch(point -> el.covers(point[0], point[1]))) {
                            anyEllipsoidCovered = true;
                        } else {
                            allElipsoidsCovered = false;
                        }
                    }
                    if (allElipsoidsCovered) {
                        regionAndCoveringAndTouchedDemes.getSecond().getFirst().add(deme);
                    }
                    if (anyEllipsoidCovered) {
                        touchedRegions++;
                        regionAndCoveringAndTouchedDemes.getSecond().getSecond().add(deme);
                    }
                }
                if (touchedRegions > 1) {
                    demesTouchingMultipleRegionsCount++;
                }
            }
            final double regionsCoveredRatio = 1. * regionsAndCoveringAndTouchingDemes.stream()
                    .filter(p -> !p.getSecond().getFirst().isEmpty())
                    .count() / regionsAndCoveringAndTouchingDemes.size();
            final double regionsMultiplyTouchedRatio = 1. * regionsAndCoveringAndTouchingDemes.stream()
                    .filter(p -> p.getSecond().getSecond().size() > 1)
                    .count() / regionsAndCoveringAndTouchingDemes.size();
            final double demesTouchingMultipleRegionsRatio = 1. * demesTouchingMultipleRegionsCount / demes.size();
            res.regionsCoveredRatioStat.addValue(regionsCoveredRatio);
            res.regionsMultiplyTouchedRatioStat.addValue(regionsMultiplyTouchedRatio);
            res.demesTouchingMultipleRegionsRatioStat.addValue(demesTouchingMultipleRegionsRatio);
        }
    }

    @RequiredArgsConstructor
    private static class Results {
        private final String category;

        private final SummaryStatistics evalsGlobalStat = new SummaryStatistics();
        private final SummaryStatistics evalsLocalStat = new SummaryStatistics();
        private final SummaryStatistics evalsTotalStat = new SummaryStatistics();
        private final SummaryStatistics clustersAfterGlobalPhaseStat = new SummaryStatistics();
        private final SummaryStatistics clustersAfterReductionPhaseStat = new SummaryStatistics();
        private final SummaryStatistics regionsCoveredRatioStat = new SummaryStatistics();
        private final SummaryStatistics regionsMultiplyTouchedRatioStat = new SummaryStatistics();
        private final SummaryStatistics demesTouchingMultipleRegionsRatioStat = new SummaryStatistics();
    }

    private final List<Results> data = new ArrayList<>();
    private Results res;

    public void setCategory(String category) {
        res = new Results(category);
        data.add(res);
    }

    public void summarize() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n").append(String.join(" ", new String[] {
                "category",
                "n",
                "evalsGlobal",
                "evalsLocal",
                "evalsTotal",
                "clustersAfterGlobalPhase",
                "clustersAfterReductionPhase",
                "regionsCoveredRatio",
                "regionsMultiplyTouchedRatio",
                "demesTouchingMultipleRegionsRatio",
        }));
        for (final Results results: data) {
            final long n = results.evalsTotalStat.getN();
            val line = new ArrayList<String>();
            line.add(results.category);
            line.add(Long.toString(n));
            addLineSegments(line, results.evalsGlobalStat, "%.1f", "%.1f", "%d", "%d");
            addLineSegments(line, results.evalsLocalStat, "%.1f", "%.1f", "%d", "%d");
            addLineSegments(line, results.evalsTotalStat, "%.1f", "%.1f", "%d", "%d");
            addLineSegments(line, results.clustersAfterGlobalPhaseStat, "%.1f", "%.1f", "%d", "%d");
            addLineSegments(line, results.clustersAfterReductionPhaseStat, "%.1f", "%.1f", "%d", "%d");
            addLineSegments(line, results.regionsCoveredRatioStat, "%.5f");
            addLineSegments(line, results.regionsMultiplyTouchedRatioStat, "%.5f");
            addLineSegments(line, results.demesTouchingMultipleRegionsRatioStat, "%.5f");
            sb.append("\n").append(String.join(" ", line));
        }
        log.warning(sb.toString());
    }

    private static void addLineSegments(List<String> line, SummaryStatistics stat, String format) {
        addLineSegments(line, stat, format, format, format, format);
    }

    private static void addLineSegments(List<String> line, SummaryStatistics stat, String meanFormat,
            String stdFormat, String minFormat, String maxFormat) {
        line.add(format(meanFormat, stat.getMean()));
        line.add(format(stdFormat, stat.getStandardDeviation()));
        if (minFormat.equals("%d")) {
            line.add(format(minFormat, (int) stat.getMin()));
        } else {
            line.add(format(minFormat, stat.getMin()));
        }
        if (maxFormat.equals("%d")) {
            line.add(format(maxFormat, (int) stat.getMax()));
        } else {
            line.add(format(maxFormat, stat.getMax()));
        }
        line.add("");
    }

}
