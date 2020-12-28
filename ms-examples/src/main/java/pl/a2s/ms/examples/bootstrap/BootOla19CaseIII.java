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
import pl.a2s.ms.examples.conf.bench.Ola19CaseIIIBenchmarkOC;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.NEA2Orchestrator;
import lombok.extern.java.Log;
import lombok.val;

import java.io.IOException;

import static pl.a2s.ms.examples.bootstrap.OCs.*;

@Log
public class BootOla19CaseIII {

    public static void main(String[] args) throws SecurityException, IOException {
        LoggingConfig.configure();

        val analyser = Ola19CaseIIIBenchmarkOC.analyser;

        val budget = 50000;
        val repetitions = 10;
        nea2.setBudget(budget);
        analyser.setCategory("nea2   "+budget);
        for (int i = 0; i < repetitions; i++) {
            log.warning("nea2 "+budget+" Run: "+i);
            new NEA2Orchestrator(ChainedOC.of(ola19CaseIIIBenchmark, fe, nea2, lba)).run();
        }
        hmsCmaEs.setBudget(budget);
        analyser.setCategory("hmsCma "+budget);
        for (int i = 0; i < repetitions; i++) {
            log.warning("hmsCma "+budget+" Run: "+i);
            new HgsOrchestrator(ChainedOC.of(ola19CaseIIIBenchmark, fe, hmsCmaEs, initialPopulation, lba)).run();
        }
        analyser.summarize();
    }
}
