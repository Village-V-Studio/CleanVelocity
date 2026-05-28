/*
 * Copyright (C) 2025 Velocity Contributors
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

package com.velocitypowered.proxy.protocol.packet.chat;

import com.velocitypowered.proxy.protocol.MinecraftPacket;

public abstract class RateLimitedCommandHandler<T extends MinecraftPacket> implements CommandHandler<T> {

    @Override
    public boolean handlePlayerCommand(MinecraftPacket packet) {
        if (packetClass().isInstance(packet)) {
            handlePlayerCommandInternal(packetClass().cast(packet));
            return true;
        }

        return false;
    }
}
