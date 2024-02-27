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

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.permission.Permission;
import dev.rollczi.litecommands.command.route.Route;
import java.util.concurrent.CompletableFuture;
import moe.rafal.firefly.server.container.ContainerizedServerController;
import net.kyori.adventure.text.Component;

@Permission("firefly.command.firefly")
@Route(name = "firefly")
class FireflyCommand {

  private final ContainerizedServerController serverController;

  FireflyCommand(
      ContainerizedServerController serverController) {
    this.serverController = serverController;
  }

  @Permission("firefly.command.firefly.request")
  @Execute(route = "request")
  public CompletableFuture<Component> requestContainerizedServer() {
    return serverController.requestContainerizedServer()
        .thenApply(RegisteredServer::getServerInfo)
        .thenApply(ServerInfo::getName)
        .thenApply(serverName -> miniMessage().deserialize(format(
            "<gray>Server has been created and is now waking up. Click to copy <white><hover:show_text:'<gray>Click to copy into clipboard.'><click:copy_to_clipboard:'%s'>server name</click></hover></white>.",
            serverName)));
  }

  @Permission("firefly.command.firefly.inspect")
  @Execute(route = "inspect")
  public CompletableFuture<Component> inspectContainerizedServer(@Arg String serverName) {
    String inspectionTemplate = """
        <gray>Container inspection:
        <dark_gray>> <gray>id: <white><container_id>
        <dark_gray>> <gray>image(sha): <white><image_hash>
        <dark_gray>> <gray>address: <white><published_address>:<published_port>
        <dark_gray>> <gray>started: <white><container_started>
        """.trim();
    return serverController.inspectContainerizedServer(serverName)
        .thenApply(containerDetails -> miniMessage().deserialize(
            inspectionTemplate,
            unparsed("container_id", containerDetails.getContainerId()),
            unparsed("image_hash", containerDetails.getImageHash()),
            unparsed("published_address", containerDetails.getAddress()),
            unparsed("published_port", valueOf(containerDetails.getPort())),
            unparsed("container_started", containerDetails.getStartedAt().toString())
        ))
        .exceptionally(exception -> miniMessage().deserialize(
            "<red>Could not inspect specified container, because it it was not requested by this proxy."));
  }

  @Permission("firefly.command.firefly.connect")
  @Execute(route = "connect")
  public CompletableFuture<Component> connectContainerizedServer(Player player,
      @Arg String serverName) {
    return serverController.connectContainerizedServer(player, serverName)
        .thenApply(state -> miniMessage().deserialize(
            "<gray>Your connection is being established with server <white><server_name><gray>.",
            unparsed("server_name", serverName)));
  }
}
