/*
 * Copyright (C) 2018-2021 Velocity Contributors
 *
 * The Velocity API is licensed under the terms of the MIT License. For more details,
 * reference the LICENSE file in the api top-level directory.
 */

package com.velocitypowered.api.proxy.config;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.Favicon;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Exposes certain proxy configuration information that plugins may use.
 */
public interface ProxyConfig {

  /**
   * Get the MOTD component shown in the tab list.
   *
   * @return the motd component
   */
  net.kyori.adventure.text.Component getMotd();

  /**
   * Get the maximum players shown in the tab list.
   *
   * @return max players
   */
  int getShowMaxPlayers();

  /**
   * Get whether the proxy is online mode. This determines if players are
   * authenticated with Mojang.
   * servers.
   *
   * @return online mode enabled
   */
  boolean isOnlineMode();

  /**
   * Get a Map of all servers registered in <code>config.toml</code>. This
   * method does
   * <strong>not</strong> return all the servers currently in memory, although in
   * most cases it
   * does. For a view of all registered servers, see
   * {@link ProxyServer#getAllServers()}.
   *
   * @return registered servers map
   */
  Map<String, String> getServers();

  /**
   * Get the order of servers that players will be connected to.
   *
   * @return connection order list
   */
  List<String> getAttemptConnectionOrder();

  /**
   * Get forced servers mapped to a given virtual host.
   *
   * @return list of server names
   */
  Map<String, List<String>> getForcedHosts();

  /**
   * Get the minimum compression threshold for packets.
   *
   * @return the compression threshold
   */
  int getCompressionThreshold();

  /**
   * Get the level of compression that packets will be compressed to.
   *
   * @return the compression level
   */
  int getCompressionLevel();

  /**
   * Get the limit for how long a player must wait to log back in.
   *
   * @return the login rate limit (in milliseconds)
   */
  int getLoginRatelimit();

  /**
   * Get the proxy favicon shown in the tablist.
   *
   * @return optional favicon
   */
  Optional<Favicon> getFavicon();

  /**
   * Returns the proxy's brand name, shown in the F3 screen and the server brand
   * plugin message.
   *
   * @return the brand name
   */
  String getBrand();

  /**
   * Get how long this proxy will wait for a connection to be established before
   * timing it out.
   *
   * @return connection timeout (in milliseconds)
   */
  int getConnectTimeout();

  /**
   * Get how long this proxy will wait until performing a read timeout.
   *
   * @return read timeout (in milliseconds)
   */
  int getReadTimeout();

}
