/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.Screen;

public final class ClientConfigScreen {
   private ClientConfigScreen() {
   }

   public static Screen create(Screen parent) {
      ClientConfig.Values values = ClientConfig.get();
      ConfigBuilder builder = ConfigBuilder.create()
         .setParentScreen(parent)
         .setTitle(Text.translatable("config.tideborne.team_journal.title"))
         .setSavingRunnable(ClientConfig::save);
      ConfigEntryBuilder entries = builder.entryBuilder();
      ConfigCategory display = builder.getOrCreateCategory(Text.translatable("config.tideborne.team_journal.display"));
      display.addEntry(
         entries.startBooleanToggle(Text.translatable("config.tideborne.team_journal.badges"), values.showRecordBadges)
            .setDefaultValue(true)
            .setSaveConsumer(value -> values.showRecordBadges = value)
            .build()
      );
      display.addEntry(
         entries.startBooleanToggle(Text.translatable("config.tideborne.team_journal.tooltips"), values.showRecordTooltips)
            .setDefaultValue(true)
            .setSaveConsumer(value -> values.showRecordTooltips = value)
            .build()
      );
      display.addEntry(
         entries.startBooleanToggle(Text.translatable("config.tideborne.team_journal.former"), values.showFormerMembers)
            .setDefaultValue(true)
            .setSaveConsumer(value -> values.showFormerMembers = value)
            .build()
      );
      display.addEntry(
         entries.startSelector(
               Text.translatable("config.tideborne.team_journal.default_tab"), new String[]{"summary", "leaderboard", "history"}, values.defaultTab
            )
            .setNameProvider(value -> Text.translatable("screen.tideborne.team_journal." + value))
            .setDefaultValue("summary")
            .setSaveConsumer(value -> values.defaultTab = value)
            .build()
      );
      display.addEntry(
         entries.startSelector(
               Text.translatable("config.tideborne.team_journal.default_metric"),
               new String[]{"catches", "species", "record_events", "active_records"},
               values.defaultMetric
            )
            .setNameProvider(value -> Text.translatable("metric.tideborne.team_journal." + value))
            .setDefaultValue("catches")
            .setSaveConsumer(value -> values.defaultMetric = value)
            .build()
      );
      ConfigCategory alerts = builder.getOrCreateCategory(Text.translatable("config.tideborne.team_journal.alerts"));
      alerts.addEntry(
         entries.startSelector(Text.translatable("config.tideborne.team_journal.toast_mode"), new String[]{"full", "compact", "off"}, values.toastMode)
            .setNameProvider(value -> Text.translatable("config.tideborne.team_journal.toast_mode." + value))
            .setDefaultValue("full")
            .setSaveConsumer(value -> values.toastMode = value)
            .build()
      );
      alerts.addEntry(
         entries.startIntSlider(Text.translatable("config.tideborne.team_journal.toast_duration"), values.toastDurationSeconds, 2, 20)
            .setDefaultValue(5)
            .setSaveConsumer(value -> values.toastDurationSeconds = value)
            .build()
      );
      alerts.addEntry(
         entries.startBooleanToggle(Text.translatable("config.tideborne.team_journal.toast_sound"), values.toastSound)
            .setDefaultValue(true)
            .setSaveConsumer(value -> values.toastSound = value)
            .build()
      );
      alerts.addEntry(
         entries.startColorField(Text.translatable("config.tideborne.team_journal.largest_color"), values.largestColor)
            .setDefaultValue(10121284)
            .setSaveConsumer(value -> values.largestColor = value)
            .build()
      );
      alerts.addEntry(
         entries.startColorField(Text.translatable("config.tideborne.team_journal.smallest_color"), values.smallestColor)
            .setDefaultValue(7757682)
            .setSaveConsumer(value -> values.smallestColor = value)
            .build()
      );
      alerts.addEntry(
         entries.startColorField(Text.translatable("config.tideborne.team_journal.discovery_color"), values.discoveryColor)
            .setDefaultValue(5207921)
            .setSaveConsumer(value -> values.discoveryColor = value)
            .build()
      );
      ConfigCategory debug = builder.getOrCreateCategory(Text.literal("Debug"));
      debug.addEntry(
         entries.startBooleanToggle(Text.literal("Show Team Records debug button"), values.debugTeamRecordsButton)
            .setDefaultValue(false)
            .setSaveConsumer(value -> values.debugTeamRecordsButton = value)
            .build()
      );
      return builder.build();
   }
}
