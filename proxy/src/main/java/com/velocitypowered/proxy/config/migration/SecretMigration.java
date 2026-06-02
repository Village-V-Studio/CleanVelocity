/*
 * Copyright (C) 2026 Velocity Contributors
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

package com.velocitypowered.proxy.config.migration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.Logger;

/**
 * Migration that moves the forwarding secret from a separate file to the main config under the "secret" key.
 */
public final class SecretMigration implements ConfigurationMigration {
  @Override
  public boolean shouldMigrate(final CommentedFileConfig config) {
    final String versionStr = config.getOrElse("config-version", "1.0");
    return !versionStr.startsWith("cv-") || configVersion(config) < 2.7;
  }

  @Override
  public void migrate(final CommentedFileConfig config, final Logger logger) throws IOException {
    logger.warn("""
        You are currently using a configuration format with the forwarding secret in a separate file.
        We will migrate your secret directly into the config.toml config under the 'secret' key.""");

    String actualSecret = null;
    final String forwardingSecretFile = config.get("forwarding-secret-file");
    if (forwardingSecretFile != null) {
      final Path path = Path.of(forwardingSecretFile);
      if (Files.exists(path)) {
        if (Files.isRegularFile(path)) {
          actualSecret = Files.readString(path).trim();
          try {
            Files.deleteIfExists(path);
          } catch (IOException e) {
            logger.warn("Failed to delete deprecated secret file: " + path, e);
          }
        }
      }
    } else {
      final Path defaultPath = Path.of("secret");
      if (Files.exists(defaultPath) && Files.isRegularFile(defaultPath)) {
        actualSecret = Files.readString(defaultPath).trim();
        try {
          Files.deleteIfExists(defaultPath);
        } catch (IOException e) {
          logger.warn("Failed to delete deprecated secret file: " + defaultPath, e);
        }
      }
    }

    if (actualSecret == null || actualSecret.isEmpty()) {
      actualSecret = com.velocitypowered.proxy.config.VelocityConfiguration.generateRandomString(12);
    }

    config.set("secret", actualSecret);
    config.remove("forwarding-secret-file");
    config.set("config-version", "cv-2.7");
  }
}
