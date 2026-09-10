package com.redslovesgames.tideborne.client;

import com.redslovesgames.tideborne.network.TideboundSettingsPayload;
import com.redslovesgames.tideborne.network.TideboundSettingsResultPayload;
import com.redslovesgames.tideborne.network.TideboundSettingsUpdatePayload;
import com.redslovesgames.tideborne.presentation.client.ClientTideboundSettings;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

/** Client half of Tideborne configuration synchronization. */
public final class TideborneConfigNetworkingClient {
   private static boolean initialized;

   private TideborneConfigNetworkingClient() {
   }

   public static void initialize() {
      if (initialized) return;
      initialized = true;
      ClientPlayNetworking.registerGlobalReceiver(
         TideboundSettingsPayload.TYPE,
         (payload, context) -> ClientTideboundSettings.update(payload.tag())
      );
      ClientPlayNetworking.registerGlobalReceiver(
         TideboundSettingsResultPayload.TYPE,
         (payload, context) -> {
            if (context.client().player != null) {
               context.client().player.sendMessage(
                  Text.literal(payload.message()).styled(style -> style.withColor(payload.success() ? 5635925 : 16733525)),
                  true
               );
            }
         }
      );
   }

   public static void sendFishingUpdate(String json) {
      ClientPlayNetworking.send(new TideboundSettingsUpdatePayload(json));
   }
}
