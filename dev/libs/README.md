# Local exact dependencies

This directory is for exact local development JARs that should not be committed to Git.

## Required for exact Tideborne 1.3.57 validation

Copy the supplied Tide 2.1.1 Fabric JAR here as:

```text
tide-fabric-1.21.1-2.1.1.jar
```

Expected SHA-256:

```text
498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8
```

Then run:

```bash
./gradlew verifyExactDependencies build
```

## Optional compatibility development

If working directly on Apex Waters integration, place its exact 1.1.1 Fabric JAR here as:

```text
apex-waters-fabric-1.21.1-1.1.1.jar
```

Normal core source should remain loadable when Apex Waters is absent.

Do not commit third-party JARs in this directory. `.gitignore` intentionally excludes them.
