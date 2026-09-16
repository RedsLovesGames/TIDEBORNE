package com.redslovesgames.tideborne.mixin.satchel.client;

import com.redslovesgames.tideborne.satchel.client.AnglersSatchelScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Keeps Records scores and its scrollbar inside the journal paper at every supported UI scale. */
@Mixin(value = AnglersSatchelScreen.class, remap = false)
abstract class AnglersSatchelRecordsLayoutMixin {
   @SuppressWarnings("PMD.UnusedPrivateMethod")
   @ModifyConstant(method = "renderRecords", constant = @Constant(intValue = 170))
   private int tideborne$insetRecordScoreEdge(int original) {
      return 156;
   }

   @SuppressWarnings("PMD.UnusedPrivateMethod")
   @ModifyConstant(method = "renderRecordScrollbar", constant = @Constant(intValue = 386))
   private int tideborne$insetRecordScrollbar(int original) {
      return 376;
   }
}
