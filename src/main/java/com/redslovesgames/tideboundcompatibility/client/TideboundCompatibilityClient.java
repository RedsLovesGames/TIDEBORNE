/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.client;

import com.li64.tide.client.TideItemModelProperties;
import com.redslovesgames.tideboundcompatibility.network.SharkCatchLossPayload;
import com.redslovesgames.tideboundcompatibility.network.TideboundSettingsPayload;
import com.redslovesgames.tideboundcompatibility.network.TideboundSettingsResultPayload;
import com.redslovesgames.tideboundcompatibility.registry.TideboundEntities;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.text.Text;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.toast.SystemToast.class_9037;

public final class TideboundCompatibilityClient implements ClientModInitializer {
   private static final KeyBinding OPEN_GUIDE = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.tidebound_compatibility.open_guide", 71, "key.categories.tidebound_compatibility")
   );

   public void onInitializeClient() {
      TideboundClientConfig.load();
      EntityRendererRegistry.register(TideboundEntities.CHUM_PROJECTILE, FlyingItemEntityRenderer::new);
      ModelPredicateProviderRegistry.register(TideboundItems.KUJIRA_BONE_FISHING_ROD, TideItemModelProperties.CAST_PROPERTY, TideItemModelProperties.CAST_FUNCTION);
      ClientPlayNetworking.registerGlobalReceiver(TideboundSettingsPayload.TYPE, (payload, context) -> ClientTideboundSettings.update(payload.tag()));
      ClientPlayNetworking.registerGlobalReceiver(
         TideboundSettingsResultPayload.TYPE,
         (payload, context) -> context.client()
            .player
            .sendMessage(Text.literal(payload.message()).styled(style -> style.withColor(payload.success() ? 5635925 : 16733525)), true)
      );
      ClientPlayNetworking.registerGlobalReceiver(
         SharkCatchLossPayload.TYPE,
         (payload, context) -> SystemToast.show(
            context.client().getToastManager(),
            class_9037.PERIODIC_NOTIFICATION,
            Text.translatable("toast.tidebound_compatibility.catch_lost.title"),
            Text.translatable("toast.tidebound_compatibility.catch_lost.body")
         )
      );
      ItemTooltipCallback.EVENT.register((ItemTooltipCallback)(stack, context, type, lines) -> TideboundTooltips.append(stack, type, lines));
      TideboundFishingHud.register();
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         while (OPEN_GUIDE.wasPressed()) {
            MinecraftClient.getInstance().setScreen(new TideboundGuideScreen());
         }
      });
   }
}
