/*
 * Copyright (C) 2018-2023 Velocity Contributors
 * Copyright (C) 2026 Village V Studio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.Expose;
import com.velocitypowered.api.proxy.config.ProxyConfig;
import com.velocitypowered.api.util.Favicon;
import com.velocitypowered.proxy.config.migration.ConfigurationMigration;
import com.velocitypowered.proxy.config.migration.ForwardingMigration;
import com.velocitypowered.proxy.config.migration.KeyAuthenticationMigration;
import com.velocitypowered.proxy.config.migration.MiniMessageTranslationsMigration;
import com.velocitypowered.proxy.config.migration.SecretMigration;
import com.velocitypowered.proxy.config.migration.TransferIntegrationMigration;
import com.velocitypowered.proxy.util.AddressUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Velocity's configuration.
 */
public class VelocityConfiguration implements ProxyConfig {

  private static final Logger logger = LogManager.getLogger(VelocityConfiguration.class);

  @Expose
  private String bind = "0.0.0.0:25565";

  @Expose
  private boolean onlineMode = true;
  @Expose
  private PlayerInfoForwarding playerInfoForwardingMode = PlayerInfoForwarding.NONE;
  private byte[] forwardingSecret = generateRandomString(12).getBytes(StandardCharsets.UTF_8);
  @Expose
  private PingPassthroughMode pingPassthrough = PingPassthroughMode.DISABLED;
  private final Servers servers;
  @Expose
  private final Advanced advanced;

  private @Nullable Favicon favicon;
  @Expose
  private boolean forceKeyAuthentication = true; // Added in 1.19

  private VelocityConfiguration(Servers servers, Advanced advanced) {
    this.servers = servers;
    this.advanced = advanced;
  }

  private VelocityConfiguration(String bind, boolean onlineMode,
      PlayerInfoForwarding playerInfoForwardingMode, byte[] forwardingSecret,
      PingPassthroughMode pingPassthrough,
      Servers servers,
      Advanced advanced,
      boolean forceKeyAuthentication) {
    this.bind = bind;
    this.onlineMode = onlineMode;
    this.playerInfoForwardingMode = playerInfoForwardingMode;
    this.forwardingSecret = forwardingSecret;
    this.pingPassthrough = pingPassthrough;
    this.servers = servers;
    this.advanced = advanced;

    this.forceKeyAuthentication = forceKeyAuthentication;
  }

  /**
   * Attempts to validate the configuration.
   *
   * @return {@code true} if the configuration is sound, {@code false} if not
   */
  public boolean validate() {
    boolean valid = true;

    if (bind.isEmpty()) {
      logger.error("'bind' option is empty.");
      valid = false;
    } else {
      try {
        AddressUtil.parseAddress(bind);
      } catch (IllegalArgumentException e) {
        logger.error("'bind' option does not specify a valid IP address.", e);
        valid = false;
      }
    }

    switch (playerInfoForwardingMode) {
      case NONE -> logger.warn("Player info forwarding is disabled! All players will appear to be connecting "
          + "from the proxy and will have offline-mode UUIDs.");
      case MODERN, BUNGEEGUARD -> {
        if (forwardingSecret == null || forwardingSecret.length == 0) {
          logger.error("You don't have a forwarding secret set. This is required for security.");
          valid = false;
        }
      }
      default -> {
      }
    }

    if (servers.getServers().isEmpty()) {
      logger.warn("You don't have any servers configured.");
    }

    for (Map.Entry<String, String> entry : servers.getServers().entrySet()) {
      try {
        AddressUtil.parseAddress(entry.getValue());
      } catch (IllegalArgumentException e) {
        logger.error("Server {} does not have a valid IP address.", entry.getKey(), e);
        valid = false;
      }
    }

    for (String s : servers.getAttemptConnectionOrder()) {
      if (!servers.getServers().containsKey(s)) {
        logger.error("Fallback server " + s + " is not registered in your configuration!");
        valid = false;
      }
    }

    try {
      getMotd();
    } catch (Exception e) {
      logger.error("Can't parse your MOTD", e);
      valid = false;
    }

    if (advanced.compressionLevel < -1 || advanced.compressionLevel > 9) {
      logger.error("Invalid compression level {}", advanced.compressionLevel);
      valid = false;
    } else if (advanced.compressionLevel == 0) {
      logger.warn("ALL packets going through the proxy will be uncompressed. This will increase "
          + "bandwidth usage.");
    }

    if (advanced.compressionThreshold < -1) {
      logger.error("Invalid compression threshold {}", advanced.compressionLevel);
      valid = false;
    } else if (advanced.compressionThreshold == 0) {
      logger.warn("ALL packets going through the proxy will be compressed. This will compromise "
          + "throughput and increase CPU usage!");
    }

    loadFavicon();

    return valid;
  }

  private void loadFavicon() {
    Path faviconPath = Path.of("server-icon.png");
    if (Files.exists(faviconPath)) {
      try {
        this.favicon = Favicon.create(faviconPath);
      } catch (Exception e) {
        logger.info("Unable to load your server-icon.png, continuing without it.", e);
      }
    }
  }

