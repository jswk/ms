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

import java.util.ArrayList;
import java.util.List;

import pl.a2s.ms.core.analysis.MinimaInfoProvider;
import pl.a2s.ms.core.analysis.MinimumInfo;
import pl.a2s.ms.core.analysis.MinimumInfo.Type;
import pl.a2s.ms.core.obj.AbstractGauss;
import pl.a2s.ms.core.util.Range;

public class GaussMultimodal2D extends AbstractGauss implements MinimaInfoProvider {
    public GaussMultimodal2D() {
        super(.5, gauss2D(1.0, 1.0, 1.0, 1.0), gauss2D(6.0, 1.0, 0.2, 0.2), gauss2D(7.0, 8.0, 0.2, 0.2));
    }

    @Override
    public List<MinimumInfo> getMinimaInfo(Range[] domain) {
        final List<MinimumInfo> infos = new ArrayList<>();
        infos.add(new MinimumInfo(Type.GLOBAL, new double[] { 1.0, 1.0 }, 1, 0.5));
        infos.add(new MinimumInfo(Type.GLOBAL, new double[] { 6.0, 1.0 }, 1, 0.1));
        infos.add(new MinimumInfo(Type.GLOBAL, new double[] { 7.0, 8.0 }, 1, 0.1));
        return infos;
    }
}
