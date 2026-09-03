# ActiveTime

> **Your world only progresses while you play.**

ActiveTime is a lightweight Minecraft Paper plugin designed for small, private survival and cooperative servers (such as servers with friends playing farming, progression, tech, or Stardew Valley-style gameplay).

When nobody is online, the Minecraft world simulation should not advance needlessly—crops shouldn't rot, farms shouldn't decay, day/night cycles shouldn't cycle through hundreds of unplayed days, and hostile mob spawns shouldn't run endlessly in loaded chunks.

ActiveTime solves this by automatically leveraging Minecraft's native simulation tick freeze mechanism when the server is empty, and resuming the simulation the instant a player joins.

---

## The Philosophy

> **World progression should depend on active play, not simply how long the server process has existed.**

---

## How It Works

ActiveTime continuously monitors player presence and manages the native simulation state:

```text
                  Player joins
                       │
                       ▼
                ┌─────────────┐
                │   RUNNING   │
                └─────────────┘
                       │
                  Last player
                     quits
                       │
                       ▼
                ┌─────────────┐
                │   FROZEN    │
                └─────────────┘
                       │
                  Player joins
                       │
                       ▼
                ┌─────────────┐
                │   RUNNING   │
                └─────────────┘
```

1. **Player joins:** The server simulation resumes immediately.
2. **Multiple players online:** Players can join and leave freely without affecting the simulation as long as at least one player remains online.
3. **Last player quits:** The simulation automatically freezes once the online player count drops to 0 (if `freeze.when-empty` is enabled).
4. **Player reconnects:** The simulation immediately unfreezes, continuing the world exactly where it stopped.
5. **Clean shutdown:** If the server or plugin shuts down while frozen, ActiveTime automatically restores normal ticking so the world is never left permanently frozen.

---

## Native Tick Freezing

ActiveTime does **NOT** use fake freezing hacks:
- ❌ No manual world time resetting
- ❌ No entity teleportation or cancellation loops
- ❌ No tampering with random tick speed
- ❌ No NMS or fragile bytecode manipulation

ActiveTime interacts directly with Paper's native `ServerTickManager` (the API equivalent of vanilla Minecraft's `/tick freeze` and `/tick unfreeze` added in Minecraft 1.20.3). The game engine itself pauses entity ticking, chunk ticking, weather, day/night cycle, block updates, and mob AI cleanly and efficiently.

---

## Platform Requirements

