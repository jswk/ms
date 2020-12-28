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

package pl.a2s.ms.examples.conf.lba;

import pl.a2s.ms.core.clu.Cluster;
import pl.a2s.ms.core.clu.ClusterMergeChecker;
import pl.a2s.ms.core.clu.HillValleyChecker;
import pl.a2s.ms.core.clu.PairwiseNeighborConsolidator;
import pl.a2s.ms.core.conf.lba.LbaOrchestratorConfigurer;
import pl.a2s.ms.core.ea.MWEAFactory;
import pl.a2s.ms.core.lsc.EpochCountLSC;
import pl.a2s.ms.core.mw.GreedyCCMWPolicy;
import pl.a2s.ms.core.mw.MWPolicy;
import pl.a2s.ms.core.mw.UtilFunction;
import pl.a2s.ms.core.mw.UtilFunctions;
import pl.a2s.ms.core.orch.LbaEnabledOrchestrator;
import pl.a2s.ms.core.orch.hgs.LbaState;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class LbaOC extends LbaOrchestratorConfigurer {
    @Setter
    private UtilFunction utilFunction = UtilFunctions.polyPlus1(1, 1);

    @Setter
    private boolean runPostInverted = true;

    @Override
    protected void doConfigure(LbaEnabledOrchestrator orch) {
        final LbaState lbaState = new LbaState();
        orch.setLbaState(lbaState);
        lbaState.setDomain(orch.getDomain());
        lbaState.setPopulationSize(0);

        final ClusterMergeChecker checker = new HillValleyChecker(3, 0.1, orch.getIndividualEvaluator());
        lbaState.setConsolidator(new PairwiseNeighborConsolidator(10., checker, Cluster::merge));

        final MWPolicy mwPolicy = new GreedyCCMWPolicy(utilFunction, orch.getRand());
        lbaState.setEvoAlgFactory(new MWEAFactory(0.1, mwPolicy, orch.getIndividualEvaluator(), orch.getRand()));
        lbaState.setStopCondition(new EpochCountLSC(1));

        lbaState.setEpoch(0);
        lbaState.setRunPostInverted(runPostInverted);
    }

}
