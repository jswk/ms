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

import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.ea.SEA;
import pl.a2s.ms.core.orch.HgsOrchestrator;

public class Report1902HmsSSSOC extends Report1902AbstractOC {

    @Override
    protected EvoAlg[] getEAs(double[] stds, double dimMultiplier, HgsOrchestrator orch) {
        return new EvoAlg[] {
                new SEA(0.5, 0.5, stds[0] / dimMultiplier, orch.rand, orch.getFitnessExtractor()),
                new SEA(0.5, 0.3, stds[1] / dimMultiplier, orch.rand, orch.getFitnessExtractor()),
                new SEA(0.5, 0.1, stds[2] / dimMultiplier, orch.rand, orch.getFitnessExtractor())
        };
    }

}
