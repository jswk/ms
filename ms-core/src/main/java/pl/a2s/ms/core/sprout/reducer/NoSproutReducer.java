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

package pl.a2s.ms.core.sprout.reducer;

import java.util.List;

import pl.a2s.ms.core.ind.Individual;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import pl.a2s.ms.core.util.Pair;

public class NoSproutReducer implements SproutReducer {

    @Override
    public List<Pair<Individual, Deme>> reduce(List<Pair<Individual, Deme>> sprouts, HgsState state, int levelIndex) {
        throw new UnsupportedOperationException("Cannot invoke NoSproutReducer");
    }

}
