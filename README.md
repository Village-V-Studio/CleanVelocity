# CleanVelocity

**CleanVelocity** is an extremely stripped-down, bare-bones, and performance-focused fork of Velocity. It intentionally strips away non-essential built-in mechanics, configuration bloat, and default protections to provide a pure, lightweight networking proxy.

The core philosophy of CleanVelocity is to serve as a **clean slate**: a minimal foundation designed for developers and server networks to implement their own security, moderation, and routing features entirely through plugins or custom code modifications, without the proxy getting in the way.

Original [Velocity](https://github.com/PaperMC/Velocity) by [PaperMC](https://papermc.io). Licensed under the GPLv3 license.

> [!WARNING]
> ### Security & Protection Notice
> CleanVelocity does **not** provide built-in anti-spam, packet limiting, command rate-limiting, or chat filtering. All internal throttle checks and packet validations have been intentionally removed for maximum raw throughput.
> 
> If you deploy CleanVelocity on a public network, **you are responsible for implementing your own security layer** (e.g. external firewall/anti-DDoS mitigation, reverse proxy protection, or dedicated security plugins).

## What was changed / removed:

- **Security & Validation:**
  - Removed chat packet validation and illegal character checks;
  - Removed anti-spam & rate-limits: `command-rate-limit`, `forward-commands-if-rate-limited`, `kick-after-rate-limited-commands`, `tab-complete-rate-limit`, `kick-after-rate-limited-tab-completes`;
  - Removed `[packet-limiter]` and connection rate limits (`login-ratelimit`);
- **Networking & Systems:**
  - Removed `[query]`, `[forced-hosts]`, and `[ping-passthrough]` systems;
  - Connection & fallback tweaks: `connection-timeout`, `read-timeout`, `try = ["server"]`, `failover-on-unexpected-server-disconnect`;
  - Removed `bungee-plugin-message-channel` and `prevent-client-proxy-connections`;
  - Removed legacy Minecraft pre-1.7.2 ping and handshake protocol support (Minecraft 1.6.4 and older);
- **Commands & Configuration:**
  - Removed legacy Configurate 3 compatibility module (Sponge Configurate 3 for outdated plugins);
  - Removed all built-in commands except `/server reload|stop`;
  - Display settings removed: `motd`, `show-max-players`, `sample-players-in-ping`;
  - Removed bStats analytics and automatic creation of the `lang` and `plugins` folders;
  - Removed verbose address and command execution logging;
- **Dependencies & Bytecode Cleanup:**
  - Removed `jopt-simple` and `ProxyOptions` (command-line argument parsing);
  - Removed `com.spotify:completable-futures` (Spotify CompletableFuture utilities; replaced with standard Java `future.exceptionNow()`);
  - Removed `space.vectrix.flare:flare` and `flare-fastutil` (concurrent map utilities; replaced with standard Java `ConcurrentHashMap`);
  - Replaced legacy `checker-qual` with standard `jspecify` annotations (`@Nullable`, `@NonNull`);
  - Removed dead `toml4j` dependency from the API module;
  - Removed unused `netty-codec-http` dependency;
  - Excluded unused Netty modules (`netty-codec-protobuf`, `netty-codec-marshalling`);
  - Removed redundant `asm` dependency (handled via `lmbda`);

## Compatibility & System Analysis

### What Works
- **Velocity API (100% Core Functionality):**
  - **Events & Listeners**: All event registration, prioritization, execution, and custom plugin events (`ProxyInitializeEvent`, `ServerPreConnectEvent`, `LoginEvent`, `PlayerChatEvent`, etc.) remain fully operational.
  - **Scheduler**: The complete task scheduling system (`Scheduler`, `ScheduledTask`, async execution, delay, repetition) works without restrictions.
  - **Commands**: Brigadier command trees and `SimpleCommand` handlers operate normally.
  - **Plugin Messaging**: Plugin message encoders, decoders, and channel registration work across all proxy connections.
  - **Server Management**: Dynamic runtime registration and deregistration of backend servers (`ServerMap`, `RegisteredServer`) function as expected.
  - **Modern Adventure API**: Adventure text components, MiniMessage, serializers, and key storage are intact.
  - Standard plugins that rely on the public Velocity API, Adventure, Guice, Guava, Caffeine, and Gson work out-of-the-box.

### What Does NOT Work / Important Caveats
- **Plugins relying on proxy classpath leakage:**
  - If an older plugin relies on internal libraries like `net.sf.jopt-simple`, `com.spotify:completable-futures`, `space.vectrix.flare`, or `com.moandjiezana.toml.Toml` (`toml4j`) being present in the proxy's runtime classpath (rather than shading them into its own jar), it will throw a `NoClassDefFoundError`. Modern plugins should always bundle their own internal dependencies.
  - Very old Velocity 1.x plugins that depend on Sponge Configurate 3 are not supported.
- **Pre-1.7.2 Minecraft Clients (< 1.7.2):**
  - Ancient legacy ping packets from Minecraft 1.6.4 or older are rejected at the network level.
- **Command-line startup flags:**
  - CLI startup arguments (such as `--port`, `--haproxy`, `--add-server`, `--ignore-config-servers`, or `-h`/`--help`) are not parsed. Port binding, proxy protocol, and backend servers must be defined in `velocity.toml`.
- **Built-in rate limiting and chat filtering:**
  - CleanVelocity does not filter illegal chat characters or throttle packet floods. Networks must use an external mitigation layer (firewall, DDoS protection, or security plugins).

## Building & Running

Pre-compiled binary releases can also be **downloaded directly from the [Releases](https://github.com/Village-V-Studio/CleanVelocity/releases)** section.

### Requirements
- **Java 25** or higher.

### Building from Source
Clone the repository and build the project using Gradle:

- **Linux / macOS:**
  ```bash
  ./gradlew build
  ```
- **Windows:**
  ```cmd
  gradlew.bat build
  ```

### Build Artifacts Location
Once the build completes, the compiled JARs can be found in:
- **Proxy Server (Executable JAR):**
  `proxy/build/libs/CleanVelocity-<version>-all.jar`
- **Proxy API (for plugin development):**
  `api/build/libs/velocity-api-<version>.jar`

### Running
Run the compiled proxy executable with Java 25:
```bash
java -jar CleanVelocity-<version>-all.jar
```
