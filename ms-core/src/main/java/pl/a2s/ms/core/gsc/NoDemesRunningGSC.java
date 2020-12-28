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

package pl.a2s.ms.core.gsc;

import lombok.RequiredArgsConstructor;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class NoDemesRunningGSC implements GlobalStopCondition {

    @Override
    public boolean shouldStop(HgsState state) {
        return Stream.of(state.getHgsDemes())
            .flatMap(level -> level.getDemes().stream())
            .allMatch(Deme::isStopped);
    }

}
