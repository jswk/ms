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

import lombok.extern.java.Log;
import lombok.val;
import pl.a2s.ms.examples.analysis.MetricsCollector;
import pl.a2s.ms.core.conf.ChainedOC;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.NEA2Orchestrator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.math3.util.FastMath.pow;
import static pl.a2s.ms.examples.bootstrap.OCs.*;

@Log
public class BootNEA2 {

    public static void main(String[] args) throws SecurityException, IOException {
        LoggingConfig.configure();

        final MetricsCollector metricsCollector = new MetricsCollector();
        flatRastrigin.setMetricsCollector(metricsCollector);

        for (final int budget: genBudgets(new int[] { 3, 4, 5 })) {
            nea2.setBudget(budget);
            metricsCollector.setCategory("nea2   "+budget);
            for (int i = 0; i < 20; i++) {
                log.warning("nea2 "+budget+" Run: "+i);
                new NEA2Orchestrator(ChainedOC.of(flatRastrigin4D, fe, nea2)).run();
            }
            hmsCmaEs.setBudget(budget);
            metricsCollector.setCategory("hmsCma "+budget);
            for (int i = 0; i < 20; i++) {
                log.warning("hmsCma "+budget+" Run: "+i);
                new HgsOrchestrator(ChainedOC.of(flatRastrigin4D, fe, hmsCmaEs, initialPopulation)).run();
            }
        }
        metricsCollector.analyse();
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
