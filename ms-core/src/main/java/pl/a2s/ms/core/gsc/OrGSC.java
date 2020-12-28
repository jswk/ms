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

import pl.a2s.ms.core.orch.hgs.HgsState;

public class OrGSC implements GlobalStopCondition {

    private final GlobalStopCondition[] gscs;

    public OrGSC(GlobalStopCondition... gscs) {
        this.gscs = gscs;
    }

    @Override
    public boolean shouldStop(HgsState state) {
        for (final GlobalStopCondition gsc: gscs) {
            if (gsc.shouldStop(state)) {
                return true;
            }
        }
        return false;
    }

    public static OrGSC of(GlobalStopCondition... gscs) {
        return new OrGSC(gscs);
    }
}
