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
import pl.a2s.ms.core.obj.ObjectiveCalculator;

public class MOFlatGaussMultimodal2D1 implements ObjectiveCalculator {

    private static class Obj1 extends AbstractGauss {

        public Obj1() {
            super(.5);
            addTerm(gauss2D(2, 2, 0.5, 1));
            addTerm(gauss2D(8, 5, 0.5, 1));
        }
    }

    private static class Obj2 extends AbstractGauss {

        public Obj2() {
            super(.5);
            addTerm(gauss2D(3, 3, 1, 0.5));
            addTerm(gauss2D(7, 5, 1, 0.5));
        }
    }

    private final Obj1 obj1;
    private final Obj2 obj2;

    public MOFlatGaussMultimodal2D1() {
        obj1 = new Obj1();
        obj2 = new Obj2();
    }

    @Override
    public double[] calculate(double[] point) {
        return new double[] { obj1.calculate(point)[0], obj2.calculate(point)[0] };
    }

    @Override
    public int getObjectiveCount() {
        return 2;
    }
}
