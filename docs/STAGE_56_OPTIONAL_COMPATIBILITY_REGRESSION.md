# Stage 56: Optional compatibility regression pass

Date: 2026-08-29

## Scope

Stage 56 validates Fishing System 2.0 against every optional fish-compatibility mod currently declared by Tideborne:

- Apex Waters 1.1.1 (`apexwaters`)
- Myths of the Sea 1.3.0 (`myths_of_the_sea`)

The validation matrix runs the complete Fabric GameTest suite in four independent server launches:

1. neither optional mod installed
2. Apex Waters only
3. Myths of the Sea only
4. Apex Waters and Myths of the Sea together

This simultaneously covers each supported optional mod in both the present and absent states and makes an accidental hard class reference fail during server bootstrap.

## Exact optional artifacts

CI resolves and verifies the exact runtime artifacts used for this pass:

- Apex Waters 1.1.1 Fabric 1.21.1, SHA-256 `00f1c5eaf5b7c2e79a2c64cdeac1a89f2430b2c9ab5f56043f148bde170dba37`
- Myths of the Sea 1.3.0 Fabric 1.21.1, SHA-256 `55e0944707b91dc3ae15d8a5162184f8327521c4ace71d05f9c0985495222bab`
- CERBON API 1.3.0, SHA-256 `0587dc8b69ed66aa98f5054a7cad23ed58fc437e7da316d5fc82491969caf03d`
- GeckoLib 4.9.2, SHA-256 `eac4cf55e1cb99b22cb91ea87c774d2a670c1711a80f4aff50e6ad56b6c58e7e`

Tide itself remains the exact 2.1.1 Fabric 1.21.1 runtime target, SHA-256 `498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8`.

## Species registry and profile behavior

The canonical Fishing System 2.0 species boundary remains Tide's live `TideData.FISH` registry.

With the exact optional versions above:

- Tide loads 106 `FishData` entries.
- every live Tide fish is adapted through `TideSpeciesProfileAdapter` and retains the same canonical species ID.
- the Stage 56 regression now walks every live `FishData` entry in every runtime matrix leg rather than sampling one profile.
- Apex Waters 1.1.1 contributes no `apexwaters:*` `FishData` entries to Tide's fish registry.
- Myths of the Sea 1.3.0 contributes no `myths_of_the_sea:*` `FishData` entries to Tide's fish registry.

Tideborne therefore does not synthesize fake SpeciesProfiles for either optional namespace. If a future optional-mod version actually registers Tide `FishData`, the live-registry adapter is the integration boundary and the Stage 56 regression will expose that registry change.

## Fishing eligibility

Fishing eligibility remains derived from registered Tide `FishData` plus its existing `shouldKeep(context)` restrictions.

Because the exact Apex and Myths versions do not register their own Tide fish entries, they are not independent candidates in the Fishing System 2.0 species selector. The Stage 56 GameTest explicitly fails if either optional namespace appears in the live Tide fish eligibility pool.

The Apex compatibility adapter is also checked directly: Apex Waters exposes no Tide-catchable species IDs and its Great White Shark entity is not adapted into a synthetic Tide fish profile.

This is intentional. Stage 56 does not invent optional fish merely because an optional mod is loaded.

## Specimen generation

For every runtime matrix leg, the regression suite adapts every live Tide `FishData` entry and performs canonical specimen generation twice for each species using the same deterministic species-derived seed.

For every live species the test verifies:

- the canonical profile retains the registered Tide species ID
- repeated generation with the same canonical seed is exactly deterministic
- generated base and final lengths are finite and positive
- the generated specimen retains the profile species ID
- canonical FishScore is present

This validates that optional-mod loading does not disturb the canonical species-profile-to-specimen pipeline anywhere in the live Tide fish registry.

## Absent-mod classloading safety

The no-optional-mod launch and the single-mod launches prove both optional integrations remain soft dependencies:

- Tideborne boots with Apex absent.
- Tideborne boots with Myths absent.
- Tideborne boots with both absent.
- no Stage 56 test or V2 species/profile/generation class directly loads optional-mod implementation classes merely by existing on the classpath.

The GameTest runtime also checks actual Fabric Loader mod presence and exact supported version against the expected matrix for each launch.

## Myths development-remap compatibility

The exact Myths/CERBON/GeckoLib stack was published with newer Fabric Loom producer metadata than Tideborne's maintained Loom 1.10.5 toolchain accepts for static remapping. Raw runtime jars are not a valid workaround because GeckoLib's class tweaker is in the intermediary namespace while the GameTest runtime is Yarn-named.

For CI compatibility validation only:

1. the exact upstream artifacts are downloaded and verified first;
2. CI removes only an incompatible newer-producer `Fabric-Loom-Version` manifest field from the ephemeral test copy when static remapping is requested;
3. `Fabric-Loom-Mixin-Remap-Type: static` is preserved;
4. every non-manifest JAR entry is SHA-256 checked to remain byte-for-byte identical;
5. Loom 1.10.5 then performs its normal mod remapping, including class tweakers, into the named GameTest runtime.

This is a test-harness adaptation only. It does not modify Tideborne gameplay code or any optional mod's runtime classes/resources.

## Result

GitHub Actions run `33271676247` is the final Stage 56 validation run after expanding profile/specimen coverage to every live Tide fish.

Final validation:

- exact optional dependency preparation and manifest-integrity checks: passed
- clean Gradle build and unit tests: passed
- no optional mods: passed
- Apex Waters only: passed
- Myths of the Sea only: passed
- Apex Waters + Myths of the Sea: passed
- built-JAR artifact upload: passed

All four launches run the complete required GameTest suite, including the Stage 56 presence, eligibility/profile, and all-live-species deterministic specimen checks.

No Fishing System 2.0 production compatibility defect was found. No production fishing, trait, scoring, persistence, or eligibility behavior was changed by Stage 56.
