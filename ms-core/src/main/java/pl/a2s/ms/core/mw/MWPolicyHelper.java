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

import static java.util.Comparator.comparingDouble;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.util.MathArrays;

/**
 * The class contains common methods used in Multiwinner Selection.
 *
 * @author Jakub Sawicki
 */
public class MWPolicyHelper {

    private final UtilFunction util;
    private final Random rand;

    public MWPolicyHelper(UtilFunction util, Random rand) {
        this.util = util;
        this.rand = rand;
    }

    /**
     * Creates a copy of the pool, shuffles it and returns it.
     * <p>
     * It should mitigate the problem of a deterministic bias.
     *
     * @param pool
     * @return
     */
    public int[] getRandomPermuation(int size) {
        final int[] shuffled = new int[size];
        for (int i = 0; i < size; i++) {
            shuffled[i] = i;
        }
        for (int i = size-1; i > 0; i--) {
            final int j = rand.nextInt(i+1);
            final int tmp = shuffled[i];
            shuffled[i] = shuffled[j];
            shuffled[j] = tmp;
        }
        return shuffled;
    }

    /**
     * Computes the utilities of the candidates.
     *
     * @param F fitness list
     * @param X point list
     * @return utility array
     */
    public double[][] computeUtilities(double[] F, double[][] X) {
        final int n = X.length;
        final double[][] D = computeDistances(X);
        final double[][] U = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                U[i][j] = util.apply(F[j], D[i][j]);
            }
        }
        return U;
    }

    /**
     * Computes the distances between points.
     *
     * @param X the points
     * @return distance matrix
     */
    public double[][] computeDistances(double[][] X) {
        final int n = X.length;
        final double[][] D = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                D[i][j] = MathArrays.distance(X[i], X[j]);
                D[j][i] = D[i][j];
            }
        }
        return D;
    }

    /**
     * Based on the utility matrix it returns the order of preference of each voter.
     *
     * @param U utility matrix
     * @return preference matrix
     */
    public int[][] computePrefOrders(double[][] U) {
        final int n = U.length;
        final int[][] P = new int[n][n];
        for (int i = 0; i < n; i++) {
            final double[] utils = U[i];
            final Integer[] prefs = new Integer[n];
            for (int j = 0; j < n; j++) {
                prefs[j] = j;
            }
            Arrays.sort(prefs, comparingDouble((Integer key) -> -utils[key]));
            for (int j = 0; j < n; j++) {
                P[i][j] = prefs[j];
            }
        }
        return P;
    }

    /**
     * It returns the total score of the winner set using CC rule.
     *
     * @param P preference matrix
     * @param W winner array
     * @param len winner array length
     * @return the winner set score
     */
    public int ccScoreProfile(int[][] P, int[] W, int len) {
        int sum = 0;
        for (final int[] p : P) {
            sum += ccScore(p,W,len);
        }
        return sum;
    }

    /**
     * Returns the score of winner set according to some preference array.
     *
     * @param p preference array
     * @param W winner array
     * @param len winner array length
     * @return the voter score of winner set
     */
    public int ccScore(int[] p, int[] W, int len) {
        final int m = p.length;
        for (int i = 0; i < m; i++) {
            final int pi = i;
            if (Arrays.stream(W, 0, len).anyMatch(w -> w == p[pi])) {
                return m-i-1;
            }
        }
        throw new IllegalArgumentException("The preference array didn't include all the candidates.");
    }

}
