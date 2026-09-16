package com.redslovesgames.tideborne.presentation.client;

import com.li64.tide.client.TideItemModelProperties;
import com.redslovesgames.tideborne.client.TideborneConfigNetworkingClient;
import com.redslovesgames.tideborne.network.SharkCatchLossPayload;
import com.redslovesgames.tideborne.registry.TideboundEntities;
import com.redslovesgames.tideborne.registry.TideboundItems;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public final class TideboundCompatibilityClient {
   private static boolean initialized;

   private TideboundCompatibilityClient() {
   }

   public static void initialize() {
      if (initialized) {
         return;
      }
      initialized = true;
      TideboundClientConfig.load();
      TideborneConfigNetworkingClient.initialize();
      EntityRendererRegistry.register(TideboundEntities.CHUM_PROJECTILE, FlyingItemEntityRenderer::new);
      ModelPredicateProviderRegistry.register(
         TideboundItems.KUJIRA_BONE_FISHING_ROD, TideItemModelProperties.CAST_PROPERTY, TideItemModelProperties.CAST_FUNCTION
      );
      registerLeaderColors();
      ClientPlayNetworking.registerGlobalReceiver(
         SharkCatchLossPayload.TYPE,
         (payload, context) -> SystemToast.show(
            context.client().getToastManager(),
            SystemToast.Type.PERIODIC_NOTIFICATION,
            Text.translatable("toast.tideborne.fishing.catch_lost.title"),
            Text.translatable("toast.tideborne.fishing.catch_lost.body")
         )
      );
      ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> TideboundTooltips.append(stack, type, lines));
      TideboundFishingHud.register();
   }

   private static void registerLeaderColors() {
      ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 0xC87533, TideboundItems.COPPER_LEADER);
      ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 0xFFD447, TideboundItems.GOLD_LEADER);
      ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 0x55DDE0, TideboundItems.DIAMOND_LEADER);
   }
}
