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
- **Commands & Configuration:**
  - Removed legacy Configurate 3 compatibility module (Sponge Configurate 3 for outdated plugins);
  - Removed all built-in commands except `/server reload|stop`;
  - Display settings removed: `motd`, `show-max-players`, `sample-players-in-ping`;
  - Removed bStats analytics and automatic creation of the `lang` and `plugins` folders;
  - Removed verbose address and command execution logging;

## Download & Running

The compiled JAR file can be **downloaded from the [Releases](https://github.com/Village-V-Studio/CleanVelocity/releases)** section on this GitHub. Requires Java 25 or higher.