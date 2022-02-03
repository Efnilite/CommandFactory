package dev.efnilite.commandfactory;

import dev.efnilite.commandfactory.command.plugin.FCommand;
import dev.efnilite.commandfactory.util.Util;
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
//                Message.send(player,
//                        "<#711FDE>> <grey>Your CommandFactory is outdated. " +
//                                "<underlined><click:open_url:https://github.com/Efnilite/CommandFactory/releases/latest><#711FDE>Click here</click></underlined> " +
//                                "<grey>to visit the latest version.");
            } else {
                player.sendMessage(Util.colour(FCommand.MESSAGE_PREFIX + "Your CommandFactory version is outdated. " +
                        "Visit the Spigot page to download the latest version."));
            }
        }
    }
}