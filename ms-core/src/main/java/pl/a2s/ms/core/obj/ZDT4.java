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

public class ZDT4 extends AbstractZDT {

    @Override
    protected double f1(double x1) {
        return x1;
    }

    @Override
    protected double g(double[] x) {
        double sum = 0.;
        for (int i = 1; i < x.length; i++) {
            sum += Math.pow(x[i], 2) - 10.*Math.cos(4.*Math.PI*x[i]);
        }
        return 1. + 10. * (x.length - 1) + sum;
    }

    @Override
    protected double h(double f1, double g) {
        return 1. - Math.sqrt(f1/g);
    }

}
