package com.redslovesgames.tideteamjournal.mixin;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideteamjournal.TeamCanonicalJournalCapture;
import com.redslovesgames.tideteamjournal.TeamProgressStore;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps team journal score/catch capture on the finalized server canonical specimen. */
@Mixin(value = TeamProgressStore.class, remap = false)
abstract class TeamProgressCanonicalJournalMixin {
    @Inject(method = "tideborneFishScore", at = @At("HEAD"), cancellable = true)
    private static void tideborne$canonicalScore(ItemStack stack, CallbackInfoReturnable<Double> callback) {
        SpecimenData specimen = CanonicalSpecimenStorage.read(stack).orElse(null);
        if (specimen != null) {
            callback.setReturnValue(specimen.fishScore().isPresent()
                    ? (double) specimen.fishScore().getAsInt()
                    : -1.0);
        }
    }

    @Inject(method = "tideborneBeginCatch", at = @At("HEAD"))
    private static void tideborne$beginCanonicalJournalCatch(ItemStack stack, CallbackInfo callback) {
        TeamCanonicalJournalCapture.begin(stack);
    }

    @Inject(method = "tideborneClearCatch", at = @At("TAIL"))
    private static void tideborne$clearCanonicalJournalCatch(CallbackInfo callback) {
        TeamCanonicalJournalCapture.clear();
    }
}
