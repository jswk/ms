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

package pl.a2s.ms.examples.conf.hgs;

import lombok.RequiredArgsConstructor;
import pl.a2s.ms.core.archive.RankingArchive;
import pl.a2s.ms.core.conf.hgs.HgsOrchestratorConfigurer;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.util.ArraysUtil;

@RequiredArgsConstructor
public class ArchiveOC extends HgsOrchestratorConfigurer {

    private final int maxRank;

    @Override
    protected void doConfigure(HgsOrchestrator orchestrator) {
        final HgsState state = orchestrator.state;
        state.setArchive(new RankingArchive(true, maxRank));
        ArraysUtil.last(state.getHgsDemes()).setArchived(true);
    }

}
