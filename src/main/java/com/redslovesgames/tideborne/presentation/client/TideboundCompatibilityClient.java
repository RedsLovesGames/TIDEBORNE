package com.redslovesgames.tideborne.presentation.client;

import com.redslovesgames.tideborne.network.SharkCatchLossPayload;
import com.redslovesgames.tideborne.network.TideboundSettingsPayload;
import com.redslovesgames.tideborne.network.TideboundSettingsResultPayload;

import com.li64.tide.client.TideItemModelProperties;
import com.redslovesgames.tideborne.registry.TideboundEntities;
import com.redslovesgames.tideborne.registry.TideboundItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public final class TideboundCompatibilityClient implements ClientModInitializer {
   private static final KeyBinding OPEN_GUIDE=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.tidebound_compatibility.open_guide",71,"key.categories.tidebound_compatibility"));
   public void onInitializeClient(){TideboundClientConfig.load();EntityRendererRegistry.register(TideboundEntities.CHUM_PROJECTILE,FlyingItemEntityRenderer::new);ModelPredicateProviderRegistry.register(TideboundItems.KUJIRA_BONE_FISHING_ROD,TideItemModelProperties.CAST_PROPERTY,TideItemModelProperties.CAST_FUNCTION);registerLeaderColors();ClientPlayNetworking.registerGlobalReceiver(TideboundSettingsPayload.TYPE,(payload,context)->ClientTideboundSettings.update(payload.tag()));ClientPlayNetworking.registerGlobalReceiver(TideboundSettingsResultPayload.TYPE,(payload,context)->context.client().player.sendMessage(Text.literal(payload.message()).styled(s->s.withColor(payload.success()?5635925:16733525)),true));ClientPlayNetworking.registerGlobalReceiver(SharkCatchLossPayload.TYPE,(payload,context)->SystemToast.show(context.client().getToastManager(),SystemToast.Type.PERIODIC_NOTIFICATION,Text.translatable("toast.tidebound_compatibility.catch_lost.title"),Text.translatable("toast.tidebound_compatibility.catch_lost.body")));ItemTooltipCallback.EVENT.register((stack,context,type,lines)->TideboundTooltips.append(stack,type,lines));TideboundFishingHud.register();ClientTickEvents.END_CLIENT_TICK.register(client->{while(OPEN_GUIDE.wasPressed())MinecraftClient.getInstance().setScreen(new TideboundGuideScreen());});}
   private static void registerLeaderColors(){ColorProviderRegistry.ITEM.register((stack,tintIndex)->0xC87533,TideboundItems.COPPER_LEADER);ColorProviderRegistry.ITEM.register((stack,tintIndex)->0xFFD447,TideboundItems.GOLD_LEADER);ColorProviderRegistry.ITEM.register((stack,tintIndex)->0x55DDE0,TideboundItems.DIAMOND_LEADER);}
}
