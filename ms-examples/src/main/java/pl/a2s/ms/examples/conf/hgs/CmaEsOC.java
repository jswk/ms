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

import pl.a2s.ms.core.cmaes.CMAES;
import pl.a2s.ms.core.conf.hgs.HgsOrchestratorConfigurer;
import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.gsc.EvaluationCountGSC;
import pl.a2s.ms.core.gsc.NoDemesRunningGSC;
import pl.a2s.ms.core.gsc.OrGSC;
import pl.a2s.ms.core.lsc.LocalStopCondition;
import pl.a2s.ms.core.lsc.TrivialLSC;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.sprout.generator.NoSprouter;
import pl.a2s.ms.core.sprout.generator.Sprouter;
import pl.a2s.ms.core.sprout.reducer.NoSproutReducer;
import pl.a2s.ms.core.sprout.reducer.SproutReducer;

public class CmaEsOC extends HgsOrchestratorConfigurer {

    @Override
    protected void doConfigure(HgsOrchestrator orch) {
        final HgsState state = orch.state;

        state.setMetaepochLength(1);
        state.setPopulationSizes(new int[] { 10 });
        state.setGlobalStopCondition(OrGSC.of(new EvaluationCountGSC(3000, orch.individualEvaluator), new NoDemesRunningGSC()));

        final Level[] hgsDemes = new Level[1];
        final EvoAlg[] eas = new EvoAlg[] {
                new CMAES(1., 1e-12, orch.rand, orch.individualEvaluator, orch.fitnessExtractor)
        };
        final LocalStopCondition[] lscs = new LocalStopCondition[] {
                new TrivialLSC()
        };
        final Sprouter[] sprouters = new Sprouter[] {
                new NoSprouter()
        };
        final SproutReducer[] sproutReducers = new SproutReducer[] {
                new NoSproutReducer()
        };
        for (int i = 0; i < hgsDemes.length; i++) {
            hgsDemes[i] = new Level(i, eas[i], lscs[i], sprouters[i], sproutReducers[i], -1);
        }
        state.setHgsDemes(hgsDemes);
        state.setEpoch(0);
    }

}
