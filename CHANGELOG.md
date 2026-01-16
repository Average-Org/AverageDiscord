# Changelog

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

