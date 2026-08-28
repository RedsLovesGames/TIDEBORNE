/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

public final class HistoryBadgeCommand implements Command<ServerCommandSource> {
   public static final HistoryBadgeCommand INSTANCE = new HistoryBadgeCommand();

   private HistoryBadgeCommand() {
   }

   public int run(CommandContext<ServerCommandSource> var1) {
      return HistoryBadgeBackfill.run((ServerCommandSource)var1.getSource());
   }
}
