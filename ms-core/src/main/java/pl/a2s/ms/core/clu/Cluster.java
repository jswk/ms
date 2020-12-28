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

import lombok.Data;
import pl.a2s.ms.core.ind.Individual;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Data
public class Cluster {
    private final List<Individual> individuals = new ArrayList<>();

    public static Cluster merge(Cluster first, Cluster second) {
        final Cluster cluster = new Cluster();
        final List<Individual> individuals = cluster.getIndividuals();
        // deliberately don't use List::addAll, to check the first cluster for duplicates too
        Stream.concat(first.getIndividuals().stream(), second.getIndividuals().stream())
            .forEach((ind) -> {
                if (!individuals.contains(ind)) {
                    individuals.add(ind);
                }
            });
        return cluster;
    }

    public static Cluster mergeDominating(Cluster first, Cluster second) {
        final Cluster cluster = new Cluster();
        final List<Individual> individuals = cluster.getIndividuals();
        individuals.addAll(first.getIndividuals());
        for (final Individual sInd: second.getIndividuals()) {
            final List<Individual> toRemove = new ArrayList<>();
            for (final Individual ind: individuals) {
                if (!ind.dominates(sInd) && sInd.dominates(ind)) {
                    toRemove.add(ind);
                }
            }
            if (!toRemove.isEmpty()) {
                individuals.removeAll(toRemove);
                individuals.add(sInd);
            }
        }

        return cluster;
    }
}
