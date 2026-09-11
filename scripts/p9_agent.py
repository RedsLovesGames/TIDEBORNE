#!/usr/bin/env python3
"""Compact resumable checkpoint/report helper for Tideborne P9."""

from __future__ import annotations

import argparse
import json
import subprocess
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
STATE_PATH = ROOT / ".github" / "TIDEBORNE_AGENT_STATE.json"
HANDOFF_PATH = ROOT / ".github" / "TIDEBORNE_AGENT_HANDOFF.md"
EXPECTED_BRANCH = "agent/p9-final-audit"
FORBIDDEN = (
    "com.redslovesgames.tidetraits",
    "com.redslovesgames.tideteamjournal",
    "com.redslovesgames.tideboundcompatibility",
    "com.redslovesgames.tideborne.fishing.v2",
)
HISTORICAL_NAMESPACES = ("tide_traits", "tide_team_journal", "tidebound_compatibility")
STALE_P4_SYMBOLS = (
    "SatchelSorter",
    "SatchelSortDescriptor",
    "SatchelTraitSortData",
    "TideSatchelSortMetadataResolver",
)


def git(*args: str, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(["git", *args], cwd=ROOT, text=True, capture_output=True, check=check)


def load_state() -> dict:
    return json.loads(STATE_PATH.read_text(encoding="utf-8"))


def save_state(state: dict) -> None:
    STATE_PATH.write_text(json.dumps(state, indent=2) + "\n", encoding="utf-8")


def branch() -> str:
    return git("rev-parse", "--abbrev-ref", "HEAD").stdout.strip()


def head() -> str:
    return git("rev-parse", "HEAD").stdout.strip()


def dirty() -> list[str]:
    text = git("status", "--porcelain").stdout.strip()
    return text.splitlines() if text else []


def grep(pattern: str, *paths: str) -> list[str]:
    cmd = ["grep", "-n", "-E", pattern, "--"]
    cmd.extend(paths)
    result = git(*cmd, check=False)
    if result.returncode not in (0, 1):
        return [f"grep-error:{result.returncode}:{result.stderr.strip()}"]
    return result.stdout.splitlines()


def direct_package_dirs() -> list[str]:
    prefix = "src/main/java/com/redslovesgames/tideborne/"
    dirs = set()
    for path in git("ls-files", "src/main/java/com/redslovesgames/tideborne").stdout.splitlines():
        if path.startswith(prefix):
            remainder = path[len(prefix):]
            if "/" in remainder:
                dirs.add(remainder.split("/", 1)[0])
    return sorted(dirs)


def count_files(prefix: str, suffix: str = "") -> int:
    files = git("ls-files", prefix).stdout.splitlines()
    return sum(1 for item in files if not suffix or item.endswith(suffix))


def baseline_ok(state: dict) -> bool:
    baseline = state["baseline_sha"]
    result = git("merge-base", "--is-ancestor", baseline, "HEAD", check=False)
    return result.returncode == 0


def normal_legacy_hits() -> list[str]:
    hits = grep(r"fishing\.specimen\.legacy", "src/main/java")
    return [line for line in hits if "/fishing/specimen/legacy/" not in line.split(":", 1)[0]]


def historical_counts() -> Counter[str]:
    hits = grep(r"tide_traits|tide_team_journal|tidebound_compatibility", "src/main/java", "src/main/resources/fabric.mod.json")
    counts: Counter[str] = Counter()
    for line in hits:
        for token in HISTORICAL_NAMESPACES:
            counts[token] += line.count(token)
    return counts


def render_report() -> str:
    state = load_state()
    worktree = dirty()
    forbidden_hits = grep("|".join(FORBIDDEN), "src/main/java")
    stale_hits = grep("|".join(STALE_P4_SYMBOLS), "src/main/java", "src/test/java")
    legacy_hits = normal_legacy_hits()
    hist = historical_counts()
    lines = [
        "P9 FINAL-AUDIT CHECKPOINT REPORT",
        f"branch: {branch()}",
        f"head: {head()}",
        f"baseline: {state['baseline_sha']}",
        f"baseline_is_ancestor: {'yes' if baseline_ok(state) else 'NO'}",
        f"status: {state['status']}",
        f"phase: {state['phase']}",
        f"checkpoint_sequence: {state['checkpoint_sequence']}",
        f"last_product_checkpoint_sha: {state.get('last_product_checkpoint_sha')}",
        f"next_action: {state['next_action']}",
        f"blockers: {state.get('blockers', [])}",
        f"worktree: {'DIRTY' if worktree else 'clean'}",
        f"production_java_files: {count_files('src/main/java', '.java')}",
        f"test_java_files: {count_files('src/test/java', '.java')}",
        "top_level_tideborne_packages: " + ", ".join(direct_package_dirs()),
        f"forbidden_java_ownership_hits: {len(forbidden_hits)}",
        f"normal_runtime_legacy_specimen_hits: {len(legacy_hits)}",
        f"stale_p4_symbol_hits: {len(stale_hits)}",
        "historical_namespace_literal_counts:",
    ]
    for token in HISTORICAL_NAMESPACES:
        lines.append(f"  {token}: {hist[token]}")
    if legacy_hits:
        lines.append("normal_runtime_legacy_specimen_examples:")
        lines.extend(f"  {line}" for line in legacy_hits[:12])
    if stale_hits:
        lines.append("stale_p4_symbol_examples:")
        lines.extend(f"  {line}" for line in stale_hits[:12])
    if forbidden_hits:
        lines.append("forbidden_ownership_examples:")
        lines.extend(f"  {line}" for line in forbidden_hits[:12])
    lines.append("validation:")
    for key, value in state.get("validation", {}).items():
        lines.append(f"  {key}: {value}")
    if worktree:
        lines.append("dirty_paths:")
        lines.extend(f"  {line}" for line in worktree[:20])
    return "\n".join(lines) + "\n"


def write_handoff(state: dict) -> None:
    blockers = state.get("blockers", [])
    validation = state.get("validation", {})
    text = f"""# P9 Continuation Handoff

Branch: `{state['branch']}`  
Baseline: `{state['baseline_sha']}`  
Status: **{state['status']}**  
Phase: **{state['phase']}**  
Checkpoint sequence: **{state['checkpoint_sequence']}**  
Last safe product SHA: `{state.get('last_product_checkpoint_sha')}`  
Last checkpoint UTC: `{state.get('last_checkpoint_utc')}`

## Exact next action

{state['next_action']}

## Blockers

{chr(10).join('- ' + item for item in blockers) if blockers else '- None'}

## Validation snapshot

{chr(10).join('- `' + key + '`: ' + str(value) for key, value in validation.items())}

## Resume

Run `python3 scripts/p9_agent.py report`, then execute the exact next action above. Read the P9 context and only the audit-ledger/subsystem evidence needed for the active phase. Do not restart the full audit.
"""
    HANDOFF_PATH.write_text(text, encoding="utf-8")


def parse_pairs(values: list[str]) -> dict[str, str]:
    result: dict[str, str] = {}
    for value in values:
        if "=" not in value:
            raise SystemExit(f"Expected key=value, got: {value}")
        key, val = value.split("=", 1)
        result[key.strip()] = val.strip()
    return result


def checkpoint(args: argparse.Namespace) -> None:
    current = branch()
    if current != EXPECTED_BRANCH:
        raise SystemExit(f"Refusing checkpoint on {current!r}; expected {EXPECTED_BRANCH!r}")
    worktree = dirty()
    if worktree:
        raise SystemExit("Refusing checkpoint with uncommitted work:\n" + "\n".join(worktree[:30]))

    state = load_state()
    state["phase"] = args.phase
    state["checkpoint_sequence"] = int(state.get("checkpoint_sequence", 0)) + 1
    state["last_product_checkpoint_sha"] = head()
    state["last_checkpoint_utc"] = datetime.now(timezone.utc).isoformat()
    state["next_action"] = args.next

    completed = list(state.get("completed_phases", []))
    if args.completed and args.phase not in completed:
        completed.append(args.phase)
    state["completed_phases"] = completed

    if args.complete:
        state["status"] = "COMPLETE"
    elif args.status:
        state["status"] = args.status
    else:
        state["status"] = "IN_PROGRESS"

    blockers = list(state.get("blockers", []))
    if args.clear_blockers:
        blockers = []
    for item in args.blocker:
        if item not in blockers:
            blockers.append(item)
    state["blockers"] = blockers
    state.setdefault("validation", {}).update(parse_pairs(args.validation))
    state.setdefault("notes", []).extend(args.note)

    save_state(state)
    write_handoff(state)
    git("add", str(STATE_PATH.relative_to(ROOT)), str(HANDOFF_PATH.relative_to(ROOT)))
    git("commit", "-m", f"chore(p9): checkpoint {args.phase}")
    print(render_report(), end="")


def main() -> None:
    parser = argparse.ArgumentParser()
    sub = parser.add_subparsers(dest="command", required=True)

    report_parser = sub.add_parser("report", help="print compact P9 continuation state")
    report_parser.add_argument("--output", help="also write the report to this path")

    cp = sub.add_parser("checkpoint", help="create a safe state-only continuation commit")
    cp.add_argument("--phase", required=True)
    cp.add_argument("--next", required=True)
    cp.add_argument("--completed", action="store_true")
    cp.add_argument("--complete", action="store_true")
    cp.add_argument("--status", choices=["READY", "IN_PROGRESS", "BLOCKED"])
    cp.add_argument("--blocker", action="append", default=[])
    cp.add_argument("--clear-blockers", action="store_true")
    cp.add_argument("--validation", action="append", default=[])
    cp.add_argument("--note", action="append", default=[])

    args = parser.parse_args()
    if args.command == "report":
        report = render_report()
        print(report, end="")
        if args.output:
            (ROOT / args.output).write_text(report, encoding="utf-8")
    else:
        checkpoint(args)


if __name__ == "__main__":
    main()
