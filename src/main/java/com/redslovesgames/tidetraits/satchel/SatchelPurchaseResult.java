/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

public record SatchelPurchaseResult(SatchelPurchaseResult.Status status, int xpCost) {
   public boolean succeeded() {
      return this.status == SatchelPurchaseResult.Status.SUCCESS;
   }

   public enum Status {
      SUCCESS,
      INVALID_REQUEST,
      INVALID_SATCHEL,
      ALREADY_OWNED,
      PREREQUISITE_MISSING,
      PREVIOUS_LEVEL_REQUIRED,
      INSUFFICIENT_XP,
      COMMIT_FAILED;
   }
}
