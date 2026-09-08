package com.redslovesgames.tideborne.mixin.journal;

import com.redslovesgames.tideborne.journal.JournalSpecimenNetworkCodec;
import com.redslovesgames.tideborne.journal.RecordHolderStore;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds canonical display-only specimen data to the existing journal metadata sync. */
@Mixin(value = RecordHolderStore.class, remap = false)
abstract class RecordHolderNetworkProjectionMixin {
    @Inject(method = "attachForClient", at = @At("RETURN"))
    private static void tideborne$attachCanonicalDisplayData(
            NbtCompound journal,
            NbtCompound teamRoot,
            CallbackInfoReturnable<NbtCompound> callback
    ) {
        JournalSpecimenNetworkCodec.attachDisplayData(callback.getReturnValue(), teamRoot);
    }
}
