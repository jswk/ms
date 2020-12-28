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

package pl.a2s.ms.examples.obj;

import lombok.val;
import pl.a2s.ms.core.obj.ObjectiveCalculator;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.max;

/**
 * Domain: [0,1]^2
 *
 * @author Jakub Sawicki
 */
public class MOKubaBenchmark implements ObjectiveCalculator {

    @Override
    public double[] calculate(double[] point) {
        if (point.length != 2) {
            throw new IllegalArgumentException("Point must have 2 dimensions, has "+point.length+" dimensions");
        }
        val x1 = point[0];
        val x2 = point[1];
        val slope = max(abs(x2 - .5) - .1, 0.);
        val f1 = x1 + slope;
        val f2 = 1. - x1 + slope;
        return new double[] { f1, f2 };
    }

    @Override
    public int getObjectiveCount() {
        return 2;
    }

}
