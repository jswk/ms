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

package pl.a2s.ms.core.cmaes;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.ie.FitnessExtractor;
import pl.a2s.ms.core.ie.IndividualEvaluator;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.orch.hgs.Level;
import pl.a2s.ms.core.util.Range;

public class CMAES implements EvoAlg {

    private final double sigma;
    private final double stopFitness;
    private final Random rand;
    private final IndividualEvaluator ie;
    private final FitnessExtractor fe;
    private final Map<Deme, CMAESOptimizer> data = new HashMap<>();

    public CMAES(double sigma, double stopFitness, Random rand, IndividualEvaluator ie, FitnessExtractor fe) {
        this.sigma = sigma;
        this.stopFitness = stopFitness;
        this.rand = rand;
        this.ie = ie;
        this.fe = fe;
    }

    public CMAESOptimizer getOptimizer(Deme deme) {
        return data.get(deme);
    }

    @Override
    public Population apply(Level level, Deme deme, State state) {
        if (deme.getName() == null) {
            throw new IllegalArgumentException("Deme must have a name in order to be properly hashed");
        } else if (!(state instanceof HgsState)) {
            throw new IllegalArgumentException("State must be HgsState");
        }
        final CMAESOptimizer optimizer = data.computeIfAbsent(deme, d -> createOptimizer(level, deme, state));
        final Population result = optimizer.apply(level, deme, state);
        if (result == null) {
            // internal stopping condition triggered
            deme.setStopped(true);
        }
        return result;
    }

    private CMAESOptimizer createOptimizer(Level level, Deme deme, State state) {
        final CMAESOptimizer optimizer = new CMAESOptimizer(stopFitness, rand, ie, fe);
        optimizer.setStopIfSigmaIncrease(true);
        final Range[] domain = state.getDomain();
        final double[] uB = new double[domain.length];
        final double[] lB = new double[domain.length];
        final double[] sigma =  new double[domain.length];
        for (int i = 0; i < domain.length; i++) {
            lB[i] = domain[i].getStart();
            uB[i] = domain[i].getEnd();
            sigma[i] = this.sigma;
        }

        final HgsState hgsState = (HgsState) state;

        optimizer.initialize(
                new CMAESOptimizer.PopulationSize(hgsState.getPopulationSizes()[level.getLevel()]),
                new CMAESOptimizer.Sigma(sigma), // TODO: this should be passed through Deme somehow if this class is meant to be used in leaves after sprout
                new InitialGuess(deme.getPopulation().getIndividuals()[0].getPoint()),
                new SimpleBounds(lB, uB),
                GoalType.MINIMIZE
                );
        return optimizer;
    }

}
