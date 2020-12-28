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

package pl.a2s.ms.core.analysis;

import lombok.Data;
import lombok.val;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.pow;

/**
 * This interface was created with single-objective functions in mind.
 *
 * @author Jakub Sawicki
 */
@Data
public class MinimumInfo {
    public enum Type {
        GLOBAL, LOCAL
    }

    private final Type type;
    private final double[] point;
    /// if zero, should be ignored
    private final double tolerance;
    /// if zero, should be ignored
    private final double distance;

    public boolean pointWithinDistance(double[] point) {
        if (point.length != this.point.length) {
            throw new IllegalArgumentException("Dimensions don't match up!");
        }
        val dim = point.length;
        val dists = new double[dim];
        for (int i = 0; i < dim; i++) {
            dists[i] = abs(point[i] - this.point[i]);
            if (dists[i] > distance) {
                return false;
            }
        }
        double sumSq = 0.;
        for (int i = 0; i < dim; i++) {
            sumSq += pow(dists[i], 2);
        }
        return sumSq <= pow(distance, 2);
    }

}
