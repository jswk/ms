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

import pl.a2s.ms.core.analysis.MinimaInfoProvider;
import pl.a2s.ms.core.analysis.MinimumInfo;
import pl.a2s.ms.core.util.ArraysUtil;
import pl.a2s.ms.core.util.Range;

import java.util.Collections;
import java.util.List;

import static org.apache.commons.math3.util.FastMath.*;

public class Ackley implements ObjectiveCalculator, MinimaInfoProvider {

    private final static double A = 20.;
    private final static double B = 0.2;
    private final static double C = 2 * PI;

    @Override
    public double[] calculate(double[] point) {
        final int n = point.length;
        if (n == 0) {
            return new double[] { 0 };
        }
        double norm2 = 0;
        double sumcos = 0;
        for (double v : point) {
            norm2 += pow(v, 2);
            sumcos += cos(C * v);
        }
        return new double[] { -A * exp(-B * sqrt(norm2 / n)) - exp(sumcos / n) + A + E };
    }

    @Override
    public List<MinimumInfo> getMinimaInfo(Range[] domain) {
        return Collections.singletonList(new MinimumInfo(MinimumInfo.Type.GLOBAL, ArraysUtil.constant(domain.length, 0.), 1e-2, 0.001));
    }

    @Override
    public int getObjectiveCount() {
        return 1;
    }

}
