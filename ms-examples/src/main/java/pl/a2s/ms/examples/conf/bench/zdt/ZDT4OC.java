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

package pl.a2s.ms.examples.conf.bench.zdt;

import pl.a2s.ms.core.obj.AbstractZDT;
import pl.a2s.ms.core.obj.ZDT4;
import pl.a2s.ms.core.util.Range;

import java.util.Arrays;

public class ZDT4OC extends AbstractZDTOC {

    @Override
    protected AbstractZDT getZdtCalculator() {
        return new ZDT4();
    }

    @Override
    protected Range[] getDomain(int dim) {
        final Range[] domain = new Range[dim];
        domain[0] = new Range(0.,1.);
        Arrays.fill(domain, 1, dim, new Range(-5.,5.));
        return domain;
    }

}
