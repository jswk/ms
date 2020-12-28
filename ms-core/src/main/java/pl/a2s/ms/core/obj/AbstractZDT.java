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

public abstract class AbstractZDT implements ObjectiveCalculator {

    @Override
    public double[] calculate(double[] point) {
        final double f1 = f1(point[0]);
        final double g = g(point);
        final double h = h(f1, g);
        final double f2 = f2(g, h);
        return new double[] {f1, f2};
    }

    protected abstract double f1(double x1);
    protected abstract double g(double[] x);
    protected abstract double h(double f1, double g);

    protected double f2(double g, double h) {
        return g * h;
    }

    public double front_f2(double f1) {
        final double minG = 1.;
        return f2(minG, h(f1, minG));
    }

    @Override
    public int getObjectiveCount() {
        return 2;
    }
}
