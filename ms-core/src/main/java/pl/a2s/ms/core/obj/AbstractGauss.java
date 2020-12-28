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

import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractGauss implements ObjectiveCalculator {

    private static final double LOG2 = log(2);

    public interface Term extends Function<double[], Double> {
    }

    private final List<Term> terms = new ArrayList<>();

    private final double cutOffLevel;

    protected AbstractGauss(double cutOffLevel, Term... terms) {
        Collections.addAll(this.terms, terms);
        this.cutOffLevel = cutOffLevel;
    }

    protected void addTerm(Term term) {
        terms.add(term);
    }

    public static Term gauss(double[] center, double[] r) {
        return (double[] x) -> {
            double d = 0;
            for (int i = 0; i < center.length; ++i) {
                final double a = (x[i] - center[i]) / r[i];
                d += a * a;
            }
            return 1 - exp(-LOG2 * d);
        };
    }

    protected static Term gauss2D(double x1, double x2, double r1, double r2) {
        return gauss(new double[] { x1, x2 }, new double[] { r1, r2 });
    }

    protected static Term gauss3D(double x1, double x2, double x3, double r1, double r2, double r3) {
        return gauss(new double[] { x1, x2, r3 }, new double[] { r1, r2, r3 });
    }

    @Override
    public double[] calculate(double[] point) {
        double val = 1;
        for (final Term t : terms) {
            val *= t.apply(point);
        }
        if (cutOffLevel != 0.) {
            return new double[] { max((val - cutOffLevel) / (1. - cutOffLevel), 0) };
        } else {
            return new double[] { val };
        }
    }

    @Override
    public int getObjectiveCount() {
        return 1;
    }

}
