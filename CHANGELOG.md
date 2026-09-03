# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-09-03

### Added
- Native Minecraft simulation freeze/unfreeze based on player presence.
- Automatic idle freezing when the last player leaves (`freeze.when-empty: true`).
- Automatic simulation unfreezing when a player joins the server.
- Safe startup and reload state reconciliation.
- Shutdown safety: automatically unfreezes simulation if the plugin is unloaded while frozen.
- Commands:
  - `/activetime` (or `/at`): Display current plugin status and simulation state.
  - `/activetime status`: Detailed state report (online players, freeze state, auto-freeze mode).
  - `/activetime freeze`: Manually freeze server simulation (requires `activetime.admin`).
  - `/activetime unfreeze`: Manually unfreeze server simulation (requires `activetime.admin`).
  - `/activetime reload`: Reload configuration and reconcile state (requires `activetime.admin`).
- Configurable broadcast and console notification messages.
- Command tab-completion for subcommands with permission filtering.

