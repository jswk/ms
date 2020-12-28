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

package pl.a2s.ms.examples.conf.bench;

import lombok.val;
import pl.a2s.ms.examples.analysis.Ola19CaseIIIAnalyser;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.examples.obj.FlatRastrigin;
import pl.a2s.ms.core.obj.TransformedObjectiveCalculator;
import pl.a2s.ms.core.orch.BenchmarkEnabledOrchestrator;
import pl.a2s.ms.core.util.Range;

public class Ola19CaseIIIBenchmarkOC extends BenchmarkOrchestratorConfigurer {

    public static Ola19CaseIIIAnalyser analyser = new Ola19CaseIIIAnalyser();

    @Override
    protected void doConfigure(BenchmarkEnabledOrchestrator orch) {
        final int dim = 4;
        val domain = getDomain(dim);
        orch.setDomain(domain);

        val calc = new TransformedObjectiveCalculator(new FlatRastrigin(), new double[] { 10., 2., 2., 2. });
        final IndividualEvaluator ie = new IndividualEvaluator(calc);
        orch.setObjectiveCalculator(calc);
        orch.setIndividualEvaluator(ie);
        analyser.setBenchmark(calc);
        analyser.setDomain(domain);
        orch.setAnalyser(analyser);
    }

    protected Range[] getDomain(int dim) {
        return new Range[] {
                new Range(-5, 5),
                new Range(-2, 2),
                new Range(-2, 2),
                new Range(-2, 2),
        };
    }
}
