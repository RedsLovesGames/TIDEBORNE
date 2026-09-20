# Getting Started

[← Wiki Home](README.md) | [Specimens & Traits →](SPECIMENS_AND_TRAITS.md)

> **Tideborne 2.1.0** · Minecraft 1.21.1 · Fabric · Java 21

## What Tideborne changes

Tide remains responsible for the core act of fishing, species availability, habitats, and the underlying fishing minigame. Tideborne layers a persistent specimen system on top. When a Tide fish is caught, Tideborne can give that exact specimen a size percentile, Body Type, Condition, Pigmentation, Specimen Quality, Perfect Catch state, and FishScore. The same specimen data then follows the fish through tooltips, storage, records, the Journal, leaderboards, and displays.

## Required mods

Tideborne 2.1.0 declares the following hard requirements in its JAR:

| Requirement | Version |
| --- | --- |
| Minecraft | `1.21.1` |
| Java | `21+` |
| Fabric Loader | `0.18.4+` |
| Fabric API | `0.116.15+1.21.1` or newer compatible build |
| Tide | **exactly `2.1.1`** |
| Architectury API | `13.0.8+` |
| FTB Library | `2101.1.30+` |
| FTB Teams | `2101.1.10+` |
| Cloth Config | `15.0.140+` |

Optional integrations:

| Mod | Tideborne 2.1.0 target |
| --- | --- |
| Myths of the Sea | `1.3.0` |
| Apex Waters | `1.1.1` |
| Mod Menu | Optional |

## Installation

1. Install Fabric for Minecraft 1.21.1.
2. Install Tide 2.1.1 and all required dependencies listed above.
3. Place `tideborne-2.1.0.jar` in the `mods` folder.
4. On multiplayer, install Tideborne and its required dependencies on both the server and connecting clients.
5. Start the game and use `/tideborne status` if you need to confirm the unified backend is active.

Tideborne's `environment` is `*`, meaning the JAR contains both client and server behavior. A server without the matching client cannot provide the full Tideborne screens and presentation.

## Your first catches

You do not need to activate the specimen system manually. Fish caught through supported Tide paths are generated and scored by the server. Hover a catch to see its specimen information. Depending on the catch, you may see traits, size information, rarity presentation, FishScore, or record indicators.

A useful early progression route is:

1. Fish normally and learn the size range of several species.
2. Craft or convert an [Angler's Satchel](ANGLERS_SATCHEL.md).
3. Start watching for personal largest and smallest records.
4. If playing with an FTB Teams party, open the [Team Journal](TEAM_JOURNAL_AND_RECORDS.md).
5. Begin targeting unusual traits and high FishScores once you understand the difference between Fishing Luck and Trait Luck.

## Fishing Luck vs Trait Luck

These are different systems.

**Fishing Luck** changes species selection weights. It helps bias the selected fish toward rarer Tide species, but it does not directly make specimen traits more likely.

**Trait Luck** changes the probability of unusual specimen traits after a species has been selected. Trait Luck is used by the trait probability system and can come from systems such as gear, Perfect Catch rewards, or Trait Momentum.

This separation is important: a build that is good at finding rare species is not automatically the best build for finding rare versions of a specific species.

## Server authority

The important parts of the catch are server-owned. Species selection, specimen generation, trait outcomes, FishScore, records, and progression are not meant to be authored independently by the client. This is why two players looking at the same stored specimen should agree on what that fish is.

## Existing worlds

If you are upgrading from an older Tideborne setup, read [Updating & Compatibility](UPDATING_AND_COMPATIBILITY.md) before launching the world. Modern Tideborne contains the historical Tide Traits, Team Journal, and compatibility systems in one JAR, so old standalone modules should not be left installed beside 2.1.0.