# AverageDiscord

A comprehensive Discord-Hytale server bridge plugin that seamlessly connects your Hytale game server with Discord, enabling real-time chat synchronization and server status notifications.

## Overview

**AverageDiscord** is a powerful plugin designed for Hytale servers that establishes a two-way communication bridge between your in-game chat and a designated Discord channel. Monitor server activity, receive player join/leave notifications, and allow Discord members to chat with players on your Hytale server—all in real-time.

## Features

### 🔗 **Bi-Directional Chat Bridge**
- Real-time synchronization between in-game chat and Discord
- Formatted message support with proper text parsing
- Discord messages sent to the designated channel appear in-game
- In-game chat messages are forwarded to Discord with player names

### 📢 **Server Status Notifications**
- Automatic server startup notification
- Automatic server shutdown notification
- Player join notifications with player names
- Player disconnect notifications with player names
- Emoji-enhanced messages for better visibility (✅ ☒ ➡️ ⬅️)

### 📡 **Flexible Multi-Channel Output**
- Route different message types to different Discord channels
- Support for 5 distinct output types: All, Chat, Join/Leave, Server State, Internal Log
- Configure multiple channels with different output filters
- Organize your Discord server with specialized channels for different events

### ⚙️ **Easy Configuration**
- JSON-based configuration file with multi-channel support
- Hot-reload functionality without restarting the server
- Customizable Discord bot prefix for in-game messages
- Custom bot activity/status message
- Support for formatted color codes in Discord messages
- Optional active player count display

### 🛠️ **Admin Commands**
- `/discordbridge reload` - Reloads plugin configuration on-the-fly
- Aliases: `/gm`, `/agm`, `/groupman`
- Graceful config synchronization

## Installation

