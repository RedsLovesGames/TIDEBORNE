/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.compat.multiplayer;

import com.redslovesgames.tidetraits.discovery.DiscoverySnapshot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

final class SharedDiscoveryNbt {
   static final String TEAM_JOURNAL_KEY = "tide_team_journal";
   static final String TIDE_TRAITS_KEY = "tide_traits";
   static final String VERSION_KEY = "data_version";
   static final String DISCOVERIES_KEY = "discoveries";
   static final String MUTATIONS_KEY = "mutations";
   static final String SIZE_BANDS_KEY = "size_bands";
   static final int CURRENT_VERSION = 1;
   private static final String PRESERVED_TEAM_ROOT_KEY = "PreservedTideTeamJournalValue";
   private static final String PRESERVED_TRAITS_KEY = "PreservedTideTraitsValue";
   private static final String PRESERVED_DISCOVERIES_KEY = "PreservedDiscoveriesValue";
   private static final int MAX_SPECIES = 512;
   private static final int MAX_IDS_PER_KIND = 128;
   private static final int MAX_ID_LENGTH = 256;
   private static final int MAX_KEYS_SCANNED = 4096;

   private SharedDiscoveryNbt() {
   }

   static boolean add(NbtCompound teamExtraData, Identifier canonicalSpeciesId, Identifier mutationId, Identifier sizeBandId) {
      if (teamExtraData != null && valid(canonicalSpeciesId) && (valid(mutationId) || valid(sizeBandId))) {
         SharedDiscoveryNbt.Change change = new SharedDiscoveryNbt.Change();
         NbtCompound journalRoot = childCompound(teamExtraData, "tide_team_journal", "PreservedTideTeamJournalValue", change);
         NbtCompound traits = childCompound(journalRoot, "tide_traits", "PreservedTideTraitsValue", change);
         if (!traits.contains("data_version", 3) || traits.getInt("data_version") < 1) {
            traits.putInt("data_version", 1);
            change.mark();
         }

         NbtCompound discoveries = childCompound(traits, "discoveries", "PreservedDiscoveriesValue", change);
         String speciesKey = canonicalSpeciesId.toString();
         NbtElement existingSpecies = discoveries.get(speciesKey);
         if (!(existingSpecies instanceof NbtCompound) && canonicalSpeciesCount(discoveries) >= 512) {
            return change.changed;
         }

         NbtCompound species = childCompound(discoveries, speciesKey, "PreservedSpeciesValue", change);
         if (valid(mutationId)) {
            addId(species, "mutations", mutationId, change);
         }

         if (valid(sizeBandId)) {
            addId(species, "size_bands", sizeBandId, change);
         }

         return change.changed;
      } else {
         return false;
      }
   }

   static DiscoverySnapshot snapshot(NbtCompound teamExtraData) {
      if (teamExtraData == null) {
         return DiscoverySnapshot.empty();
      }

      if (teamExtraData.get("tide_team_journal") instanceof NbtCompound journalRoot) {
         if (journalRoot.get("tide_traits") instanceof NbtCompound traits) {
            return traits.get("discoveries") instanceof NbtCompound discoveries ? decodeDiscoveries(discoveries) : DiscoverySnapshot.empty();
         } else {
            return DiscoverySnapshot.empty();
         }
      } else {
         return DiscoverySnapshot.empty();
      }
   }

   static NbtCompound encodePacket(SharedDiscoverySnapshot snapshot) {
      NbtCompound packet = new NbtCompound();
      packet.putString("availability", snapshot.availability().serializedName());
      if (snapshot.isAvailable()) {
         packet.put("discoveries", encodeDiscoveries(snapshot.discoveries()));
      }

      return packet;
   }

   static SharedDiscoverySnapshot decodePacket(NbtCompound packet) {
      if (packet == null) {
         return SharedDiscoverySnapshot.unknown();
      }

      SharedDiscoveryAvailability availability = SharedDiscoveryAvailability.fromSerializedName(packet.getString("availability"));
      if (!availability.isAvailable()) {
         return SharedDiscoverySnapshot.unavailable(availability);
      }

      DiscoverySnapshot discoveries = packet.get("discoveries") instanceof NbtCompound compound
         ? decodeDiscoveries(compound)
         : DiscoverySnapshot.empty();
      return SharedDiscoverySnapshot.available(discoveries);
   }

