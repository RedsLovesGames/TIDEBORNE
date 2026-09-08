/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import java.util.Objects;
import java.util.function.UnaryOperator;

public final class AtomicXpTransaction {
   private AtomicXpTransaction() {
   }

   public static <S> AtomicXpTransaction.Result apply(
      AtomicXpTransaction.StateStore<S> store, AtomicXpTransaction.ExperienceAccount experience, int cost, UnaryOperator<S> upgrade
   ) {
      Objects.requireNonNull(store, "store");
      Objects.requireNonNull(experience, "experience");
      Objects.requireNonNull(upgrade, "upgrade");
      if (cost < 0) {
         return AtomicXpTransaction.Result.INVALID_COST;
      }

      int originalPoints = experience.points();
      if (originalPoints < cost) {
         return AtomicXpTransaction.Result.INSUFFICIENT_XP;
      }

      S before = store.get();

      S after;
      try {
         after = Objects.requireNonNull(upgrade.apply(before), "upgrade returned null");
      } catch (RuntimeException exception) {
         return AtomicXpTransaction.Result.COMMIT_FAILED;
      }

      if (Objects.equals(before, after)) {
         return AtomicXpTransaction.Result.NO_CHANGE;
      }

      try {
         store.set(after);
      } catch (RuntimeException exception) {
         return AtomicXpTransaction.Result.COMMIT_FAILED;
      }

      boolean deducted;
      try {
         deducted = experience.deductExactly(cost) && experience.points() == originalPoints - cost;
      } catch (RuntimeException exception) {
         deducted = false;
      }

      if (deducted) {
         return AtomicXpTransaction.Result.SUCCESS;
      }

      try {
         experience.restorePoints(originalPoints);
      } catch (RuntimeException var10) {
      }

      try {
         store.set(before);
      } catch (RuntimeException ignored) {
         return AtomicXpTransaction.Result.ROLLBACK_FAILED;
      }

      return AtomicXpTransaction.Result.DEDUCTION_FAILED;
   }

   public interface ExperienceAccount {
      int points();

      boolean deductExactly(int var1);

      void restorePoints(int var1);
   }

   public enum Result {
      SUCCESS,
      INVALID_COST,
      INSUFFICIENT_XP,
      NO_CHANGE,
      COMMIT_FAILED,
      DEDUCTION_FAILED,
      ROLLBACK_FAILED;
   }

   public interface StateStore<S> {
      S get();

      void set(S var1);
   }
}
