# CleanVelocity

**CleanVelocity** is a stripped-down and optimized fork of Velocity, with many unnecessary features, configuration options, and commands removed for maximum stability and simplicity. The main goal is to remove all optional built-in functionality so that the proxy can be fully customized through plugins based on your specific needs.

Original [Velocity](https://github.com/PaperMC/Velocity) by [PaperMC](https://papermc.io). Velocity is licensed under the GPLv3 license.

**You can use [CleanPaper](https://github.com/Village-V-Studio/CleanPaper) as a backend server for your CleanVelocity.**

## What was changed / removed:

- Removed bStats analytics;
- Removed `lang` folder;
- Connection and timeout settings: `login-ratelimit`, `connection-timeout`, `read-timeout`;
- Display settings: `motd`, `show-max-players`;
- Anti-spam settings: `command-rate-limit`, `forward-commands-if-rate-limited`, `kick-after-rate-limited-commands`, `tab-complete-rate-limit`, `kick-after-rate-limited-tab-completes`;
- Specific features and logging: `kick-existing-players`, `sample-players-in-ping`, `log-player-connections`, `enable-player-address-logging`, `announce-forge`, `forwarding-secret-file`;
- Other system settings: `bungee-plugin-message-channel`, `show-ping-requests`, `failover-on-unexpected-server-disconnect`, `announce-proxy-commands`, `log-command-executions`, `prevent-client-proxy-connections`;
- Removed the `[query]` and `[packet-limiter]` system;
- Removed the `[forced-hosts]` section;
- Removed all built-in commands. Only `/server dump|heap|reload|stop` remains;
- And some other minor changes.

## Download & Running

The compiled JAR file can be **downloaded from the [Releases](https://github.com/Village-V-Studio/CleanVelocity/releases)** section on this GitHub. Requires Java 21 or higher.