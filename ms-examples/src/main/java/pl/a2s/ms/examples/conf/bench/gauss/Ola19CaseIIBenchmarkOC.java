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

package pl.a2s.ms.examples.conf.bench.gauss;

import lombok.val;
import pl.a2s.ms.examples.analysis.Ola19CaseIIAnalyser;
import pl.a2s.ms.examples.conf.bench.BenchmarkOrchestratorConfigurer;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.examples.obj.GaussCShape2DMultimodalLandscape1;
import pl.a2s.ms.core.orch.BenchmarkEnabledOrchestrator;
import pl.a2s.ms.core.util.ArraysUtil;
import pl.a2s.ms.core.util.Range;

public class Ola19CaseIIBenchmarkOC extends BenchmarkOrchestratorConfigurer {

    public static Ola19CaseIIAnalyser ola19CaseIIAnalyser = new Ola19CaseIIAnalyser();

    @Override
    protected void doConfigure(BenchmarkEnabledOrchestrator orch) {
        final int dim = 2;
        val domain = getDomain(dim);
        orch.setDomain(domain);

        val gauss = new GaussCShape2DMultimodalLandscape1(domain);
        ola19CaseIIAnalyser.setBenchmark(gauss);
        final IndividualEvaluator ie = new IndividualEvaluator(gauss);
        orch.setObjectiveCalculator(gauss);
        orch.setIndividualEvaluator(ie);
        orch.setAnalyser(ola19CaseIIAnalyser);
    }

    protected Range[] getDomain(int dim) {
        return ArraysUtil.constant(new Range(0., 20.), new Range[dim]);
    }
}
