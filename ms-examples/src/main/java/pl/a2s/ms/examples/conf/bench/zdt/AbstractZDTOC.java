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

package pl.a2s.ms.examples.conf.bench.zdt;

import lombok.Setter;
import pl.a2s.ms.examples.analysis.MetricsCollector;
import pl.a2s.ms.examples.analysis.ZdtHgsAnalyser;
import pl.a2s.ms.examples.conf.bench.BenchmarkOrchestratorConfigurer;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.obj.AbstractZDT;
import pl.a2s.ms.core.orch.BenchmarkEnabledOrchestrator;
import pl.a2s.ms.core.util.ArraysUtil;
import pl.a2s.ms.core.util.Range;

public abstract class AbstractZDTOC extends BenchmarkOrchestratorConfigurer {

    @Setter
    private MetricsCollector metricsCollector;

    @Override
    protected void doConfigure(BenchmarkEnabledOrchestrator orch) {
        final AbstractZDT zdtFc = getZdtCalculator();
        orch.setObjectiveCalculator(zdtFc);
        orch.setIndividualEvaluator(new IndividualEvaluator(zdtFc));
        final ZdtHgsAnalyser analyser = new ZdtHgsAnalyser(zdtFc, null /* TODO: this null should be fixed if used */);
        orch.setAnalyser(analyser);

        final int dim = 4;
        orch.setDomain(getDomain(dim));
    }

    protected abstract AbstractZDT getZdtCalculator();

    protected Range[] getDomain(int dim) {
        return ArraysUtil.constant(new Range(0., 1.), new Range[dim]);
    }
}
