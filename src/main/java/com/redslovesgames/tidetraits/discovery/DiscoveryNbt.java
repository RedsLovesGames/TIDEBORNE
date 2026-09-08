/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.discovery;

import com.li64.tide.data.TidePlayer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

final class DiscoveryNbt {
   static final String PLAYER_DATA_KEY = "TideTraitsPlayerData";
   static final int CURRENT_VERSION = 1;
   private static final String VERSION_KEY = "DataVersion";
   private static final String DISCOVERIES_KEY = "Discoveries";
   private static final String MUTATIONS_KEY = "Mutations";
   private static final String SIZE_BANDS_KEY = "SizeBands";
   private static final int MAX_SPECIES = 512;
   private static final int MAX_IDS_PER_KIND = 128;
   private static final int MAX_ID_LENGTH = 256;
   private static final int MAX_KEYS_SCANNED = 4096;

   private DiscoveryNbt() {
   }

   static DiscoverySnapshot snapshot(PlayerEntity player) {
      NbtCompound tideRoot = tideRoot(player, false);
      if (tideRoot == null) {
         return DiscoverySnapshot.empty();
      } else {
         return tideRoot.get("TideTraitsPlayerData") instanceof NbtCompound playerData ? decodeSnapshot(playerData) : DiscoverySnapshot.empty();
      }
   }

   static boolean addMutation(PlayerEntity player, Identifier speciesId, Identifier mutationId) {
      return add(player, speciesId, mutationId, "Mutations");
   }

   static boolean addSizeBand(PlayerEntity player, Identifier speciesId, Identifier sizeBandId) {
      return add(player, speciesId, sizeBandId, "SizeBands");
   }

   static DiscoverySnapshot decodeSnapshot(NbtCompound playerData) {
      if (!(playerData.get("Discoveries") instanceof NbtCompound discoveries)) {
         return DiscoverySnapshot.empty();
      } else {
         List<String> speciesKeys = new ArrayList<>(discoveries.getKeys());
         speciesKeys.sort(String::compareTo);
         LinkedHashMap result = new LinkedHashMap();
         int scanned = 0;

         for (String rawSpecies : speciesKeys) {
            if (++scanned > 4096 || result.size() >= 512) {
               break;
            }

            Identifier speciesId = tryParse(rawSpecies);
            if (speciesId != null && discoveries.get(rawSpecies) instanceof NbtCompound speciesData) {
               Set<Identifier> mutations = readIds(speciesData.get("Mutations"));
               Set<Identifier> sizeBands = readIds(speciesData.get("SizeBands"));
               if (!mutations.isEmpty() || !sizeBands.isEmpty()) {
                  result.put(speciesId, new DiscoverySnapshot.SpeciesDiscoveries(mutations, sizeBands));
               }
            }
         }

         return result.isEmpty() ? DiscoverySnapshot.empty() : new DiscoverySnapshot(result);
      }
   }

   static NbtCompound encodeSnapshot(DiscoverySnapshot snapshot) {
      NbtCompound playerData = new NbtCompound();
      playerData.putInt("DataVersion", 1);
      NbtCompound discoveries = new NbtCompound();
      snapshot.species().entrySet().stream().sorted(Entry.comparingByKey(Comparator.comparing(Identifier::toString))).limit(512L).forEach(entry -> {
         NbtCompound speciesData = new NbtCompound();
         speciesData.put("Mutations", writeIds(entry.getValue().mutations()));
         speciesData.put("SizeBands", writeIds(entry.getValue().sizeBands()));
         discoveries.put(entry.getKey().toString(), speciesData);
      });
      playerData.put("Discoveries", discoveries);
      return playerData;
   }

