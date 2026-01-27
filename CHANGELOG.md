# Changelog

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