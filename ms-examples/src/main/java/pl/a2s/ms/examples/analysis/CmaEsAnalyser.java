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

package pl.a2s.ms.examples.analysis;

import pl.a2s.ms.core.analysis.Analyser;
import pl.a2s.ms.core.cmaes.CMAES;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.Orchestrator;
import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.HgsState;
import lombok.extern.java.Log;
import lombok.val;
import org.apache.commons.math3.util.FastMath;

import java.util.function.DoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log
public class CmaEsAnalyser implements Analyser {

    @Override
    public boolean supports(Orchestrator orch) {
        if (orch instanceof HgsOrchestrator) {
            if (((HgsOrchestrator) orch).getState().getHgsDemes()[0].getEvoAlg() instanceof CMAES) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void analyse(State state) {
        final DoubleFunction<Double> inverseTransform = (a) -> FastMath.pow(10, a - 1.);

        final HgsState hgsState = (HgsState) state;
        val sb = new StringBuilder();
        val cmaEsDeme = hgsState.getHgsDemes()[0].getDemes().get(0);
        val populations = Stream.concat(cmaEsDeme.getHistory().stream().map(hi -> hi.getPopulation()),
                                        Stream.of(cmaEsDeme.getPopulation())).collect(Collectors.toList());
        int count = 0;
        for (val population: populations) {
            sb.append("Epoch: ").append(count++).append("\n");
            for (val individual: population.getIndividuals()) {
                val point = individual.getPoint();
                sb.append(String.format("%f %f %f %f %e",
                        inverseTransform.apply(point[0]),
                        inverseTransform.apply(point[1]),
                        inverseTransform.apply(point[2]),
                        inverseTransform.apply(point[3]),
                        individual.getObjectives()[0]));
                sb.append("\n");
            }
        }
        log.info(sb.toString());
    }

}
