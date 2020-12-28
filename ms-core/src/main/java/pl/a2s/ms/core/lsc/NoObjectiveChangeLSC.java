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

package pl.a2s.ms.core.lsc;

import org.apache.commons.math3.util.MathArrays;

import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NoObjectiveChangeLSC implements LocalStopCondition {

    private final int epochs;
    private final double[] objectiveChangeThreshold;

    @Override
    public boolean shouldStop(Deme deme, State state) {
        if (deme.getHistory().size() < epochs) {
            return false;
        }
        final Population curr = deme.getPopulation();
        final Population prev = deme.getHistory().get(deme.getHistory().size() - epochs).getPopulation();
        final double[] avgCurr = getAverageObjectives(curr);
        final double[] avgPrev = getAverageObjectives(prev);
        final double[] diff = MathArrays.ebeSubtract(avgCurr, avgPrev);
        for (int i = 0; i < diff.length; i++) {
            final double threshold = objectiveChangeThreshold[i];
            // if any objective changed over threshold, then continue
            if (Math.abs(diff[i]) > threshold) {
                return false;
            }
        }
        // otherwise stop the deme
        return true;
    }

    private static double[] getAverageObjectives(Population population) {
        final int objCount = population.getIndividuals()[0].getObjectives().length;
        final double[] avg = new double[objCount];
        for (final Individual ind: population.getIndividuals()) {
            for (int i = 0; i < objCount; i++) {
                avg[i] += ind.getObjectives()[i];
            }
        }
        for (int i = 0; i < objCount; i++) {
            avg[i] /= population.getSize();
        }
        return avg;
    }

}
