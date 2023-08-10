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

package moe.rafal.firefly;

import static moe.rafal.cory.message.RedisMessageBrokerFactory.produceRedisMessageBroker;
import static moe.rafal.firefly.FireflyConstants.PLUGIN_ARTIFACT_ID;
import static moe.rafal.firefly.FireflyConstants.PLUGIN_AUTHORS;
import static moe.rafal.firefly.FireflyConstants.PLUGIN_VERSION;
import static moe.rafal.firefly.FireflyConstants.WHETHER_COMMANDS_SHOULD_USE_NATIVE_PERMISSION;
import static moe.rafal.firefly.FireflyUtils.createRedisUriWithConfiguration;
import static moe.rafal.firefly.config.ConfigFacadeFactory.produceConfigFacade;
import static moe.rafal.firefly.config.PluginConfig.PLUGIN_CONFIG_FILE_NAME;
import static moe.rafal.firefly.server.container.ContainerizedServerControllerFactory.produceContainerizedServerController;
import static moe.rafal.firefly.server.container.ContainerizedServerRegistryFactory.produceContainerizedServerRegistry;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.rollczi.litecommands.velocity.LiteVelocityFactory;
import dev.rollczi.litecommands.velocity.tools.VelocityOnlyPlayerContextual;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import java.io.IOException;
import java.nio.file.Path;
import moe.rafal.agnes.Agnes;
import moe.rafal.agnes.AgnesBuilder;
import moe.rafal.cory.Cory;
import moe.rafal.cory.CoryBuilder;
import moe.rafal.firefly.config.ConfigFacade;
import moe.rafal.firefly.config.PluginConfig;
import moe.rafal.firefly.server.container.ContainerizedServerController;
import moe.rafal.firefly.server.container.ContainerizedServerRegistry;

@Plugin(id = PLUGIN_ARTIFACT_ID, version = PLUGIN_VERSION, authors = PLUGIN_AUTHORS)
public class FireflyPlugin {

  private final ProxyServer proxyServer;
  private final PluginConfig pluginConfig;
  private Cory cory;

  @Inject
  public FireflyPlugin(ProxyServer proxyServer, @DataDirectory Path dataPath) {
    this.proxyServer = proxyServer;
    final ConfigFacade configFacade = produceConfigFacade(dataPath, YamlSnakeYamlConfigurer::new);
    this.pluginConfig = configFacade.produceConfig(PluginConfig.class, PLUGIN_CONFIG_FILE_NAME,
        new SerdesCommons());
  }

  @Subscribe
  public void onProxyInitialize(ProxyInitializeEvent event) {
    cory = CoryBuilder.newBuilder()
        .withMessageBroker(produceRedisMessageBroker(createRedisUriWithConfiguration(
            pluginConfig.messageBroker),
            pluginConfig.messageBroker.requestCleanupInterval))
        .build();
    final Agnes agnes = AgnesBuilder.newBuilder()
        .withCory(cory)
        .build();

    final ContainerizedServerRegistry serverRegistry =
        produceContainerizedServerRegistry(proxyServer);
    final ContainerizedServerController serverController =
        produceContainerizedServerController(pluginConfig, agnes, serverRegistry);

    LiteVelocityFactory.builder(proxyServer, WHETHER_COMMANDS_SHOULD_USE_NATIVE_PERMISSION)
        .contextualBind(Player.class, new VelocityOnlyPlayerContextual<>(
            miniMessage().deserialize("<red>You cannot use that command from Console.")))
        .commandInstance(new FireflyCommand(serverController))
        .register();
  }

  @Subscribe
  public void onProxyShutdown(ProxyShutdownEvent event) throws IOException {
    cory.close();
  }
}
