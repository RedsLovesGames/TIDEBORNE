package com.redslovesgames.tideborne.fishing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.Test;

class FishingGameplayInitializerCommandTest {
    @Test
    void registersFishingInspectCommand() {
        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();

        FishingGameplayInitializer.registerCommands(dispatcher);

        CommandNode<ServerCommandSource> fishing = dispatcher.getRoot().getChild("tideborne_internal_fishing");
        assertNotNull(fishing);
        CommandNode<ServerCommandSource> inspect = fishing.getChild("inspect");
        assertNotNull(inspect);
        assertNotNull(inspect.getCommand());
        assertNotNull(inspect.getRequirement());
    }

    @Test
    void registersAdminDeterministicReproduceCommandWithContextAndPercentileArguments() {
        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();

        FishingGameplayInitializer.registerCommands(dispatcher);

        CommandNode<ServerCommandSource> fishing = dispatcher.getRoot().getChild("tideborne_internal_fishing");
        assertNotNull(fishing);
        CommandNode<ServerCommandSource> reproduce = fishing.getChild("reproduce");
        assertNotNull(reproduce);
        assertNotNull(reproduce.getRequirement());

        CommandNode<ServerCommandSource> species = reproduce.getChild("species");
        assertNotNull(species);
        CommandNode<ServerCommandSource> seed = species.getChild("seed");
        assertNotNull(seed);
        assertNotNull(seed.getCommand());
        CommandNode<ServerCommandSource> fishingLuck = seed.getChild("fishingLuck");
        assertNotNull(fishingLuck);
        CommandNode<ServerCommandSource> traitLuck = fishingLuck.getChild("traitLuck");
        assertNotNull(traitLuck);
        assertNotNull(traitLuck.getCommand());
        CommandNode<ServerCommandSource> perfectCatch = traitLuck.getChild("perfectCatch");
        assertNotNull(perfectCatch);
        assertNotNull(perfectCatch.getCommand());
        CommandNode<ServerCommandSource> forcedPercentile = perfectCatch.getChild("forcedPercentile");
        assertNotNull(forcedPercentile);
        assertNotNull(forcedPercentile.getCommand());
    }
}
