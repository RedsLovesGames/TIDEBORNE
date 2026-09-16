[CmdletBinding()]
param(
    [switch]$Fast,
    [switch]$Full,
    [switch]$SkipDependencies,
    [switch]$SkipQodana,
    [string]$Base
)

$ErrorActionPreference = "Stop"

if ($Fast -and $Full) {
    throw "Choose either -Fast or -Full, not both."
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$wsl = Get-Command wsl.exe -ErrorAction SilentlyContinue
if (-not $wsl) {
    throw @"
WSL is required for Tideborne local CI because the existing runtime smoke tests use Linux-only
features such as xvfb, /proc/net/tcp, and FIFOs.

Install it once from an elevated PowerShell:
  wsl --install -d Ubuntu

Then restart Windows if prompted and rerun:
  .\scripts\local-ci.ps1
"@
}

$wslRoot = (& wsl.exe wslpath -a -u $repoRoot 2>&1 | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($wslRoot)) {
    throw "Could not translate the repository path into WSL: $repoRoot`n$wslRoot"
}

function Quote-Bash([string]$Value) {
    return "'" + $Value.Replace("'", "'\''") + "'"
}

$arguments = New-Object System.Collections.Generic.List[string]
if ($Fast) {
    $arguments.Add("--fast")
}
elseif ($Full) {
    $arguments.Add("--full")
}
else {
    $arguments.Add("--standard")
}

if ($SkipDependencies) {
    $arguments.Add("--skip-deps")
}
if ($SkipQodana) {
    $arguments.Add("--skip-qodana")
}
if (-not [string]::IsNullOrWhiteSpace($Base)) {
    $arguments.Add("--base")
    $arguments.Add($Base)
}

$bashArgs = ($arguments | ForEach-Object { Quote-Bash $_ }) -join " "
$quotedRoot = Quote-Bash $wslRoot
$command = "cd $quotedRoot && bash scripts/local_ci.sh $bashArgs"

Write-Host "Tideborne local CI" -ForegroundColor Cyan
Write-Host "Windows repo: $repoRoot"
Write-Host "WSL repo:     $wslRoot"
Write-Host ""

& wsl.exe bash -lc $command
$exitCode = $LASTEXITCODE

if ($exitCode -ne 0) {
    Write-Host ""
    Write-Host "TIDEBORNE LOCAL CI FAILED (exit $exitCode)" -ForegroundColor Red
    Write-Host "See build/local-ci/logs for the failing step."
    exit $exitCode
}

Write-Host ""
Write-Host "TIDEBORNE LOCAL CI PASSED" -ForegroundColor Green
