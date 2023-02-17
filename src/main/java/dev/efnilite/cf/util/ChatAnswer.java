package dev.efnilite.cf.util;

import dev.efnilite.vilib.ViMain;
import dev.efnilite.vilib.event.EventWatcher;
import dev.efnilite.vilib.util.Strings;
import dev.efnilite.vilib.util.Task;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Class for making listening to player answers in chat really easy
 */
public class ChatAnswer implements EventWatcher {

    /**
     * The player
     */
    private final Player player;

    /**
     * The text, when entered, which will disable the instance of this class
     */
    private final String cancelText;

    /**
     * The amount of chars that have to be added, removed or changed to get the change message;
     */
    private int matchDistance = 2;

    /**
     * What to do after the message has been sent. This BiConsumer provides the answer and the player instance.
     */
    private BiConsumer<Player, String> postMessage;

    /**
     * What to do if the message is cancelled
     */
    private Consumer<Player> cancelMessage;

    /**
     * Constructor.
     *
     * @param   player
     *          The player of which the chat will be monitored for answers
     *
     * @param   cancelText
     *          The text, which on entering, will cancel this answer listener.
     */
    public ChatAnswer(Player player, String cancelText) {
        this.player = player;
        this.cancelText = cancelText;

        register();
    }

    /**
     * Updates the match distance for the cancel string
     *
     * @param   matchDistance
     *          The distance
     *
     * @return the instance of this class
     */
    public ChatAnswer matchDistance(int matchDistance) {
        this.matchDistance = matchDistance;
        return this;
    }

    /**
     * What will happen before the answer is given.
     * The consumer will be executed the moment it's assigned.
     *
     * @param   consumer
     *          What to do. The player is given.
     *
     * @return the instance of this class
     */
    public ChatAnswer pre(Consumer<Player> consumer) {
        consumer.accept(player);
        return this;
    }

    /**
     * What will happen after the answer is given
     *
     * @param   consumer
     *          What to do after the answer. The player and the answer are given.
     *
     * @return the instance of this class
     */
    public ChatAnswer post(BiConsumer<Player, String> consumer) {
        this.postMessage = consumer;
        return this;
    }


    /**
     * What will happen if the answer is cancelled
     *
     * @param   consumer
     *          What to do on cancel. The player is given.
     *
     * @return the instance of this class
     */
    public ChatAnswer cancel(Consumer<Player> consumer) {
        this.cancelMessage = consumer;
        return this;
    }

    @EventHandler(priority = EventPriority.HIGHEST) // highest to prevent interference from chat plugins
    public void chat(AsyncPlayerChatEvent event) {
        if (event.getPlayer() != player) {
            return;
        }

        String message = event.getMessage();
        event.setCancelled(true);

        if (Strings.getLevenshteinDistance(cancelText, message) > matchDistance) {
            if (postMessage == null) {
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
                return;
            }
            Task.create(ViMain.getPlugin()) // move from async to sync
                    .execute(() -> postMessage.accept(player, message))
                    .run();
        } else {
            if (cancelMessage == null) {
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
                return;
            }

            Task.create(ViMain.getPlugin()) // move from async to sync
                    .execute(() -> cancelMessage.accept(player))
                    .run();
        }

        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // highest to prevent interference from chat plugins
    public void chat(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer() != player) {
            return;
        }

        String message = event.getMessage();
        event.setCancelled(true);
        if (Strings.getLevenshteinDistance(cancelText, message) > matchDistance) {
            Task.create(ViMain.getPlugin()) // move from async to sync
                    .execute(() -> postMessage.accept(player, message))
                    .run();
        } else {
            Task.create(ViMain.getPlugin()) // move from async to sync
                    .execute(() -> cancelMessage.accept(player))
                    .run();
        }

        PlayerCommandPreprocessEvent.getHandlerList().unregister(this);
    }
}