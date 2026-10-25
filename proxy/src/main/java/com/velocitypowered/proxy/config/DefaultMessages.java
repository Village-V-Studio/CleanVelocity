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

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Protective fallback translation strings for CleanVelocity.
 * Acts as a safety net when a phrase is missing from translation files,
 * or when no translation file exists on disk or inside the JAR.
 *
 * <p>Contains protective fallbacks for all 21 custom proxy.* phrases.
 *
 * <p>Vanilla Minecraft phrases used by CleanVelocity:
 * <ul>
 *   <li>Category 1 (Pre-login):
 *     multiplayer.disconnect.authservers_down,
 *     multiplayer.disconnect.invalid_player_data,
 *     multiplayer.disconnect.invalid_public_key,
 *     multiplayer.disconnect.invalid_public_key_signature,
 *     multiplayer.disconnect.missing_public_key,
 *     multiplayer.disconnect.outdated_client,
 *     multiplayer.disconnect.slow_login,
 *     multiplayer.disconnect.transfers_disabled</li>
 *   <li>Category 2 (Post-login):
 *     multiplayer.disconnect.out_of_order_chat,
 *     multiplayer.requiredTexturePrompt.disconnect</li>
 * </ul>
 *
 * <p>These 10 vanilla phrases are INTENTIONALLY omitted from the DEFAULTS map below
 * so that the Minecraft client automatically displays its own built-in translation
 * in the player's selected language. If a server administrator wants to override
 * them, they can define them in 'lang/messages.properties'.
 *
 * <p>How to add custom localization:
 * <ol>
 *   <li>External folder: Create a 'lang/' folder next to the server jar
 *   and place language files there (e.g. lang/messages.properties).</li>
 *   <li>Inside the JAR: Place language files in proxy/src/main/resources/lang/
 *   (e.g. proxy/src/main/resources/lang/messages.properties).</li>
 * </ol>
 *
 * <p>Any translation file can contain only partial messages — missing keys
 * automatically fall back to this class.
 */
public final class DefaultMessages {

  private static final Map<String, String> DEFAULTS;

  static {
    final Map<String, String> map = new LinkedHashMap<>();

    // Category 1: Proxy messages before login (player locale is unknown to the proxy)
    map.put("proxy.error.already-connected-proxy",
        "You are already connected to this proxy!");
    map.put("proxy.error.modern-forwarding-failed",
        "Your server did not send a forwarding request to the proxy. Make sure the server is configured for Velocity forwarding.");
    map.put("proxy.error.modern-forwarding-needs-new-client",
        "This server is only compatible with Minecraft 1.13 and above.");
    map.put("proxy.error.no-available-servers",
        "There are no available servers to connect you to. Try again later or contact an admin.");
    map.put("proxy.error.online-mode-only",
        "You are not logged into your Minecraft account. If you are logged into your Minecraft account, "
            + "try restarting your Minecraft client.");
    map.put("proxy.error.plugin-message-overflow",
        "You sent too many plugin messages before completing the connection.");

    // Category 2: Proxy messages after login (support per-player localization based on client language)
    map.put("proxy.error.already-connected",
        "You are already connected to this server!");
    map.put("proxy.error.already-connecting",
        "You are already trying to connect to a server!");
    map.put("proxy.error.cant-connect",
        "Unable to connect to <arg:0>: <arg:1>");
    map.put("proxy.error.connected-server-error",
        "Your connection to <arg:0> encountered a problem.");
    map.put("proxy.error.connecting-server-error",
        "Unable to connect you to <arg:0>. Please try again later.");
    map.put("proxy.error.fallback-server-not-found",
        "Your server has been changed, but we could not move you to any fallback servers.");
    map.put("proxy.error.internal-server-connection-error",
        "An internal server connection error occurred.");
    map.put("proxy.error.moved-to-new-server",
        "You were kicked from <arg:0>: <arg:1>");
    map.put("proxy.error.player-connection-error",
        "An internal error occurred in your connection.");
    map.put("proxy.error.plugin-illegal-protocol-state",
        "A proxy plugin caused an illegal protocol state. Contact your network administrator.");
    map.put("proxy.kick.shutdown",
        "Proxy shutting down.");

    // Category 3: Console and command messages (support localization for console/admin language)
    map.put("proxy.command.command-does-not-exist",
        "This command does not exist.");
    map.put("proxy.command.generic-error",
        "An error occurred while running this command.");
    map.put("proxy.command.reload-failure",
        "Unable to reload your CleanVelocity configuration. Check the console for more details.");
    map.put("proxy.command.reload-success",
        "CleanVelocity configuration successfully reloaded.");

    DEFAULTS = Collections.unmodifiableMap(map);
  }

  private DefaultMessages() {
  }

  /**
   * Returns an unmodifiable map of all protective fallback messages.
   *
   * @return fallback messages map
   */
  public static Map<String, String> getDefaults() {
    return DEFAULTS;
  }

  /**
   * Creates a {@link ResourceBundle} containing all protective fallback messages.
   *
   * @return fallback resource bundle
   */
  public static ResourceBundle createResourceBundle() {
    return new ResourceBundle() {
      @Override
      protected Object handleGetObject(final String key) {
        return DEFAULTS.get(key);
      }

      @Override
      public Enumeration<String> getKeys() {
        return Collections.enumeration(DEFAULTS.keySet());
      }
    };
  }
}
