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

import lombok.val;
import org.junit.jupiter.api.Test;
import pl.a2s.ms.core.util.Range;
import pl.a2s.ms.examples.obj.FlatRastrigin;

import static org.assertj.core.api.Assertions.assertThat;

public class FlatRastriginTest {

    @Test
    public void test() {
        val rastrigin = new FlatRastrigin();

        val minimaInfo = rastrigin.getMinimaInfo(new Range[] {
                new Range(0., 2.5),
                new Range(-2.5, -1.5),
                new Range(1.5, 2.)
        });

        assertThat(minimaInfo).hasSize(3);
    }

}
