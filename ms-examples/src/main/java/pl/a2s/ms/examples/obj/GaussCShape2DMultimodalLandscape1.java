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

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.pow;

import java.util.ArrayList;
import java.util.List;

import pl.a2s.ms.core.obj.AbstractGauss;
import pl.a2s.ms.core.util.Range;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class GaussCShape2DMultimodalLandscape1 extends AbstractGauss {

    private static final double SIZE_1 = 1.0;
    private static final double SIZE_2 = 0.5;
    private static final double SPACING = 0.8;
    private static final double BLOCK_SIZE = 4.0;
    private static final int DIM = 2;

    @Getter
    private final List<MinimalRegion> minimalRegions;

    public GaussCShape2DMultimodalLandscape1(Range[] domain) {
        super(0.1);
        if (domain.length != DIM) {
            throw new IllegalArgumentException("Domain must be 2D");
        }
        for (int i = 0; i < DIM; i++) {
            if (domain[i].getStart() != 0.) {
                throw new IllegalArgumentException("Domain must start at 0. in every dimension");
            }
        }
        minimalRegions = new ArrayList<>();
        for (int i = 0; BLOCK_SIZE * i < domain[0].getEnd(); i++) {
            for (int j = 0; BLOCK_SIZE * j < domain[1].getEnd(); j++) {
                addValley(BLOCK_SIZE * i + BLOCK_SIZE / 2, BLOCK_SIZE * j + BLOCK_SIZE / 2, i + j);
            }
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class MinimalRegion {
        private final List<Ellipsoid> ellipsoids;
    }

    @Data
    @RequiredArgsConstructor
    public static class Ellipsoid {
        private final double x1;
        private final double x2;
        private final double r1;
        private final double r2;

        public boolean covers(double x1, double x2) {
            val x1dist = abs(x1 - this.x1);
            val x2dist = abs(x2 - this.x2);
            if (x1dist > r1 || x2dist >  r2) {
                return false;
            }
            val normSqDist = pow(x1dist / r1, 2) + pow(x2dist / r2, 2);
            return normSqDist <= 1.;
        }
    }

    private void addTermAndEllipsoid(List<Ellipsoid> ellipsoids, double x1, double x2, double r1, double r2) {
        addTerm(gauss2D(x1, x2, r1, r2));
        ellipsoids.add(new Ellipsoid(x1, x2, r1, r2));
    }

    private void addValley(double x1, double x2, int selector) {
        final int variant = selector % 4;
        val ellipsoids = new ArrayList<Ellipsoid>();
        switch (variant) {
        case 0:
//            addTermAndEllipsoid(ellipsoids, x1,           x2 + SPACING, SIZE_1, SIZE_2);
            addTermAndEllipsoid(ellipsoids, x1 + SPACING, x2          , SIZE_2, SIZE_1);
            addTermAndEllipsoid(ellipsoids, x1,           x2 - SPACING, SIZE_1, SIZE_2);
            addTermAndEllipsoid(ellipsoids, x1 - SPACING, x2          , SIZE_2, SIZE_1);
            break;
        case 1:
            addTermAndEllipsoid(ellipsoids, x1,           x2 + SPACING, SIZE_1, SIZE_2);
//            addTermAndEllipsoid(ellipsoids, x1 + SPACING, x2          , SIZE_2, SIZE_1);
            addTermAndEllipsoid(ellipsoids, x1,           x2 - SPACING, SIZE_1, SIZE_2);
            addTermAndEllipsoid(ellipsoids, x1 - SPACING, x2          , SIZE_2, SIZE_1);
            break;
        case 2:
            addTermAndEllipsoid(ellipsoids, x1,           x2 + SPACING, SIZE_1, SIZE_2);
            addTermAndEllipsoid(ellipsoids, x1 + SPACING, x2          , SIZE_2, SIZE_1);
//            addTermAndEllipsoid(ellipsoids, x1,           x2 - SPACING, SIZE_1, SIZE_2);
            addTermAndEllipsoid(ellipsoids, x1 - SPACING, x2          , SIZE_2, SIZE_1);
            break;
        case 3:
            addTermAndEllipsoid(ellipsoids, x1,           x2 + SPACING, SIZE_1, SIZE_2);
            addTermAndEllipsoid(ellipsoids, x1 + SPACING, x2          , SIZE_2, SIZE_1);
            addTermAndEllipsoid(ellipsoids, x1,           x2 - SPACING, SIZE_1, SIZE_2);
//            addTermAndEllipsoid(ellipsoids, x1 - SPACING, x2          , SIZE_2, SIZE_1);
            break;
        }
        minimalRegions.add(new MinimalRegion(ellipsoids));
    }

}
