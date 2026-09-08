# Reconstruction workspace

This directory records how the Tideborne 1.3.57 maintained source baseline was recovered.

## Normal developer workflow

Once reconstruction is complete, you should not need anything in this directory to build Tideborne. Use the normal Gradle project at the repository root.

## Re-running reconstruction locally

With the authoritative 1.3.57 JAR available:

```bash
python3 scripts/reconstruct_sources.py reconstruction/reference/Tideborne-1.3.57-perfect-catch-trait-luck.jar --force
```

The script validates the exact SHA-256 before touching `src/`.

It pins:

- Vineflower 1.12.0
- Yarn 1.21.1+build.3

Generated provenance is written to `reconstruction/manifest.json`.

## Temporary CI input

During the initial repository recovery only, `reconstruction/input/` may contain base64 text chunks representing the authoritative JAR. `READY` is committed last to trigger the reconstruction action. The action verifies the decoded SHA-256, generates `src/`, and removes the temporary input directory before committing the recovered baseline.

Do not use this mechanism for normal releases or dependency storage.
