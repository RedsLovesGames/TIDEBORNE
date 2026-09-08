package com.redslovesgames.tideteamjournal.mixin;

import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenStore;
import com.redslovesgames.tideteamjournal.RecordHolderStore;
import com.redslovesgames.tideteamjournal.TeamCanonicalJournalCapture;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds canonical specimen sidecars without changing the existing team record-holder schema. */
@Mixin(value = RecordHolderStore.class, remap = false)
abstract class RecordHolderCanonicalJournalMixin {
    @Inject(method = "updateAfterCatch", at = @At("HEAD"))
    private static void tideborne$migrateLegacyTeamJournalRecords(
            NbtCompound root,
            TidePlayerData before,
            TidePlayerData after,
            UUID playerId,
            String playerName,
            CallbackInfoReturnable<Boolean> callback
    ) {
        JournalSpecimenStore.migrateLegacyJournal(root);
    }

    @Inject(method = "updateAfterCatch", at = @At("TAIL"))
    private static void tideborne$captureCanonicalJournalSpecimen(
            NbtCompound root,
            TidePlayerData before,
            TidePlayerData after,
            UUID playerId,
            String playerName,
            CallbackInfoReturnable<Boolean> callback
    ) {
        TeamCanonicalJournalCapture.capture(root, before);
    }
}
