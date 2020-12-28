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
import org.apache.commons.math3.util.MathArrays;

import pl.a2s.ms.core.ind.Individual;

public class Clusters {

    public static double[] computeCentroid(Cluster cluster) {
        final double[][] points = cluster.getIndividuals().stream().map(Individual::getPoint)
                .toArray(double[][]::new);
        double[] sum = new double[points[0].length];
        for (double[] point : points) {
            sum = MathArrays.ebeAdd(sum, point);
        }
        MathArrays.scaleInPlace(1. / points.length, sum);
        return sum;
    }

    public static Pair<Individual, Individual> findClosest(
            Cluster first, Cluster second) {
        double minDistance = Double.POSITIVE_INFINITY;
        Pair<Individual, Individual> current = null;
        for (final Individual p1 : first.getIndividuals()) {
            for (final Individual p2 : second.getIndividuals()) {
                final double distance = MathArrays.distance(p1.getPoint(), p2.getPoint());
                if (distance < minDistance) {
                    current = Pair.of(p1, p2);
                    minDistance = distance;
                }
            }
        }
        return current;
    }

}
