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

package com.velocitypowered.proxy.command.builtin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.proxy.VelocityServer;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements the {@code /server} command and friends.
 */
public final class VelocityCommand {
  private static final String USAGE = "/server <%s>";

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static BrigadierCommand create(final VelocityServer server) {
    final LiteralCommandNode<CommandSource> reload = BrigadierCommand
        .literalArgumentBuilder("reload")
        .requires(source -> source.getPermissionValue("velocity.command.reload") == Tristate.TRUE)
        .executes(new Reload(server))
        .build();
    final LiteralCommandNode<CommandSource> stop = BrigadierCommand
        .literalArgumentBuilder("stop")
        .requires(source -> source instanceof com.velocitypowered.api.proxy.ConsoleCommandSource)
        .executes(new Stop(server, null))
        .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<CommandSource, String>argument("reason",
            com.mojang.brigadier.arguments.StringArgumentType.greedyString())
            .executes(context -> {
              return new Stop(server, context.getArgument("reason", String.class)).run(context);
            }))
        .build();

    final List<LiteralCommandNode<CommandSource>> commands = List
        .of(reload, stop);
    return new BrigadierCommand(
        commands.stream()
            .reduce(
                BrigadierCommand.literalArgumentBuilder("server")
                    .executes(ctx -> {
                      final CommandSource source = ctx.getSource();
                      final String availableCommands = commands.stream()
                          .filter(e -> e.getRequirement().test(source))
                          .map(LiteralCommandNode::getName)
                          .collect(Collectors.joining("|"));
                      final String commandText = USAGE.formatted(availableCommands);
                      source.sendMessage(Component.text(commandText, NamedTextColor.RED));
                      return Command.SINGLE_SUCCESS;
                    })
                    .requires(commands.stream()
                        .map(CommandNode::getRequirement)
                        .reduce(Predicate::or)
                        .orElseThrow()),
                ArgumentBuilder::then,
                ArgumentBuilder::then));
  }

  private record Reload(VelocityServer server) implements Command<CommandSource> {

    private static final Logger logger = LogManager.getLogger(Reload.class);

    @Override
    public int run(final CommandContext<CommandSource> context) {
      final CommandSource source = context.getSource();
      try {
        if (server.reloadConfiguration()) {
          source.sendMessage(Component.translatable("velocity.command.reload-success",
              NamedTextColor.GREEN));
        } else {
          source.sendMessage(Component.translatable("velocity.command.reload-failure",
              NamedTextColor.RED));
        }
      } catch (Exception e) {
        logger.error("Unable to reload configuration", e);
        source.sendMessage(Component.translatable("velocity.command.reload-failure",
            NamedTextColor.RED));
      }
      return Command.SINGLE_SUCCESS;
    }
  }

  private record Stop(VelocityServer server, @org.checkerframework.checker.nullness.qual.Nullable String reason)
      implements Command<CommandSource> {
    @Override
    public int run(final CommandContext<CommandSource> context) {
      if (reason == null) {
        server.shutdown(true);
      } else {
        Component reasonComponent = null;

        if (reason.startsWith("{") || reason.startsWith("[") || reason.startsWith("\"")) {
          try {
            reasonComponent = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson()
                .deserializeOrNull(reason);
          } catch (com.google.gson.JsonSyntaxException expected) {
          }
        }

        if (reasonComponent == null) {
          reasonComponent = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(reason);
        }

        server.shutdown(true, reasonComponent);
      }
      return Command.SINGLE_SUCCESS;
    }
  }
}
