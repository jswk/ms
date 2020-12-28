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

import java.util.ArrayList;
import java.util.List;

import pl.a2s.ms.core.sprout.generator.Sprouter;
import pl.a2s.ms.core.sprout.reducer.SproutReducer;
import pl.a2s.ms.core.ea.EvoAlg;
import pl.a2s.ms.core.lsc.LocalStopCondition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class Level {

    private final int level;

    private final EvoAlg evoAlg;
    private final LocalStopCondition stopCondition;
    private final Sprouter sprouter;
    private final SproutReducer sproutReducer;
    @Setter private double precision = -1;

    @Setter private boolean archived = false;

    private final List<Deme> demes = new ArrayList<>();

    public Level(int level, EvoAlg alg, LocalStopCondition lsc, Sprouter sprouter,
            SproutReducer reducer, double precision) {
        this(level, alg, lsc, sprouter, reducer);
        this.precision = precision;
    }

}
