package com.redslovesgames.tideboundcompatibility;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.Test;

class TideboundCompatibilityCommandTest {
    @Test
    void registersFishingInspectCommand() {
        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();

        TideboundCompatibility.registerCommands(dispatcher);

        CommandNode<ServerCommandSource> fishing = dispatcher.getRoot().getChild("tideborne_internal_fishing");
        assertNotNull(fishing);
        CommandNode<ServerCommandSource> inspect = fishing.getChild("inspect");
        assertNotNull(inspect);
        assertNotNull(inspect.getCommand());
        assertNotNull(inspect.getRequirement());
    }
}
