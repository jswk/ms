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

import lombok.val;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.*;

public class LoggingConfig {
    public LoggingConfig() {
        try {
            System.setProperty("java.util.logging.SimpleFormatter.format",
                    "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %2$s %5$s%6$s%n");

            val consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.FINEST);
            consoleHandler.setFormatter(new SimpleFormatter());

            val app = Logger.getLogger("");
            app.setLevel(Level.INFO);
            app.addHandler(consoleHandler);

            val cmaes = Logger.getLogger("pl.a2s.ms.core.cmaes.CMAESOptimizer");
            cmaes.setLevel(Level.WARNING);

            val mw = Logger.getLogger("pl.a2s.ms.core.mw");
            mw.setLevel(Level.INFO);

            val mwPolicy = Logger.getLogger("pl.a2s.ms.core.mw.GreedyCCMWPolicy");
            mwPolicy.setLevel(Level.WARNING);

            val analysis = Logger.getLogger("pl.a2s.ms.examples.analysis");
            analysis.setLevel(Level.INFO);
        } catch (final Exception e) {
            // The runtime won't show stack traces if the exception is thrown
            e.printStackTrace();
        }
    }

    /**
     * Sets the logging config to this class. Should be called in Boot classes' main methods.
     *
     * @throws SecurityException
     * @throws IOException
     */
    public static void configure() throws SecurityException, IOException {
        final Properties props = System.getProperties();
        if (!props.containsKey("java.util.logging.config.class")) {
            props.putIfAbsent("java.util.logging.config.class", "pl.a2s.ms.examples.bootstrap.LoggingConfig");
            LogManager.getLogManager().reset();
            LogManager.getLogManager().readConfiguration();
        }
    }
}
