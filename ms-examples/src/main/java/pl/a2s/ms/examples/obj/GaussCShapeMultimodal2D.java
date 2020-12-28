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

// Double valley C-shaped Gauss (multiwinner paper, 6.2 slightly modified)
public class GaussCShapeMultimodal2D extends AbstractGauss {

    public GaussCShapeMultimodal2D() {
        super(.5);
        addValley(-1, 0, true);
        addValley(1.5, -1.5, false);
    }

    protected void addValley(double x1, double x2, boolean flip) {
        final double off = (flip ? -1 : 1) * 1.5;
        addTerm(gauss2D(1.0, 0.5, x1 + 0.0, x2 + 1.5));
        addTerm(gauss2D(0.5, 1.0, x1 + off, x2 + 0.0));
        addTerm(gauss2D(1.0, 0.5, x1 + 0.0, x2 - 1.5));
    }
}
