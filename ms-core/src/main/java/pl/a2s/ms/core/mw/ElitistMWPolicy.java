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

package pl.a2s.ms.core.mw;

import java.util.Arrays;

import org.apache.commons.math3.exception.MathIllegalArgumentException;

import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import lombok.Data;

public class ElitistMWPolicy extends MWPolicy {

    @Override
    public void select(Population poolPopulation, Individual[] selected) throws MathIllegalArgumentException {
        final Individual[] pool = poolPopulation.getIndividuals();
        final int[] ranks = poolPopulation.getRanks();
        final int n = pool.length;
        final int k = selected.length;
        checkParameters(pool, k);

        final Element[] sorted = new Element[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = new Element(pool[i], ranks[i]);
        }
        Arrays.sort(sorted, (e1, e2) -> -Integer.compare(e1.getRank(), e2.getRank()));
        for (int i = 0; i < k; i++) {
            selected[i] = sorted[i].getIndividual();
        }
    }

    @Data
    public static class Element {
        private final Individual individual;
        private final int rank;
    }
}
