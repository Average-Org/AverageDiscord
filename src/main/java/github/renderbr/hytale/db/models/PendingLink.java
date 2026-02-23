package github.renderbr.hytale.db.models;

import java.util.UUID;

public record PendingLink(UUID hytalePlayerId, long discordUserId, long messageId) {
}