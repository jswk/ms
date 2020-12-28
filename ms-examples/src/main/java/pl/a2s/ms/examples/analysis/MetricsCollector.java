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

import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

@Log
public class MetricsCollector {

    @Data
    @Builder
    public static class AnalyserResult {
        private long evalsCountLevel1;
        private long evalsCountLevel2;
        private long evalsCountLevel3;
        private long demesCountLevel1;
        private long demesCountLevel2;
        private long demesCountLevel3;
        private double bestFitness0Level1;
        private double bestFitness0Level2;
        private double bestFitness0Level3;
        private double bestFitness1Level1;
        private double bestFitness1Level2;
        private double bestFitness1Level3;
        private double bestDistToSolutionLevel1;
        private double bestDistToSolutionLevel2;
        private double bestDistToSolutionLevel3;
    }

    @Data
    private final static class RunResults {
        private final String category;
        // DescriptiveStatistics
        // SummaryStatistics
        private final DescriptiveStatistics evalsRatioLevel1 = new DescriptiveStatistics();
        private final DescriptiveStatistics evalsRatioLevel2 = new DescriptiveStatistics();
        private final DescriptiveStatistics evalsRatioLevel3 = new DescriptiveStatistics();
        private final DescriptiveStatistics demesCountLevel1 = new DescriptiveStatistics();
        private final DescriptiveStatistics demesCountLevel2 = new DescriptiveStatistics();
        private final DescriptiveStatistics demesCountLevel3 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestFitness0Level1 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestFitness0Level2 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestFitness0Level3 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestFitness0 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestFitness1Level1 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestFitness1Level2 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestFitness1Level3 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestFitness1 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestDistToSolutionLevel1 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestDistToSolutionLevel2 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestDistToSolutionLevel3 = new DescriptiveStatistics();
        private final DescriptiveStatistics bestDistToSolution = new DescriptiveStatistics();
    }

    private final List<RunResults> data = new ArrayList<>();
    private RunResults currentResults;

    public void setCategory(String category) {
        currentResults = new RunResults(category);
        data.add(currentResults);
    }

    public void collect(AnalyserResult analyserResult) {
        // if any value is NaN it is not counted towards min, max, median, percentile calculation
        // but it doesn't cause an error
        final long totalEvals = analyserResult.getEvalsCountLevel1() + analyserResult.getEvalsCountLevel2() + analyserResult.getEvalsCountLevel3();
        currentResults.evalsRatioLevel1.addValue(1. * analyserResult.getEvalsCountLevel1() / totalEvals);
        currentResults.evalsRatioLevel2.addValue(1. * analyserResult.getEvalsCountLevel2() / totalEvals);
        currentResults.evalsRatioLevel3.addValue(1. * analyserResult.getEvalsCountLevel3() / totalEvals);
        currentResults.demesCountLevel1.addValue(analyserResult.getDemesCountLevel1());
        currentResults.demesCountLevel2.addValue(analyserResult.getDemesCountLevel2());
        currentResults.demesCountLevel3.addValue(analyserResult.getDemesCountLevel3());
        currentResults.bestFitness0Level1.addValue(analyserResult.getBestFitness0Level1());
        currentResults.bestFitness0Level2.addValue(analyserResult.getBestFitness0Level2());
        currentResults.bestFitness0Level3.addValue(analyserResult.getBestFitness0Level3());
        currentResults.bestFitness0.addValue(myMin(analyserResult.getBestFitness0Level1(), analyserResult.getBestFitness0Level2(), analyserResult.getBestFitness0Level3()));
        currentResults.bestFitness1Level1.addValue(analyserResult.getBestFitness1Level1());
        currentResults.bestFitness1Level2.addValue(analyserResult.getBestFitness1Level2());
        currentResults.bestFitness1Level3.addValue(analyserResult.getBestFitness1Level3());
        currentResults.bestFitness1.addValue(myMin(analyserResult.getBestFitness1Level1(), analyserResult.getBestFitness1Level2(), analyserResult.getBestFitness1Level3()));
        currentResults.bestDistToSolutionLevel1.addValue(analyserResult.getBestDistToSolutionLevel1());
        currentResults.bestDistToSolutionLevel2.addValue(analyserResult.getBestDistToSolutionLevel2());
        currentResults.bestDistToSolutionLevel3.addValue(analyserResult.getBestDistToSolutionLevel3());
        currentResults.bestDistToSolution.addValue(myMin(analyserResult.getBestDistToSolutionLevel1(), analyserResult.getBestDistToSolutionLevel2(), analyserResult.getBestDistToSolutionLevel3()));
    }

    private static double myMin(double v1, double v2, double v3) {
        double min = v1;
        if (v2 != Double.NaN && v2 < min) {
            min = v2;
        }
        if (v3 != Double.NaN && v3 < min) {
            min = v3;
        }
        return min;
    }

    private static final String SEP = " ";
    private static final String HEADS = String.join(SEP, new String[] {"25", "50", "75", "100"});

    public void analyse() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n").append(String.join(SEP, new String[] {
                "category",
                "n",
                "evalsRatioLevel1", HEADS,
                "evalsRatioLevel2", HEADS,
                "evalsRatioLevel3", HEADS,
                "demesCountLevel1", HEADS,
                "demesCountLevel2", HEADS,
                "demesCountLevel3", HEADS,
                "bestFitness0Level1", HEADS,
                "bestFitness0Level2", HEADS,
                "bestFitness0Level3", HEADS,
                "bestFitness0", HEADS,
                "bestFitness1Level1", HEADS,
                "bestFitness1Level2", HEADS,
                "bestFitness1Level3", HEADS,
                "bestFitness1", HEADS,
                "bestDistToSolutionLevel1", HEADS,
                "bestDistToSolutionLevel2", HEADS,
                "bestDistToSolutionLevel3", HEADS,
                "bestDistToSolution", HEADS,
        }));
        for (final RunResults results: data) {
            final long n = results.bestFitness0.getN();
            final String[] line = new String[] {
                    results.getCategory(),
                    Long.toString(n),
                    formatMedianOutput(results.evalsRatioLevel1),
                    formatMedianOutput(results.evalsRatioLevel2),
                    formatMedianOutput(results.evalsRatioLevel3),
                    formatMedianOutput(results.demesCountLevel1),
                    formatMedianOutput(results.demesCountLevel2),
                    formatMedianOutput(results.demesCountLevel3),
                    formatMedianOutput(results.bestFitness0Level1),
                    formatMedianOutput(results.bestFitness0Level2),
                    formatMedianOutput(results.bestFitness0Level3),
                    formatMedianOutput(results.bestFitness0),
                    formatMedianOutput(results.bestFitness1Level1),
                    formatMedianOutput(results.bestFitness1Level2),
                    formatMedianOutput(results.bestFitness1Level3),
                    formatMedianOutput(results.bestFitness1),
                    formatMedianOutput(results.bestDistToSolutionLevel1),
                    formatMedianOutput(results.bestDistToSolutionLevel2),
                    formatMedianOutput(results.bestDistToSolutionLevel3),
                    formatMedianOutput(results.bestDistToSolution),
            };
            sb.append("\n").append(String.join(" ", line));
        }
        log.warning(sb.toString());
    }

    private static String formatMedianOutput(DescriptiveStatistics ds) {
        final String[] parts = new String[] {
                String.format("%f", ds.getMin()),
                String.format("%f", ds.getPercentile(25)),
                String.format("%f", ds.getPercentile(50)),
                String.format("%f", ds.getPercentile(75)),
                String.format("%f", ds.getMax()),
        };
        return String.join(SEP, parts);
    }

}
