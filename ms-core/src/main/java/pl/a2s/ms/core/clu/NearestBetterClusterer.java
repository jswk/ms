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
import lombok.ToString;
import lombok.extern.java.Log;
import org.apache.commons.math3.util.MathArrays;
import pl.a2s.ms.core.ind.Individual;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;

/**
 * This is an implementation of NBC from a book by Preuss, 2015, Ch. 4.
 * <p>
 * It uses rule 2 variant.
 *
 * @author Jakub Sawicki
 */
@RequiredArgsConstructor
@Log
public class NearestBetterClusterer {

    /**
     * This is based on eq. (4.18) and Fig. 4.16, Preuss2015.
     *
     * @param dimensionCount
     * @param sampleSize
     * @return
     */
    public static NearestBetterClusterer createForParameters(int dimensionCount, int sampleSize) {
        double phi = 1.5;
        if (dimensionCount == 2) {
            if (sampleSize <= 100) {
                phi = 2.;
            } else {
                phi = 2.5;
            }
        } else if (dimensionCount == 3) {
            if (sampleSize > 100 && sampleSize <= 400) {
                phi = 2.;
            } else if (sampleSize > 400) {
                phi = 2.4;
            }
        } else if (dimensionCount == 5 && sampleSize > 400) {
            phi = 1.7;
        }
        final double b =
                (
                    - 4.69e-4 * Math.pow(dimensionCount, 2)
                    + 0.0263 * dimensionCount
                    + 3.66 / dimensionCount
                    - 0.457
                ) * Math.log10(sampleSize)
                + 7.51e-4 * Math.pow(dimensionCount, 2)
                - 0.0421 * dimensionCount
                - 2.26 / dimensionCount
                + 1.83;

        log.info(format("Dim count: %d, sample size: %d => NBC with phi: %f, b: %f", dimensionCount, sampleSize, phi, b));

        return new NearestBetterClusterer(phi, b);
    }

    @RequiredArgsConstructor
    @ToString(of = { "ind", "out", "outLen", "visited" })
    private static class Node {
        final Individual ind;
        final List<Node> in = new ArrayList<>();
        Node out;
        double outLen;
        boolean visited = false;
    }

    /// rule 1 parameter
    private final double phi;
    /// rule 2 parameter
    private final double b;

    public List<Cluster> clusterize(List<Individual> sample) {
        final List<Node> nodes = new ArrayList<>(sample.size());
        for (Individual individual : sample) {
            nodes.add(new Node(individual));
        }
        for (int i = 0; i < nodes.size(); i++) {
            final Node n1 = nodes.get(i);
            final double obj1 = n1.ind.getObjectives()[0];
            for (int j = 0; j < i; j++) {
                final Node n2 = nodes.get(j);
                final double obj2 = n2.ind.getObjectives()[0];
                final double dist = MathArrays.distance(n1.ind.getPoint(), n2.ind.getPoint());
                if (obj1 < obj2 && (n2.out == null || n2.outLen > dist)) {
                    addEdge(n2, n1, dist);
                } else if (obj2 < obj1 && (n1.out == null || n1.outLen > dist)) {
                    addEdge(n1, n2, dist);
                }
            }
        }

        final double avgLen;
        {
            double sum = 0.;
            int sumCount = 0;
            int trees = 0;
            for (final Node node: nodes) {
                if (node.out != null) {
                    sum += node.outLen;
                    sumCount++;
                } else {
                    trees++;
                }
            }
            avgLen = sum / sumCount;
            log.info(format("Nodes: %d, trees: %d", nodes.size(), trees));
        }

        // RULE 1:
        {
            int count = 0;
            for (final Node node: nodes) {
                if (node.outLen > phi * avgLen) {
                    count++;
                    removeOutEdge(node);
                }
            }
            log.info(format("Rule 1: %d edges removed", count));
        }
        // RULE 2:
        {
            final List<Node> nodesForOutEdgeRemoval = new LinkedList<>();
            for (final Node node: nodes) {
                if (node.in.size() >= 3 && node.out != null) {
                    final List<Double> lens = new ArrayList<>();
                    node.in.stream().map(n -> n.outLen).forEach(lens::add);
                    lens.sort(Double::compare);
                    double median;
                    final int size = lens.size();
                    if (size % 2 == 0) {
                        median = (lens.get(size / 2) + lens.get(size / 2 - 1)) / 2.;
                    } else {
                        median = lens.get(size / 2);
                    }
                    if (node.outLen/median > b) {
                        nodesForOutEdgeRemoval.add(node);
                    }
                }
            }
            for (final Node node: nodesForOutEdgeRemoval) {
                removeOutEdge(node);
            }
            log.info(format("Rule 2: %d edges removed", nodesForOutEdgeRemoval.size()));
        }

        final List<Cluster> clusters = new ArrayList<>();
        final Deque<Node> toVisit = new LinkedList<>();
        for (final Node nodeFor: nodes) {
            // skip visited trees
            if (nodeFor.visited) {
                continue;
            }

            final Cluster cluster = new Cluster();
            clusters.add(cluster);
            final List<Individual> inds = cluster.getIndividuals();

            // go to the root node for this tree
            Node currNode = nodeFor;
            while (currNode.out != null) {
                currNode = currNode.out;
            }

            // add the root
            toVisit.addLast(currNode);
            // and iterate in DFS order
            while (!toVisit.isEmpty()) {
                final Node node = toVisit.removeLast();
                inds.add(node.ind);
                node.visited = true;
                toVisit.addAll(node.in);
            }
        }

        log.info(format("Cluster count: %d", clusters.size()));

        return clusters;
    }

    private void addEdge(Node from, Node to, double len) {
        removeOutEdge(from);
        from.outLen = len;
        from.out = to;
        to.in.add(from);
    }

    private void removeOutEdge(Node node) {
        if (node.out == null) {
            return;
        }

        node.out.in.remove(node);
        node.out = null;
        node.outLen = 0.;
    }
}
