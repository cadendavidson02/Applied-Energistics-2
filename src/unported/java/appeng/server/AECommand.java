/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.server;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import appeng.api.features.AEFeature;
import appeng.core.AEConfig;

public final class AECommand {

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        LiteralArgumentBuilder<ServerCommandSource> builder = literal("ae2");
        for (Commands command : Commands.values()) {
            if (command.test && !AEConfig.instance().isFeatureEnabled(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS)) {
                continue;
            }
            add(builder, command);
        }

        dispatcher.register(builder);
    }

    private void add(LiteralArgumentBuilder<net.minecraft.server.command.ServerCommandSource> builder, Commands subCommand) {

        LiteralArgumentBuilder<ServerCommandSource> subCommandBuilder = literal(subCommand.name().toLowerCase())
                .requires(src -> src.hasPermissionLevel(subCommand.level));
        subCommand.command.addArguments(subCommandBuilder);
        subCommandBuilder.executes(ctx -> {
            subCommand.command.call(ServerLifecycleHooks.getCurrentServer(), ctx, ctx.getSource());
            return 1;
        });
        builder.then(subCommandBuilder);

    }

}