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

package pl.a2s.ms.core.clu;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import pl.a2s.ms.core.cmaes.CMAES;
import pl.a2s.ms.core.cmaes.CMAESOptimizer;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.util.ArraysUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CMADemeToClusterConverter {

    public List<Cluster> convert(HgsState state) {
        final List<Ellipsoid> ellipsoids = new ArrayList<>();
        final Level level = ArraysUtil.last(state.getHgsDemes());
        if (!(level.getEvoAlg() instanceof CMAES)) {
            throw new IllegalStateException("Last level doesn't have CMAES set as evoAlg");
        }
        final CMAES levelCmaEs = (CMAES) level.getEvoAlg();
        for (final Deme deme: level.getDemes()) {
            final CMAESOptimizer cmaesOptimizer = levelCmaEs.getOptimizer(deme);
            try {
            ellipsoids.add(Ellipsoid.builder()
                    .mean(cmaesOptimizer.getXmean())
                    .invCov(MatrixUtils.inverse(cmaesOptimizer.getC()))
                    .sigma(cmaesOptimizer.getSigma())
                    .build());
            } catch (final Exception ex) {
                // basically ignore, the ellipsoid doesn't exist in this case
            }
        }
        final List<Cluster> clusters = new ArrayList<>();
        for (final Deme deme: level.getDemes()) {
            final List<Individual> inds = new LinkedList<>();
            for (final Deme.HistoryItem hi: deme.getHistory()) {
                inds.addAll(Arrays.asList(hi.getPopulation().getIndividuals()));
            }
            inds.addAll(Arrays.asList(deme.getPopulation().getIndividuals()));
            final Cluster cluster = new Cluster();
            final List<Individual> cluInds = cluster.getIndividuals();
            for (final Individual ind: inds) {
                final RealMatrix point = MatrixUtils.createColumnRealMatrix(ind.getPoint());
                for (final Ellipsoid el: ellipsoids) {
                    if (el.mahalanobisDistance(point) <= 1) {
                        cluInds.add(ind);
                    }
                }
            }
            if (cluInds.size() > 0) {
                clusters.add(cluster);
            }
        }
        return clusters;
    }

}