1. **Download** the latest AverageDiscord JAR file
2. **Place** it in your Hytale server's plugins folder
3. **Create a Discord bot** at [Discord Developer Portal](https://discord.com/developers/applications)
4. **Configure** the plugin (see Configuration section below)
5. **Restart** your Hytale server or use `/discordbridge reload`

## Configuration

After first run, a `discord_bridge.json` configuration file will be created in your server's `AverageDiscord` folder.

### Configuration File Example

```json
{
  "botToken": "your_bot_token_here",
  "channels": [
    {
      "channelId": "1234567890",
      "type": ["all"]
    },
    {
      "channelId": "2345678901",
      "type": ["chat"]
    },
    {
      "channelId": "3456789012",
      "type": ["join_leave", "server_state"]
    },
    {
      "channelId": "4567890123",
      "type": ["internal_log"]
    }
  ],
  "botActivityMessage": "Playing Hytale!",
  "discordIngamePrefix": "&9[Discord] ",
  "showActivePlayerCount": true
}
```

### Configuration Options

| Option | Type | Description | Example |
|--------|------|-------------|---------|
| `botToken` | String | Your Discord bot token from the Developer Portal | `"token_here"` |
| `channels` | Array | Array of channel configurations (see Channel Configuration below) | `[{...}]` |
| `botActivityMessage` | String | The bot's status message in Discord | `"Playing Hytale!"` |
| `discordIngamePrefix` | String | Prefix for Discord messages in-game (supports color codes) | `"&9[Discord] "` |
| `showActivePlayerCount` | Boolean | Show active player count in bot activity status | `true` |

### Channel Configuration

Each channel object in the `channels` array has the following structure:

| Option | Type | Description | Example |
|--------|------|-------------|---------|
| `channelId` | String | The Discord channel ID where messages of this type will be sent | `"1234567890"` |
| `type` | Array of Strings | Output types for this channel (see Output Types below) | `["chat", "join_leave"]` |

### Channel Output Types

Configure which types of messages are sent to each channel:

| Type | Description |
|------|-------------|
| `all` | All messages except internal logs (chat, join/leave, server state) |
| `chat` | In-game player chat messages |
| `join_leave` | Player join and disconnect notifications |
| `server_state` | Server startup and shutdown notifications |
| `internal_log` | Internal plugin logging and debugging messages |

**Note:** The `all` output type does not include `internal_log`. If you want internal logs, you must explicitly add the `internal_log` type to a channel.

### How to Get Your Bot Token and Channel ID

1. **Bot Token:**
   - Go to [Discord Developer Portal](https://discord.com/developers/applications)
   - Create a new application or select an existing one
   - Navigate to the "Bot" tab
   - Click "Add Bot" (if new)
   - Copy the token under the "TOKEN" section

2. **Channel ID:**
   - Enable Developer Mode in Discord (User Settings → Advanced → Developer Mode)
   - Right-click on your desired channel
   - Select "Copy Channel ID"
   - Paste it in the configuration

## How It Works

### Message Routing
The plugin uses a flexible multi-channel system to route different message types to different Discord channels. Each channel is configured with one or more output types that determine what messages it receives.

### Chat Synchronization
- **In-Game → Discord**: When a player types in chat, the message is sent to all channels configured with the `chat` or `all` output types
- **Discord → In-Game**: When a non-bot user sends a message in a channel configured with the `chat` or `all` output types, it appears in-game with a customizable prefix

### Server Events
The plugin monitors and reports the following events:

- **Player Join**: Sent to channels with `join_leave` or `all` output types
- **Player Disconnect**: Sent to channels with `join_leave` or `all` output types
- **Server Startup**: Sent to channels with `server_state` or `all` output types
- **Server Shutdown**: Sent to channels with `server_state` or `all` output types
- **Player Chat**: Sent to channels with `chat` or `all` output types
- **Internal Logs**: Sent only to channels explicitly configured with `internal_log` type

### Event Listeners
The plugin registers listeners for:
- `PlayerChatEvent` - Forwards in-game chat to Discord
- `PlayerReadyEvent` - Notifies Discord when players join
- `PlayerDisconnectEvent` - Notifies Discord when players leave
- `AllWorldsLoadedEvent` - Notifies Discord when the server starts
- `ShutdownEvent` - Notifies Discord when the server stops

## Dependencies

Please note: The JAR you download is a shadowed JAR that bundles all dependencies, so you do not need to install any additional libraries. These are listed solely for reference and transparency.

- **Hytale Server** - The core server implementation (HytaleServer.jar)
- **AverageHytaleCore** - Core utilities library
- **JDA (Java Discord API)** - Version 6.3.0 for Discord bot functionality
- **Gson** - JSON parsing and serialization for configuration
- **SLF4J** - Logging implementation

## Requirements

- Java 11 or higher
- Active Hytale server installation
- Discord bot with proper permissions:
  - `View Channels`
  - `Send Messages`
  - `Read Message History`
  - Intents: `GUILD_MESSAGES`, `MESSAGE_CONTENT`

## Commands

### `/discordbridge reload`
Reloads the plugin configuration without requiring a server restart.

**Aliases:** `gm`, `agm`, `groupman`

**Usage:** `/discordbridge reload`

**Permission:** Server admin/operator

## Color Codes

The `discordIngamePrefix` supports Minecraft color codes for customizing message appearance:
- `&9` - Blue
- `&a` - Green
- `&c` - Red
- `&e` - Yellow
- `&f` - White
- And many more standard Minecraft color codes

## Troubleshooting

### Bot Token Invalid
- Verify the bot token is correctly copied from the Discord Developer Portal
- Ensure you've copied the entire token without extra spaces
- Regenerate the token if necessary

### Channel ID Not Found
- Ensure Developer Mode is enabled in Discord
- Verify the channel ID is correct (should be numeric)
- Ensure the bot has access to the channel

### Messages Not Appearing in Discord
- Check that the bot has permission to send messages in the channel
- Verify the channel ID in the configuration is correct
- Ensure the channel is configured with the correct output types (`chat`, `all`, `join_leave`, `server_state`, or `internal_log`)
- Check that at least one channel is configured with the message type you're trying to send
- Check server logs for any errors
- Try using `/discordbridge reload` to refresh the configuration

### Messages Not Appearing In-Game
- Verify that the Discord bot has permission to send messages in the configured channels
- Ensure the channel IDs in the configuration are correct
- Make sure the Discord channel is configured with `chat` or `all` output types to receive Discord messages and relay them in-game

### Configuration Issues
- Ensure the `discord_bridge.json` file is valid JSON (use a JSON validator if needed)
- Verify that all required fields are present: `botToken`, `channels`, `botActivityMessage`, `discordIngamePrefix`
- Check that the `type` array in each channel contains valid output types
- Try using `/discordbridge reload` after making configuration changes

### Bot Offline
- Verify the bot token is valid
- Check that your server has internet connectivity
- Look for error messages in the server console

## Author

**Average** (js3 on Hytale)

---

**Note:** This plugin requires a valid Discord bot token and proper configuration to function. Ensure all prerequisites are met before installation.

