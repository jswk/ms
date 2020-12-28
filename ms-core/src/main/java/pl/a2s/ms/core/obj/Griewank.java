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

public class Griewank implements ObjectiveCalculator, MinimaInfoProvider {

    private static final int FREE_TERM = 1;
    private static final double DIVISOR = 4000.0;

    @Override
    public double[] calculate(double[] point) {
        double sum = 0;
        double product = 1;
        for (int i = 0; i < point.length; i++) {
            sum += pow(point[i], 2);
            product *= cos(point[i] / sqrt(i + 1));
        }
        return new double[] { sum / DIVISOR - product + FREE_TERM };
    }

    @Override
    public List<MinimumInfo> getMinimaInfo(Range[] domain) {
        return Collections.singletonList(new MinimumInfo(MinimumInfo.Type.GLOBAL, ArraysUtil.constant(domain.length, 0.), 1e-6, 0.001));
    }

    @Override
    public int getObjectiveCount() {
        return 1;
    }

}
