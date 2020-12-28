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

package pl.a2s.ms.examples.bootstrap;

import pl.a2s.ms.core.conf.ChainedOC;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.NEA2Orchestrator;
import lombok.extern.java.Log;

import java.io.IOException;

import static pl.a2s.ms.examples.bootstrap.OCs.*;

@Log
public class BootOla19CaseI {

    public static void main(String[] args) throws SecurityException, IOException {
        LoggingConfig.configure();

        final int budget = 500;
        hmsCmaEs.setBudget(budget);
        nea2.setBudget(budget);
        log.warning("NEA2");
        new NEA2Orchestrator(ChainedOC.of(ola19CaseIBenchmark, fe, nea2, lba)).run();
        log.warning("HMS-CMA-ES");
        new HgsOrchestrator(ChainedOC.of(ola19CaseIBenchmark, fe, hmsCmaEs, lba, initialPopulation)).run();
    }
}
