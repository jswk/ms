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

import pl.a2s.ms.core.conf.hgs.HgsOrchestratorConfigurer;
import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.ea.NSGA2;
import pl.a2s.ms.core.gsc.EvaluationCountGSC;
import pl.a2s.ms.core.lsc.LocalStopCondition;
import pl.a2s.ms.core.lsc.TrivialLSC;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.sprout.generator.NoSprouter;
import pl.a2s.ms.core.sprout.generator.Sprouter;
import pl.a2s.ms.core.sprout.reducer.NoSproutReducer;
import pl.a2s.ms.core.sprout.reducer.SproutReducer;

public class MTOmegaNsga2OC extends HgsOrchestratorConfigurer {

    @Override
    protected void doConfigure(HgsOrchestrator orch) {
        final HgsState state = orch.state;
        final int dim = state.getDomain().length;

        state.setMetaepochLength(1);
        state.setPopulationSizes(new int[] { 60 });
        state.setGlobalStopCondition(new EvaluationCountGSC(3000, orch.individualEvaluator));

        final Level[] hgsDemes = new Level[1];
        final double dimMultiplier = Math.pow(dim, .5);
        final double[] stds = new double[] { 0.1 };
        final EvoAlg[] eas = new EvoAlg[] {
                new NSGA2(0.5, 0.5, stds[0] / dimMultiplier, orch.individualEvaluator, orch.rand, orch.fitnessExtractor)
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
            hgsDemes[i] = new Level(i, eas[i], lscs[i], sprouters[i], sproutReducers[i]);
        }
        state.setHgsDemes(hgsDemes);
        state.setEpoch(0);
    }

}
