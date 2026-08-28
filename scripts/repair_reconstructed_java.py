#!/usr/bin/env python3
"""Normalize mechanical Vineflower/Yarn reconstruction artifacts.

This script is intentionally narrow. It repairs source forms that are valid bytecode
patterns but invalid or poorly inferred Java after decompilation. Gameplay behavior is
not changed here.
"""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src" / "main" / "java"

SELF_CAST_TARGETS = (
    "Screen",
    "DrawContext",
    "TidePlayerData",
    "TideFishingHook",
    "FishData",
    "ItemStack",
    "FishDisplayBlockEntity",
    "MobEntity",
)


def replace_between(text: str, start: str, end: str, replacement: str) -> str:
    """Replace one decompiler-damaged method block using stable declaration markers."""
    begin = text.find(start)
    if begin < 0:
        return text
    finish = text.find(end, begin)
    if finish < 0:
        raise RuntimeError(f"Found repair start marker but not end marker: {start!r}")
    return text[:begin] + replacement + text[finish:]


def rewrite(path: Path, text: str) -> str:
    # Yarn 1.21.1 RegistryEntry<T>.value() was emitted by the decompiler as the
    # Kotlin/JVM component-style intermediary name comp_349().
    text = text.replace(".comp_349()", ".value()")

    # Mixin source classes are intentionally not Java subclasses of their targets.
    # The standard source-safe cast is (Target)(Object)this.
    if "/mixin/" in path.as_posix():
        for target in SELF_CAST_TARGETS:
            text = text.replace(f"({target})this", f"({target})(Object)this")

    if path.name == "TideTraitsComponents.java":
        text = text.replace("ComponentType.builder().codec(Codec.STRING)", "ComponentType.<String>builder().codec(Codec.STRING)")
        text = text.replace("ComponentType.builder().codec(Codec.LONG)", "ComponentType.<Long>builder().codec(Codec.LONG)")
        text = text.replace("ComponentType.builder().codec(Codec.DOUBLE)", "ComponentType.<Double>builder().codec(Codec.DOUBLE)")
        text = text.replace("ComponentType.builder().codec(Codec.BOOL)", "ComponentType.<Boolean>builder().codec(Codec.BOOL)")

    if path.name == "SatchelRegistration.java":
        text = text.replace(
            "ComponentType.builder().codec(NbtCompound.CODEC)",
            "ComponentType.<NbtCompound>builder().codec(NbtCompound.CODEC)",
        )

    if path.name == "TeamJournalService.java":
        text = text.replace(
            "         List events = TeamProgressStore.recordCatch(\n",
            "         List<TeamProgressStore.RecordEvent> events = TeamProgressStore.recordCatch(\n",
        )

    if path.name == "TideboundEntities.java":
        text = text.replace(
            "Builder.create(ChumProjectileEntity::new, SpawnGroup.MISC)",
            "Builder.<ChumProjectileEntity>create(ChumProjectileEntity::new, SpawnGroup.MISC)",
        )

    if path.name == "AnglingTableLeaderSupport.java":
        text = text.replace(
            "var2.invoke(var1, 4, 110, 56, SteelLeaderAttachment::isSteelLeaderStack);",
            "var2.invoke(var1, 4, 110, 56, (Predicate<Object>)SteelLeaderAttachment::isSteelLeaderStack);",
        )

    if path.name == "TideboundCompatibility.java":
        replacement = '''   private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("tideborne_internal_fishing")
         .executes(context -> status(context.getSource()))
         .then(CommandManager.literal("status").executes(context -> status(context.getSource())))
         .then(
            CommandManager.literal("reload")
               .requires(source -> source.hasPermissionLevel(2))
               .executes(context -> {
                  TideboundConfig.Result result = TideboundConfig.reloadBalance();
                  if (!result.success()) {
                     context.getSource().sendError(Text.literal(result.message()));
                     return 0;
                  }
                  context.getSource().getServer().getPlayerManager().getPlayerList().forEach(TideboundCompatibility::syncSettings);
                  context.getSource().sendFeedback(() -> Text.translatable("command.tidebound_compatibility.reload"), true);
                  return 1;
               })
         );
      dispatcher.register(root);
   }

'''
        text = replace_between(
            text,
            "   private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {",
            "   private static int status(ServerCommandSource source) {",
            replacement,
        )

    if path.name == "TideborneCommands.java":
        replacement = '''   private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      CommandNode<ServerCommandSource> traits = dispatcher.getRoot().getChild("tideborne_internal_traits");
      CommandNode<ServerCommandSource> journal = dispatcher.getRoot().getChild("tideborne_internal_team");
      CommandNode<ServerCommandSource> fishing = dispatcher.getRoot().getChild("tideborne_internal_fishing");

      LiteralArgumentBuilder<ServerCommandSource> root = LiteralArgumentBuilder.<ServerCommandSource>literal("tideborne")
         .executes(context -> help(context.getSource()))
         .then(LiteralArgumentBuilder.<ServerCommandSource>literal("help").executes(context -> help(context.getSource())))
         .then(LiteralArgumentBuilder.<ServerCommandSource>literal("status").executes(context -> status(context.getSource())))
         .then(
            LiteralArgumentBuilder.<ServerCommandSource>literal("reload")
               .requires(source -> source.hasPermissionLevel(2))
               .executes(context -> reload(context.getSource()))
         )
         .then(
            LiteralArgumentBuilder.<ServerCommandSource>literal("migrate")
               .then(LiteralArgumentBuilder.<ServerCommandSource>literal("status").executes(context -> migrationStatus(context.getSource())))
         )
         .then(
            LiteralArgumentBuilder.<ServerCommandSource>literal("debug")
               .then(LiteralArgumentBuilder.<ServerCommandSource>literal("backend").executes(context -> debugBackend(context.getSource())))
         )
         .then(
            LiteralArgumentBuilder.<ServerCommandSource>literal("badges")
               .then(LiteralArgumentBuilder.<ServerCommandSource>literal("backfillhistory").executes(HistoryBadgeCommand.INSTANCE))
         );

      redirect(root, "traits", traits);
      redirect(root, "journal", journal);
      redirect(root, "team", journal);
      redirect(root, "fishing", fishing);
      dispatcher.register(root);
      alias(dispatcher, "tidetraits", traits);
      alias(dispatcher, "tideteamjournal", journal);
      alias(dispatcher, "tideboundcompat", fishing);
   }

   private static void redirect(LiteralArgumentBuilder<ServerCommandSource> root, String name, CommandNode<ServerCommandSource> target) {
      if (target != null) {
         root.then(LiteralArgumentBuilder.<ServerCommandSource>literal(name).redirect(target));
      }
   }

   private static void alias(CommandDispatcher<ServerCommandSource> dispatcher, String name, CommandNode<ServerCommandSource> target) {
      if (target != null) {
         dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal(name).redirect(target));
      }
   }

'''
        text = replace_between(
            text,
            "   private static void register(CommandDispatcher<ServerCommandSource> var0) {",
            "   private static int help(ServerCommandSource var0) {",
            replacement,
        )

    if path.name == "TideTraitsCommands.java":
        replacement = '''   public static synchronized void init() {
      if (!initialized) {
         CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
               CommandManager.literal(ROOT)
                  .requires(source -> source.hasPermissionLevel(2))
                  .then(CommandManager.literal("inspect").executes(context -> inspect(context.getSource())))
                  .then(
                     CommandManager.literal("setmutation")
                        .then(
                           CommandManager.argument("id", StringArgumentType.word())
                              .executes(context -> setMutation(context.getSource(), StringArgumentType.getString(context, "id")))
                        )
                  )
                  .then(CommandManager.literal("clearmutation").executes(context -> clearMutation(context.getSource())))
                  .then(
                     CommandManager.literal("percentile")
                        .then(
                           CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                              .executes(context -> setPercentile(context.getSource(), DoubleArgumentType.getDouble(context, "value")))
                        )
                  )
                  .then(CommandManager.literal("dumpfish").executes(context -> dumpFish(context.getSource())))
            )
         );
         initialized = true;
      }
   }

'''
        text = replace_between(
            text,
            "   public static synchronized void init() {",
            "   private static int inspect(ServerCommandSource source) throws CommandSyntaxException {",
            replacement,
        )

    return text


def main() -> int:
    changed = 0
    for path in sorted(JAVA.rglob("*.java")):
        original = path.read_text(encoding="utf-8")
        updated = rewrite(path, original)
        if updated != original:
            path.write_text(updated, encoding="utf-8", newline="\n")
            print(f"repaired {path.relative_to(ROOT)}")
            changed += 1

    # Vineflower emitted an enum switch helper that is not referenced by the actual
    # reconstructed ItemRenderer mixin and cannot compile as standalone Java source.
    synthetic = JAVA / "com/redslovesgames/tidetraits/mixin/client/ItemRendererMutationTintMixin$1.java"
    if synthetic.exists():
        synthetic.unlink()
        print(f"removed {synthetic.relative_to(ROOT)}")
        changed += 1

    print(f"mechanical reconstruction repairs: {changed}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
