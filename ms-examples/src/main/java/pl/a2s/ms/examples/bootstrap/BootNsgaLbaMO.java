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
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.ie.ParallelIndividualEvaluator;
import pl.a2s.ms.core.orch.HgsOrchestrator;

import java.io.IOException;

import static pl.a2s.ms.examples.bootstrap.OCs.*;

//@Log
public class BootNsgaLbaMO {

    public static void main(String[] args) throws SecurityException, IOException {
        LoggingConfig.configure();

//        final Options options = new Options();
//        final Option outputOption = new Option("o", "output", true, "output dir, will be created");
//        options.addOption(outputOption);
//
//        final CommandLineParser parser = new DefaultParser();
//        final HelpFormatter formatter = new HelpFormatter();
//        CommandLine cmd;
//
//        try {
//            cmd = parser.parse(options, args);
//        } catch (final ParseException e) {
//            System.out.println(e.getMessage());
//            formatter.printHelp("mo-mw", options);
//
//            System.exit(1);
//            return;
//        }
//
//        final String outputDir = cmd.getOptionValue("output");

        final HgsOrchestrator orch = new HgsOrchestrator(ChainedOC.of(zdt1, fe, hmsNsga2, initialPopulation, lba));
        try {
            orch.run();
        } finally {
            final IndividualEvaluator ie = orch.getIndividualEvaluator();
            if (ie instanceof ParallelIndividualEvaluator) {
                ((ParallelIndividualEvaluator) ie).getExecutorService().shutdown();
            }
        }
    }
}
