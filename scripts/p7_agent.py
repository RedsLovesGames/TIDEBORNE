#!/usr/bin/env python3
"""Compact, resumable status/checkpoint helper for the P7 resource worker.

Stdlib only. `report` is read-only. `checkpoint` updates the tracked state file after
product changes have already been committed, making a fresh chat able to resume from
one small JSON file instead of reconstructing the full history.
"""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
STATE_PATH = ROOT / ".github" / "TIDEBORNE_AGENT_STATE.json"
RESOURCES = ROOT / "src" / "main" / "resources"
SCAN_ROOTS = [ROOT / "src" / "main" / "java", RESOURCES, ROOT / "src" / "test" / "java"]
HISTORICAL_NAMESPACES = ("tide_traits", "tide_team_journal", "tidebound_compatibility")
TRANSLATION_FAMILIES = ("config", "tooltip", "message", "screen", "toast", "key")
TEXT_SUFFIXES = {
    ".java", ".json", ".properties", ".mcmeta", ".txt", ".md", ".gradle",
    ".kts", ".yml", ".yaml", ".toml", ".cfg", ".xml", ".sh", ".py"
}
VALIDATION_KEYS = {
    "repository_structure",
    "java21_clean_build",
    "unit_architecture_tests",
    "resource_paths_translations",
    "mixin_refmap_metadata",
    "optional_compatibility_safety",
    "release_artifact",
}


def run_git(*args: str) -> str | None:
    try:
        result = subprocess.run(
            ["git", *args], cwd=ROOT, check=True, capture_output=True, text=True
        )
    except (OSError, subprocess.CalledProcessError):
        return None
    return result.stdout.strip()


def load_state() -> dict:
    try:
        return json.loads(STATE_PATH.read_text(encoding="utf-8"))
    except FileNotFoundError:
        raise SystemExit(f"Missing P7 state file: {STATE_PATH}")
    except json.JSONDecodeError as exc:
        raise SystemExit(f"Invalid P7 state JSON: {exc}") from exc


def write_state(state: dict) -> None:
    STATE_PATH.write_text(json.dumps(state, indent=2) + "\n", encoding="utf-8")


def namespace_counts(kind: str) -> list[tuple[str, int]]:
    base = RESOURCES / kind
    if not base.is_dir():
        return []
    rows = []
    for child in sorted(path for path in base.iterdir() if path.is_dir()):
        count = sum(1 for path in child.rglob("*") if path.is_file())
        rows.append((child.name, count))
    return rows


def iter_text_files():
    for root in SCAN_ROOTS:
        if not root.exists():
            continue
        for path in root.rglob("*"):
            if not path.is_file() or path.suffix.lower() not in TEXT_SUFFIXES:
                continue
            try:
                yield path, path.read_text(encoding="utf-8")
            except (UnicodeDecodeError, OSError):
                continue


def literal_counts() -> Counter:
    counts: Counter[str] = Counter()
    targets = ("tideborne", *HISTORICAL_NAMESPACES)
    for _, text in iter_text_files():
        for namespace in targets:
            counts[namespace] += text.count(namespace)
    return counts


def translation_counts() -> Counter:
    counts: Counter[str] = Counter()
    namespaces = ("tideborne", *HISTORICAL_NAMESPACES)
    needles = {
        f"{family}.{namespace}.": (family, namespace)
        for family in TRANSLATION_FAMILIES
        for namespace in namespaces
    }
    for _, text in iter_text_files():
        for needle, key in needles.items():
            counts[f"{key[0]}.{key[1]}"] += text.count(needle)
    return counts


def root_config_files() -> list[str]:
    if not RESOURCES.is_dir():
        return []
    names = []
    for pattern in ("*.mixins.json", "*.refmap.json"):
        names.extend(path.name for path in RESOURCES.glob(pattern))
    return sorted(set(names))


def git_branch() -> str:
    branch = run_git("branch", "--show-current")
    if branch:
        return branch
    # Detached checkout in GitHub Actions: make the report still useful.
    return "detached"


def git_head() -> str:
    return run_git("rev-parse", "HEAD") or "unknown"


