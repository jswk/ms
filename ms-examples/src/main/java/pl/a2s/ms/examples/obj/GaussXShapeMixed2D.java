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

import pl.a2s.ms.core.obj.AbstractGauss;
import pl.a2s.ms.core.obj.AbstractGauss.Term;
import pl.a2s.ms.core.obj.ObjectiveCalculator;

public class GaussXShapeMixed2D implements ObjectiveCalculator {

    private final Term termFlatGaussian;
    private final Term termGaussian;

    public GaussXShapeMixed2D() {
        termFlatGaussian = AbstractGauss.gauss(new double[] {0., 0.}, new double[] {0.5, 5.});
        termGaussian = AbstractGauss.gauss(new double[] {0., 0.}, new double[] {5., 0.5});
    }

    @Override
    public double[] calculate(double[] point) {
        double val = termFlatGaussian.apply(point);
        val = Math.max(2 * val - 1., 0);
        val *= termGaussian.apply(point);
        return new double[] {val};
    }

    @Override
    public int getObjectiveCount() {
        return 1;
    }

}
