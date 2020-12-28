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
import java.util.List;

import org.apache.commons.math3.util.MathArrays;

import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.util.Pair;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SeedDistanceSproutReducer implements SproutReducer {

    private final double thresholdDistance;

    @Override
    public List<Pair<Individual, Deme>> reduce(List<Pair<Individual, Deme>> sprouts, HgsState state, int levelIndex) {
        final List<Pair<Individual, Deme>> selected = new ArrayList<>();
        for (final Pair<Individual, Deme> pair: sprouts) {
            final Individual seed = pair.getFirst();
            if (!containsCloser(selected, seed)) {
                selected.add(pair);
            }
        }
        return selected;
    }

    private boolean containsCloser(List<Pair<Individual, Deme>> selected, Individual seed) {
        return selected.stream()
                .map(pair -> pair.getFirst().getPoint())
                .map(point -> dist(point, seed.getPoint()))
                .anyMatch(dist -> dist < thresholdDistance);
    }

    private static double dist(double[] point, double[] point2) {
        return MathArrays.distance(point, point2);
    }
}
