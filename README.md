# ActiveTime

> **Your world only progresses while you play.**

ActiveTime is a lightweight, zero-bloat Minecraft mod and plugin that automatically freezes Minecraft's native simulation when **no players are online**, and seamlessly resumes the simulation the instant someone connects.

---

## Why ActiveTime?

When nobody is playing on a Minecraft server, the world continues running needlessly:
- Crops grow, rot, and decay while everyone is asleep.
- Farms and mob grinders run continuously in loaded chunks.
- The in-game day/night cycle advances hundreds of unplayed days.
- Time-sensitive events and seasonal clocks tick away without players present.

ActiveTime solves this for small, cooperative, and private servers:
* Private & friends' servers
* Farming and progression servers
* Stardew-Valley-like survival gameplay
* Community smp servers

### A typical scenario

* **23:00 — Last player quits:** ActiveTime detects zero active players and freezes the world simulation instantly (`/tick freeze`).
* **00:00 to 07:00 — Overnight idle:** 8 hours pass in the real world, but 0 in-game ticks advance. Seasons stay still, crop growth timers pause, and mob spawners remain dormant.
* **07:00 — A friend joins early:** The simulation unfreezes before the player fully loads in. Everyone continues exactly where the group left off.

The world continues from essentially the exact simulation state instead of progressing while nobody is playing.

---

## ✨ Features

- **Native Tick Control:** Uses Minecraft 1.20.3+'s native engine-level simulation tick freeze (`ServerTickRateManager` / `/tick freeze`). No fake lag loops, no entity hacks.
- **Multi-Loader Support:** Official builds for **Fabric**, **Forge**, and **Paper**.
- **Multiplayer Aware:** Seamlessly handles multiple players joining and leaving. The server only freezes when the *last* player quits, and stays running as long as at least one player is online.
- **Shutdown Protection:** Automatically unfreezes the server if the mod/plugin is unloaded or stopped so your world is never stuck in a frozen state.
- **In-Game Commands:** Easy inspection and manual override commands (`/activetime [status|freeze|unfreeze|reload]`).
- **Configurable:** Highly customizable toggles, prefix, and notification messages.

---

## ⚡ Supported Platforms & Requirements

| Platform | Loader Version | Supported Minecraft Versions |
|---|---|---|
| **Paper** / Purpur | Paper 1.20.4+ | 1.20.3, 1.20.4, 1.20.6, 1.21+ |
| **Fabric** | Fabric Loader >= 0.15.0 | 1.20.3, 1.20.4, 1.20.6, 1.21+ |
| **Forge** | Forge >= 49.0.0 | 1.20.3, 1.20.4, 1.20.6, 1.21+ |

*Requires Java 17 or Java 21+.*

---

## 📦 Download & Installation

Download the appropriate JAR from the [Modrinth Project Page](https://modrinth.com/plugin/activetime):

* **Paper / Purpur servers:** Place `activetime-paper-1.0.0.jar` into your server's `plugins/` directory.
* **Fabric servers:** Place `activetime-fabric-1.0.0.jar` into your server's `mods/` directory.
* **Forge servers:** Place `activetime-forge-1.0.0.jar` into your server's `mods/` directory.

Restart your server. ActiveTime will generate its configuration file and begin managing simulation automatically.

---

## ⚙️ Configuration

### Paper (`plugins/ActiveTime/config.yml`)
```yaml
# Master switch for automatic simulation management.
enabled: true

# Freeze behavior settings
freeze:
  when-empty: true

# Notification settings
messages:
  enabled: true
  prefix: "&8[&bActiveTime&8]&r "
  freeze: "&eServer simulation frozen because no players are online."
  unfreeze: "&aPlayer activity detected. Server simulation resumed."
```

### Fabric & Forge (`config/activetime.properties`)
```properties
enabled=true
freeze.when-empty=true
messages.enabled=true
messages.prefix=&8[&bActiveTime&8]&r 
messages.freeze=&eServer simulation frozen because no players are online.
messages.unfreeze=&aPlayer activity detected. Server simulation resumed.
```

---

## 📋 Commands & Permissions

| Command | Description | Permission |
|---|---|---|
| `/activetime` | Shows summary of current state and player count | `activetime.use` (default: all) |
| `/activetime status` | Shows detailed status report | `activetime.use` (default: all) |
| `/activetime freeze` | Manually freeze server simulation | `activetime.admin` (default: op) |
| `/activetime unfreeze` | Manually resume server simulation | `activetime.admin` (default: op) |
| `/activetime reload` | Reload configuration from disk | `activetime.admin` (default: op) |

---

## ⚠️ Important Limitation

> **Real-World Wall Clock Time vs. Minecraft Simulation Ticks**
>
> ActiveTime controls Minecraft's simulation ticks. Freezing the simulation pauses crop growth, mob AI, entity movement, chunk ticking, daylight cycle, weather, and game loop schedules.
>
> However, mods or plugins that independently use `System.currentTimeMillis()`, external background threads, or real-world timers outside the Minecraft tick system may continue operating while the simulation is frozen.

---

## 🛠️ Building from Source

```powershell
# Run the test suite across modules
.\gradlew.bat test

# Build all loader JARs
.\gradlew.bat build
```

Compiled artifacts will be collected in `build/libs/`:
* `activetime-fabric-1.0.0.jar`
* `activetime-forge-1.0.0.jar`
* `activetime-paper-1.0.0.jar`

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
