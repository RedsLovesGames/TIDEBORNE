package com.redslovesgames.tideborne.command;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Clickable chat presentation for the unified Tideborne command surface. */
public final class TideborneCommandUi {
    private static final String RULE = "════════════════════════════════════════";

    private TideborneCommandUi() {
    }

    public static int showRoot(ServerCommandSource source) {
        send(source, title("══════════════ TIDEBORNE ══════════════"));
        send(source, Text.empty());
        send(source, button("OPEN TEAM JOURNAL", "/tideborne journal", ClickEvent.Action.RUN_COMMAND,
                Formatting.AQUA, "View your team's catches, records, history and rankings"));
        send(source, Text.empty());
        send(source, Text.literal("Fishing System 2.0                  ").formatted(Formatting.GRAY)
                .append(Text.literal("✓ Active").formatted(Formatting.GREEN)));
        send(source, button("Status", "/tideborne status", ClickEvent.Action.RUN_COMMAND,
                Formatting.AQUA, "View Tideborne server and Fishing System status"));
        if (source.hasPermissionLevel(2)) {
            send(source, Text.empty());
            send(source, Text.literal("Administration").formatted(Formatting.GOLD, Formatting.BOLD));
            MutableText admin = button("Reload Config", "/tideborne reload", ClickEvent.Action.RUN_COMMAND,
                    Formatting.GREEN, "Reload Tideborne server configuration");
            admin.append(Text.literal("    "));
            admin.append(button("Debug Tools", "/tideborne debug", ClickEvent.Action.RUN_COMMAND,
                    Formatting.GOLD, "Open Fishing System 2.0 developer tools"));
            send(source, admin);
        }
        send(source, Text.literal(RULE).formatted(Formatting.DARK_GRAY));
        return 1;
    }

    public static int showDebug(ServerCommandSource source) {
        send(source, title("═══════════ TIDEBORNE DEBUG ═══════════"));
        send(source, Text.empty());
        send(source, Text.literal("Fishing").formatted(Formatting.GRAY));
        send(source, button("Fishing Tools", "/tideborne debug fishing", ClickEvent.Action.RUN_COMMAND,
                Formatting.AQUA, "Inspect and reproduce canonical catches"));
        send(source, Text.literal("Specimens").formatted(Formatting.GRAY));
        send(source, button("Specimen Tools", "/tideborne debug specimen", ClickEvent.Action.RUN_COMMAND,
                Formatting.AQUA, "Inspect or edit canonical held specimens"));
        send(source, Text.literal("Data").formatted(Formatting.GRAY));
        send(source, button("Fish Registry", "/tideborne debug registry", ClickEvent.Action.RUN_COMMAND,
                Formatting.AQUA, "Dump registered Tide fish profile diagnostics"));
        send(source, Text.empty());
        send(source, back("/tideborne", "Back to Tideborne"));
        send(source, Text.literal(RULE).formatted(Formatting.DARK_GRAY));
        return 1;
    }

    public static int showFishingDebug(ServerCommandSource source) {
        send(source, title("════════════ FISHING DEBUG ════════════"));
        send(source, Text.empty());
        send(source, button("Inspect Active Catch", "/tideborne debug fishing inspect", ClickEvent.Action.RUN_COMMAND,
                Formatting.GREEN, "View server-owned canonical state for your active catch"));
        send(source, button("Reproduce Catch", "/tideborne debug fishing reproduce ", ClickEvent.Action.SUGGEST_COMMAND,
                Formatting.AQUA, "Prepare deterministic catch reproduction arguments in chat"));
        send(source, Text.empty());
        send(source, back("/tideborne debug", "Back to Debug Tools"));
        send(source, Text.literal(RULE).formatted(Formatting.DARK_GRAY));
        return 1;
    }

