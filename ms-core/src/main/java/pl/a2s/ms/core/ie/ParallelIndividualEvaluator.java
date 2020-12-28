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

package pl.a2s.ms.core.ie;

import lombok.Getter;
import lombok.extern.java.Log;
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.obj.AdaptiveCalculator;
import pl.a2s.ms.core.obj.AdaptiveParallelCalculator;
import pl.a2s.ms.core.obj.ObjectiveCalculator;
import pl.a2s.ms.core.obj.ParallelCalculator;
import pl.a2s.ms.core.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@Log
public class ParallelIndividualEvaluator extends IndividualEvaluator {

    private final String sourceSolverDir;
    private final String targetTmpDir;
    @Getter private final ExecutorService executorService;
    private final AtomicInteger count = new AtomicInteger(0);

    public ParallelIndividualEvaluator(AdaptiveParallelCalculator fc,
                                       String sourceSolverDir, String targetTmpDir, int threadCount) {
        super(fc);
        this.sourceSolverDir = sourceSolverDir;
        this.targetTmpDir = targetTmpDir;
        executorService = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    public void evaluate(Population population) {
        final Individual[] individuals = population.getIndividuals();
        final List<Future<?>> futures = new LinkedList<>();
        boolean changed = false;
        for (final Individual ind : individuals) {
            if (ind.getObjectives() == null) {
                evaluationCount++;
                final Future<?> future = executorService.submit(() -> ind.setObjectives(evaluateInNewDir(ind.getPoint())));
                futures.add(future);
                changed = true;
            }
        }
        futures.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.log(Level.SEVERE, "Couldn't compute", e);
            }
        });
        if (fc.getObjectiveCount() > 1 && changed) {
            population.updateRanks();
        }
    }

    @Override
    public double[] evaluate(double[] point) {
        return evaluateInNewDir(point);
    }

    public void setPrecision(double precision) {
        final ObjectiveCalculator fc = this.fc;
        if (!(fc instanceof AdaptiveCalculator)) {
            throw new IllegalStateException(
                    "It's possible to set precision only on AdaptiveCalculator");
        }
        ((AdaptiveCalculator) fc).setPrecision(precision);
    }

    private double[] evaluateInNewDir(double[] point) {
        final int id = count.getAndIncrement();
        final String solverDir = targetTmpDir + "/" + id;
        try {
            FileUtils.copy(new File(sourceSolverDir), new File(solverDir));
            final Properties props = new Properties();
//            props.put(SolverConfig.SOLVER_DIR_PROPERTY, solverDir);
            final ParallelCalculator fc = ((ParallelCalculator) this.fc).parallelInstance(props);
            return fc.calculate(point);
        } catch (final IOException e) {
            log.log(Level.SEVERE, "Cannot copy source dir to " + solverDir, e);
            return null;
        } finally {
            try {
                FileUtils.removeDirectory(new File(solverDir));
            } catch (final IOException e) {
                log.log(Level.WARNING, "Cannot clean dir " + solverDir, e);
            }
        }
    }

}
