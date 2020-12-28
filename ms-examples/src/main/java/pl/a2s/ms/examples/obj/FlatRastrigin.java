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

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.math3.util.FastMath;
import pl.a2s.ms.core.analysis.MinimaInfoProvider;
import pl.a2s.ms.core.analysis.MinimumInfo;
import pl.a2s.ms.core.analysis.MinimumInfo.Type;
import pl.a2s.ms.core.obj.ObjectiveCalculator;
import pl.a2s.ms.core.util.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.apache.commons.math3.util.FastMath.*;

@RequiredArgsConstructor
public class FlatRastrigin implements ObjectiveCalculator, MinimaInfoProvider {

    private final double A = 1./2.;

    @Override
    public double[] calculate(double[] point) {
        final int n = point.length;
        double f = A * n;
        for (int i = 0; i < n; i++) {
            f -= A * cos(2 * FastMath.PI * point[i]);
        }
        return new double[] { f };
    }

    @Override
    public int getObjectiveCount() {
        return 1;
    }

    @Override
    public List<MinimumInfo> getMinimaInfo(Range[] domain) {
        final double tol = 0.5;
        final double dist = 0.2;
        val out = new ArrayList<MinimumInfo>();
        final int n = domain.length;
        val curr = new int[n];
        val limit = new int[n]; // inclusive
        val start = new double[n];
        final Supplier<double[]> genPoint = () -> {
            val o = new double[n];
            for (int i = 0; i < n; i++) {
                o[i] = start[i] + curr[i];
            }
            return o;
        };
        for (int i = 0; i < n; i++) {
            curr[i] = 0;
            limit[i] = (int) round(floor(domain[i].getEnd()) - ceil(domain[i].getStart()));
            start[i] = ceil(domain[i].getStart());
        }
        while (curr[0] <= limit[0]) {
            out.add(new MinimumInfo(Type.GLOBAL, genPoint.get(), tol, dist));
            curr[n-1]++;
            // omit i == 0 explicitly, as it would lead to reference outside of curr
            for (int i = n-1; i > 0; i--) {
                // if curr[i] is valid, don't modify it and assume previous ones are ok as well
                if (curr[i] <= limit[i]) {
                    break;
                }
                // like addition with carry on
                curr[i] = 0;
                curr[i-1]++;
            }
        }
        return out;
    }

}
