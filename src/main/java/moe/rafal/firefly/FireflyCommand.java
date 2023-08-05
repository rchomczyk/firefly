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
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

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
    return serverController.createContainerizedServer()
        .thenApply(RegisteredServer::getServerInfo)
        .thenApply(ServerInfo::getName)
        .thenApply(serverName -> miniMessage().deserialize(format(
            "<gray>Server has been created and is now waking up. Click to copy <white><hover:show_text:'<gray>Click to copy into clipboard.'><click:copy_to_clipboard:'%s'>server name</click></hover></white>.",
            serverName)));
  }

  @Permission("firefly.command.firefly.connect")
  @Execute(route = "connect")
  public CompletableFuture<Component> connectContainerizedServer(Player player,
      @Arg String serverName) {
    return serverController.connectContainerizedServer(player, serverName)
        .thenApply(state -> miniMessage().deserialize(
            "<gray>Your connection is being established with server <white><server_name><gray>.",
            Placeholder.unparsed("server_name", serverName)));
  }

  @Permission("firefly.command.firefly.inspect")
  @Execute(route = "inspect")
  public CompletableFuture<Component> inspectContainerizedServer(Player player,
      @Arg String serverName) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