* **Minecraft Version:** Java Edition **1.20.3 or newer** (tested on 1.20.4)
* **Server Software:** [Paper](https://papermc.io/) or any compatible modern Paper fork (Purpur, Folia, etc.)
* **Java Version:** Java 17 or Java 21+

> [!NOTE]
> ActiveTime strictly targets Minecraft 1.20.3+ because it relies on the native simulation tick control introduced in 1.20.3. Versions below 1.20.3 are not supported.

---

## Installation

1. Ensure your server is running **Paper 1.20.3** or newer with Java 17 or 21.
2. Download `ActiveTime-0.1.0.jar` from the releases page (or build from source).
3. Place `ActiveTime-0.1.0.jar` into your server's `plugins/` directory.
4. Restart your server (or load it via your plugin manager).
5. ActiveTime will generate its default `config.yml` and begin managing the world simulation automatically.

---

## Configuration

The configuration file is located at `plugins/ActiveTime/config.yml`:

```yaml
# ==============================================================================
# ActiveTime Configuration
# Tagline: Your world only progresses while you play.
# ==============================================================================

# Master switch for automatic simulation management.
# Set to false to disable all automatic freeze/unfreeze behaviors.
enabled: true

# Freeze behavior settings
freeze:
  # Automatically freeze native Minecraft simulation when the last player leaves.
  when-empty: true

# Notification settings
messages:
  # Master switch for notification announcements.
  enabled: true

  # Prefix prepended to ActiveTime chat messages.
  prefix: "&8[&bActiveTime&8]&r "

  # Message broadcast/logged when simulation freezes due to no online players.
  freeze: "&eServer simulation frozen because no players are online."

  # Message broadcast/logged when simulation resumes upon player join.
  unfreeze: "&aPlayer activity detected. Server simulation resumed."
```

### Configuration Options

| Option | Type | Default | Description |
|---|---|---|---|
| `enabled` | boolean | `true` | Master switch. When `false`, ActiveTime disables all automatic freezing. |
| `freeze.when-empty` | boolean | `true` | When `true`, freezes simulation whenever the server becomes empty (0 players). |
| `messages.enabled` | boolean | `true` | Enables or disables broadcast chat messages and notifications. |
| `messages.prefix` | string | `&8[&bActiveTime&8]&r ` | Prefix displayed before ActiveTime chat messages. |
| `messages.freeze` | string | `&eServer simulation frozen...` | Message displayed when simulation enters frozen state. |
| `messages.unfreeze` | string | `&aPlayer activity detected...` | Message displayed when simulation resumes. |

---

## Commands & Permissions

### Commands

| Command | Aliases | Description | Default Permission |
|---|---|---|---|
| `/activetime` | `/at` | Displays basic plugin information and current simulation status. | `activetime.use` |
| `/activetime status` | `/at status` | Displays detailed status (ticks frozen, online players, config state). | `activetime.use` |
| `/activetime freeze` | `/at freeze` | Manually freezes the server simulation. | `activetime.admin` |
| `/activetime unfreeze` | `/at unfreeze` | Manually unfreezes the server simulation. | `activetime.admin` |
| `/activetime reload` | `/at reload` | Reloads `config.yml` and reconciles current state. | `activetime.admin` |

### Permissions

| Permission Node | Description | Default |
|---|---|---|
| `activetime.use` | Allows viewing basic info and status via `/activetime` and `/activetime status`. | Everyone (`true`) |
| `activetime.admin` | Allows manual freeze/unfreeze control and `/activetime reload`. | Server Operators (`op`) |

---

## Important Limitation to Note

> [!WARNING]
> **Real-World Timers vs. Minecraft Simulation Ticks**
>
> Freezing Minecraft's native simulation pauses all in-game ticking (crops, mobs, redstone, daytime cycle, chunk ticks, and Bukkit synchronous tasks).
>
> However, freezing the simulation **does not stop real-world wall clock time**.
> If another plugin or mod calculates progression using `System.currentTimeMillis()` or runs its own external asynchronous background thread, that external code will continue advancing according to real-world time.
>
> ActiveTime guarantees full native Minecraft simulation freezing, but cannot forcefully intercept third-party plugins that rely on wall-clock system timestamps.

---

## Future Architecture & Roadmap

Future versions of ActiveTime will introduce a public developer API allowing other plugins to hook into ActiveTime as a logical progression clock:

```java
// Future API Concept
ActiveTime.isPaused();
ActiveTime.getActiveTicks();
ActiveTime.getActiveSeconds();
ActiveTime.getActiveDays();
```

### Roadmap

* **v0.1.0 (MVP)**
  - Native Minecraft simulation freeze/unfreeze
  - Player join/quit detection with empty server auto-freeze
  - Safe startup and reload state reconciliation
  - Administrative commands (`/activetime [status|freeze|unfreeze|reload]`)
  - Configurable notifications and permissions
* **v0.2.x**
  - Configurable idle freeze delay / grace period
  - Discord webhook integration for freeze/unfreeze notifications
  - Granular world exclusions (if supported by native tick features)
* **v0.3.x**
  - Public ActiveTime Java API (`ActiveTimeAPI`)
  - Custom events: `ActiveTimeFreezeEvent`, `ActiveTimeUnfreezeEvent`
* **v0.4.x**
  - Active Clock / Active Day counter (in-game time that only elapses while players are active)
* **v0.5.x**
  - Integrations with popular farming and economy plugins
* **v1.0.0**
  - Stable, production-grade ActiveTime API

---

## Building from Source

### Prerequisites
- JDK 17 or JDK 21+
- Internet connection (to fetch Paper API dependencies)

### Build Steps

Clone the repository and run:

```bash
# On Linux / macOS
./gradlew build

# On Windows
.\gradlew.bat build
```

The compiled plugin JAR will be located in:
```text
build/libs/ActiveTime-0.1.0.jar
```

To run the automated test suite:

```bash
.\gradlew.bat test
```

---

## License

This project is licensed under the [MIT License](LICENSE).
