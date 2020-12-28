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

package pl.a2s.ms.core.ea;

import java.util.Random;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;

import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.mw.MWPolicy;
import pl.a2s.ms.core.orch.hgs.Deme;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class MWEAFactory implements EvoAlgFactory {

    private final double defaultMutationStd;
    private final MWPolicy defaultMwPolicy;

    private final IndividualEvaluator ie;
    private final Random rand;

    @Override
    public EvoAlg create(Params params) {
        if (!(params instanceof MWEAFactoryParams)) {
            throw new IllegalArgumentException("Params of type ("+params.getClass().getName()+") different than MWEAFactoryParams passed");
        }
        final MWEAFactoryParams castedParams = (MWEAFactoryParams) params;

        int populationSize = 0;
        final int minPopulationSize = 10;
        double mutationStd = defaultMutationStd;
        if (castedParams.getDeme() != null) {
            mutationStd = computeDemeDiameter(castedParams.getDeme()) / 2;
            if (castedParams.getDeme().getPopulation().getSize() < minPopulationSize) {
                populationSize = minPopulationSize;
            }
        } else if (castedParams.getMutationStd() > 0.) {
            mutationStd = castedParams.getMutationStd();
        }

        MWPolicy mwPolicy = defaultMwPolicy;
        if (castedParams.getMwPolicy() != null) {
            mwPolicy = castedParams.getMwPolicy();
        }

        val mwea = new MWEA(mutationStd, mwPolicy, ie, rand);
        if (populationSize > 0) {
            mwea.setPopulationSize(populationSize);
        }
        return mwea;
    }

    private double computeDemeDiameter(Deme deme) {
        double maxDist = Double.NEGATIVE_INFINITY;
        final Individual[] individuals = deme.getPopulation().getIndividuals();
        for (int i = 0; i < individuals.length; i++) {
            final double[] p1 = individuals[i].getPoint();
            for (int j = i+1; j < individuals.length; j++) {
                final double[] p2 = individuals[j].getPoint();
                final double dist = MathArrays.distance(p1, p2);
                maxDist = FastMath.max(maxDist, dist);
            }
        }
        return maxDist;
    }

    @Data
    @Builder
    public static class MWEAFactoryParams implements Params {
        /// default if below or equal to 0
        private final double mutationStd;
        /// default if null
        private final MWPolicy mwPolicy;
        /// if passed, bypasses mutationStd and adjusts it based on the deme's population
        private final Deme deme;
    }

}
