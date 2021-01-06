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

package pl.a2s.ms.core.orch;

import pl.a2s.ms.core.analysis.Analyser;
import pl.a2s.ms.core.util.Range;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.obj.ObjectiveCalculator;

public interface BenchmarkEnabledOrchestrator extends Orchestrator {

    void setObjectiveCalculator(ObjectiveCalculator oc);
    void setIndividualEvaluator(IndividualEvaluator ie);
    void setAnalyser(Analyser analyser);
    void setDomain(Range[] domain);
    ObjectiveCalculator getObjectiveCalculator();
    IndividualEvaluator getIndividualEvaluator();
    Analyser getAnalyser();
    Range[] getDomain();
}
