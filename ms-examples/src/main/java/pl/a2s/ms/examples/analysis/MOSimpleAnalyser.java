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

package pl.a2s.ms.examples.analysis;

import pl.a2s.ms.core.analysis.ConfigAwareAnalyser;
import pl.a2s.ms.core.archive.RankingArchive;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.Orchestrator;
import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.LbaState;
import lombok.extern.java.Log;
import lombok.val;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

@Log
public class MOSimpleAnalyser implements ConfigAwareAnalyser {

    public static final String OUT_DIR_NAME_TRAILER_DEFAULT = "output";

    public static final String OUT_DIR_PATTERN = "%1$s-%2$tFT%2$tH%2$tM%2$tS";

    public static final String ALL_FILE = "all.dat";

    private final String experimentName;
    private String outDirNameTrailer;

    public MOSimpleAnalyser() {
        this(OUT_DIR_NAME_TRAILER_DEFAULT);
    }

    public MOSimpleAnalyser(String experimentName) {
        if (experimentName == null) {
            experimentName = OUT_DIR_NAME_TRAILER_DEFAULT;
        }
        this.experimentName = experimentName;
        this.outDirNameTrailer = experimentName;
    }

    @Override
    public boolean supports(Orchestrator orch) {
        return orch instanceof HgsOrchestrator;
    }

    public String getOutDirName() {
        return String.format(OUT_DIR_PATTERN, outDirNameTrailer, new Date());
    }

    @Override
    public void setConfigId(String id) {
        if (id != null) {
            outDirNameTrailer = experimentName + "-" + id;
        } else {
            outDirNameTrailer = experimentName;
        }
    }

    @Override
    public void analyse(State state) {
        val hgsState = (HgsState) state;
        final String outDir = getOutDirName();
        final LbaState lbaState = hgsState.getLbaState();
        try {
            Files.createDirectory(Paths.get(outDir));
            if (lbaState != null) {
                dump(hgsState, outDir);
            } else {
                dumpNoLba(hgsState, outDir);
            }
        } catch (final Exception e) {
            log.warning(e.toString());
            e.printStackTrace();
        }
    }

    private void dump(HgsState state, String outDir) throws FileNotFoundException {
        if (!(state.getArchive() instanceof RankingArchive)) {
            throw new IllegalStateException("Archive is not a RankingArchive");
        }
        final RankingArchive archive = (RankingArchive) state.getArchive();
//        log.info(archive.dump());
        final Map<String, Population> split = archive.splitAlongDemes();
        for (final String name : split.keySet()) {
            final File lbaFile = new File(outDir, name + ".dat");
            try (PrintWriter lbaOut = new PrintWriter(lbaFile)) {
//                lbaOut.println(split.get(name).dump());
            }
        }
        final File allFile = new File(outDir, ALL_FILE);
        try (PrintWriter allOut = new PrintWriter(allFile)) {
//            allOut.println(archive.getPopulation().dump());
        }
    }

    private void dumpNoLba(HgsState state, String outDir) throws FileNotFoundException {
        log.warning("No LBA state");
        final File allFile = new File(outDir, ALL_FILE);
        try (PrintWriter out = new PrintWriter(allFile)) {
            state.getArchive().getIndividuals().forEach(i -> out.println(i));
        }
    }

}
