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

import lombok.Setter;
import lombok.val;
import pl.a2s.ms.examples.analysis.MetricsCollector;
import pl.a2s.ms.examples.analysis.MinimaInfoAnalyser;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.examples.obj.FlatRastrigin;
import pl.a2s.ms.core.obj.TransformedObjectiveCalculator;
import pl.a2s.ms.core.orch.BenchmarkEnabledOrchestrator;
import pl.a2s.ms.core.util.ArraysUtil;
import pl.a2s.ms.core.util.Range;

public class FlatRastrigin4DOC extends BenchmarkOrchestratorConfigurer {

    @Setter
    private MetricsCollector metricsCollector;

    @Override
    protected void doConfigure(BenchmarkEnabledOrchestrator orch) {
        val calc = new TransformedObjectiveCalculator(new FlatRastrigin(), new double[] { 10., 2., 2., 2. });
        final IndividualEvaluator ie = new IndividualEvaluator(calc);
        orch.setObjectiveCalculator(calc);
        orch.setIndividualEvaluator(ie);
        val analyser = new MinimaInfoAnalyser(calc, ie);
//        analyser.setMetricsCollector(metricsCollector);
        orch.setAnalyser(analyser);

        final int dim = 4;
        orch.setDomain(getDomain(dim));
    }

    protected Range[] getDomain(int dim) {
        return ArraysUtil.constant(new Range(-10., 10.), new Range[dim]);
    }
}
