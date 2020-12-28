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

import java.util.ArrayList;
import java.util.List;

import pl.a2s.ms.core.ind.Individual;
import lombok.Getter;
import lombok.ToString;

@ToString
public class SimpleArchive implements Archive {
    @Getter
    private final List<Individual> individuals = new ArrayList<>();

    @Getter
    private final boolean enabled;

    public SimpleArchive(boolean enabled) {
        this.enabled = enabled;
    }

    public void add(Individual individual) {
        final List<Individual> toRemove = new ArrayList<>();
        for (final Individual ind: individuals) {
            if (ind.dominates(individual)) {
                if (!toRemove.isEmpty()) {
                    throw new IllegalStateException("toRemove should be empty in case individual is dominated");
                }
                return;
            } else if (individual.dominates(ind)) {
                toRemove.add(ind);
            }
        }
        individuals.removeAll(toRemove);
        individuals.add(individual);
    }
}
