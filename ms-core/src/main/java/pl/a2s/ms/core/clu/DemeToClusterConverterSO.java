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

package pl.a2s.ms.core.clu;

import java.util.List;

import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.orch.hgs.Deme;

public class DemeToClusterConverterSO {

    public Cluster convert(Deme deme) {
        double maxFitness = Double.NEGATIVE_INFINITY;
        double minFitness = Double.POSITIVE_INFINITY;

        for (final Individual ind: deme.getPopulation().getIndividuals()) {
            maxFitness = Math.max(maxFitness, ind.getObjectives()[0]);
            minFitness = Math.min(minFitness, ind.getObjectives()[0]);
        }
        for (final Deme.HistoryItem hi: deme.getHistory()) {
            for (final Individual ind: hi.getPopulation().getIndividuals()) {
                maxFitness = Math.max(maxFitness, ind.getObjectives()[0]);
                minFitness = Math.min(minFitness, ind.getObjectives()[0]);
            }
        }

        final double fitnessThreshold = minFitness + (maxFitness - minFitness) / 10; // arbitrary, by an order of magnitude
        final Cluster cluster = new Cluster();
        final List<Individual> individuals = cluster.getIndividuals();

        for (final Individual ind: deme.getPopulation().getIndividuals()) {
            if (ind.getObjectives()[0] <= fitnessThreshold) {
                individuals.add(ind);
            }
        }
        for (final Deme.HistoryItem hi: deme.getHistory()) {
            for (final Individual ind: hi.getPopulation().getIndividuals()) {
                if (ind.getObjectives()[0] <= fitnessThreshold) {
                    individuals.add(ind);
                }
            }
        }
        return cluster;
    }
}