  public InetSocketAddress getBind() {
    return AddressUtil.parseAndResolveAddress(bind);
  }

  @Override
  public net.kyori.adventure.text.Component getMotd() {
    return net.kyori.adventure.text.Component.text("A Minecraft Server");
  }

  @Override
  public int getShowMaxPlayers() {
    return 100;
  }

  @Override
  public boolean isOnlineMode() {
    return onlineMode;
  }

  public PlayerInfoForwarding getPlayerInfoForwardingMode() {
    return playerInfoForwardingMode;
  }

  public byte[] getForwardingSecret() {
    return forwardingSecret.clone();
  }

  @Override
  public Map<String, String> getServers() {
    return servers.getServers();
  }

  @Override
  public List<String> getAttemptConnectionOrder() {
    return servers.getAttemptConnectionOrder();
  }

  @Override
  public Map<String, List<String>> getForcedHosts() {
    return Collections.emptyMap();
  }

  @Override
  public int getCompressionThreshold() {
    return advanced.getCompressionThreshold();
  }

  @Override
  public int getCompressionLevel() {
    return advanced.getCompressionLevel();
  }

  @Override
  public int getLoginRatelimit() {
    return advanced.getLoginRatelimit();
  }

  @Override
  public Optional<Favicon> getFavicon() {
    return Optional.ofNullable(favicon);
  }

  @Override
  public String getBrand() {
    return "CleanVelocity";
  }

  @Override
  public int getConnectTimeout() {
    return advanced.getConnectionTimeout();
  }

  @Override
  public int getReadTimeout() {
    return advanced.getReadTimeout();
  }

  public boolean isProxyProtocol() {
    return advanced.isProxyProtocol();
  }

  public void setProxyProtocol(boolean proxyProtocol) {
    advanced.setProxyProtocol(proxyProtocol);
  }

  public boolean useTcpFastOpen() {
    return advanced.isTcpFastOpen();
  }

  public PingPassthroughMode getPingPassthrough() {
    return pingPassthrough;
  }

  public boolean isAcceptTransfers() {
    return this.advanced.isAcceptTransfers();
  }

  public boolean isForceKeyAuthentication() {
    return forceKeyAuthentication;
  }

  public boolean isEnableReusePort() {
    return advanced.isEnableReusePort();
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("bind", bind)
        .add("onlineMode", onlineMode)
        .add("playerInfoForwardingMode", playerInfoForwardingMode)
        .add("forwardingSecret", forwardingSecret)
        .add("servers", servers)
        .add("advanced", advanced)

        .add("favicon", favicon)
        .add("forceKeyAuthentication", forceKeyAuthentication)
        .toString();
  }

  /**
   * Reads the Velocity configuration from {@code path}.
   *
   * @param path the path to read from
   * @return the deserialized Velocity configuration
   * @throws IOException if we could not read from the {@code path}.
   */
  @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
  public static VelocityConfiguration read(Path path) throws IOException {
    URL defaultConfigLocation = VelocityConfiguration.class.getClassLoader()
        .getResource("default-velocity.toml");
    if (defaultConfigLocation == null) {
      throw new RuntimeException("Default configuration file does not exist.");
    }

    try (final CommentedFileConfig config = CommentedFileConfig.builder(path)
        .defaultData(defaultConfigLocation)
        .autosave()
        .preserveInsertionOrder()
        .sync()
        .build()) {
      config.load();

      final ConfigurationMigration[] migrations = {
          new ForwardingMigration(),
          new KeyAuthenticationMigration(),

          new MiniMessageTranslationsMigration(),
          new TransferIntegrationMigration(),
          new SecretMigration()
      };

      for (final ConfigurationMigration migration : migrations) {
        if (migration.shouldMigrate(config)) {
          migration.migrate(config, logger);
        }
      }

      String forwardingSecretString = System.getenv().getOrDefault(
          "VELOCITY_FORWARDING_SECRET", "");
      if (forwardingSecretString.isBlank()) {
        forwardingSecretString = config.getOrElse("secret", "");
        if (forwardingSecretString.isBlank()) {
          forwardingSecretString = generateRandomString(12);
          config.set("secret", forwardingSecretString);
        }
      }
      final byte[] forwardingSecret = forwardingSecretString.getBytes(StandardCharsets.UTF_8);
      // Read the rest of the config
      final CommentedConfig serversConfig = config.get("servers");
      final PlayerInfoForwarding forwardingMode = config.getEnumOrElse(
          "player-info-forwarding-mode", PlayerInfoForwarding.NONE);
      final PingPassthroughMode pingPassthroughMode = config.getEnumOrElse("ping-passthrough",
          PingPassthroughMode.DISABLED);

      final String bind = config.getOrElse("bind", "0.0.0.0:25565");
      final boolean onlineMode = config.getOrElse("online-mode", true);
      final boolean forceKeyAuthentication = config.getOrElse("force-key-authentication", true);

      // Throw an exception if the forwarding secret is empty and the proxy is
      // using a
      // forwarding mode that requires it.
      if (forwardingSecret.length == 0
          && (forwardingMode == PlayerInfoForwarding.MODERN
              || forwardingMode == PlayerInfoForwarding.BUNGEEGUARD)) {
        throw new RuntimeException("The forwarding secret must not be empty.");
      }

      return new VelocityConfiguration(
          bind,
          onlineMode,
          forwardingMode,
          forwardingSecret,
          pingPassthroughMode,
          new Servers(serversConfig),
          new Advanced(config),
          forceKeyAuthentication
      );
    }
  }

