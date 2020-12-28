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

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.val;
import pl.a2s.ms.core.clu.Cluster;
import pl.a2s.ms.core.clu.Consolidator;
import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.ea.MWEAFactory;
import pl.a2s.ms.core.ea.MWEAFactory.MWEAFactoryParams;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.lsc.LocalStopCondition;
import pl.a2s.ms.core.obj.ObjectiveCalculator;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.LbaState;
import pl.a2s.ms.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.math3.util.FastMath.min;

@Log
@RequiredArgsConstructor
public class LbaExecutor {

    private final Random rand;
    private final ObjectiveCalculator objectiveCalculator;
    private final IndividualEvaluator individualEvaluator;

    public void run(LbaState lbaState) {
        val lbaDemes = lbaState.getDemesAndAlgs();
        val evoAlgFactory = lbaState.getEvoAlgFactory();
        val archive = lbaState.getArchive();
        {
            final Consolidator consolidator = lbaState.getConsolidator();
            lbaState.setReducedClusters(consolidator.reduceClusters(lbaState.getClusters()));
            final List<Deme> demes = lbaState.getReducedClusters().stream()
                    .map(cluster -> clusterToDeme(cluster, lbaState.getPopulationSize()))
                    .collect(Collectors.toList());
            if (objectiveCalculator.getObjectiveCount() > 1) {
                demes.forEach(deme -> deme.getPopulation().updateRanks());
            }
            for (int i = 0; i < demes.size(); i++) {
                demes.get(i).setName("lba" + i);
            }
            for (final Deme deme: demes) {
                if (evoAlgFactory instanceof MWEAFactory) {
                    final EvoAlg evoAlg = evoAlgFactory.create(MWEAFactoryParams.builder().deme(deme).build());
                    lbaDemes.add(Pair.of(deme, evoAlg));
                } else {
                    throw new IllegalArgumentException("HO currently only supports MWEAFactory");
                }

                if (deme.getPopulation().getSize() < 2) {
                    deme.setStopped(true);
                }
            }
            String sb = format("Reduced %d clusters into %d:",
                    lbaState.getClusters().size(),
                    lbaState.getReducedClusters().size()) + lbaState.getReducedClusters().stream()
                            .map(clu -> clu.getIndividuals().size())
                            .map(size -> " " + size)
                            .reduce((a, b) -> a + b)
                            .orElse("");
            log.info(sb);
        }
        while (lbaDemes.stream().map(Pair::getFirst).anyMatch(deme -> !deme.isStopped())) {
            log.info(format("Starting epoch %d", lbaState.getEpoch()));
            for (final Pair<Deme, EvoAlg> pair: lbaDemes) {
                final Deme deme = pair.getFirst();
                final EvoAlg evoAlg = pair.getSecond();
                if (!deme.isStopped()) {
                    log.info(format("[epoch %d] Deme %s", lbaState.getEpoch(), deme.getName()));
                    final Population population = evoAlg.apply(null, deme, lbaState);
                    individualEvaluator.evaluate(population);
                    deme.getHistory().add(Deme.HistoryItem.builder()
                            .epoch(lbaState.getEpoch())
                            .population(deme.getPopulation())
                            .build());
                    deme.setPopulation(population);
                    if (archive != null && archive.isEnabled()) {
                        archive.addAllFrom(deme);
                    }
                }
            }
            if (archive != null && archive.isEnabled()) {
                log.info("Archive size " + archive.getIndividuals().size()
                        + ", actual max rank " + archive.getActualMaxRank());
            }
            // check stopping conditions
            final LocalStopCondition lsc = lbaState.getStopCondition();
            for (final Pair<Deme, EvoAlg> pair: lbaDemes) {
                final Deme deme = pair.getFirst();
                if (!deme.isStopped() && lsc.shouldStop(deme, null)) {
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

    private Deme clusterToDeme(Cluster cluster, int size) {
        if (size <= 0) {
            size = min(cluster.getIndividuals().size(), 100);
        }
        final Population population = new Population(size);
        final Individual[] individuals = population.getIndividuals();
        final List<Individual> clusterIndividuals = cluster.getIndividuals();
        if (clusterIndividuals.size() > size) {
            final List<Integer> sortedChosen = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int chosen = rand.nextInt(size-sortedChosen.size());

                for (final int ind: sortedChosen) {
                    if (ind <= chosen) {
                        chosen++;
                    } else {
                        break;
                    }
                }

                sortedChosen.add(chosen);
                sortedChosen.sort(Integer::compare);
                individuals[i] = clusterIndividuals.get(chosen);
            }
        } else {
            for (int i = 0; i < clusterIndividuals.size(); i++) {
                individuals[i] = clusterIndividuals.get(i);
            }
            for (int i = clusterIndividuals.size(); i < size; i++) {
                // don't control for clones, as they will be in the resulting population anyway
                individuals[i] = clusterIndividuals.get(rand.nextInt(clusterIndividuals.size()));
            }
        }

        return Deme.builder()
                .population(population)
                .build();
    }

}
