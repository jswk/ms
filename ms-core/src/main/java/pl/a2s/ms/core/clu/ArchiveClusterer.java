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
import pl.a2s.ms.core.archive.Archive;
import pl.a2s.ms.core.archive.RankingArchive;
import pl.a2s.ms.core.ind.Population;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ArchiveClusterer {

    private final Archive archive;

    public List<Cluster> createClusters() {
        Map<String, Population> grouped = archive.splitAlongDemes();
        List<Cluster> clusters = new ArrayList<>(grouped.values().size());
        for (String key: grouped.keySet()) {
            Cluster c = new Cluster();
            c.getIndividuals().addAll(grouped.get(key));
            clusters.add(c);
        }
        return clusters;
    }

    public static void main(String[] args) {
        RankingArchive ra = RankingArchive.setupSampleArchive(3);
        List<Cluster> clusters = new ArchiveClusterer(ra).createClusters();
        for (int i = 0; i < clusters.size(); i++) {
            System.out.printf("Cluster #%d:%n", i);
            System.out.println(
                    clusters.get(i).getIndividuals().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"))
                    );
        }
    }

}
