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

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.DummyLocalizable;

import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import lombok.Getter;
import lombok.Setter;

public abstract class MWPolicy {

    /**
     * If the policy can transform the original fitness. A concrete
     * policy may completely ignore this attribute.
     */
    @Getter @Setter
    private boolean keepOriginalFitness = false;

    /**
     * Selects k individuals from the pool without replacement.
     *
     * @param pool individuals pool
     * @param k number of individuals to select
     * @return the selected individuals
     * @throws MathIllegalArgumentException
     */
    public abstract void select(Population pool, Individual[] selected) throws MathIllegalArgumentException;

    /**
     * A check for the validity of the pool size and k parameter.
     *
     * @param pool
     * @param k
     * @throws MathIllegalArgumentException
     */
    protected void checkParameters(Individual[] pool, int k) throws MathIllegalArgumentException {
        if (pool.length < k) {
            throw new MathIllegalArgumentException(new DummyLocalizable(
                    "Offspring must not be smaller than pool size"));
        }
    }
}
