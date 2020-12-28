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
import pl.a2s.ms.core.conf.OrchestratorConfigurer;
import pl.a2s.ms.examples.conf.bench.gauss.MOFlatGaussMultimodal2D1OC;
import pl.a2s.ms.examples.conf.hgs.ArchiveOC;
import pl.a2s.ms.examples.conf.hgs.HmsSSNOC;
import pl.a2s.ms.core.mw.UtilFunction;
import pl.a2s.ms.core.mw.UtilFunctions;
import pl.a2s.ms.core.orch.HgsRunner;

import static pl.a2s.ms.examples.bootstrap.OCs.*;

import java.io.IOException;

public class BootGaussSSNLbaMO {

    public static final int NUMBER_OF_RUNS = 10;

    public static final int ARCHIVE_MAX_RANK = 2;

    public static final UtilFunction UTIL_FUNCTION = UtilFunctions.polyPlus1(1, 1);

    public static final OrchestratorConfigurer moFlatGauss =
            new MOFlatGaussMultimodal2D1OC();
    public static final OrchestratorConfigurer hms = new HmsSSNOC();
    public static final OrchestratorConfigurer archive =
            new ArchiveOC(ARCHIVE_MAX_RANK);

    public static void main(String[] args) throws SecurityException, IOException {
        LoggingConfig.configure();
        lba.setUtilFunction(UTIL_FUNCTION);
        lba.setRunPostInverted(true);
        final OrchestratorConfigurer oc = ChainedOC.of(moFlatGauss, fe, hms,
                initialPopulation, lba, archive);
        try (HgsRunner runner = new HgsRunner(NUMBER_OF_RUNS, oc)) {
            runner.run();
        }
    }
}
