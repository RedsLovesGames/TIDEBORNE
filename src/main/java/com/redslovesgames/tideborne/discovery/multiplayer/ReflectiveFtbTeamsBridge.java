/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.discovery.multiplayer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

final class ReflectiveFtbTeamsBridge {
   private static final String API_CLASS = "dev.ftb.mods.ftbteams.api.FTBTeamsAPI";
   private static final String MANAGER_CLASS = "dev.ftb.mods.ftbteams.api.TeamManager";
   private static final String TEAM_CLASS = "dev.ftb.mods.ftbteams.api.Team";
   private final MethodHandle api;
   private final MethodHandle getManager;
   private final MethodHandle getTeamForPlayer;
   private final MethodHandle getExtraData;
   private final MethodHandle markDirty;
   private final MethodHandle getOnlineMembers;

   private ReflectiveFtbTeamsBridge(
      MethodHandle api,
      MethodHandle getManager,
      MethodHandle getTeamForPlayer,
      MethodHandle getExtraData,
      MethodHandle markDirty,
      MethodHandle getOnlineMembers
   ) {
      this.api = api;
      this.getManager = getManager;
      this.getTeamForPlayer = getTeamForPlayer;
      this.getExtraData = getExtraData;
      this.markDirty = markDirty;
      this.getOnlineMembers = getOnlineMembers;
   }

   static ReflectiveFtbTeamsBridge create() {
      try {
         ClassLoader loader = ReflectiveFtbTeamsBridge.class.getClassLoader();
         Class<?> apiClass = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI", false, loader);
         Class<?> managerClass = Class.forName("dev.ftb.mods.ftbteams.api.TeamManager", false, loader);
         Class<?> teamClass = Class.forName("dev.ftb.mods.ftbteams.api.Team", false, loader);
         Lookup lookup = MethodHandles.publicLookup();
         Method apiMethod = method(apiClass, "api", 0, true);
         Method getManagerMethod = method(apiMethod.getReturnType(), "getManager", 0, false);
         Method effectiveTeamMethod = methodAccepting(managerClass, "getTeamForPlayer", ServerPlayerEntity.class);
         Method extraDataMethod = method(teamClass, "getExtraData", 0, false);
         Method markDirtyMethod = method(teamClass, "markDirty", 0, false);
         Method onlineMembersMethod = optionalMethod(teamClass, "getOnlineMembers", 0);
         return new ReflectiveFtbTeamsBridge(
            lookup.unreflect(apiMethod),
            lookup.unreflect(getManagerMethod),
            lookup.unreflect(effectiveTeamMethod),
            lookup.unreflect(extraDataMethod),
            lookup.unreflect(markDirtyMethod),
            onlineMembersMethod == null ? null : lookup.unreflect(onlineMembersMethod)
         );
      } catch (ReflectiveOperationException exception) {
         throw new IllegalStateException("FTB Teams API shape does not match the inspected 2101 API", exception);
      }
   }

   Optional<Object> effectiveTeam(ServerPlayerEntity player) {
      Object apiInstance = invoke(this.api);
      Object manager = invoke(this.getManager, apiInstance);
      if (invoke(this.getTeamForPlayer, manager, player) instanceof Optional<?> optional) {
         return optional.map(team -> team);
      } else {
         throw new IllegalStateException("FTB TeamManager.getTeamForPlayer did not return Optional");
      }
   }

   NbtCompound extraData(Object team) {
      if (invoke(this.getExtraData, team) instanceof NbtCompound tag) {
         return tag;
      } else {
         throw new IllegalStateException("FTB Team.getExtraData did not return CompoundTag");
      }
   }

   void markDirty(Object team) {
      invoke(this.markDirty, team);
   }

   Collection<?> onlineMembers(Object team) {
      if (this.getOnlineMembers == null) {
         return List.of();
      } else {
         return invoke(this.getOnlineMembers, team) instanceof Collection<?> collection ? collection : List.of();
      }
   }

   private static Method method(Class<?> owner, String name, int parameters, boolean requireStatic) throws NoSuchMethodException {
      Method method = optionalMethod(owner, name, parameters);
      if (method != null && Modifier.isStatic(method.getModifiers()) == requireStatic) {
         return method;
      } else {
         throw new NoSuchMethodException(owner.getName() + "." + name + "/" + parameters);
      }
   }

   private static Method optionalMethod(Class<?> owner, String name, int parameters) {
      for (Method method : owner.getMethods()) {
         if (method.getName().equals(name) && method.getParameterCount() == parameters) {
            return method;
         }
      }

      return null;
   }

   private static Method methodAccepting(Class<?> owner, String name, Class<?> argument) throws NoSuchMethodException {
      for (Method method : owner.getMethods()) {
         if (method.getName().equals(name)
            && !Modifier.isStatic(method.getModifiers())
            && method.getParameterCount() == 1
            && method.getParameterTypes()[0].isAssignableFrom(argument)) {
            return method;
         }
      }

      throw new NoSuchMethodException(owner.getName() + "." + name + "(" + argument.getName() + ")");
   }

   private static Object invoke(MethodHandle handle, Object... arguments) {
      try {
         return handle.invokeWithArguments(arguments);
      } catch (RuntimeException | LinkageError failure) {
         throw failure;
      } catch (Error fatal) {
         throw fatal;
      } catch (Throwable failure) {
         throw new IllegalStateException("Optional FTB Teams invocation failed", failure);
      }
   }
}
