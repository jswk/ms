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

import org.apache.commons.lang3.tuple.Pair;

import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ie.IndividualEvaluator;

/**
 *
 * ClusterMergeChecker based on 'hill-valley' method described in R. K. Ursem's paper <em>Multinational
 * evolutionary algorithms</em> from CEC'99.
 *
 * @author Maciej Smołka
 *
 * @param <T> type of objects that form clusters
 */
public class HillValleyChecker implements ClusterMergeChecker {
    private final HillValleyFunction hvf;
    private final double threshold;

    public HillValleyChecker(int intermediatePoints, double threshold, IndividualEvaluator ie) {
        hvf = new HillValleyFunction(intermediatePoints, ie);
        this.threshold = threshold;
    }

    @Override
    public boolean canMerge(Cluster first, Cluster second) {
        final Pair<Individual, Individual> closest = Clusters.findClosest(first, second);
        return hvf.compute(closest.getLeft().getPoint(),
                closest.getRight().getPoint()) <= threshold;
    }

}