    public static int showSpecimenDebug(ServerCommandSource source) {
        send(source, title("═══════════ SPECIMEN DEBUG ════════════"));
        send(source, Text.empty());
        send(source, Text.literal("Held Specimen").formatted(Formatting.GRAY));
        send(source, button("Inspect", "/tideborne debug specimen inspect", ClickEvent.Action.RUN_COMMAND,
                Formatting.GREEN, "Inspect the held Tide specimen"));
        send(source, Text.literal("Edit").formatted(Formatting.GRAY));
        MutableText edit = button("Percentile", "/tideborne debug specimen set percentile ", ClickEvent.Action.SUGGEST_COMMAND,
                Formatting.AQUA, "Prepare a canonical natural percentile edit");
        edit.append(Text.literal("  "));
        edit.append(button("Body Type", "/tideborne debug specimen body", ClickEvent.Action.RUN_COMMAND,
                Formatting.AQUA, "Choose Normal, Giant, or Dwarf"));
        send(source, edit);
        MutableText axes = button("Condition", "/tideborne debug specimen condition", ClickEvent.Action.RUN_COMMAND,
                Formatting.AQUA, "Choose Normal, Scarred, or Parasite-Ridden");
        axes.append(Text.literal("  "));
        axes.append(button("Pigmentation", "/tideborne debug specimen pigmentation", ClickEvent.Action.RUN_COMMAND,
                Formatting.AQUA, "Choose Normal, Albino, or Iridescent"));
        send(source, axes);
        send(source, Text.literal("Destructive").formatted(Formatting.RED));
        MutableText reroll = button("Reroll Held", "/tideborne debug specimen reroll held --confirm",
                ClickEvent.Action.SUGGEST_COMMAND, Formatting.RED,
                "WARNING: replaces specimen identity. Click only prepares the confirmed command.");
        reroll.append(Text.literal("  "));
        reroll.append(button("Reroll Inventory", "/tideborne debug specimen reroll inventory --confirm",
                ClickEvent.Action.SUGGEST_COMMAND, Formatting.RED,
                "WARNING: replaces specimen identity for fish in inventory. Click only prepares the confirmed command."));
        send(source, reroll);
        send(source, Text.empty());
        send(source, back("/tideborne debug", "Back to Debug Tools"));
        send(source, Text.literal(RULE).formatted(Formatting.DARK_GRAY));
        return 1;
    }

    public static int showBodyTypes(ServerCommandSource source) {
        send(source, title("════════════ SET BODY TYPE ════════════"));
        MutableText line = button("Normal", "/tideborne debug specimen set body normal", ClickEvent.Action.RUN_COMMAND,
                Formatting.GREEN, "Set canonical Body Type to Normal");
        line.append(Text.literal("  "));
        line.append(button("Giant", "/tideborne debug specimen set body giant", ClickEvent.Action.RUN_COMMAND,
                Formatting.GOLD, "Set canonical Body Type to Giant and recompute physical size"));
        line.append(Text.literal("  "));
        line.append(button("Dwarf", "/tideborne debug specimen set body dwarf", ClickEvent.Action.RUN_COMMAND,
                Formatting.GOLD, "Set canonical Body Type to Dwarf and recompute physical size"));
        send(source, line);
        send(source, back("/tideborne debug specimen", "Back to Specimen Tools"));
        return 1;
    }

    public static int showConditions(ServerCommandSource source) {
        send(source, title("════════════ SET CONDITION ════════════"));
        send(source, choiceLine(
                new Choice("Normal", "normal"),
                new Choice("Scarred", "scarred"),
                new Choice("Parasite-Ridden", "parasite_ridden"),
                "/tideborne debug specimen set condition "));
        send(source, back("/tideborne debug specimen", "Back to Specimen Tools"));
        return 1;
    }

    public static int showPigmentations(ServerCommandSource source) {
        send(source, title("══════════ SET PIGMENTATION ═══════════"));
        send(source, choiceLine(
                new Choice("Normal", "normal"),
                new Choice("Albino", "albino"),
                new Choice("Iridescent", "iridescent"),
                "/tideborne debug specimen set pigmentation "));
        send(source, back("/tideborne debug specimen", "Back to Specimen Tools"));
        return 1;
    }

    private static MutableText choiceLine(Choice first, Choice second, Choice third, String prefix) {
        MutableText line = button(first.label(), prefix + first.value(), ClickEvent.Action.RUN_COMMAND,
                Formatting.GREEN, "Set canonical value to " + first.label());
        line.append(Text.literal("  "));
        line.append(button(second.label(), prefix + second.value(), ClickEvent.Action.RUN_COMMAND,
                Formatting.GOLD, "Set canonical value to " + second.label()));
        line.append(Text.literal("  "));
        line.append(button(third.label(), prefix + third.value(), ClickEvent.Action.RUN_COMMAND,
                Formatting.LIGHT_PURPLE, "Set canonical value to " + third.label()));
        return line;
    }

    private static MutableText title(String value) {
        return Text.literal(value).formatted(Formatting.DARK_AQUA, Formatting.BOLD);
    }

    private static MutableText back(String command, String hover) {
        return button("← Back", command, ClickEvent.Action.RUN_COMMAND, Formatting.GRAY, hover);
    }

    private static MutableText button(
            String label,
            String command,
            ClickEvent.Action action,
            Formatting color,
            String hover
    ) {
        return Text.literal("[ " + label + " ]").styled(style -> style
                .withColor(color)
                .withBold(true)
                .withClickEvent(new ClickEvent(action, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hover))));
    }

    private static void send(ServerCommandSource source, Text text) {
        source.sendFeedback(() -> text, false);
    }

    private record Choice(String label, String value) {
    }
}
