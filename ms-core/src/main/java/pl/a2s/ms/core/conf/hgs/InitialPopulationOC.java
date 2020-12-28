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

package pl.a2s.ms.core.conf.hgs;

import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.util.RandomSampleGenerator;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.ie.ParallelIndividualEvaluator;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.ind.SimpleIndividual;

import java.util.Random;

public class InitialPopulationOC extends HgsOrchestratorConfigurer {

    @Override
    protected void doConfigure(HgsOrchestrator orch) {
        final HgsState state = orch.state;

        final Population population = initialPopulation(state, orch.rand);
        setPrecisionIfPossible(orch.individualEvaluator, state.getHgsDemes()[0]);
        orch.individualEvaluator.evaluate(population);

        final Deme rootDeme = Deme.builder()
                .stopped(false)
                .population(population)
                .name("global-0")
                .build();
        state.getHgsDemes()[0].getDemes().add(rootDeme);
    }

    private void setPrecisionIfPossible(IndividualEvaluator ie, Level level) {
        if (ie instanceof ParallelIndividualEvaluator && level.getPrecision() > 0) {
            ((ParallelIndividualEvaluator) ie).setPrecision(level.getPrecision());
        }
    }

    private Population initialPopulation(HgsState state, Random rand) {
        final int size = state.getPopulationSizes()[0];
        final Population population = new Population(size);
        final Individual[] individuals = population.getIndividuals();
        final double[][] uniformSample = new RandomSampleGenerator(rand).uniformSample(size, state.getDomain());
        for (int i = 0; i < size; i++) {
            individuals[i] = new SimpleIndividual(uniformSample[i]);
        }
        return population;
    }

}
