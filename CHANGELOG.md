# Changelog

## [0.3.1] - 2026-02-10

### ✨ Added

- **Intelligent ANSI Translation** - Server logs in Discord now support 256-color and RGB sequences by automatically mapping them to the nearest Discord-compatible color.
- **Log Batching** - Internal logs are now buffered and sent in batches, preventing Discord rate limits during high-activity periods.

### 🔄 Changed

- **Performance Optimizations** - Rewrote player tracking and message routing logic for significantly better server performance.
- **Enhanced Chat Relay** - Improved chat formatting with more robust fallbacks and more reliable message sanitization.

### 🐛 Fixed

- Fixed an issue where complex ANSI codes would incorrectly render as background colors in Discord.
- Fixed a bug where players could occasionally trigger duplicate join messages.

## [0.3.0] - 2026-02-01

### ✨ Added

- `/execute` command - Discord admins can now run server console commands from Discord
- SLF4J logging provider for JDA integration with Hytale's native logger
- `HYTALE_DISCORD_TOKEN` environment variable support for bot token configuration

### 🔄 Changed

- Bot initialization is now async with better error handling
- `/discordbridge reload` now uses async restart flow
- Discord commands are pre-registered and defer replies to prevent timeouts
- Build config improved to resolve SLF4J conflicts and relocate dependencies

### 🐛 Fixed

- SLF4J module conflicts
- Bot token validation (rejects placeholder values)

## [0.2.5] - 2026-01-28

### ✨ Added

- **Link Stripping** - Adds a new configuration option: `stripLinksInChat`. This is set to true by default. When enabled, attempted links in chat messages will be stripped from the message before being sent to Discord.
- **Ping Stripping** - Adds a new configuration option: `stripPingsInChat`. This is set to true by default. When enabled, attempted Discord mentions in chat messages will be stripped from the message before being sent to Discord.

## [0.2.4] - 2026-01-27

### ✨ Added

- **Player Death Event Support** - Track and report player death events in Discord

### 🔄 Changed

- **Project Dependencies** - Updated dependencies to improve overall stability and performance
- **Improved player join/leave tracking** - Players who go to different worlds within the universe will no longer be considered as leaving and joining the server again.


## [0.2.3] - 2026-01-25

### ✨ Added

- **Discord Slash Commands**
  - `/players` - View currently online players
  - `/status` - Get detailed server status, including TPS, memory usage, and online players
- **Command Localization** - Translated command descriptions using Hytale's Message translation system

### 🔄 Changed

- **Command System** - Implemented dynamic Discord slash command registration
- **Status Command** - Added detailed server vitals reporting with color-coded TPS indication

## [0.2.2] - 2026-01-18

### ✨ Added

- **DiscordBotService** - New service for enhanced Discord bot management and status updates
- **Automatic Activity Updates** - Bot activity now updates every 10 minutes with current player count when enabled
- **Channel Description Updates** - Automatically update designated Discord channel descriptions with server status (e.g., player count)
- **desc_status Output Type** - New channel output type for routing channel description updates
- **Enhanced Internal Logging Implementation** - Improved internal logging system for better debugging and monitoring

### 🔄 Changed

- **Bot Status Management** - Improved handling of bot online/offline states and periodic updates

## [0.1.3] - 2026-01-15

### ✨ Added

- **Multi-Channel Output System** - Route different message types to different Discord channels
- **5 Channel Output Types**:
  - `all` - All messages (chat, join/leave, server state)
  - `chat` - Player chat messages
  - `join_leave` - Player join/disconnect notifications
  - `server_state` - Server startup/shutdown notifications
  - `internal_log` - Plugin logs and debug messages
- **Flexible Configuration** - Configure multiple channels with different output filters
- **ChannelOutputTypes enum** - Strongly-typed output type system with GSON serialization
- **Message routing methods** - `GetOfType()` and `SendMessageToType()` for efficient delivery

### 🔄 Changed

- **Configuration structure**: Single channel → multi-channel array
  ```json
  // Old
  "mainChatChannelId": "123456789"

  // New
  "channels": [{"channelId": "123456789", "type": ["all"]}]
  ```
- **Message delivery**: Type-based routing instead of broadcasting to single channel
- **New config field**: `showActivePlayerCount` for optional player count display

### 📝 Note

**Configuration migration required** - Update your `discord_bridge.json` file to the new format. To do so, simply delete the old config and let it be recreated on startup.