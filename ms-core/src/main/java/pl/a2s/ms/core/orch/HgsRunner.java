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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import pl.a2s.ms.core.conf.OrchestratorConfigurer;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.ie.ParallelIndividualEvaluator;
import lombok.extern.java.Log;

@Log
public class HgsRunner implements Closeable {

    public static final int DEFAULT_NUMBER_OF_RUNS = 1;

    private final List<HgsOrchestrator> orchestrators;

    private boolean clean = false;

    public HgsRunner(OrchestratorConfigurer configurer) {
        this(DEFAULT_NUMBER_OF_RUNS, configurer);
    }

    public HgsRunner(int numberOfRuns, OrchestratorConfigurer configurer) {
        orchestrators = new ArrayList<>(numberOfRuns);
        for (int i = 0; i < numberOfRuns; i++) {
            orchestrators.add(new HgsOrchestrator(configurer));
        }
    }

    public void run() {
        for (int i = 0; i < orchestrators.size(); i++) {
            log.info(String.format("Run %d of %d", i, orchestrators.size()));
            orchestrators.get(i).run();
        }
    }

    public void cleanup() {
        if (clean) {
            return;
        }
        orchestrators.forEach(this::cleanupSingle);
        clean = true;
    }

    private void cleanupSingle(HgsOrchestrator orch) {
        final IndividualEvaluator ie = orch.getIndividualEvaluator();
        if (ie instanceof ParallelIndividualEvaluator) {
            final ParallelIndividualEvaluator pie = (ParallelIndividualEvaluator) ie;
            pie.getExecutorService().shutdown();
        }
    }

    @Override
    public void close() {
        cleanup();
    }

}
