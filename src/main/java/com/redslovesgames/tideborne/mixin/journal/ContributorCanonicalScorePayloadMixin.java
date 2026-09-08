package com.redslovesgames.tideborne.mixin.journal;

import com.redslovesgames.tideborne.journal.StoredFishScoreStorage;
import com.redslovesgames.tideborne.journal.TeamProgressStore;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds canonical FishScore storage to contributor payloads while preserving the old wire key. */
@Mixin(value = TeamProgressStore.Contributor.class, remap = false)
abstract class ContributorCanonicalScorePayloadMixin {
    @Inject(method = "toTag", at = @At("RETURN"))
    private void tideborne$writeCanonicalContributorScore(CallbackInfoReturnable<NbtCompound> callback) {
        TeamProgressStore.Contributor contributor = (TeamProgressStore.Contributor) (Object) this;
        int score = TeamProgressStore.tideborneContributorFishScore(contributor);
        StoredFishScoreStorage.writeCanonical(callback.getReturnValue(), score);
    }
}
