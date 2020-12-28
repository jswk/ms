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

import pl.a2s.ms.core.util.ArraysUtil;

import java.util.Arrays;

public interface PrecisionSet {

    /**
     * This method should return nonempty array containing
     * all available precision levels
     * ordered according to increasing precision.
     * @return all precision levels (sorted)
     */
    double[] available();

    default double min() {
        return ArraysUtil.first(available());
    }

    default double max() {
        return ArraysUtil.last(available());
    }

    default double getDefault() {
        return max();
    }

    /**
     * This default implementation supports the following cases:
     * levelNo == 1, levelNo == 2, levelNo >= available().length.
     *
     * @param levelNo number of levels to set precision at
     * @return precisions for given number of levels
     * @throws IllegalArgumentException if levelNo is unsupported
     */
    default double[] getForLevels(int levelNo) {
        final double[] av = available();
        if (av.length == 0) {
            return new double[0];
        } else if (levelNo == 1) {
            return new double[] {max()};
        } else if (levelNo == 2) {
            return new double[] {min(), max()};
        } else if (levelNo == av.length) {
            return available();
        } else if (levelNo > av.length) {
            final double[] precs = Arrays.copyOf(av, levelNo);
            for (int i = av.length; i < levelNo; i++) {
                precs[i] = max();
            }
            return precs;
        }
        throw new IllegalArgumentException("Unsupported level number: " + levelNo);
    }

}
