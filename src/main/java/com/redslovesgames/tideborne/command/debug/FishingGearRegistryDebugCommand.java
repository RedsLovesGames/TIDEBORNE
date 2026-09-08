package com.redslovesgames.tideborne.command.debug;

import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/** Read-only operator diagnostics for the canonical Fishing System 2.0 gear registry. */
public final class FishingGearRegistryDebugCommand {
    private FishingGearRegistryDebugCommand() {
    }

    public static int run(ServerCommandSource source) {
        if (!source.hasPermissionLevel(2)) {
            source.sendError(Text.literal("Fishing gear registry diagnostics require operator permission level 2."));
            return 0;
        }

        long tide = FishingGearRegistry.profiles().stream()
                .filter(profile -> profile.origin() == FishingGearRegistry.Origin.TIDE)
                .count();
        long tideborne = FishingGearRegistry.profiles().stream()
                .filter(profile -> profile.origin() == FishingGearRegistry.Origin.TIDEBORNE)
                .count();

        send(source, "Canonical fishing gear profiles: " + FishingGearRegistry.profiles().size()
                + " (Tide=" + tide + ", Tideborne=" + tideborne + ")");
        for (FishingGearRegistry.GearProfile profile : FishingGearRegistry.GearProfile.values()) {
            String itemId = FishingGearRegistry.registeredId(profile)
                    .map(Object::toString)
                    .orElse("unregistered");
            send(source, " - " + itemId + " -> " + profile.name()
                    + " [" + profile.origin().name() + ", " + profile.slot().name() + "]");
        }
        return 1;
    }

    private static void send(ServerCommandSource source, String message) {
        source.sendFeedback(() -> Text.literal(message), false);
    }
}
