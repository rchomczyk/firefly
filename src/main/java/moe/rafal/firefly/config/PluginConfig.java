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

import static java.util.Arrays.asList;
import static moe.rafal.agnes.AgnesUtils.getBytesCountOf;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import java.time.Duration;
import java.util.List;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class PluginConfig extends OkaeriConfig {

  @Exclude
  public static final String PLUGIN_CONFIG_FILE_NAME = "config.yml";

  @Comment("Configuration for default containerized server powered by firefly.")
  public DefaultServerConfiguration serverConfiguration = new DefaultServerConfiguration();

  @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
  public static class DefaultServerConfiguration extends OkaeriConfig {

    @Comment("Determines hostname on which server will be bound.")
    public String hostname = "127.0.0.1";

    @Comment("Determines a template for bind between container and host environment.")
    public String bindTemplatePath = "C:\\Users\\rafal\\Documents\\firefly\\%s";

    @Comment("Determines a custom path to retrieve server's jar.")
    public String customServerPath = "http://host.docker.internal:24424/yVCD0IwAbH9o.jar";

    @Comment("Determines prefix for generic pack uri template.")
    public String genericPackPrefix = "http://host.docker.internal:24424/";

    @Comment("Determines suffix for generic pack uri template.")
    public String genericPackSuffix = ".zip";

    @Comment("Determines a list of ids for generic packs, which will be validated and applied.")
    public List<String> genericPacks = asList("Ywq1ufTVrde7", "772cqORmN3zd");

    @Comment("Determines amount of memory, which will be available for containerized server.")
    public long availableMemory = getBytesCountOf(1);

    @Comment("Determines amount of memory located in swap, which will be available for containerized server.")
    public long availableMemorySwap = getBytesCountOf(2);
  }

  @Comment("Configuration for message broker, used to configure cory messaging library.")
  public MessageBrokerConfiguration messageBroker = new MessageBrokerConfiguration();

  @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
  public static class MessageBrokerConfiguration extends OkaeriConfig {

    @Comment("Determines connection uri for communication with messaging server.")
    public String connectionUri = "nats://127.0.0.1:4222";

    @Comment("Determines username used to confirm identity for messaging server.")
    public String username = "firefly_identity";

    @Comment("Determines password used to confirm identity for messaging server.")
    public String password = "my-secret-password-123";

    @Comment("Determines the period after which request will be timed out, if will not receive replying packet.")
    public Duration requestCleanupInterval = Duration.ofSeconds(20);
  }
}
