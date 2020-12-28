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

import static org.apache.commons.math3.util.FastMath.*;

import java.util.List;
import java.util.stream.Collectors;

import pl.a2s.ms.core.analysis.MinimaInfoProvider;
import pl.a2s.ms.core.analysis.MinimumInfo;
import pl.a2s.ms.core.util.Range;
import lombok.val;

public class TransformedObjectiveCalculator implements ObjectiveCalculator, MinimaInfoProvider {

    private final ObjectiveCalculator oc;
    private final MinimaInfoProvider mip;
    private final double[] scale;

    /**
     * @param oc
     * @param scale the features of oc will be scaled by it
     */
    public TransformedObjectiveCalculator(ObjectiveCalculator oc, double[] scale) {
        this.oc = oc;
        if (!(oc instanceof MinimaInfoProvider)) {
            throw new IllegalArgumentException("Passed OC must be a MinimaInfoProvider as well");
        }
        this.mip = (MinimaInfoProvider) oc;
        this.scale = scale;
    }

    @Override
    public double[] calculate(double[] point) {
        val scaledPoint = new double[point.length];
        for (int i = 0; i < point.length; i++) {
            scaledPoint[i] = point[i] / scale[i];
        }
        return oc.calculate(scaledPoint);
    }

    @Override
    public int getObjectiveCount() {
        return oc.getObjectiveCount();
    }

    @Override
    public List<MinimumInfo> getMinimaInfo(Range[] domain) {
        val scaledDomain = new Range[domain.length];
        for (int i = 0; i < domain.length; i++) {
            scaledDomain[i] = new Range(domain[i].getStart() / scale[i], domain[i].getEnd() / scale[i]);
        }
        val scaledMinimaInfo = mip.getMinimaInfo(scaledDomain);
        double distScale = Double.MAX_VALUE;
        for (int i = 0; i < domain.length; i++) {
            distScale = min(distScale, scale[i]);
        }
        return scaledMinimaInfo.stream()
                .map(smi -> {
                    val rescaledPoint = new double[domain.length];
                    val scaledPoint = smi.getPoint();
                    for (int i = 0; i < domain.length; i++) {
                        rescaledPoint[i] = scaledPoint[i] * scale[i];
                    }
                    return new MinimumInfo(smi.getType(), rescaledPoint, smi.getTolerance(), smi.getDistance());
                })
                .collect(Collectors.toList());
    }

}
