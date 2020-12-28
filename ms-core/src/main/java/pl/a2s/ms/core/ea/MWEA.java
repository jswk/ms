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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.ind.SimpleIndividual;
import pl.a2s.ms.core.mw.MWPolicy;
import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.util.Range;

import java.util.Arrays;
import java.util.Random;

@RequiredArgsConstructor
public class MWEA implements EvoAlg {

    @Getter
    private final double mutationStd;
    @Getter
    private final MWPolicy mwPolicy;
    private final IndividualEvaluator ie;
    private final Random rand;

    @Setter
    private int populationSize = 0;

    @Override
    public Population apply(Level level, Deme deme, State state) {
        final Population population = deme.getPopulation();
        final Individual[] individuals = population.getIndividuals();
        final int selectedAndPopulationSize = (populationSize > 0) ? populationSize : individuals.length;
        final Individual[] selected = select(population, selectedAndPopulationSize);
        for (int i = 0; i < selected.length; i++) {
            mutate(selected, i, state.getDomain());
        }

        final Population midPopulation = new Population(individuals.length + selected.length);
        final Individual[] midIndividuals = midPopulation.getIndividuals();
        for (int i = 0; i < individuals.length; i++) {
            midIndividuals[i] = individuals[i];
        }
        for (int i = individuals.length; i < individuals.length + selected.length; i++) {
            midIndividuals[i] = selected[i-individuals.length];
        }
        ie.evaluate(midPopulation);
        midPopulation.updateRanks();

        final Population output = new Population(selectedAndPopulationSize);
        mwPolicy.select(midPopulation, output.getIndividuals());
        return output;
    }

    private Individual[] select(Population population, int length) {
        final Individual[] individuals = population.getIndividuals();
        final double[] fitnesses = new double[individuals.length];
        for (int i = 0; i < fitnesses.length; i++) {
            fitnesses[i] = individuals[i].getObjectives()[0];
        }
        final Individual[] selected = new Individual[length];
        final double maxFitness = Arrays.stream(fitnesses).max().getAsDouble();
        if (maxFitness == 0) {
            // in case no individual is dominated just return random one uniformly
            for (int i = 0; i < length; i++) {
                final Individual ri = individuals[rand.nextInt(individuals.length)];
                selected[i] = new SimpleIndividual(ri);
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
            final double randDouble = rand.nextDouble() * lastCumulative;
            while (cumulative[selectedIndex] < randDouble) {
                selectedIndex++;
            }
            selected[i] = individuals[selectedIndex];
        }
        return selected;
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
