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

package pl.a2s.ms.core.orch.hgs;

import lombok.Data;
import pl.a2s.ms.core.archive.Archive;
import pl.a2s.ms.core.clu.Cluster;
import pl.a2s.ms.core.clu.Consolidator;
import pl.a2s.ms.core.util.Pair;
import pl.a2s.ms.core.util.Range;
import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.ea.EvoAlgFactory;
import pl.a2s.ms.core.lsc.LocalStopCondition;
import pl.a2s.ms.core.orch.State;

import java.util.ArrayList;
import java.util.List;

@Data
public class LbaState implements State {
    private int epoch;
    private Consolidator consolidator;
    /// if <= 0 then equal to cluster size, not more than 100
    private int populationSize;
    private EvoAlgFactory evoAlgFactory;
    private LocalStopCondition stopCondition;
    private boolean runPostInverted;

    private Range[] domain;

    private Archive archive;
    private List<Cluster> clusters;
    private List<Cluster> reducedClusters;
    private List<Pair<Deme, EvoAlg>> demesAndAlgs = new ArrayList<>();
}
