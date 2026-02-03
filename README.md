[![Get quality hosting!](https://i.imgur.com/4rIaoNo.jpeg)](https://billing.kinetichosting.com/aff.php?aff=1261)

> **[Kinetic Hosting](https://billing.kinetichosting.com/aff.php?aff=1261):** Check out my game hosting partner! They offer fast, affordable hosting with excellent customer support!

# AverageDiscord

A Discord bridge plugin for Hytale servers. Forwards chat messages, player join/leave events, and server status between the game server and Discord channels.

## Features

- Chat synchronization (in-game ↔ Discord)
- Player join/leave notifications
- Server startup/shutdown notifications
- Player death notifications
- Multi-channel message routing (assign different event types to different Discord channels)
- Discord slash commands: `/players`, `/status`, `/execute`
- Hot-reload configuration
- Bot status updates with player count every 10 minutes

## Version 0.3.0 Changes

- Added `/execute` command to run server console commands from Discord (admin only)
- Added `HYTALE_DISCORD_TOKEN` environment variable support for bot token
- Improved async command processing and error handling

## Setup

1. Download the latest JAR from releases
2. Place it in your Hytale server's plugins folder
3. Create a Discord bot at [Discord Developer Portal](https://discord.com/developers/applications)
4. Start the server to generate `discord_bridge.json`
5. Edit the config with your bot token and channel IDs
6. Run `/discordbridge reload` or restart the server

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
      "type": ["join_leave", "server_state", "desc_status"]
    },
    {
      "channelId": "4567890123",
      "type": ["internal_log"]
    }
  ],
  "botActivityMessage": "Playing Hytale!",
  "discordIngamePrefix": "&9[Discord] ",
  "showActivePlayerCount": true,
  "stripLinksInChat": true,
  "stripPingsInChat": true
}
```

### Configuration Options

| Option | Type | Description |
|--------|------|-------------|
| `botToken` | String | Discord bot token (or use `HYTALE_DISCORD_TOKEN` env var) |
| `channels` | Array | Channel routing config |
| `botActivityMessage` | String | Bot status text in Discord |
| `discordIngamePrefix` | String | Prefix for Discord messages in-game (supports color codes) |
| `showActivePlayerCount` | Boolean | Show player count in bot status |
| `stripLinksInChat` | Boolean | Remove links from in-game chat before sending to Discord |
| `stripPingsInChat` | Boolean | Remove mentions from in-game chat before sending to Discord |

### Channel Configuration

| Field | Description |
|-------|-------------|
| `channelId` | Discord channel ID |
| `type` | Array of output types (e.g., `["chat", "join_leave"]`) |

### Output Types

| Type | Sends |
|------|-------|
| `chat` | In-game chat messages |
| `join_leave` | Player joins and leaves |
| `server_state` | Server startup and shutdown |
| `player_death` | Player death messages |
| `internal_log` | Plugin logs |
| `desc_status` | Channel topic updates with player count |
| `all` | All of the above except `internal_log` |

Note: `internal_log` is not included in `all` and must be explicitly configured if you want it.

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

Messages are routed to Discord channels based on their output type. Each channel in the config specifies which types it receives.

**In-Game → Discord**: Chat messages are sent to channels with `chat` or `all` types.

**Discord → In-Game**: Messages in channels with `chat` or `all` types are sent to the game with a configurable prefix.

**Events and their output types:**
- `chat` - In-game chat messages
- `join_leave` - Player join/disconnect
- `server_state` - Server startup/shutdown
- `player_death` - Player deaths
- `internal_log` - Plugin logs
- `desc_status` - Channel topic updates with player count
- `all` - All of the above except `internal_log`

## Dependencies

The JAR bundles all dependencies. No additional libraries needed.

- Hytale Server
- AverageHytaleCore
- JDA 6.3.0
- Gson
- SLF4J

## Requirements

- Java 11+
- Hytale server
- Discord bot token with permissions: `View Channels`, `Send Messages`, `Read Message History`
- Bot intents: `GUILD_MESSAGES`, `MESSAGE_CONTENT`

## Commands

### `/discordbridge reload`
Reloads the plugin configuration without requiring a server restart.

**Aliases:** `db`, `discord`

**Usage:** `/discordbridge reload`

**Permission:** Server admin/operator

### `/execute`
Run server console commands from Discord. Requires Discord administrator role and must be run in a channel with the `all` output type configured.

**Usage:** `/execute command:<command>`

**Example:** `/execute command:say Hello from Discord!`

## Color Codes

The `discordIngamePrefix` supports Minecraft color codes: `&9` (blue), `&a` (green), `&c` (red), `&e` (yellow), `&f` (white), and other standard codes.

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

Average (js3 on Hytale)