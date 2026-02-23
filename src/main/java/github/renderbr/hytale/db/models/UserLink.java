package github.renderbr.hytale.db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user_links")
public class UserLink {
    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false)
    public String discordUserId;

    @DatabaseField(canBeNull = false)
    public String hytaleUserId;

    public UserLink() {}

    public UserLink(String discordUserId, String hytaleUserId) {
        this.discordUserId = discordUserId;
        this.hytaleUserId = hytaleUserId;
    }
}