   private static boolean add(PlayerEntity player, Identifier speciesId, Identifier discoveryId, String listKey) {
      if (speciesId != null && discoveryId != null && speciesId.toString().length() <= 256 && discoveryId.toString().length() <= 256) {
         TidePlayer tidePlayer = asTidePlayer(player);
         NbtCompound tideRoot = tideRoot(player, true);
         NbtCompound playerData = playerData(tideRoot);
         updateVersion(playerData);
         NbtCompound discoveries = childCompound(playerData, "Discoveries", "PreservedDiscoveriesValue");
         String speciesKey = speciesId.toString();
         NbtCompound speciesData = childCompound(discoveries, speciesKey, "PreservedSpeciesValue");
         NbtList ids = stringList(speciesData, listKey);
         int validIds = 0;

         for (int i = 0; i < ids.size(); i++) {
            Identifier existing = tryParse(ids.getString(i));
            if (existing != null) {
               if (existing.equals(discoveryId)) {
                  return false;
               }

               validIds++;
            }
         }

         if (validIds >= 128) {
            return false;
         }

         ids.add(NbtString.of(discoveryId.toString()));
         speciesData.put(listKey, ids);
         discoveries.put(speciesKey, speciesData);
         playerData.put("Discoveries", discoveries);
         tideRoot.put("TideTraitsPlayerData", playerData);
         tidePlayer.tide$setTidePlayerData(tideRoot);
         return true;
      } else {
         return false;
      }
   }

   private static Set<Identifier> readIds(NbtElement stored) {
      if (stored instanceof NbtList list && list.getHeldType() == 8) {
         Set<Identifier> result = new LinkedHashSet<>();

         for (int i = 0; i < list.size() && result.size() < 128; i++) {
            Identifier id = tryParse(list.getString(i));
            if (id != null) {
               result.add(id);
            }
         }

         return result;
      } else {
         return Set.of();
      }
   }

   private static NbtList writeIds(Set<Identifier> ids) {
      NbtList result = new NbtList();
      ids.stream().sorted(Comparator.comparing(Identifier::toString)).limit(128L).forEach(id -> result.add(NbtString.of(id.toString())));
      return result;
   }

   private static NbtList stringList(NbtCompound parent, String key) {
      NbtElement stored = parent.get(key);
      if (!(stored instanceof NbtList list && (list.isEmpty() || list.getHeldType() == 8))) {
         if (stored != null) {
            preserve(parent, "Preserved" + key + "Value", stored);
         }

         NbtList result = new NbtList();
         parent.put(key, result);
         return result;
      } else {
         return list;
      }
   }

   private static NbtCompound childCompound(NbtCompound parent, String key, String preservedKey) {
      NbtElement stored = parent.get(key);
      if (stored instanceof NbtCompound compound) {
         return compound;
      } else {
         if (stored != null) {
            preserve(parent, preservedKey, stored);
         }

         NbtCompound result = new NbtCompound();
         parent.put(key, result);
         return result;
      }
   }

   private static NbtCompound playerData(NbtCompound tideRoot) {
      NbtElement stored = tideRoot.get("TideTraitsPlayerData");
      if (stored instanceof NbtCompound compound) {
         return compound;
      } else {
         NbtCompound result = new NbtCompound();
         if (stored != null) {
            preserve(result, "PreservedRootValue", stored);
         }

         tideRoot.put("TideTraitsPlayerData", result);
         return result;
      }
   }

   private static void preserve(NbtCompound parent, String baseKey, NbtElement value) {
      String key = baseKey;
      int suffix = 2;

      while (parent.contains(key)) {
         key = baseKey + suffix++;
      }

      parent.put(key, value.copy());
   }

   private static void updateVersion(NbtCompound playerData) {
      if (!playerData.contains("DataVersion", 3) || playerData.getInt("DataVersion") < 1) {
         playerData.putInt("DataVersion", 1);
      }
   }

   private static NbtCompound tideRoot(PlayerEntity player, boolean create) {
      TidePlayer tidePlayer = asTidePlayer(player);
      NbtCompound root = tidePlayer.tide$getTidePlayerData();
      if (root == null && create) {
         root = new NbtCompound();
         tidePlayer.tide$setTidePlayerData(root);
      }

      return root;
   }

   private static TidePlayer asTidePlayer(PlayerEntity player) {
      if (player == null) {
         throw new IllegalArgumentException("player cannot be null");
      } else if (player instanceof TidePlayer tidePlayer) {
         return tidePlayer;
      } else {
         throw new IllegalStateException("TidePlayer mixin is not applied to " + player.getClass().getName());
      }
   }

   private static Identifier tryParse(String raw) {
      return raw != null && !raw.isEmpty() && raw.length() <= 256 ? Identifier.tryParse(raw) : null;
   }
}
