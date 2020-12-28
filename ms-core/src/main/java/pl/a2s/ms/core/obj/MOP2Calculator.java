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

package pl.a2s.ms.core.obj;

import org.apache.commons.math3.util.FastMath;

import static org.apache.commons.math3.util.FastMath.exp;

public class MOP2Calculator implements ObjectiveCalculator {

    @Override
    public double[] calculate(double[] point) {
        final int n = point.length;
        double sum1 = 0.;
        double sum2 = 0.;
        final double oneOverRootN = 1./FastMath.sqrt(n);
        for (double v : point) {
            sum1 += FastMath.pow(v - oneOverRootN, 2);
            sum2 += FastMath.pow(v + oneOverRootN, 2);
        }
        return new double[] { 1. - exp(-sum1), 1. - exp(-sum2) };
    }

    @Override
    public int getObjectiveCount() {
        return 2;
    }

}
