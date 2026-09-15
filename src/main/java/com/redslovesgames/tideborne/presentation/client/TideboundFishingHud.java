/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.presentation.client;

import com.li64.tide.data.minigame.FishCatchMinigame;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.util.BaitUtils;
import com.redslovesgames.tideborne.ecosystem.SharkScentManager;
import com.redslovesgames.tideborne.fishing.gear.SteelLeaderAttachment;
import com.redslovesgames.tideborne.registry.TideboundItems;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

final class TideboundFishingHud {
   private TideboundFishingHud() {
   }

   static void register() {
      HudRenderCallback.EVENT.register(TideboundFishingHud::render);
   }

   private static void render(DrawContext graphics, RenderTickCounter deltaTracker) {
      MinecraftClient minecraft = MinecraftClient.getInstance();
      if (TideboundClientConfig.get().showFishingHud
         && ClientTideboundSettings.available()
         && minecraft.player != null
         && FishCatchMinigame.minigameActive(minecraft.player)) {
         TideFishingHook hook = HookAccessor.getHook(minecraft.player);
         if (hook != null) {
            List<Text> rows = new ArrayList<>();
            rows.add(Text.literal("Tideborne Fishing").styled(style -> style.withBold(true)));
            if (ClientTideboundSettings.bool("myths_active") && hook.getLine().isOf(TideboundItems.TENTACLE_LINE)) {
               rows.add(Text.literal("Tentacle Line: Catch Zone " + TideboundTooltips.multiplier("tentacle_zone") + "; Fish Movement unchanged"));
            }

            if (ClientTideboundSettings.bool("myths_active") && hook.getLine().isOf(TideboundItems.SWIFT_LINE)) {
               rows.add(Text.literal("Abaia Line: Catch Zone " + TideboundTooltips.multiplier("swift_zone")));
            }

            if (ClientTideboundSettings.bool("apex_active") && SteelLeaderAttachment.hasOnHook(hook)) {
               rows.add(Text.literal("Steel Leader: " + TideboundTooltips.percent("steel_prevent") + " Shark Protection"));
            }

            if (ClientTideboundSettings.bool("myths_active") && hook.getHook().isOf(TideboundItems.SEAFARERS_HOOK)) {
               rows.add(Text.literal("Seafarer's Hook: favors legendary fish at night in the ocean"));
            }

            if (ClientTideboundSettings.bool("apex_active") && hook.getHook().isOf(TideboundItems.SHARK_TOOTH_HOOK)) {
               rows.add(Text.literal("Shark Tooth Hook: favors large and predatory fish"));
            }

            if (ClientTideboundSettings.bool("myths_active") && BaitUtils.hasBait(TideboundItems.LEVIATHAN_BAIT, hook.getRod())) {
               rows.add(Text.literal("Leviathan Bait: always hooks a fish, but makes the fight harder"));
            }

            if (ClientTideboundSettings.bool("apex_active")
               && ClientTideboundSettings.bool("shark_attraction")
               && hook.getHookedItems().stream().anyMatch(SharkScentManager::isLargeCatch)) {
               rows.add(Text.literal("Large catch: attracting sharks"));
            }

            if (rows.size() != 1) {
               int width = rows.stream().mapToInt(row -> minecraft.textRenderer.getWidth(row)).max().orElse(0) + 12;
               int x = (graphics.getScaledWindowWidth() - width) / 2;
               int y = graphics.getScaledWindowHeight() - 84 - rows.size() * 10;
               graphics.fill(x - 3, y - 3, x + width + 3, y + rows.size() * 10 + 3, -1610612736);

               for (int i = 0; i < rows.size(); i++) {
                  graphics.drawText(minecraft.textRenderer, rows.get(i), x, y + i * 10, 16777215, true);
               }
            }
         }
      }
   }
}
