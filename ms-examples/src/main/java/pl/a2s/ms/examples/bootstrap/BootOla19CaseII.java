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

import pl.a2s.ms.examples.analysis.Ola19CaseIIAnalyser;
import pl.a2s.ms.core.conf.ChainedOC;
import pl.a2s.ms.examples.conf.bench.gauss.Ola19CaseIIBenchmarkOC;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.NEA2Orchestrator;
import lombok.extern.java.Log;
import lombok.val;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static pl.a2s.ms.examples.bootstrap.OCs.fe;
import static pl.a2s.ms.examples.bootstrap.OCs.hmsCmaEs;
import static pl.a2s.ms.examples.bootstrap.OCs.initialPopulation;
import static pl.a2s.ms.examples.bootstrap.OCs.lba;
import static pl.a2s.ms.examples.bootstrap.OCs.nea2;
import static pl.a2s.ms.examples.bootstrap.OCs.ola19CaseIIBenchmark;
import static org.apache.commons.math3.util.FastMath.pow;
import static pl.a2s.ms.examples.conf.bench.gauss.Ola19CaseIIBenchmarkOC.ola19CaseIIAnalyser;

@Log
public class BootOla19CaseII {

    public static void main(String[] args) throws SecurityException, IOException {
        LoggingConfig.configure();

        final Ola19CaseIIBenchmarkOC benchmarkOC = ola19CaseIIBenchmark;
        final Ola19CaseIIAnalyser analyser = ola19CaseIIAnalyser;

        val repetitions = 10;
        for (final int budget: genBudgets(new int[] { 3 })) {
            hmsCmaEs.setBudget(budget);
            nea2.setBudget(budget);
            analyser.setCategory("NEA2 "+budget);
            for (int i = 0; i < repetitions; i++) {
                log.warning("NEA2 "+budget+" "+(i+1));
                new NEA2Orchestrator(ChainedOC.of(benchmarkOC, fe, nea2, lba)).run();
            }
            analyser.setCategory("HMS  "+budget);
            for (int i = 0; i < repetitions; i++) {
                log.warning("HMS-CMA-ES "+budget+" "+(i+1));
                new HgsOrchestrator(ChainedOC.of(benchmarkOC, fe, hmsCmaEs, lba, initialPopulation)).run();
            }
        }
        analyser.summarize();
    }

    public static List<Integer> genBudgets(int[] exps) {
        final int count = 5;
        val out = new ArrayList<Integer>();
        for (final int exp: exps) {
            for (int i = 0; i < count; i++) {
                out.add(((int)pow(10, exp)) * (2*i + 2));
            }
        }
        return out;
    }
}
