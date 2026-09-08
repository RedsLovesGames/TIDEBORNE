package com.redslovesgames.tideborne.mixin.journal;

import com.redslovesgames.tideborne.journal.StoredFishScoreStorage;
import com.redslovesgames.tideborne.journal.TeamProgressStore;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds canonical FishScore storage to history payloads while preserving the old wire key. */
@Mixin(value = TeamProgressStore.RecordEvent.class, remap = false)
abstract class RecordEventCanonicalScorePayloadMixin {
    @Inject(method = "toTag", at = @At("RETURN"))
    private void tideborne$writeCanonicalEventScore(CallbackInfoReturnable<NbtCompound> callback) {
        TeamProgressStore.RecordEvent event = (TeamProgressStore.RecordEvent) (Object) this;
        int score = TeamProgressStore.tideborneEventFishScoreForWrite(event);
        StoredFishScoreStorage.writeCanonical(callback.getReturnValue(), score);
    }
}
