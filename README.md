[![Get quality hosting!](https://i.imgur.com/4rIaoNo.jpeg)](https://billing.kinetichosting.com/aff.php?aff=1261)

> **[Kinetic Hosting](https://billing.kinetichosting.com/aff.php?aff=1261):** Check out my game hosting partner! They offer fast, affordable hosting with excellent customer support!

# AverageDiscord

A comprehensive Discord-Hytale server bridge plugin that seamlessly connects your Hytale game server with Discord, enabling real-time chat synchronization and server status notifications.

## Overview

**AverageDiscord** is a powerful plugin designed for Hytale servers that establishes a two-way communication bridge between your in-game chat and a designated Discord channel. Monitor server activity, receive player join/leave notifications, and allow Discord members to chat with players on your Hytale server—all in real-time.

## What's new in 0.2.4

- Added support for player death event tracking
- Improved player join/leave tracking. Players who go to different worlds within the universe will no longer be considered as leaving and joining the server again.
- Updated project dependencies to improve performance and JAR size

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
- Support for 6 distinct output types: All, Chat, Join/Leave, Server State, Internal Log, Desc Status
- Configure multiple channels with different output filters
- Organize your Discord server with specialized channels for different events

### 🤖 **Discord Slash Commands**
- `/players` - View the list of currently online players
- `/status` - Get a comprehensive server status report
  - Server TPS (Ticks Per Second)
  - Memory usage
  - Online player list
  - Server version

### ⚙️ **Easy Configuration**
- JSON-based configuration file with multi-channel support
- Hot-reload functionality without restarting the server
- Customizable Discord bot prefix for in-game messages
- Custom bot activity/status message
- Support for formatted color codes in Discord messages
- Optional active player count display

### 📊 **Dynamic Status Updates**
- Automatic bot activity updates with current player count every 10 minutes
- Optional channel description updates with server status information (e.g., player count)

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

(Rest of the README remains the same as in the previous version)