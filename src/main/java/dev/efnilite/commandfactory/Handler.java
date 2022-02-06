package dev.efnilite.commandfactory;

import dev.efnilite.fycore.chat.Message;
import dev.efnilite.fycore.event.EventWatcher;
import dev.efnilite.fycore.util.Version;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Handles events
 */
@ApiStatus.Internal
public class Handler implements EventWatcher {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isOp() && CommandFactory.IS_OUTDATED) {
            if (Version.isHigherOrEqual(Version.V1_16)) {
                Message.send(player,
                        CommandFactory.MESSAGE_PREFIX + "Your version is outdated. " +
                                "Please <underline>visit the Spigot page</underline> to update.");
            } else {
                Message.send(player, CommandFactory.MESSAGE_PREFIX + "Your CommandFactory version is outdated. Please update!");
            }
        }
    }
}