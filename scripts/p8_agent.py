#!/usr/bin/env python3
"""Compact, resumable checkpoint/report helper for Tideborne P8.

The script intentionally keeps product commits separate from state-only checkpoint
commits. A checkpoint is refused when the worktree is dirty so continuation chats
never receive a false safe state.
"""

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
EXPECTED_BRANCH = "agent/p8-legacy-ids"
TOKENS = ("tide_traits", "tide_team_journal", "tidebound_compatibility")


def git(*args: str, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["git", *args], cwd=ROOT, text=True, capture_output=True, check=check
    )


def load_state() -> dict:
    return json.loads(STATE_PATH.read_text(encoding="utf-8"))


def save_state(state: dict) -> None:
    STATE_PATH.write_text(json.dumps(state, indent=2) + "\n", encoding="utf-8")


def current_branch() -> str:
    return git("rev-parse", "--abbrev-ref", "HEAD").stdout.strip()


def head_sha() -> str:
    return git("rev-parse", "HEAD").stdout.strip()


def dirty_lines() -> list[str]:
    out = git("status", "--porcelain").stdout.strip()
    return out.splitlines() if out else []


def historical_hits() -> list[str]:
    result = git(
        "grep",
        "-n",
        "-E",
        "tide_traits|tide_team_journal|tidebound_compatibility",
        "--",
        "src/main/java",
        "src/main/resources/fabric.mod.json",
        "src/test/java",
        "scripts",
        ".github",
        check=False,
    )
    return result.stdout.splitlines() if result.returncode in (0, 1) else []


def hit_summary() -> tuple[Counter, Counter]:
    token_counts: Counter[str] = Counter()
    file_counts: Counter[str] = Counter()
    for line in historical_hits():
        path = line.split(":", 1)[0]
        file_counts[path] += 1
        for token in TOKENS:
            token_counts[token] += line.count(token)
    return token_counts, file_counts


def render_report() -> str:
    state = load_state()
    branch = current_branch()
    dirty = dirty_lines()
    token_counts, file_counts = hit_summary()
    lines = [
        "P8 HISTORICAL-ID CHECKPOINT REPORT",
        f"branch: {branch}",
        f"head: {head_sha()}",
        f"status: {state['status']}",
        f"phase: {state['phase']}",
        f"checkpoint_sequence: {state['checkpoint_sequence']}",
        f"last_product_checkpoint_sha: {state.get('last_product_checkpoint_sha')}",
        f"next_action: {state['next_action']}",
        f"blockers: {state.get('blockers', [])}",
        f"worktree: {'DIRTY' if dirty else 'clean'}",
        "historical_literal_counts:",
    ]
    for token in TOKENS:
        lines.append(f"  {token}: {token_counts[token]}")
    lines.append("top_files_with_historical_literals:")
    for path, count in file_counts.most_common(12):
        lines.append(f"  {count:>3}  {path}")
    if not file_counts:
        lines.append("  none")
    lines.append("validation:")
    for key, value in state.get("validation", {}).items():
        lines.append(f"  {key}: {value}")
    if dirty:
        lines.append("dirty_paths:")
        lines.extend(f"  {line}" for line in dirty[:20])
    return "\n".join(lines) + "\n"


def write_handoff(state: dict) -> None:
    blockers = state.get("blockers", [])
    validation = state.get("validation", {})
    text = f"""# P8 Continuation Handoff

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

{chr(10).join('- ' + b for b in blockers) if blockers else '- None'}

## Validation snapshot

{chr(10).join('- `' + k + '`: ' + str(v) for k, v in validation.items())}

## Resume command

Run `python3 scripts/p8_agent.py report`, then execute the exact next action above. Read `.github/TIDEBORNE_P8_CONTEXT.md` and `.github/TIDEBORNE_P8_ID_LEDGER.md` only for the active phase. Do not restart the full audit.
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
    branch = current_branch()
    if branch != EXPECTED_BRANCH:
        raise SystemExit(f"Refusing checkpoint on {branch!r}; expected {EXPECTED_BRANCH!r}")
    dirty = dirty_lines()
    if dirty:
        raise SystemExit(
            "Refusing checkpoint with uncommitted work. Commit/revert product edits first:\n"
            + "\n".join(dirty[:30])
        )

    state = load_state()
    product_sha = head_sha()
    state["phase"] = args.phase
    state["checkpoint_sequence"] = int(state.get("checkpoint_sequence", 0)) + 1
    state["last_product_checkpoint_sha"] = product_sha
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
    for blocker in args.blocker:
        if blocker not in blockers:
            blockers.append(blocker)
    if args.clear_blockers:
        blockers = []
    state["blockers"] = blockers

    state.setdefault("validation", {}).update(parse_pairs(args.validation))
    state.setdefault("notes", []).extend(args.note)

    save_state(state)
    write_handoff(state)
    git("add", str(STATE_PATH.relative_to(ROOT)), str(HANDOFF_PATH.relative_to(ROOT)))
    git("commit", "-m", f"chore(p8): checkpoint {args.phase}")
    print(render_report(), end="")


def main() -> None:
    parser = argparse.ArgumentParser()
    sub = parser.add_subparsers(dest="command", required=True)

    report_parser = sub.add_parser("report", help="print compact continuation state")
    report_parser.add_argument("--output", help="also write report to this path")

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
