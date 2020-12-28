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

import pl.a2s.ms.core.analysis.Analyser;
import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.Orchestrator;
import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.LbaState;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.util.Pair;
import lombok.extern.java.Log;
import lombok.val;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pl.a2s.ms.core.util.ArraysUtil.last;

@Log
public class MTOmegaAnalyser implements Analyser {

    public static final String OUTPUT_FILE = "pareto-front.dat";

    @Override
    public boolean supports(Orchestrator orch) {
        return orch instanceof HgsOrchestrator;
    }

    @Override
    public void analyse(State state) {
        val hgsState = (HgsState) state;
        log.info("GLOBAL PHASE FINAL STATE: LEAF DEMES");
        final Level[] levels = hgsState.getHgsDemes();
        logDemes(last(levels).getDemes());
        final List<Deme> finalDemes = new ArrayList<Deme>();
        final LbaState lbaState = hgsState.getLbaState();
        if (lbaState != null) {
            log.info("LBA PHASE FINAL STATE");
            logDemesAndAlgs(lbaState.getDemesAndAlgs());
            finalDemes.addAll(lbaState.getDemesAndAlgs().stream().map(Pair::getFirst).collect(Collectors.toList()));
        } else {
            finalDemes.addAll(last(levels).getDemes());
        }
        final List<Individual> finalIndividuals = new ArrayList<>();
        for (final Deme d : finalDemes) {
            finalIndividuals.addAll(Arrays.asList(d.getPopulation().getIndividuals()));
        }
        final Population finalPopulation = new Population(finalIndividuals);
        finalPopulation.updateRanks();
        try (PrintWriter out = new PrintWriter(OUTPUT_FILE)) {
//            out.println(finalPopulation.dump());
        } catch (final FileNotFoundException e) {
            log.warning(e.toString());
        }
    }

    private void logDemesAndAlgs(final List<Pair<Deme, EvoAlg>> demesAndAlgs) {
        logDemes(demesAndAlgs.stream().map(Pair::getFirst).collect(Collectors.toList()));
    }

    private void logDemes(final List<Deme> demes) {
        for (final Deme deme : demes) {
            log.info(deme.getName());
            for (final Individual ind : deme.getPopulation().getIndividuals()) {
                log.info(ind.toString());
            }
        }

    }

}
