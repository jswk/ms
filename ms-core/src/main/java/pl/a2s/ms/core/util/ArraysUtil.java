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

import java.util.Arrays;
import java.util.Collections;

import lombok.val;

public class ArraysUtil {

    public static int[] constant(int length, int value) {
        val out = new int[length];
        Arrays.fill(out, value);
        return out;
    }

    public static double[] constant(int length, double value) {
        val out = new double[length];
        Arrays.fill(out, value);
        return out;
    }

    public static <T> T[] constant(T value, T[] array) {
        return Collections.nCopies(array.length, value).toArray(array);
    }

    public static <T> T first(T[] tab) {
        return tab[0];
    }

    public static double first(double[] tab) {
        return tab[0];
    }

    public static <T> T last(T[] tab) {
        return tab[tab.length - 1];
    }

    public static double last(double[] tab) {
        return tab[tab.length - 1];
    }

}
