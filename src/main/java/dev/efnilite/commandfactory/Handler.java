package dev.efnilite.commandfactory;

import dev.efnilite.vilib.chat.Message;
import dev.efnilite.vilib.event.EventWatcher;
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

        if (player.isOp() && CommandFactory.getElevator().isOutdated()) {
            Message.send(player, "");
            Message.send(player, CommandFactory.MESSAGE_PREFIX + "Your version is outdated. Please visit the Spigot page to update.");
            Message.send(player, "");
        }
    }
}