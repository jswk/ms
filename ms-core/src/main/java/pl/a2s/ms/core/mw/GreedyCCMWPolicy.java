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

import lombok.extern.java.Log;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Implements the greedyCC multiwinner rule.
 * <p>
 * It is based on the python code by Piotr Faliszewski.
 *
 * @author Jakub Sawicki
 */
@Log
public class GreedyCCMWPolicy extends MWPolicy {

    /// Multiwinner helper.
    final private MWPolicyHelper helper;

    public GreedyCCMWPolicy(UtilFunction util, Random rand) {
        this.helper = new MWPolicyHelper(util, rand);
        log.info("GreedyCCMWPolicy created with util="+util.toString());
    }

    @Override
    public void select(Population poolPopulation, Individual[] selected) throws MathIllegalArgumentException {
        final Individual[] pool = poolPopulation.getIndividuals();
        final int[] ranks = poolPopulation.getRanks();
        final int n = pool.length;
        final int k = selected.length;
        checkParameters(pool, k);

        final int[] randPerm = helper.getRandomPermuation(pool.length);

        final double[] F = new double[n];
        final double[][] X = new double[n][];

        log.fine(() -> "Selecting from pool of "+n+", k="+k);

        int maxRank = 0;
        for (int i = 0; i < n; i++) {
            maxRank = Math.max(maxRank, ranks[i]);
        }

        for (int i = 0; i < n; i++) {
            if (isKeepOriginalFitness()) {
                // Not default. Only for inverted-LBA phases
                F[i] = ranks[randPerm[i]];
                log.info("Original fitnesses taken in MW selection:\n"
                        + Arrays.toString(F));
            } else {
                // linear transformation of rank to fitness,
                // bounded by 1 from the bottom
                // This is the default!
                F[i] = maxRank+1-ranks[randPerm[i]];
                log.info("Transformed fitnesses taken in MW selection:\n"
                        + Arrays.toString(F));
            }
            X[i] = pool[randPerm[i]].getPoint();
        }
        log.finer(() -> "Candidates:"+candidatesToString(X, F));

        final int[] winners = select(F, X, k);
        log.finer(() -> "Winners: "+winnersToString(winners, X));
        for (int i = 0; i < winners.length; i++) {
            selected[i] = pool[randPerm[winners[i]]];
        }
    }

    private int[] select(double[] F, double[][] X, int k) {
        final double[][] U = helper.computeUtilities(F, X);

        final int[][] P = helper.computePrefOrders(U);

        return rule(P, k);
    }

    private int[] rule(int[][] P, int k) {
        final int m = P[0].length;
        final List<Integer> C = new ArrayList<>(m);
        IntStream.range(0, m).forEachOrdered(C::add);
        final int[] W = new int[k];

        for (int i = 0; i < k; i++) {
            int bestScore = -1;
            int bestCandidate = -1;
            for (final int j : C) {
                W[i] = j;
                final int s = helper.ccScoreProfile(P, W, i + 1);
                if (s > bestScore) {
                    bestScore = s;
                    bestCandidate = j;
                }
            }
            final int bc = bestCandidate;
            W[i] = bc;
            C.removeIf((Integer val) -> (val == bc));
        }

        return W;
    }

    private String candidatesToString(double[][] X, double[] F) {
        final int n = X.length;
        final StringBuilder sb = new StringBuilder();
        final int dim = X[0].length;
        for (int i = 0; i < n; i++) {
            sb.append('\n');
            sb.append(i);
            sb.append(' ');
            for (int j = 0; j < dim; j++) {
                sb.append(X[i][j]);
                sb.append(' ');
            }
            sb.append(F[i]);
        }
        return sb.toString();
    }

    private String winnersToString(int[] winners, double[][] X) {
        final int dim = X[0].length;
        final StringBuilder sb = new StringBuilder();
        for (int winner : winners) {
            sb.append('\n');
            sb.append(winner);
            for (int j = 0; j < dim; j++) {
                sb.append(' ');
                sb.append(X[winner][j]);
            }

        }
        return sb.toString();
    }
}
