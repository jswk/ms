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

import java.util.Arrays;
import java.util.Random;

import pl.a2s.ms.core.ie.FitnessExtractor;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.ind.SimpleIndividual;
import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.util.Range;

public class SEA implements EvoAlg {

    private final double crossoverProb;
    private final double mutationProb;
    private final double mutationStd;
    private final Random rand;
    private final FitnessExtractor fe;

    public SEA(double crossoverProb, double mutationProb, double mutationStd, Random rand, FitnessExtractor fe) {
        this.crossoverProb = crossoverProb;
        this.mutationProb = mutationProb;
        this.mutationStd = mutationStd;
        this.rand = rand;
        this.fe = fe;
    }

    @Override
    public Population apply(Level level, Deme deme, State state) {
        final Population population = deme.getPopulation();
        final Individual[] individuals = population.getIndividuals();
        if (individuals.length % 2 != 0) {
            throw new IllegalArgumentException("Population size must be even");
        }
        final Individual[] selected = select(population, individuals.length);
        for (int i = 0; i < selected.length; i = i + 2) {
            if (shouldCrossover()) {
                crossover(selected, i, i+1);
            }
        }
        for (int i = 0; i < selected.length; i++) {
            if (shouldMutate()) {
                mutate(selected, i, state.getDomain());
            }
        }

        final Population newPopulation = new Population(individuals.length);
        final Individual[] newIndividuals = newPopulation.getIndividuals();
        for (int i = 0; i < individuals.length; i++) {
            newIndividuals[i] = selected[i];
        }
        return newPopulation;
    }

    private Individual[] select(Population population, int length) {
        final Individual[] individuals = population.getIndividuals();
        final double[] fitnesses = fe.extractFitness(population);
        final Individual[] selected = new SimpleIndividual[length];
        final double maxFitness = Arrays.stream(fitnesses).max().getAsDouble();
        if (maxFitness == 0) {
            // in case no individual is dominated just return random one uniformly
            for (int i = 0; i < length; i++) {
                selected[i] = individuals[rand.nextInt(length)];
            }
            return selected;
        }
        final double[] cumulative = new double[individuals.length];
        double lastCumulative = 0;
        for (int i = 0; i < individuals.length; i++) {
            cumulative[i] = lastCumulative + maxFitness - fitnesses[i] + 1; // give the worst ones a chance too by adding 1
            lastCumulative = cumulative[i];
        }
        for (int i = 0; i < length; i++) {
            int selectedIndex = 0;
            final double randInt = rand.nextDouble() * lastCumulative;
            while (cumulative[selectedIndex] < randInt) {
                selectedIndex++;
            }
            selected[i] = individuals[selectedIndex];
        }
        return selected;
    }

    private boolean shouldCrossover() {
        return rand.nextDouble() < crossoverProb;
    }

    private void crossover(Individual[] selected, int i, int j) {
        final double[] point1 = selected[i].getPoint();
        final double[] point2 = selected[j].getPoint();
        final int dim = point1.length;
        final double randDouble = rand.nextDouble();
        final double[] new1 = new double[dim];
        final double[] new2 = new double[dim];
        for (int k = 0; k < dim; k++) {
            new1[k] = point1[k] * randDouble        + point2[k] * (1. - randDouble);
            new2[k] = point1[k] * (1. - randDouble) + point2[k] * randDouble;
        }
        selected[i] = new SimpleIndividual(new1);
        selected[j] = new SimpleIndividual(new2);
    }

    private boolean shouldMutate() {
        return rand.nextDouble() < mutationProb;
    }

    private void mutate(Individual[] selected, int i, Range[] domain) {
        final double[] point = selected[i].getPoint();
        final int dim = point.length;
        final double[] newPoint = new double[dim];
        for (int j = 0; j < dim; j++) {
            newPoint[j] = point[j] + rand.nextGaussian() * mutationStd;
            // not perfect, should be modified if this becomes a performance problem
            while (newPoint[j] > domain[j].getEnd() || newPoint[j] < domain[j].getStart()) {
                newPoint[j] = point[j] + rand.nextGaussian() * mutationStd;
            }
        }
        selected[i] = new SimpleIndividual(newPoint);
    }

}
