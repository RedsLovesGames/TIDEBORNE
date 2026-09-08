from pathlib import Path

storage = Path('src/main/java/com/redslovesgames/tideborne/satchel/AnglersSatchelStorage.java')
s = storage.read_text()
if 'public static List<SatchelPreset> presets(' not in s:
    s = s.replace('import java.util.TreeSet;\n', 'import java.util.TreeSet;\nimport java.util.UUID;\nimport net.minecraft.registry.RegistryWrapper;\nimport net.minecraft.nbt.NbtElement;\nimport net.minecraft.nbt.NbtList;\nimport net.minecraft.component.DataComponentTypes;\nimport net.minecraft.component.type.NbtComponent;\n')
    marker = '   private static void migrateStoredContents(ItemStack satchel) {'
    methods = '''   public static List<SatchelPreset> presets(ItemStack satchel, RegistryWrapper.WrapperLookup registries) {
      requireSatchel(satchel);
      NbtCompound data = state(satchel).toTag();
      if (!data.contains("physical_presets")) return List.of();
      NbtElement raw = data.get("physical_presets");
      if (!(raw instanceof NbtList list)) throw new IllegalStateException("Malformed physical_presets encoding");
      ArrayList<SatchelPreset> result = new ArrayList<>();
      for (int i = 0; i < list.size(); i++) {
         NbtCompound encoded = list.getCompound(i);
         UUID id = encoded.containsUuid("id") ? encoded.getUuid("id") : UUID.randomUUID();
         String name = encoded.getString("name");
         UUID legacyRod = encoded.containsUuid("legacy_rod") ? encoded.getUuid("legacy_rod") : null;
         NbtList encodedItems = encoded.getList("items", NbtElement.COMPOUND_TYPE);
         ArrayList<ItemStack> items = new ArrayList<>();
         for (int slot = 0; slot < SatchelPreset.SLOTS.size(); slot++) {
            if (slot < encodedItems.size()) items.add(ItemStack.fromNbt(registries, encodedItems.getCompound(slot)).orElse(ItemStack.EMPTY));
            else items.add(ItemStack.EMPTY);
         }
         result.add(new SatchelPreset(id, name, items, legacyRod));
      }
      return List.copyOf(result);
   }

   public static void setPresets(ItemStack satchel, List<SatchelPreset> presets, RegistryWrapper.WrapperLookup registries) {
      requireSatchel(satchel);
      NbtCompound data = state(satchel).toTag();
      NbtList list = new NbtList();
      for (SatchelPreset preset : presets == null ? List.<SatchelPreset>of() : presets) {
         NbtCompound encoded = new NbtCompound();
         encoded.putUuid("id", preset.id());
         encoded.putString("name", preset.name());
         if (preset.legacyRod() != null) encoded.putUuid("legacy_rod", preset.legacyRod());
         NbtList items = new NbtList();
         for (ItemStack stack : preset.items()) items.add(stack.isEmpty() ? new NbtCompound() : stack.encode(registries));
         encoded.put("items", items);
         list.add(encoded);
      }
      data.put("physical_presets", list);
      setState(satchel, SatchelState.fromTag(data));
   }

   public static void migratePresets(ItemStack satchel, net.minecraft.entity.player.PlayerEntity player) {
      requireSatchel(satchel);
      if (!presets(satchel, player.getRegistryManager()).isEmpty()) return;
      ArrayList<SatchelPreset> migrated = new ArrayList<>();
      List<ItemStack> stored = new ArrayList<>(contents(satchel));
      String[] names = {"Trophy Hunter", "Trait Hunter", "Deep Water", "Custom Preset"};
      for (int index = 0; index < 16; index++) {
         Optional<UUID> reference = state(satchel).presetRod(index);
         if (reference.isEmpty()) continue;
         UUID rodId = reference.get();
         ItemStack matched = ItemStack.EMPTY;
         int matchedIndex = -1;
         for (int slot = 0; slot < stored.size(); slot++) {
            NbtComponent custom = stored.get(slot).get(DataComponentTypes.CUSTOM_DATA);
            if (custom == null) continue;
            NbtCompound tag = custom.copyNbt();
            if (tag.containsUuid("tideborne_satchel_rod_reference") && rodId.equals(tag.getUuid("tideborne_satchel_rod_reference"))) {
               matched = stored.get(slot).copy(); matchedIndex = slot; break;
            }
         }
         ArrayList<ItemStack> pockets = new ArrayList<>(java.util.Collections.nCopies(SatchelPreset.SLOTS.size(), ItemStack.EMPTY));
         if (!matched.isEmpty()) { pockets.set(0, matched); stored.remove(matchedIndex); }
         migrated.add(new SatchelPreset(UUID.randomUUID(), index < names.length ? names[index] : "Preset " + (index + 1), pockets, matched.isEmpty() ? rodId : null));
      }
      if (!migrated.isEmpty()) {
         writeContents(satchel, stored);
         setPresets(satchel, migrated, player.getRegistryManager());
      }
   }

'''
    if marker not in s:
        raise RuntimeError('AnglersSatchelStorage insertion marker missing')
    s = s.replace(marker, methods + marker)
    storage.write_text(s)

state = Path('src/main/java/com/redslovesgames/tideborne/satchel/SatchelState.java')
s = state.read_text()
if 'public Optional<UUID> presetRod(' not in s:
    s = s.replace('import java.util.TreeSet;\n', 'import java.util.TreeSet;\nimport java.util.UUID;\nimport java.util.Optional;\n')
    marker = '   public NbtCompound toTag() {'
    methods = '''   public Optional<UUID> presetRod(int index) {
      if (index < 0) return Optional.empty();
      String key = "legacy_preset_rod_" + index;
      return this.data.containsUuid(key) ? Optional.of(this.data.getUuid(key)) : Optional.empty();
   }

   public SatchelState withPresetRod(int index, UUID rodId) {
      if (index < 0) throw new IllegalArgumentException("index must be non-negative");
      NbtCompound copy = this.data.copy();
      String key = "legacy_preset_rod_" + index;
      if (rodId == null) copy.remove(key); else copy.putUuid(key, rodId);
      return new SatchelState(copy);
   }

'''
    if marker not in s:
        raise RuntimeError('SatchelState insertion marker missing')
    s = s.replace(marker, methods + marker)
    state.write_text(s)
