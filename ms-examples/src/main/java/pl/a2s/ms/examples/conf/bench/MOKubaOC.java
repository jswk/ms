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

import pl.a2s.ms.examples.analysis.MOSimpleAnalyser;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.examples.obj.MOKubaBenchmark;
import pl.a2s.ms.core.orch.BenchmarkEnabledOrchestrator;
import pl.a2s.ms.core.util.ArraysUtil;
import pl.a2s.ms.core.util.Range;

public class MOKubaOC extends BenchmarkOrchestratorConfigurer {

    @Override
    protected void doConfigure(BenchmarkEnabledOrchestrator orch) {
        final MOKubaBenchmark moKuba = new MOKubaBenchmark();
        final IndividualEvaluator ie = new IndividualEvaluator(moKuba);
        orch.setObjectiveCalculator(moKuba);
        orch.setIndividualEvaluator(ie);
        orch.setAnalyser(new MOSimpleAnalyser("kuba"));

        final int dim = 2;
        orch.setDomain(getDomain(dim));

    }

    protected Range[] getDomain(int dim) {
        return ArraysUtil.constant(new Range(0., 1.), new Range[dim]);
    }
}
