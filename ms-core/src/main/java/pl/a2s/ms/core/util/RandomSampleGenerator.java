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

package pl.a2s.ms.core.util;

import java.util.Random;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RandomSampleGenerator {
    private final Random rand;

    public double[][] uniformSample(int n, Range[] domain) {
        if (n < 1 || domain == null || domain.length < 1) {
            throw new IllegalArgumentException("n and domain length must be positive");
        }
        final double[][] sample = new double[n][domain.length];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < domain.length; j++) {
                // uniform within domain box
                sample[i][j] = rand.nextDouble() * (domain[j].getEnd() - domain[j].getStart()) + domain[j].getStart();
            }
        }
        return sample;
    }
}
