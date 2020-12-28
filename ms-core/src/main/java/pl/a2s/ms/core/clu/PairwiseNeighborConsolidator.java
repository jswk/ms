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

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.MathArrays;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PairwiseNeighborConsolidator implements Consolidator {

    private final double maxDistance;
    private final ClusterMergeChecker checker;
    private final ClusterMergeStrategy clusterMergeStrategy;

    @Override
    public List<Cluster> reduceClusters(
            List<Cluster> clusters) {
        final ArrayList<ClusterView> sc = new ArrayList<>();
        clusters.forEach(c -> sc.add(new ClusterView(c)));
        Pair<Integer, Integer> neighbors;
        Pair<Integer, Integer> lastVisited = null;
        while ((neighbors = findNeighbors(sc, lastVisited)) != null) {
            final ClusterView first = sc.get(neighbors.getLeft());
            final ClusterView second = sc.get(neighbors.getRight());
            if (checker.canMerge(first.getCluster(), second.getCluster())) {
                final int index1 = neighbors.getLeft();
                final int index2 = neighbors.getRight();
                if (index1 > index2) {
                    sc.remove(index1);
                    sc.remove(index2);
                } else {
                    sc.remove(index2);
                    sc.remove(index1);
                }
                sc.add(new ClusterView(clusterMergeStrategy.merge(first.getCluster(), second.getCluster())));

                lastVisited = null;
            } else {
                lastVisited = neighbors;
            }
        }
        return sc.stream().map(ClusterView::getCluster).filter(c -> c.getIndividuals().size() > 1).collect(Collectors.toList());
    }

    private Pair<Integer, Integer> findNeighbors(
            ArrayList<ClusterView> clusters,
            Pair<Integer, Integer> lastVisited) {
        if (lastVisited == null) {
            lastVisited = Pair.of(0, 0);
        }
        for (int i = lastVisited.getLeft(); i < clusters.size() - 1; i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                if (i == lastVisited.getLeft() && j <= lastVisited.getRight()) {
                    continue;
                }
                final ClusterView first = clusters.get(i);
                final ClusterView second = clusters.get(j);
                final double distance = MathArrays.distance(
                        first.getCentroid(), second.getCentroid());
                if (distance < maxDistance) {
                    return Pair.of(i, j);
                }
            }
        }
        return null;
    }

}
