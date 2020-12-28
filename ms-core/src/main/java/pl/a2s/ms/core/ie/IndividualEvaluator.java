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

package pl.a2s.ms.core.ie;

import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.obj.ObjectiveCalculator;
import lombok.Getter;

/**
 * By evaluating I mean calculating objectives, not necessarily the
 * fitness/rank.
 *
 * @author Jakub Sawicki
 */
public class IndividualEvaluator {

    protected final ObjectiveCalculator fc;
    @Getter
    protected int evaluationCount;

    public IndividualEvaluator(ObjectiveCalculator fc) {
        this.fc = fc;
        evaluationCount = 0;
    }

    public void evaluate(Population population) {
        final Individual[] individuals = population.getIndividuals();
        boolean changed = false;
        for (final Individual ind: individuals) {
            if (ind.getObjectives() == null) {
                evaluationCount++;
                ind.setObjectives(fc.calculate(ind.getPoint()));
                changed = true;
            }
        }
        if (fc.getObjectiveCount() > 1 && changed) {
            population.updateRanks();
        }
    }

    public double[] evaluate(double[] point) {
        evaluationCount++;
        return fc.calculate(point);
    }

}
