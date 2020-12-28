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

package pl.a2s.ms.core.mw;

import org.apache.commons.math3.util.FastMath;

public class UtilFunctions {

    /**
     * Returns an instance of {@link UtilFunction} with exponents set to a and b.
     * <p>
     * The util function is of type: fit^a/dist^b.
     *
     * @param a fitness exponent
     * @param b distance exponent
     * @return the util function
     */
    public static UtilFunction poly(double a, double b) {
        return new UtilFunction() {

            @Override
            public Double apply(Double fitness, Double distance) {
                if (distance != 0.) {
                    return FastMath.pow(fitness, a) / FastMath.pow(distance, b);
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }

            @Override
            public String toString() {
                return "f^a/d^b, a=" + a + ", b=" + b;
            }
        };
    }

    /**
     * Returns an instance of {@link UtilFunction} with exponents set to a and b.
     * <p>
     * The util function is of type: fit^a/(1+dist^b).
     *
     * @param a fitness exponent
     * @param b distance exponent
     * @return the util function
     */
    public static UtilFunction polyPlus1(double a, double b) {
        return new UtilFunction() {

            @Override
            public Double apply(Double fitness, Double distance) {
                return FastMath.pow(fitness, a) / (1. + FastMath.pow(distance, b));
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder();
                sb.append("f^a/(1+d^b), a=");
                sb.append(a);
                sb.append(", b=");
                sb.append(b);
                return sb.toString();
            }
        };
    }

    /**
     * Returns linearly scaled version of a given {@link UtilFunction}.
     * <p>
     * The util function is of type: f(scale * fit, dist).
     *
     * @param scale scale
     * @param f {@link UtilFunction} function to be scaled
     * @return the util function
     */
    public static UtilFunction scaled(double scale, UtilFunction f) {
        return new UtilFunction() {

            @Override
            public Double apply(Double fitness, Double distance) {
                return f.apply(scale * fitness, distance);
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder();
                sb.append("scaled ");
                sb.append(f);
                sb.append(", scale=");
                sb.append(scale);
                return sb.toString();
            }
        };
    }

    /**
     * Returns an instance of {@link UtilFunction} with exponents set to a and b.
     * <p>
     * The util function is of type: fit^a/(1+dist^b) and also returns 0 if dist==0.
     * <p>
     * Watch out with this one, it reduces the diversity maintenance greatly.
     * It is not possible for a single individual to survive outside the main cluster.
     * On the other hand, duplicates are penalized.
     *
     * @param a fitness exponent
     * @param b distance exponent
     * @return the util function
     */
    public static UtilFunction polyzeroPlus1(double a, double b) {
        return new UtilFunction() {

            @Override
            public Double apply(Double fitness, Double distance) {
                if (distance == 0) {
                    return 0.;
                } else {
                    return FastMath.pow(fitness, a) / (1. + FastMath.pow(distance, b));
                }
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder();
                sb.append("f^a/(1+d^b) or 0, a=");
                sb.append(a);
                sb.append(", b=");
                sb.append(b);
                return sb.toString();
            }
        };
    }
}
