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

import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.orch.hgs.Deme;

import java.util.ArrayList;
import java.util.List;

public class DemeToClusterConverter {

    public Cluster convert(Deme deme) {
        final Cluster cluster = new Cluster();
        // should be faster than doing it after the loop, as it should contain relatively good individuals
        addPopulationToCluster(deme.getPopulation(), cluster);
        for (final Deme.HistoryItem hi: deme.getHistory()) {
            addPopulationToCluster(hi.getPopulation(), cluster);
        }
        return cluster;
    }

    private void addPopulationToCluster(Population population, Cluster cluster) {
        final List<Individual> cInds = cluster.getIndividuals();
        for (final Individual pInd: population.getIndividuals()) {
            final List<Individual> toRemove = new ArrayList<>();
            boolean isDominated = false;
            for (final Individual cInd: cInds) {
                if (cInd.dominates(pInd)) {
                    isDominated = true;
                } else if (pInd.dominates(cInd)) {
                    toRemove.add(cInd);
                }
            }
            if (isDominated && !toRemove.isEmpty()) {
                throw new IllegalStateException("If an individual is dominated no individuals should have to be removed");
            }
            if (!isDominated) {
                cInds.removeAll(toRemove);
                cInds.add(pInd);
            }
        }
    }
}
