/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

import com.li64.tide.data.TideTags.Items;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.journal.FishRarity;
import com.li64.tide.util.TideUtils;
import com.redslovesgames.tideteamjournal.BobberBonuses;
import com.redslovesgames.tideteamjournal.TeamProgressStore;
import com.redslovesgames.tideteamjournal.network.BobberSettingsPayload;
import com.redslovesgames.tideteamjournal.network.OpenTeamRecordsPayload;
import com.redslovesgames.tideteamjournal.network.RecordEventPayload;
import com.redslovesgames.tideteamjournal.network.RecordHoldersPayload;
import com.redslovesgames.tideteamjournal.network.TeamDataPayload;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public final class TideTeamJournalClient implements ClientModInitializer {
   public void onInitializeClient() {
      ClientConfig.load();
      RecordScoreboard.register();
      ClientPlayNetworking.registerGlobalReceiver(RecordHoldersPayload.TYPE, (payload, context) -> ClientRecordHolders.update(payload.tag()));
      ClientPlayNetworking.registerGlobalReceiver(TeamDataPayload.TYPE, (payload, context) -> ClientTeamData.update(payload.tag()));
      ClientPlayNetworking.registerGlobalReceiver(RecordEventPayload.TYPE, (payload, context) -> {
         TeamProgressStore.RecordEvent event = TeamProgressStore.RecordEvent.fromTag(payload.tag());
         if (event != null) {
            RecordScoreboard.show(event);
         }
      });
      ClientPlayNetworking.registerGlobalReceiver(OpenTeamRecordsPayload.TYPE, (payload, context) -> TeamRecordsScreen.open(context.client().currentScreen));
      ClientPlayNetworking.registerGlobalReceiver(
         BobberSettingsPayload.TYPE,
         (payload, context) -> {
            Map<Identifier, BobberBonuses.Bonus> bonuses = new HashMap<>();
            NbtCompound bonusTag = payload.tag().getCompound("bonuses");

            for (String key : bonusTag.getKeys()) {
               Identifier id = Identifier.tryParse(key);
               if (id != null) {
                  NbtCompound value = bonusTag.getCompound(key);
                  bonuses.put(id, new BobberBonuses.Bonus(value.getInt("luck"), value.getInt("speed")));
               }
            }

            BobberBonuses.updateClient(
               payload.tag().getBoolean("enabled"),
               new BobberBonuses.Bonus(payload.tag().getInt("fallback_luck"), payload.tag().getInt("fallback_speed")),
               bonuses
            );
            ClientServerSettings.recordBadgesEnabled = payload.tag().getBoolean("record_badges");
            ClientServerSettings.recordTooltipsEnabled = payload.tag().getBoolean("record_tooltips");
         }
      );
      ItemTooltipCallback.EVENT
         .register(
            (ItemTooltipCallback)(stack, context, type, lines) -> {
               FishData fishData = FishData.get(stack).or(() -> FishData.fromBucket(stack)).orElse(null);
               if (fishData != null) {
                  FishRarity rarity = fishData.profile().rarity();
                  Text rarityName = Text.translatable("journal.rarity." + rarity.getKey()).styled(style -> style.withColor(6650722));
                  Text stars = Text.literal("\u2605".repeat(rarity.getNumStars())).styled(style -> style.withColor(10121284));
                  lines.add(
                     Text.translatable("tooltip.tide_team_journal.rarity", new Object[]{rarityName, stars})
                        .styled(style -> style.withColor(6650722))
                  );
               }

               ClientRecordFishMarkers.Status record = ClientRecordFishMarkers.get(stack);
               if (ClientConfig.get().showRecordTooltips && ClientServerSettings.recordTooltipsEnabled && record.largest()) {
                  lines.add(
                     Text.translatable("tooltip.tide_team_journal.largest_record", new Object[]{TideUtils.getFormattedLength(record.largestSize())})
                        .styled(style -> style.withBold(true).withColor(10121284))
                  );
               }

               if (ClientConfig.get().showRecordTooltips && ClientServerSettings.recordTooltipsEnabled && record.smallest()) {
                  lines.add(
                     Text.translatable("tooltip.tide_team_journal.smallest_record", new Object[]{TideUtils.getFormattedLength(record.smallestSize())})
                        .styled(style -> style.withBold(true).withColor(7757682))
                  );
               }

               BobberBonuses.Bonus bonus = !stack.isEmpty() && stack.isIn(Items.BOBBERS)
                  ? BobberBonuses.forClientId(Registries.ITEM.getId(stack.getItem()))
                  : BobberBonuses.Bonus.NONE;
               if (!bonus.isEmpty()) {
                  lines.add(Text.translatable("tooltip.tide_team_journal.bobber_bonus").styled(style -> style.withColor(14074789)));
                  if (bonus.luck() > 0) {
                     lines.add(
                        Text.translatable("tooltip.tide_team_journal.bobber_luck", new Object[]{bonus.luck()})
                           .styled(style -> style.withColor(10121284))
                     );
                  }

                  if (bonus.lureSpeed() > 0) {
                     lines.add(
                        Text.translatable("tooltip.tide_team_journal.bobber_speed", new Object[]{bonus.lureSpeed()})
                           .styled(style -> style.withColor(8628900))
                     );
                  }
               }
            }
         );
   }
}
