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
import pl.a2s.ms.core.ea.SEA;
import pl.a2s.ms.core.gsc.EvaluationCountGSC;
import pl.a2s.ms.core.gsc.NoDemesRunningGSC;
import pl.a2s.ms.core.gsc.OrGSC;
import pl.a2s.ms.core.lsc.LocalStopCondition;
import pl.a2s.ms.core.lsc.NoObjectiveChangeLSC;
import pl.a2s.ms.core.lsc.NoSproutLSC;
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

public class MTOmegaHmsCmaEsOC extends HgsOrchestratorConfigurer {

    public static final int NUMBER_OF_LEVELS = 2;

    @Override
    protected void doConfigure(HgsOrchestrator orch) {
        final HgsState state = orch.state;
        final int dim = state.getDomain().length;

        state.setMetaepochLength(2);
        state.setPopulationSizes(new int[] { 20, 5 });
        state.setGlobalStopCondition(OrGSC.of(new EvaluationCountGSC(2600, orch.individualEvaluator),
                new NoDemesRunningGSC()));

        final double dimMultiplier = 1; // Math.pow(dim, .5);

        final Level[] hgsDemes = new Level[NUMBER_OF_LEVELS];
        final EvoAlg[] eas = new EvoAlg[] {
                new SEA(0.1, 0.5, 2.0 / dimMultiplier, orch.rand, orch.fitnessExtractor),
                new CMAES(0.5, 0, orch.rand, orch.individualEvaluator, orch.fitnessExtractor)
        };
        final LocalStopCondition[] lscs = new LocalStopCondition[] {
                new NoSproutLSC(5),
                new NoObjectiveChangeLSC(5, ArraysUtil.constant(dim, 1e-7))
        };
        final Sprouter[] sprouters = new Sprouter[] {
                new BasicSprouter(1e-6, 0., 1, orch.rand),
                new NoSprouter()
        };
        final SproutReducer[] sproutReducers = new SproutReducer[] {
                new ChainedSproutReducer(
                        new DemeDistanceSproutReducer(1.0),
                        new SeedDistanceSproutReducer(1.0)),
                new NoSproutReducer()
        };
        for (int i = 0; i < hgsDemes.length; i++) {
            hgsDemes[i] = new Level(i, eas[i], lscs[i], sprouters[i], sproutReducers[i]);
        }
        state.setHgsDemes(hgsDemes);
        state.setEpoch(0);
    }

}
