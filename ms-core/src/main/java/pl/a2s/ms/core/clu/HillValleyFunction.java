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

import java.util.Arrays;

import org.apache.commons.math3.util.MathArrays;

import pl.a2s.ms.core.ie.IndividualEvaluator;

/**
 * Implementation of 'hill-valley' function from R. K. Ursem CEC'99 paper <em>Multinational
 * evolutionary algorithms</em>. It is however translated into minimization language, so it
 * is actually 'hollow-ridge' function.
 *
 * @author Maciej Smołka
 *
 */
public class HillValleyFunction {

    private final int intermediatePoints;
    private final IndividualEvaluator ie;

    public HillValleyFunction(int intermediatePoints, IndividualEvaluator ie) {
        this.intermediatePoints = intermediatePoints;
        this.ie = ie;
    }

    public double compute(double[] point1, double[] point2) {
        final double objective1 = Arrays.stream(ie.evaluate(point1)).max().getAsDouble();
        final double objective2 = Arrays.stream(ie.evaluate(point2)).max().getAsDouble();
        final double maxObjective = Math.max(objective1, objective2);
        double out = 0.;
        for (int i = 1; i <= intermediatePoints; i++) {
            final double r = i / (intermediatePoints + 1.0);
            final double[] intermediate = MathArrays.ebeAdd(
                    MathArrays.scale(1 - r, point1), MathArrays.scale(r, point2));
            final double[] interObjs = ie.evaluate(intermediate);
            final double interObj = Arrays.stream(interObjs).max().getAsDouble();
            if (interObj - maxObjective > out) {
                out = interObj - maxObjective;
            }
        }
        return out;
    }

}
