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

import pl.a2s.ms.core.conf.FitnessExtractorOC;
import pl.a2s.ms.core.conf.hgs.InitialPopulationOC;
import pl.a2s.ms.examples.conf.bench.*;
import pl.a2s.ms.examples.conf.bench.gauss.GaussMultimodal2DOC;
import pl.a2s.ms.examples.conf.bench.gauss.GaussXShapeMixed2DOC;
import pl.a2s.ms.examples.conf.bench.gauss.Ola19CaseIBenchmarkOC;
import pl.a2s.ms.examples.conf.bench.gauss.Ola19CaseIIBenchmarkOC;
import pl.a2s.ms.examples.conf.bench.zdt.*;
import pl.a2s.ms.examples.conf.hgs.*;
import pl.a2s.ms.examples.conf.lba.LbaOC;
import pl.a2s.ms.examples.conf.nea2.NEA2OC;

public class OCs {

    public final static ZDT1OC zdt1 = new ZDT1OC();
    public final static ZDT2OC zdt2 = new ZDT2OC();
    public final static ZDT3OC zdt3 = new ZDT3OC();
    public final static ZDT4OC zdt4 = new ZDT4OC();
    public final static ZDT6OC zdt6 = new ZDT6OC();
    public final static RosenbrockOC rosenbrock = new RosenbrockOC();
    public final static AckleyOC ackley = new AckleyOC();
    public final static GriewankOC griewank = new GriewankOC();
    public final static GaussMultimodal2DOC gaussMultimodal2D = new GaussMultimodal2DOC();
    public final static GaussXShapeMixed2DOC gaussXShapeMixed2D = new GaussXShapeMixed2DOC();
    public final static FlatRastriginOC flatRastrigin = new FlatRastriginOC();
    public final static FlatRastrigin4DOC flatRastrigin4D = new FlatRastrigin4DOC();
    public final static Ola19CaseIBenchmarkOC ola19CaseIBenchmark = new Ola19CaseIBenchmarkOC();
    public final static Ola19CaseIIBenchmarkOC ola19CaseIIBenchmark = new Ola19CaseIIBenchmarkOC();
    public final static Ola19CaseIIIBenchmarkOC ola19CaseIIIBenchmark = new Ola19CaseIIIBenchmarkOC();

    public final static FitnessExtractorOC fe = new FitnessExtractorOC();
    public final static InitialPopulationOC initialPopulation = new InitialPopulationOC();

    public final static HmsSeaOC hmsSea = new HmsSeaOC();
    public final static HmsSea2LevelOC hmsSea2Level = new HmsSea2LevelOC();
    public final static HmsNNNOC hmsNsga2 = new HmsNNNOC();

    public final static HmsSNNOC hmsSeaNsga2 = new HmsSNNOC();
    public final static SeaOC sea = new SeaOC();
    public final static Nsga2OC nsga2 = new Nsga2OC();
    public final static CmaEsOC cmaEs = new CmaEsOC();
    public final static HmsCmaEsOC hmsCmaEs = new HmsCmaEsOC();
    public final static MTOmegaHmsCmaEsOC mtoHmsCmaEs = new MTOmegaHmsCmaEsOC();
    public final static MTOmegaNsga2OC mtoNsga2 = new MTOmegaNsga2OC();

    public final static LbaOC lba = new LbaOC();

    public final static NEA2OC nea2 = new NEA2OC();

}