  /**
   * Generates a Random String.
   *
   * @param length the required string size.
   * @return a new random string.
   */
  public static String generateRandomString(int length) {
    final String chars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
    final StringBuilder builder = new StringBuilder();
    final Random rnd = new SecureRandom();
    for (int i = 0; i < length; i++) {
      builder.append(chars.charAt(rnd.nextInt(chars.length())));
    }
    return builder.toString();
  }

  private static class Servers {

    private Map<String, String> servers = ImmutableMap.of(
        "server", "127.0.0.1:25566");
    private List<String> attemptConnectionOrder = ImmutableList.of("server");

    private Servers() {
    }

    private Servers(CommentedConfig config) {
      if (config != null) {
        Map<String, String> servers = new HashMap<>();
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
          if (entry.getValue() instanceof String) {
            servers.put(cleanServerName(entry.getKey()), entry.getValue());
          } else {
            if (!entry.getKey().equalsIgnoreCase("try")) {
              throw new IllegalArgumentException(
                  "Server entry " + entry.getKey() + " is not a string!");
            }
          }
        }
        this.servers = ImmutableMap.copyOf(servers);
        this.attemptConnectionOrder = config.getOrElse("try", attemptConnectionOrder);
      }
    }

    private Servers(Map<String, String> servers, List<String> attemptConnectionOrder) {
      this.servers = servers;
      this.attemptConnectionOrder = attemptConnectionOrder;
    }

    private Map<String, String> getServers() {
      return servers;
    }

    public List<String> getAttemptConnectionOrder() {
      return attemptConnectionOrder;
    }

    /**
     * TOML requires keys to match a regex of {@code [A-Za-z0-9_-]} unless it is
     * wrapped in quotes;
     * however, the TOML parser returns the key with the quotes so we need to clean
     * the server name
     * before we pass it onto server registration to keep proper server name
     * behavior.
     *
     * @param name the server name to clean
     * @return the cleaned server name
     */
    private String cleanServerName(String name) {
      return name.replace("\"", "");
    }

    @Override
    public String toString() {
      return "Servers{"
          + "servers=" + servers
          + ", attemptConnectionOrder=" + attemptConnectionOrder
          + '}';
    }
  }

  private static class Advanced {

    @Expose
    private int compressionThreshold = 256;
    @Expose
    private int compressionLevel = -1;
    @Expose
    private boolean proxyProtocol = false;
    @Expose
    private boolean tcpFastOpen = false;

    @Expose
    private boolean acceptTransfers = false;
    @Expose
    private boolean enableReusePort = false;

    private Advanced() {
    }

    private Advanced(CommentedConfig config) {
      if (config != null) {
        this.compressionThreshold = config.getIntOrElse("compression-threshold", 256);
        this.compressionLevel = config.getIntOrElse("compression-level", -1);
        if (config.contains("haproxy-protocol")) {
          this.proxyProtocol = config.getOrElse("haproxy-protocol", false);
        } else {
          this.proxyProtocol = config.getOrElse("proxy-protocol", false);
        }
        this.tcpFastOpen = config.getOrElse("tcp-fast-open", false);

        this.acceptTransfers = config.getOrElse("accepts-transfers", false);
        this.enableReusePort = config.getOrElse("enable-reuse-port", false);

      }
    }

    public int getCompressionThreshold() {
      return compressionThreshold;
    }

    public int getCompressionLevel() {
      return compressionLevel;
    }

    public int getLoginRatelimit() {
      return 0;
    }

    public int getConnectionTimeout() {
      return 10000;
    }

    public int getReadTimeout() {
      return 30000;
    }

    public boolean isProxyProtocol() {
      return proxyProtocol;
    }

    public void setProxyProtocol(boolean proxyProtocol) {
      this.proxyProtocol = proxyProtocol;
    }

    public boolean isTcpFastOpen() {
      return tcpFastOpen;
    }

    public boolean isAcceptTransfers() {
      return this.acceptTransfers;
    }

    public boolean isEnableReusePort() {
      return enableReusePort;
    }

    @Override
    public String toString() {
      return "Advanced{"
          + "compressionThreshold=" + compressionThreshold
          + ", compressionLevel=" + compressionLevel
          + ", proxyProtocol=" + proxyProtocol
          + ", tcpFastOpen=" + tcpFastOpen
          + ", acceptTransfers=" + acceptTransfers
          + ", enableReusePort=" + enableReusePort
          + '}';
    }
  }
}
