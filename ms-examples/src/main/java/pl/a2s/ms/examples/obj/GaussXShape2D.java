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

package pl.a2s.ms.examples.obj;

import pl.a2s.ms.core.obj.AbstractGauss;

// X-shaped 3D Gauss (MC paper)
public class GaussXShape2D extends AbstractGauss {

    public GaussXShape2D() {
        super(.5, gauss2D(0, 0, 5, 0.5), gauss2D(0, 0, 0.5, 5));
    }

}
