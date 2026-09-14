package com.redslovesgames.tideborne.client.validation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CI-only client connection hook used by the dedicated-server smoke test.
 * Normal launches never register this hook because the environment variable is absent.
 */
public final class CiDirectConnect {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tideborne/CI");
    private static final String TARGET_ENV = "TIDEBORNE_CI_DIRECT_CONNECT_TARGET";
    private static boolean registered;
    private static boolean attempted;

    private CiDirectConnect() {
    }

    public static void initializeFromEnvironment() {
        if (registered) {
            return;
        }

        String target = System.getenv(TARGET_ENV);
        if (target == null || target.isBlank()) {
            return;
        }

        registered = true;
        String normalizedTarget = target.trim();
        ClientTickEvents.END_CLIENT_TICK.register(client -> connectOnce(client, normalizedTarget));
        LOGGER.info("CI direct-connect armed for {}.", normalizedTarget);
    }

    private static void connectOnce(MinecraftClient client, String target) {
        if (attempted || client.currentScreen == null || client.getOverlay() != null) {
            return;
        }
        if (client.world != null || client.getNetworkHandler() != null) {
            return;
        }

        attempted = true;
        ServerInfo serverInfo = new ServerInfo("Tideborne CI smoke", target, ServerInfo.ServerType.OTHER);
        LOGGER.info("CI direct-connect attempting {}.", target);
        ConnectScreen.connect(
                client.currentScreen,
                client,
                ServerAddress.parse(target),
                serverInfo,
                false,
                null
        );
    }
}
