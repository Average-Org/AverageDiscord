package github.renderbr.hytale.models.playerdeath;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ADPlayerDeathSystem extends DeathSystems.OnDeathSystem {
    @Override
    public void onComponentAdded(@NotNull Ref<EntityStore> ref, @NotNull DeathComponent deathComponent, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {
        if (AverageDiscord.instance == null) {
            return;
        }

        Player playerComponent = store.getComponent(ref, Player.getComponentType());

        if (playerComponent == null) {
            return;
        }

        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.PLAYER_DEATH,
                Message.translation("server.bot.averagediscord.playerdeath")
                        .param("player", playerComponent.getDisplayName())
                        .getAnsiMessage());
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }
}
