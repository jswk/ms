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

package pl.a2s.ms.core.sprout.reducer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.MathArrays;

import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.util.Pair;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DemeDistanceSproutReducer implements SproutReducer {

    private final double thresholdDistance;

    @Override
    public List<Pair<Individual, Deme>> reduce(List<Pair<Individual, Deme>> sprouts, HgsState state, int levelIndex) {
        final List<Pair<Individual, Deme>> selected = new ArrayList<>();
        final List<Deme> demes = state.getHgsDemes()[levelIndex+1].getDemes();
        final List<double[]> centroids = demes.stream()
                .flatMap(deme -> demeToCentroids(deme).stream())
                .collect(Collectors.toList());
        for (final Pair<Individual, Deme> pair: sprouts) {
            final Individual seed = pair.getFirst();
            if (!containsCloser(centroids, seed)) {
                selected.add(pair);
            }
        }
        return selected;
    }

    private boolean containsCloser(List<double[]> centroids, Individual seed) {
        return centroids.stream()
                .map(point -> dist(point, seed.getPoint()))
                .anyMatch(dist -> dist < thresholdDistance);
    }

    private static List<double[]> demeToCentroids(Deme deme) {
        final List<double[]> out = new LinkedList<>();
        out.add(populationToCentroid(deme.getPopulation()));
        if (!deme.getHistory().isEmpty()) {
            out.add(populationToCentroid(deme.getHistory().get(0).getPopulation()));
        }
        return out;
    }

    private static double[] populationToCentroid(Population population) {
        final Individual[] individuals = population.getIndividuals();
        final double[] sum = new double[individuals[0].getPoint().length];
        for (Individual individual : individuals) {
            for (int j = 0; j < sum.length; j++) {
                sum[j] += individual.getPoint()[j];
            }
        }
        for (int i = 0; i < sum.length; i++) {
            sum[i] /= individuals.length;
        }
        return sum;
    }

    private static double dist(double[] point, double[] point2) {
        return MathArrays.distance(point, point2);
    }
}
