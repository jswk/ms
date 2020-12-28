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

import pl.a2s.ms.core.analysis.Analyser;
import pl.a2s.ms.core.analysis.ConfigAwareAnalyser;
import pl.a2s.ms.core.conf.hgs.HgsOrchestratorConfigurer;
import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.ea.SEA;
import pl.a2s.ms.core.gsc.EvaluationCountGSC;
import pl.a2s.ms.core.gsc.OrGSC;
import pl.a2s.ms.core.lsc.LocalStopCondition;
import pl.a2s.ms.core.lsc.NoObjectiveChangeLSC;
import pl.a2s.ms.core.lsc.TrivialLSC;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.sprout.generator.BasicSprouter;
import pl.a2s.ms.core.sprout.generator.NoSprouter;
import pl.a2s.ms.core.sprout.generator.Sprouter;
import pl.a2s.ms.core.sprout.reducer.ChainedSproutReducer;
import pl.a2s.ms.core.sprout.reducer.DemeDistanceSproutReducer;
import pl.a2s.ms.core.sprout.reducer.NoSproutReducer;
import pl.a2s.ms.core.sprout.reducer.SeedDistanceSproutReducer;
import pl.a2s.ms.core.sprout.reducer.SproutReducer;
import pl.a2s.ms.core.util.ArraysUtil;

public class HmsSSOC extends HgsOrchestratorConfigurer {

    private static final String CONFIG_ID = "ss";

    @Override
    public void doConfigure(HgsOrchestrator orch) {
        final HgsState state = orch.state;
        final int dim = state.getDomain().length;

        state.setMetaepochLength(3);
        state.setPopulationSizes(new int[] { 64, 10 });
        state.setGlobalStopCondition(OrGSC.of(new EvaluationCountGSC(10000, orch.getIndividualEvaluator())));

        final Level[] hgsDemes = new Level[2];
        final double dimMultiplier = 1; // Math.pow(dim, .5);
        final double[] stds = new double[] { 5.0, 0.4 };
        final EvoAlg[] eas = new EvoAlg[] {
                new SEA(0.1, 0.5, stds[0] / dimMultiplier, orch.rand, orch.getFitnessExtractor()),
                new SEA(0.7, 0.5, stds[1] / dimMultiplier, orch.rand, orch.getFitnessExtractor())
        };
        final LocalStopCondition[] lscs = new LocalStopCondition[] {
                new TrivialLSC(),
                new NoObjectiveChangeLSC(5, ArraysUtil.constant(dim, 0.001))
        };
        final Sprouter[] sprouters = new Sprouter[] {
                new BasicSprouter(0.1, stds[1] / dimMultiplier, state.getPopulationSizes()[1], orch.rand),
                new NoSprouter()
        };
        final SproutReducer[] sproutReducers = new SproutReducer[] {
                new ChainedSproutReducer(
                        new DemeDistanceSproutReducer(2.0 * stds[1]),
                        new SeedDistanceSproutReducer(2.0 * stds[1])
                ),
                new NoSproutReducer()
        };
        for (int i = 0; i < hgsDemes.length; i++) {
            hgsDemes[i] = new Level(i, eas[i], lscs[i], sprouters[i], sproutReducers[i], -1);
        }
        state.setHgsDemes(hgsDemes);
        state.setEpoch(0);

        final Analyser analyser = orch.getAnalyser();
        if (analyser instanceof ConfigAwareAnalyser) {
            final ConfigAwareAnalyser caa = (ConfigAwareAnalyser) analyser;
            caa.setConfigId(CONFIG_ID);
        }
   }

}
