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

package moe.rafal.firefly.server.container;

import static java.lang.String.format;
import static java.lang.String.join;
import static moe.rafal.agnes.AgnesUtils.parseDataSize;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder.Result;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import moe.rafal.agnes.Agnes;
import moe.rafal.agnes.container.ContainerDetails;
import moe.rafal.agnes.container.specification.ContainerSpecification;
import moe.rafal.agnes.container.specification.ContainerSpecificationBuilder;
import moe.rafal.agnes.image.Image;
import moe.rafal.firefly.config.PluginConfig;

class ContainerizedServerControllerImpl implements ContainerizedServerController {

  private static final int SERVER_SOCKET_CHOOSE_ANY_AVAILABLE_PORT = 0;
  private final PluginConfig pluginConfig;
  private final Agnes agnes;
  private final ContainerizedServerRegistry serverRegistry;

  ContainerizedServerControllerImpl(PluginConfig pluginConfig, Agnes agnes,
      ContainerizedServerRegistry serverRegistry) {
    this.pluginConfig = pluginConfig;
    this.agnes = agnes;
    this.serverRegistry = serverRegistry;
  }

  @Override
  public CompletableFuture<RegisteredServer> requestContainerizedServer() {
    return agnes.createContainer(getDefaultServerSpecification())
        .thenCompose(agnes::startContainer)
        .thenCompose(agnes::inspectContainer)
        .thenApply(serverRegistry::registerServer);
  }

  @Override
  public CompletableFuture<ContainerDetails> inspectContainerizedServer(String serverName)
      throws ContainerizedServerInspectException {
    final boolean whetherServerIsRegistered = serverRegistry.getServerByName(serverName).isEmpty();
    if (whetherServerIsRegistered) {
      return CompletableFuture.failedFuture(new ContainerizedServerInspectException(format(
          "Could not find registered server named %s so inspection has been denied.",
          serverName)));
    }
    return agnes.inspectContainer(serverName);
  }

  @Override
  public CompletableFuture<Result> connectContainerizedServer(Player player, String serverName) {
    return serverRegistry.getServerByName(serverName)
        .map(player::createConnectionRequest)
        .map(ConnectionRequestBuilder::connect)
        .orElseThrow();
  }

  private ContainerSpecification getDefaultServerSpecification() {
    return ContainerSpecificationBuilder.newBuilder()
        .withImage(new Image("itzg/minecraft-server", "latest"))
        .withAssignedMemory(parseDataSize(pluginConfig.serverConfiguration.availableMemory))
        .withAssignedMemorySwap(parseDataSize(pluginConfig.serverConfiguration.availableMemorySwap))
        .withHostname(pluginConfig.serverConfiguration.hostname)
        .withExposedPorts(new String[]{"25565/tcp"})
        .withPublishPorts(new String[]{format("25565/tcp:%d", findAnyAvailablePort())})
        .withEnvironmentalVariables(
            new String[]{"EULA=true", "TYPE=CUSTOM", "ONLINE_MODE=false", "USE_AIKAR_FLAGS=true",
                format("CUSTOM_SERVER=%s", pluginConfig.serverConfiguration.customServerPath),
                format("GENERIC_PACKS=%s",
                    join(",", pluginConfig.serverConfiguration.genericPacks)),
                format("GENERIC_PACKS_PREFIX=%s",
                    pluginConfig.serverConfiguration.genericPackPrefix),
                format("GENERIC_PACKS_SUFFIX=%s",
                    pluginConfig.serverConfiguration.genericPackSuffix)})
        .withBinds(new String[]{
            format("%s:/data",
                format(pluginConfig.serverConfiguration.bindTemplatePath, UUID.randomUUID()))
        })
        .build();
  }

  private Integer findAnyAvailablePort() throws ContainerizedServerRequestException {
    try (ServerSocket socket = new ServerSocket(SERVER_SOCKET_CHOOSE_ANY_AVAILABLE_PORT)) {
      return socket.getLocalPort();
    } catch (IOException exception) {
      throw new ContainerizedServerRequestException(
          "Could not find any available port, with use of server socket assignation method.",
          exception);
    }
  }
}
