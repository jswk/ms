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

package pl.a2s.ms.core.lsc;

import java.util.List;
import java.util.ListIterator;

import pl.a2s.ms.core.orch.State;
import pl.a2s.ms.core.orch.hgs.Deme;
import pl.a2s.ms.core.orch.hgs.HgsState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NoSproutLSC implements LocalStopCondition {

    private final int metaepochs;

    @Override
    public boolean shouldStop(Deme deme, State state) {
        if (!(state instanceof HgsState)) {
            throw new IllegalArgumentException("State must be HgsState");
        }
        final HgsState hgsState = (HgsState) state;

        final List<Deme.HistoryItem> history = deme.getHistory();
        if (history.size() < metaepochs * hgsState.getMetaepochLength()) {
            return false;
        }
        final ListIterator<Deme.HistoryItem> it = history.listIterator(history.size());
        for (int i = 0; i < metaepochs * hgsState.getMetaepochLength(); i++) {
            // if the deme sprouted in the last `epochs` epochs, let it live
            if (!it.previous().getChildren().isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
