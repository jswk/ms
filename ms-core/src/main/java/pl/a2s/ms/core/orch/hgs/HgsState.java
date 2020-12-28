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

package pl.a2s.ms.core.orch.hgs;

import pl.a2s.ms.core.archive.Archive;
import pl.a2s.ms.core.archive.SimpleArchive;
import pl.a2s.ms.core.util.Range;
import pl.a2s.ms.core.gsc.GlobalStopCondition;
import pl.a2s.ms.core.orch.State;
import lombok.Data;

@Data
public class HgsState implements State {
    private Range[] domain;
    private int[] populationSizes;
    private GlobalStopCondition globalStopCondition;
    private int metaepochLength;
    // increments only exactly before next epoch, i.e. after all stopping conditions, sprouts and so on
    private int epoch;
    private Level[] hgsDemes;
    private Archive archive = new SimpleArchive(false);

    private LbaState lbaState;
}
