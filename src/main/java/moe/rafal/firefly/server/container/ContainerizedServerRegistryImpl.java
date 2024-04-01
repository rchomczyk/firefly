/*
 *    Copyright 2023-2024 firefly
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

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import java.net.InetSocketAddress;
import java.util.Optional;
import moe.rafal.agnes.container.ContainerDetails;

class ContainerizedServerRegistryImpl implements ContainerizedServerRegistry {

  private final ProxyServer proxyServer;

  public ContainerizedServerRegistryImpl(ProxyServer proxyServer) {
    this.proxyServer = proxyServer;
  }

  @Override
  public RegisteredServer registerServer(ContainerDetails details) {
    return proxyServer.registerServer(new ServerInfo(details.getContainerId(),
        new InetSocketAddress(details.getAddress(), details.getPort())));
  }

  @Override
  public Optional<RegisteredServer> getServerByName(String serverName) {
    return proxyServer.getServer(serverName);
  }
}
