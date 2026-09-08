package com.redslovesgames.tideborne.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.Test;

class TideborneCommandsRedirectTest {
    @Test
    void executableRedirectPreservesTargetCommandRequirementAndRouting() {
        AtomicBoolean allowed = new AtomicBoolean(false);
        Command<ServerCommandSource> command = context -> 7;
        CommandNode<ServerCommandSource> target = CommandManager.literal("target")
                .requires(source -> allowed.get())
                .executes(command)
                .build();

        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("root");
        TideborneCommands.redirect(root, "alias", target);
        CommandNode<ServerCommandSource> alias = root.build().getChild("alias");

        assertNotNull(alias);
        assertSame(command, alias.getCommand(), "Alias lost the target's executable command");
        assertSame(target, alias.getRedirect(), "Alias no longer routes deeper parsing to the target");
        assertFalse(alias.canUse(null), "Alias bypassed the target permission requirement");
        allowed.set(true);
        assertTrue(alias.canUse(null), "Alias did not preserve the target permission requirement");
    }

    @Test
    void routingOnlyTargetRemainsIncompleteUntilItsRequiredChildIsProvided() {
        CommandNode<ServerCommandSource> target = CommandManager.literal("target")
                .then(CommandManager.literal("required").executes(context -> 1))
                .build();

        CommandNode<ServerCommandSource> alias = TideborneCommands.executableRedirect("alias", target).build();

        assertNull(alias.getCommand(), "Routing-only target unexpectedly became executable at the alias root");
        assertSame(target, alias.getRedirect());
    }
}
