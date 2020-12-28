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
import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.ind.Population;
import pl.a2s.ms.core.obj.AbstractZDT;
import pl.a2s.ms.core.orch.HgsOrchestrator;
import pl.a2s.ms.core.orch.Orchestrator;
import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import lombok.extern.java.Log;
import lombok.val;
import org.apache.commons.math3.util.MathArrays;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Log
public class ZdtHgsAnalyser implements Analyser {

    public static String gnuplotTemplate = "plot '{datafile}' u 1:2 index 0 title '{label1}', '{datafile}' u 1:2 index 1 title '{label2}' w l";

    private final AbstractZDT oc;
    private final String outputDir;

    public ZdtHgsAnalyser(AbstractZDT oc, String outputDir) {
        this.oc = oc;
        this.outputDir = outputDir;
    }

    @Override
    public boolean supports(Orchestrator orch) {
        return orch instanceof HgsOrchestrator;
    }

    @Override
    public void analyse(State state) {
        val hgsState = (HgsState) state;
        final List<Individual> solutions = new ArrayList<>();
        final int levelCount = hgsState.getHgsDemes().length;
        for (final Deme deme: hgsState.getHgsDemes()[levelCount-1].getDemes()) {
            // omit just sprouted demes
            if (deme.getHistory().isEmpty()) {
                continue;
            }
            for (final Individual ind: deme.getPopulation().getIndividuals()) {
                if (solutions.stream().noneMatch(ind2 -> Arrays.equals(ind.getPoint(), ind2.getPoint()))) {
                    solutions.add(ind);
                }
            }
        }

        final int dim = solutions.get(0).getPoint().length;
        final double[][] paretoOptimalFrontObjss = getParetoOptimalFront();
//        final double[][] solutionsObjss = new double[solutions.size()][2];
//        final double[][] solutionsParams = new double[solutions.size()][dim];
//        for (int i = 0; i < solutions.size(); i++) {
//            final Individual individual = solutions.get(i);
//            solutionsObjss[i] = Arrays.copyOf(individual.getObjectives(), 2);
//            solutionsParams[i] = Arrays.copyOf(individual.getObjectives(), dim);
//        }
        final List<Individual> frontIndividuals = extractFrontFromSolutions(solutions);
        final double[][] frontObjss;
        if (hgsState.getArchive().isEnabled()) {
            frontObjss = hgsState.getArchive().getIndividuals().stream().map(ind -> ind.getObjectives()).toArray(len -> new double[len][]);
        } else {
            frontObjss = frontIndividuals.stream().map(ind -> ind.getObjectives()).toArray(len -> new double[len][]);
        }

        final double gd = gd(frontObjss, paretoOptimalFrontObjss);
        final double igd = igd(frontObjss, paretoOptimalFrontObjss);
        final double ahd = ahd(frontObjss, paretoOptimalFrontObjss);

        log.info(String.format("gd: %f igd: %f ahd: %f", gd, igd, ahd));
//        log.info("\n"+objectivesToString(paretoFront));
//        logFrontAndSolutions(paretoFront, solutionsObj);
//        outputAndPlotFront(solutionsObjss, paretoOptimalFrontObjss);
//        log.info("\n"+
//                individualsToString(solutions, true, true)+
//                "\n\n"+
//                individualsToString(frontIndividuals, true, true));

//        final Deme deme = state.getHgsDemes()[levelCount-1].getDemes().get(0);
//        logHistory(deme);
    }

    private void outputAndPlotFront(double[][] solutionsObjss, double[][] paretoOptimalFrontObjss) {
        if (outputDir == null) {
            return;
        }

        final String dataFileName = "solutions-and-optimal-front.data";
        final Path dataFilePath = Paths.get(outputDir, dataFileName);
        final Path gnuplotFilePath = Paths.get(outputDir, "solutions-and-optimal-front.plt");
        final String output = objectivesToString(solutionsObjss)+"\n\n"+objectivesToString(paretoOptimalFrontObjss);
        try {
            Files.write(dataFilePath, output.getBytes());
            Files.write(gnuplotFilePath, gnuplotTemplate
                    .replaceAll("\\{datafile\\}", dataFileName)
                    .replaceAll("\\{label1\\}", "solution Pareto front")
                    .replaceAll("\\{label2\\}", "optimum Pareto front")
                    .getBytes());
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String objectivesToString(double[][] objss) {
        final StringBuilder sb = new StringBuilder();
        for (final double[] objs: objss) {
            String sep = "";
            for (final double obj: objs) {
                sb.append(sep);
                sb.append(String.format("%f", obj));
                sep = " ";
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private void logHistory(Deme deme) {
        final List<Population> history = new ArrayList<>();
        history.addAll(deme.getHistory().stream().map(histItem -> histItem.getPopulation()).collect(Collectors.toList()));
        history.add(deme.getPopulation());
        final StringBuilder sb = new StringBuilder("A deme history:\n");
        for (final Population population: history) {
            for (final Individual ind: population.getIndividuals()) {
                String sep = "";
                for (final double obj: ind.getObjectives()) {
                    sb.append(sep).append(obj);
                    sep = " ";
                }
                for (final double coord: ind.getPoint()) {
                    sb.append(sep).append(coord);
                }
                sb.append("\n");
            }
            sb.append("\n\n");
        }
        log.info(sb.toString());
    }

    private String individualsToString(List<Individual> individuals, boolean objectives, boolean points) {
        final StringBuilder sb = new StringBuilder();
        for (final Individual ind: individuals) {
            String sep = "";
            if (objectives) {
                for (final double obj: ind.getObjectives()) {
                    sb.append(sep);
                    sb.append(String.format("%f", obj));
                    sep = " ";
                }
            }
            if (points) {
                for (final double coord: ind.getPoint()) {
                    sb.append(sep);
                    sb.append(String.format("%f", coord));
                    sep = " ";
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private List<Individual> extractFrontFromSolutions(List<Individual> solutions) {
        final List<Individual> front = new LinkedList<>();
        for (final Individual ind: solutions) {
            front.removeIf(frontInd -> ind.dominates(frontInd));
            if (front.stream().noneMatch(frontInd -> frontInd.dominates(ind))) {
                front.add(ind);
            }
        }
        front.sort((i1, i2) -> Double.compare(i1.getObjectives()[0], i2.getObjectives()[0]));
        return front;
    }

    private double[][] getParetoOptimalFront() {
        final int count = 10001;
        final double[][] front = new double[count][2];
        final double start = 0.;
        final double end = 1.;
        for (int i = 0; i < count; i++) {
            final double f1 = start + (end - start) * i / count;
            front[i][0] = f1;
            front[i][1] = oc.front_f2(f1);
        }
        return front;
    }

    private static double gd(double[][] solutions, double[][] paretoFront) {
        double sum = 0.;
        for (int sol_i = 0; sol_i < solutions.length; sol_i++) {
            double minDist = Double.POSITIVE_INFINITY;
            for (int fr_i = 0; fr_i < paretoFront.length; fr_i++) {
                final double dist = MathArrays.distance(solutions[sol_i], paretoFront[fr_i]);
                minDist = Math.min(minDist, dist);
            }
            sum += Math.pow(minDist, 2);
        }
        return Math.sqrt(sum / solutions.length);
    }

    private static double igd(double[][] solutions, double[][] paretoFront) {
        return gd(paretoFront, solutions);
    }

    private static double ahd(double[][] solutions, double[][] paretoFront) {
        return Math.max(gd(solutions, paretoFront), igd(solutions, paretoFront));
    }

}