def markdown_report(state: dict) -> str:
    branch = git_branch()
    head = git_head()
    assets = namespace_counts("assets")
    data = namespace_counts("data")
    literals = literal_counts()
    translations = translation_counts()

    lines = [
        "# P7 AI Checkpoint Report",
        "",
        f"- Branch: `{branch}`",
        f"- HEAD: `{head}`",
        f"- State: **{state.get('status', 'UNKNOWN')}**",
        f"- Phase: **{state.get('phase', 'UNKNOWN')}**",
        f"- Checkpoint sequence: {state.get('checkpoint_sequence', 0)}",
        f"- Last product checkpoint: `{state.get('last_product_checkpoint_sha') or 'none'}`",
        f"- Next action: {state.get('next_action') or 'none'}",
    ]

    blockers = state.get("blockers") or []
    lines.append(f"- Blockers: {('; '.join(blockers)) if blockers else 'none'}")
    completed = state.get("completed_phases") or []
    lines.append(f"- Completed phases: {', '.join(completed) if completed else 'none'}")

    lines.extend(["", "## Resource namespace file counts", "", "### Assets"])
    if assets:
        lines.extend(f"- `{name}`: {count}" for name, count in assets)
    else:
        lines.append("- none")
    lines.extend(["", "### Data"])
    if data:
        lines.extend(f"- `{name}`: {count}" for name, count in data)
    else:
        lines.append("- none")

    lines.extend(["", "## Historical namespace literal inventory", ""])
    for namespace in ("tideborne", *HISTORICAL_NAMESPACES):
        lines.append(f"- `{namespace}` occurrences in scoped text: {literals[namespace]}")
    lines.append("These are audit seeds, not automatic deletion targets; persisted compatibility IDs may be valid.")

    lines.extend(["", "## Translation-key ownership inventory", ""])
    for family in TRANSLATION_FAMILIES:
        target = translations[f"{family}.tideborne"]
        historical = sum(translations[f"{family}.{ns}"] for ns in HISTORICAL_NAMESPACES)
        lines.append(f"- `{family}`: tideborne={target}, historical={historical}")

    lines.extend(["", "## Root mixin/refmap files", ""])
    configs = root_config_files()
    lines.extend(f"- `{name}`" for name in configs) if configs else lines.append("- none")

    lines.extend(["", "## Validation state", ""])
    validation = state.get("validation") or {}
    for key in sorted(VALIDATION_KEYS):
        lines.append(f"- `{key}`: {validation.get(key, 'not_run')}")

    lines.extend(
        [
            "",
            "## Resume",
            "",
            "Read `.github/TIDEBORNE_AGENT_STATE.json`, then execute only its `next_action` for the current phase.",
        ]
    )
    return "\n".join(lines) + "\n"


def command_report(_: argparse.Namespace) -> int:
    print(markdown_report(load_state()), end="")
    return 0


def ensure_checkpoint_is_safe(state: dict) -> str:
    expected_branch = state.get("branch")
    branch = git_branch()
    if branch != expected_branch:
        raise SystemExit(f"Refusing checkpoint: branch is {branch!r}, expected {expected_branch!r}")
    status = run_git("status", "--porcelain")
    if status is None:
        raise SystemExit("Refusing checkpoint: git status unavailable")
    if status.strip():
        raise SystemExit(
            "Refusing checkpoint with uncommitted work. Commit the smallest coherent product substep first, then rerun checkpoint."
        )
    head = git_head()
    if head == "unknown":
        raise SystemExit("Refusing checkpoint: git HEAD unavailable")
    return head


def command_checkpoint(args: argparse.Namespace) -> int:
    state = load_state()
    product_head = ensure_checkpoint_is_safe(state)

    if args.phase:
        state["phase"] = args.phase
    if args.status:
        state["status"] = args.status
    if args.next_action is not None:
        state["next_action"] = args.next_action

    completed = list(state.get("completed_phases") or [])
    for phase in args.complete or []:
        if phase not in completed:
            completed.append(phase)
    state["completed_phases"] = completed

    if args.clear_blockers:
        state["blockers"] = []
    blockers = list(state.get("blockers") or [])
    for blocker in args.blocker or []:
        if blocker not in blockers:
            blockers.append(blocker)
    state["blockers"] = blockers

    validation = dict(state.get("validation") or {})
    for item in args.validation or []:
        if "=" not in item:
            raise SystemExit(f"Invalid --validation {item!r}; expected key=value")
        key, value = item.split("=", 1)
        if key not in VALIDATION_KEYS:
            raise SystemExit(
                f"Unknown validation key {key!r}; valid keys: {', '.join(sorted(VALIDATION_KEYS))}"
            )
        validation[key] = value
    state["validation"] = validation

    if args.note:
        notes = list(state.get("notes") or [])
        notes.append(args.note)
        state["notes"] = notes[-20:]

    state["checkpoint_sequence"] = int(state.get("checkpoint_sequence", 0)) + 1
    state["last_product_checkpoint_sha"] = product_head
    state["last_checkpoint_utc"] = datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    write_state(state)
    print(markdown_report(state), end="")
    print(
        "\nCheckpoint state updated. Commit `.github/TIDEBORNE_AGENT_STATE.json` as a separate checkpoint commit and push it.",
        file=sys.stderr,
    )
    return 0


def parser() -> argparse.ArgumentParser:
    root = argparse.ArgumentParser(description="P7 resumable AI worker helper")
    sub = root.add_subparsers(dest="command", required=True)

    report = sub.add_parser("report", help="Print compact current P7 status/inventory")
    report.set_defaults(func=command_report)

    checkpoint = sub.add_parser("checkpoint", help="Update P7 continuation state after a safe product commit")
    checkpoint.add_argument("--phase")
    checkpoint.add_argument("--status", choices=("READY", "IN_PROGRESS", "BLOCKED", "COMPLETE"))
    checkpoint.add_argument("--next", dest="next_action")
    checkpoint.add_argument("--complete", action="append", default=[])
    checkpoint.add_argument("--blocker", action="append", default=[])
    checkpoint.add_argument("--clear-blockers", action="store_true")
    checkpoint.add_argument("--validation", action="append", default=[], metavar="KEY=VALUE")
    checkpoint.add_argument("--note")
    checkpoint.set_defaults(func=command_checkpoint)
    return root


def main() -> int:
    args = parser().parse_args()
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