   private static DiscoverySnapshot decodeDiscoveries(NbtCompound discoveries) {
      List<String> keys = new ArrayList<>(discoveries.getKeys());
      keys.sort(String::compareTo);
      Map<Identifier, DiscoverySnapshot.SpeciesDiscoveries> result = new LinkedHashMap<>();
      int scanned = 0;

      for (String rawSpecies : keys) {
         if (++scanned > 4096 || result.size() >= 512) {
            break;
         }

         Identifier speciesId = tryParse(rawSpecies);
         if (speciesId != null && discoveries.get(rawSpecies) instanceof NbtCompound species) {
            Set<Identifier> mutations = readIds(species.get("mutations"));
            Set<Identifier> sizeBands = readIds(species.get("size_bands"));
            if (!mutations.isEmpty() || !sizeBands.isEmpty()) {
               result.put(speciesId, new DiscoverySnapshot.SpeciesDiscoveries(mutations, sizeBands));
            }
         }
      }

      return result.isEmpty() ? DiscoverySnapshot.empty() : new DiscoverySnapshot(result);
   }

   private static NbtCompound encodeDiscoveries(DiscoverySnapshot snapshot) {
      NbtCompound discoveries = new NbtCompound();
      snapshot.species().entrySet().stream().sorted(Entry.comparingByKey(Comparator.comparing(Identifier::toString))).limit(512L).forEach(entry -> {
         NbtCompound species = new NbtCompound();
         species.put("mutations", writeIds(entry.getValue().mutations()));
         species.put("size_bands", writeIds(entry.getValue().sizeBands()));
         discoveries.put(entry.getKey().toString(), species);
      });
      return discoveries;
   }

   private static void addId(NbtCompound species, String key, Identifier id, SharedDiscoveryNbt.Change change) {
      NbtList values = stringList(species, key, change);
      int validIds = 0;

      for (int index = 0; index < values.size(); index++) {
         Identifier existing = tryParse(values.getString(index));
         if (existing != null) {
            if (existing.equals(id)) {
               return;
            }

            validIds++;
         }
      }

      if (validIds < 128) {
         values.add(NbtString.of(id.toString()));
         change.mark();
      }
   }

   private static Set<Identifier> readIds(NbtElement stored) {
      if (stored instanceof NbtList list && (list.isEmpty() || list.getHeldType() == 8)) {
         Set<Identifier> result = new LinkedHashSet<>();

         for (int index = 0; index < list.size() && result.size() < 128; index++) {
            Identifier id = tryParse(list.getString(index));
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

   private static NbtList stringList(NbtCompound parent, String key, SharedDiscoveryNbt.Change change) {
      NbtElement stored = parent.get(key);
      if (!(stored instanceof NbtList list && (list.isEmpty() || list.getHeldType() == 8))) {
         if (stored != null) {
            preserve(parent, "Preserved" + capitalize(key) + "Value", stored);
         }

         NbtList result = new NbtList();
         parent.put(key, result);
         change.mark();
         return result;
      } else {
         return list;
      }
   }

   private static NbtCompound childCompound(NbtCompound parent, String key, String preservedKey, SharedDiscoveryNbt.Change change) {
      NbtElement stored = parent.get(key);
      if (stored instanceof NbtCompound compound) {
         return compound;
      } else {
         if (stored != null) {
            preserve(parent, preservedKey, stored);
         }

         NbtCompound result = new NbtCompound();
         parent.put(key, result);
         change.mark();
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

   private static String capitalize(String value) {
      return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
   }

   private static boolean valid(Identifier id) {
      return id != null && id.toString().length() <= 256;
   }

   private static Identifier tryParse(String raw) {
      return raw != null && !raw.isEmpty() && raw.length() <= 256 ? Identifier.tryParse(raw) : null;
   }

   private static int canonicalSpeciesCount(NbtCompound discoveries) {
      int count = 0;
      int scanned = 0;

      for (String key : discoveries.getKeys()) {
         if (++scanned > 4096) {
            return 512;
         }

         if (tryParse(key) != null && discoveries.get(key) instanceof NbtCompound) {
            if (++count >= 512) {
               return count;
            }
         }
      }

      return count;
   }

   private static final class Change {
      private boolean changed;

      private void mark() {
         this.changed = true;
      }
   }
}
