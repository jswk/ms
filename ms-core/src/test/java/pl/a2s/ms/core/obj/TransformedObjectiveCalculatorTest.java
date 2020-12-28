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

import lombok.val;
import org.junit.jupiter.api.Test;
import pl.a2s.ms.core.analysis.MinimaInfoProvider;
import pl.a2s.ms.core.analysis.MinimumInfo;
import pl.a2s.ms.core.util.Range;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class TransformedObjectiveCalculatorTest {

    private static class TestOC implements ObjectiveCalculator, MinimaInfoProvider {

        private Range[] domain;

        @Override
        public List<MinimumInfo> getMinimaInfo(Range[] domain) {
            this.domain = domain;
            return Arrays.asList(new MinimumInfo(null, new double[] { 1., 1. }, 0, 1.));
        }

        @Override
        public double[] calculate(double[] point) {
            throw new IllegalStateException();
        }

        @Override
        public int getObjectiveCount() {
            throw new IllegalStateException();
        }

    }

    @Test
    public void test() {
        val oc = new TestOC();
        val transOC = new TransformedObjectiveCalculator(oc, new double[] { 0.5, 1. });

        val minimaInfo = transOC.getMinimaInfo(new Range[] {
                new Range(-1,1),
                new Range(-3,2)
        });

        assertThat(minimaInfo).hasSize(1);
        assertThat(minimaInfo.get(0).getPoint()[0]).isEqualTo(0.5);
        assertThat(minimaInfo.get(0).getPoint()[1]).isEqualTo(1.);

        assertThat(oc.domain).isEqualTo(new Range[] {
                new Range(-2, 2),
                new Range(-3, 2)
        });
    }

}
