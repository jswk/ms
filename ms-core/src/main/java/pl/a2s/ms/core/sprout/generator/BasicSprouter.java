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

package pl.a2s.ms.core.sprout.generator;

import lombok.RequiredArgsConstructor;
import lombok.val;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.ind.SimpleIndividual;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.util.Pair;
import pl.a2s.ms.core.util.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class BasicSprouter implements Sprouter {

    private final double fitnessThreshold;
    private final double sproutStd;
    private final int populationSize;
    private final Random rand;

    @Override
    public List<Pair<Individual, Deme>> createSprouts(Level level, Level nextLevel, Deme deme, HgsState state) {
        final List<Individual> seeds = new ArrayList<>();
        final Individual[] individuals = deme.getPopulation().getIndividuals();
        for (val ind: individuals) {
            final double maxObj = Arrays.stream(ind.getObjectives()).min().orElse(Double.MAX_VALUE);
            if (maxObj <= fitnessThreshold) {
                seeds.add(ind);
            }
        }
        final List<Pair<Individual, Deme>> sprouts = new ArrayList<>();
        for (final Individual seed: seeds) {
            sprouts.add(Pair.of(
                    seed,
                    Deme.builder()
                        .population(generatePopulation(nextLevel, seed, state.getDomain()))
                        .stopped(false)
                        .parent(deme)
                    .build()));
        }
        return sprouts;
    }

    private Population generatePopulation(Level nextLevel, Individual seed, Range[] domain) {
        final double[] point = seed.getPoint();
        final int dim = point.length;
        final Population population = new Population(populationSize);
        final Individual[] individuals = population.getIndividuals();
        for (int i = 0; i < individuals.length; i++) {
            final double[] newPoint = new double[dim];
            for (int j = 0; j < dim; j++) {
                newPoint[j] = rand.nextGaussian() * sproutStd + point[j];
                newPoint[j] = Math.max(newPoint[j], domain[j].getStart());
                newPoint[j] = Math.min(newPoint[j], domain[j].getEnd());
            }
            individuals[i] = new SimpleIndividual(newPoint);
        }
        return population;
    }

}
