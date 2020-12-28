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

package pl.a2s.ms.core.archive;

import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.orch.hgs.Deme;

import java.util.*;

public interface Archive {

    String NULL_DEME_NAME = "<null>";
    int INVALID_RANK = -1;

    boolean isEnabled();

    List<Individual> getIndividuals();

    /**
     * Adds an individual to this Archive.
     * @param ind who to add
     */
    void add(Individual ind);

    default void addAll(Collection<? extends Individual> c) {
        for (Individual ind : c) {
            add(ind);
        }
    }

    default void addAll(Individual... individuals) {
        addAll(Arrays.asList(individuals));
    }

    /**
     * Adds all individuals from a given {@link Deme}.
     * This default implementation does not store the information
     * about the deme of origin.
     *
     * @param origin deme of origin
     */
    default void addAllFrom(Deme origin) {
        addAll(origin.getPopulation().getIndividuals());
    }

    /**
     * Performs pre-clustering of this archive.
     * This default implementation returns a map containing exactly
     * one key-value pair with key {@value #NULL_DEME_NAME} and
     * population of all individuals as a value.
     *
     * @return new map with location names as keys and populations coming from
     * respective demes as values
     */
    default Map<String, Population> splitAlongDemes() {
        Map<String, Population> split = new HashMap<>();
        split.put(NULL_DEME_NAME, new Population(getIndividuals()));
        return split;
    }

    default int getActualMaxRank() {
        return INVALID_RANK;
    }

}
