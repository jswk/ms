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

package pl.a2s.ms.examples.bootstrap;

import pl.a2s.ms.examples.obj.GaussXShapeMixed2D;
import pl.a2s.ms.core.obj.ObjectiveCalculator;

public class ObjCalc1 {

    public static void main(String[] args) {
        final ObjectiveCalculator calc = new GaussXShapeMixed2D();

        final int count = 200;
        final double min = -10;
        final double max = 10;

        for (int i = 0; i <= count; i++) {
            for (int j = 0; j <= count; j++) {
                final double x = min + (max-min)*i/count;
                final double y = min + (max-min)*j/count;
                final double[] vals = calc.calculate(new double[] {x, y});
                System.out.printf("%f %f %e%n", x, y, vals[0]);
            }
            System.out.println();
        }
    }

}
