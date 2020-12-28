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
import pl.a2s.ms.core.ea.SEA;
import pl.a2s.ms.core.gsc.EvaluationCountGSC;
import pl.a2s.ms.core.gsc.NoDemesRunningGSC;
import pl.a2s.ms.core.gsc.OrGSC;
import pl.a2s.ms.core.lsc.LocalStopCondition;
import pl.a2s.ms.core.lsc.NoObjectiveChangeLSC;
import pl.a2s.ms.core.lsc.NoSproutLSC;
import pl.a2s.ms.core.lsc.TrivialLSC;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.sprout.generator.BasicSprouter;
import pl.a2s.ms.core.sprout.generator.NoSprouter;
import pl.a2s.ms.core.sprout.generator.Sprouter;
import pl.a2s.ms.core.sprout.reducer.ChainedSproutReducer;
import pl.a2s.ms.core.sprout.reducer.DemeDistanceSproutReducer;
import pl.a2s.ms.core.sprout.reducer.NoSproutReducer;
import pl.a2s.ms.core.sprout.reducer.SeedDistanceSproutReducer;
import pl.a2s.ms.core.sprout.reducer.SproutReducer;
import pl.a2s.ms.core.util.ArraysUtil;

public class HmsSeaOC extends HgsOrchestratorConfigurer {

    @Override
    protected void doConfigure(HgsOrchestrator orch) {
        final HgsState state = orch.state;
        final int dim = state.getDomain().length;

        state.setMetaepochLength(5);
        state.setPopulationSizes(new int[] { 10, 10, 10 });
        state.setGlobalStopCondition(OrGSC.of(new EvaluationCountGSC(10000, orch.individualEvaluator), new NoDemesRunningGSC()));

        final Level[] hgsDemes = new Level[3];
        final double dimMultiplier = Math.pow(dim, .5);
        final double[] stds = new double[] { 0.3, 0.1, 0.01 };
        final EvoAlg[] eas = new EvoAlg[] {
                new SEA(0.5, 0.5, stds[0] / dimMultiplier, orch.rand, orch.fitnessExtractor),
                new SEA(0.8, 0.3, stds[1] / dimMultiplier, orch.rand, orch.fitnessExtractor),
                new SEA(0.9, 0.1, stds[2] / dimMultiplier, orch.rand, orch.fitnessExtractor)
        };
        final LocalStopCondition[] lscs = new LocalStopCondition[] {
                new TrivialLSC(),
                new NoSproutLSC(5),
                new NoObjectiveChangeLSC(5, ArraysUtil.constant(dim, 0.001))
        };
        final Sprouter[] sprouters = new Sprouter[] {
                new BasicSprouter(0.1, stds[1] / dimMultiplier, state.getPopulationSizes()[1], orch.rand),
                new BasicSprouter(0.01, stds[2] / dimMultiplier, state.getPopulationSizes()[2], orch.rand),
                new NoSprouter()
        };
        final SproutReducer[] sproutReducers = new SproutReducer[] {
                new ChainedSproutReducer(
//                        new DominatingOnlySproutReducer(orch.ie),
                        new DemeDistanceSproutReducer(2 * stds[1]),
                        new SeedDistanceSproutReducer(2 * stds[1])),
                new ChainedSproutReducer(
//                        new DominatingOnlySproutReducer(orch.ie),
                        new DemeDistanceSproutReducer(2 * stds[2]),
                        new SeedDistanceSproutReducer(2 * stds[2])),
                new NoSproutReducer()
        };
        for (int i = 0; i < hgsDemes.length; i++) {
            hgsDemes[i] = new Level(i, eas[i], lscs[i], sprouters[i], sproutReducers[i], -1);
        }
        state.setHgsDemes(hgsDemes);
        state.setEpoch(0);
    }

}
