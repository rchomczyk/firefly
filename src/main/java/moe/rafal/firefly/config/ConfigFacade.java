/*
 *    Copyright 2023 firefly
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package moe.rafal.firefly.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import java.nio.file.Path;

public interface ConfigFacade {

  <T extends OkaeriConfig> T produceConfig(Class<T> configClass, String configFileName,
      OkaeriSerdesPack... serdesPacks);

  <T extends OkaeriConfig> T produceConfig(Class<T> configClass, Path configPath,
      OkaeriSerdesPack... serdesPacks);
}
