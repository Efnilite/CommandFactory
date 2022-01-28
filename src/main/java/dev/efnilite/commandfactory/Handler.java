package dev.efnilite.commandfactory;

import dev.efnilite.commandfactory.command.FCommand;
import dev.efnilite.commandfactory.util.Util;
import dev.efnilite.fycore.event.EventWatcher;
import dev.efnilite.fycore.util.Version;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class Handler implements EventWatcher {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isOp() && CommandFactory.IS_OUTDATED) {
            if (Version.isHigherOrEqual(Version.V1_16)) {
                BaseComponent[] message = new ComponentBuilder()
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Efnilite/Ethereal/releases/latest"))
                        .append("> ").color(ChatColor.RED).bold(true).append("Your CommandFactory version is outdated. ").color(ChatColor.GRAY)
                        .bold(false).append("Click here").color(ChatColor.RED).underlined(true).append(" to visit the latest version!").color(ChatColor.GRAY)
                        .underlined(false).create();

                player.spigot().sendMessage(message);
            } else {
                player.sendMessage(Util.colour(FCommand.MESSAGE_PREFIX + "Your CommandFactory version is outdated. " +
                        "Visit the Spigot page to download the latest version."));
            }
        }
    }
}