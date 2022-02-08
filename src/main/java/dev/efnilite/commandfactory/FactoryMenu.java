package dev.efnilite.commandfactory;

import dev.efnilite.commandfactory.command.Executor;
import dev.efnilite.commandfactory.command.RegisterNotification;
import dev.efnilite.commandfactory.command.wrapper.AliasedCommand;
import dev.efnilite.commandfactory.util.config.Option;
import dev.efnilite.fycore.chat.ChatAnswer;
import dev.efnilite.fycore.chat.Message;
import dev.efnilite.fycore.config.ConfigOption;
import dev.efnilite.fycore.inventory.Menu;
import dev.efnilite.fycore.inventory.PagedMenu;
import dev.efnilite.fycore.inventory.animation.*;
import dev.efnilite.fycore.inventory.item.Item;
import dev.efnilite.fycore.inventory.item.MenuItem;
import dev.efnilite.fycore.inventory.item.SliderItem;
import dev.efnilite.fycore.inventory.item.TimedItem;
import dev.efnilite.fycore.util.Time;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Menu handling
 *
 * @author Efnilite
 */
@ApiStatus.Internal
public class FactoryMenu {

    public static void openMain(Player player) {
        PagedMenu mainMenu = new PagedMenu(4, "&fCommands");

        List<MenuItem> commands = new ArrayList<>();
        for (String alias : CommandFactory.getProcessor().getAliases()) {
            commands.add(new Item(Material.WRITABLE_BOOK, "<#1F85DE><bold>" + alias)
                    .lore("<gray>Click to edit this alias.")
                    .click((menu, event) -> openEditor(player, alias)));
        }

        mainMenu.
                displayRows(0, 1)
                .addToDisplay(commands)

                .nextPage(35, new Item(Material.LIME_DYE, "<#0DCB07><bold>»") // next page
                        .click((menu, event) -> mainMenu.page(1)))
                .prevPage(27, new Item(Material.RED_DYE, "<#DE1F1F><bold>«") // previous page
                        .click((menu, event) -> mainMenu.page(-1)))

                .item(30, new Item(Material.PAPER, "<#2055B8><bold>New command")
                        .lore("<gray>Create a new command")
                        .click((menu, event) -> initNew(player)))

                .item(31, new Item(Material.NOTE_BLOCK, "<#5F9DAD><bold>Settings")
                        .lore("<gray>Open the settings menu")
                        .click((menu, event) -> openSettings(player)))

                .item(32, new Item(Material.ARROW, "<red><bold>Close").click((menu, event) -> player.closeInventory()))

                .fillBackground(Material.GRAY_STAINED_GLASS_PANE)
                .animation(new SnakeSingleAnimation())
                .open(player);
    }

    public static void initNew(Player player) {
        new ChatAnswer(player, "cancel")
                .pre((pl) -> {
                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "Please enter the alias. " +
                            "This is the other (usually shorter) version of the main command. Example: /gmc (with main command /gamemode creative). " +
                            "Type 'cancel' to cancel.");
                    pl.closeInventory();
                })
                .post((pl, alias) -> new ChatAnswer(player, "cancel")
                        .pre((pl1) -> {
                            Message.send(pl1, CommandFactory.MESSAGE_PREFIX + "Please enter the main command. " +
                                    "This is the command that actually gets executed when you enter the alias. Example: /gamemode creative (with alias /gmc). " +
                                    "Type 'cancel' to cancel.");
                            pl1.closeInventory();
                        })
                        .post((pl1, main) -> {
                            RegisterNotification notification = CommandFactory.getProcessor().register(alias, main);

                            if (notification != null) {
                                switch (notification) {
                                    case ARGUMENT_NULL:
                                        Message.send(pl, CommandFactory.MESSAGE_PREFIX + "<red>You entered a value which is null!");
                                        return;
                                    case ALIAS_ALREADY_EXISTS:
                                        Message.send(pl, CommandFactory.MESSAGE_PREFIX + "<red>That alias already exists!");
                                        return;
                                }
                            }
                            openEditor(pl1, alias);
                        })
                        .cancel((pl1) -> Message.send(pl, CommandFactory.MESSAGE_PREFIX + "Cancelled your command.")))
                .cancel((pl) -> Message.send(pl, CommandFactory.MESSAGE_PREFIX + "Cancelled your command."));
    }

    public static void openEditor(Player player, String alias) {
        AliasedCommand command = CommandFactory.getProcessor().get(alias);

        if (command == null) {
            return;
        }

        Menu editor = new Menu(4, "&fEditing " + alias);

        RegisterNotification notification = command.getNotification();
        if (notification != null) {
            if (notification == RegisterNotification.OVERRIDING_EXISTING) {
                editor
                        .item(31, new Item(Material.REDSTONE_TORCH, "<#B61616><bold>Warning!")
                                .lore("<gray>This command overrides another command.",
                                        "<gray>This may cause issues with the server,",
                                        "<gray>or the plugin that owns this command.", "",
                                        "<gray>Please <#CB7575>don't report problems or errors<gray>",
                                        "<gray>with this command if you see this warning."));
            }
        }

        editor
                .distributeRowEvenly(1)
                .item(9, new Item(Material.COMMAND_BLOCK, "<#91AEE2><bold>Main command")
                        .lore("<#7285A9>Currently<gray>: " + orNothing(command.getMainCommand()),
                                "<gray>This is the other (usually shorter) version of the main command.",
                                "<gray>Example: /gmc (with main command /gamemode creative)",
                                "<gray>Set the main command by typing it.")
                        .click((menu, event) -> new ChatAnswer(player, "cancel")
                                .pre((pl) -> {
                                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "<gray>Please enter a command. Type 'cancel' to cancel.");
                                    pl.closeInventory();
                                })
                                .post((pl, msg) -> {
                                    CommandFactory.getProcessor().editMainCommand(alias, msg);
                                    openEditor(pl, alias);
                                })
                                .cancel((pl) -> openEditor(pl, alias))))

                .item(10, new Item(Material.NAME_TAG, "<#91AEE2><bold>Permission")
                        .lore("<#7285A9>Currently<gray>: " + orNothing(command.getPermission()),
                                "<gray>Set the permission by typing it.")
                        .click((menu, event) -> new ChatAnswer(player, "cancel")
                                .pre((pl) -> {
                                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "<gray>Please enter a permission. Type 'cancel' to cancel.");
                                    pl.closeInventory();
                                })
                                .post((pl, msg) -> {
                                    CommandFactory.getProcessor().editPermission(alias, msg);
                                    openEditor(pl, alias);
                                })
                                .cancel((pl) -> openEditor(pl, alias))))

                .item(11, new Item(Material.IRON_HORSE_ARMOR, "<#91AEE2><bold>Permission message")
                        .lore("<#7285A9>Currently<gray>: " + orNothing(command.getPermissionMessage()),
                                "<gray>Set the permission message by typing it.",
                                "<gray>This is the message players get when",
                                "<gray>they don't have enough permissions.")
                        .click((menu, event) -> new ChatAnswer(player, "cancel")
                                .pre((pl) -> {
                                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "Please enter a permission message. " +
                                            "Use '&' or '<name>' for colours. Hex colours are supported ('<#abcde>'). Type 'cancel' to cancel.");
                                    pl.closeInventory();
                                })
                                .post((pl, msg) -> {
                                    CommandFactory.getProcessor().editPermissionMessage(alias, msg);
                                    openEditor(pl, alias);
                                })
                                .cancel((pl) -> openEditor(pl, alias))))

                .item(12, new Item(Material.CLOCK, "<#91AEE2><bold>Cooldown")
                        .lore("<#7285A9>Currently<gray>: " + orNothing(formatDuration(command.getCooldownMs())), "<gray>Set the cooldown.")
                        .click((menu, event) -> openCooldown(player, alias)))

                .item(13, new Item(Material.GOLDEN_HORSE_ARMOR, "<#91AEE2><bold>Cooldown message")
                        .lore("<#7285A9>Currently<gray>: " + orNothing(command.getCooldownMessage()), "<gray>Set the cooldown message by typing it.",
                                "<gray>This is the message players get when", "<gray>they still have a cooldown.")
                        .click((menu, event) -> new ChatAnswer(player, "cancel")
                                .pre((pl) -> {
                                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "Please enter a cooldown message. " +
                                            "Use '&' or '<name>' for colours. Hex colours are supported ('<#abcde>'). Use '%time%' for the remaining time." +
                                            "Use '%cooldown%' for the total cooldown time. Type 'cancel' to cancel.");
                                    pl.closeInventory();
                                })
                                .post((pl, msg) -> {
                                    CommandFactory.getProcessor().editCooldownMessage(alias, msg);
                                    openEditor(pl, alias);
                                })
                                .cancel((pl) -> openEditor(pl, alias))))

                .item(14, new Item(Material.PLAYER_HEAD, "<#91AEE2><bold>Executor")
                        .lore("<#7285A9>Currently<gray>: " + command.getExecutableBy().name().toLowerCase(), "<gray>Set who can execute this command.")
                        .click((menu, event) -> openExecutor(player, alias)))

                .item(15, new Item(Material.DIAMOND_HORSE_ARMOR, "<#91AEE2><bold>Executor message")
                        .lore("<#7285A9>Currently<gray>: " + orNothing(command.getExecutableByMessage()), "<gray>Set the executor message by typing it.",
                                "<gray>This is the message players/console get when", "<gray>they can't execute this command.")
                        .click((menu, event) -> new ChatAnswer(player, "cancel")
                                .pre((pl) -> {
                                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "Please enter an executor message. " +
                                            "Use '&' or '<name>'. Hex colours are supported ('<#abcde>'). Type 'cancel' to cancel.");
                                    pl.closeInventory();
                                })
                                .post((pl, msg) -> {
                                    CommandFactory.getProcessor().editExecutableByMessage(alias, msg);
                                    openEditor(pl, alias);
                                })
                                .cancel((pl) -> openEditor(pl, alias))))

                .item(30, new Item(Material.BUCKET, "<#C43131><bold>Delete")
                        .lore("<gray>Deletes this command forever.")
                        .click((menu, event) -> {
                            menu.item(event.getSlot(), new TimedItem(new Item(Material.BARRIER, "<#980F0F><bold>Are you sure?")
                                    .lore("<gray>If you click this item again,", "<gray>this command will be <underline>permanently deleted</underline><gray>!")
                                    .click((menu1, event1) -> {
                                        Message.send(player, CommandFactory.MESSAGE_PREFIX + "Deleted command '" + alias + "'!");
                                        CommandFactory.getProcessor().unregister(alias);
                                        openMain(player);
                                    }), menu, event, 5 * 20));
                            menu.updateItem(event.getSlot());
                        }))

                .item(32, new Item(Material.ARROW, "<red><bold>Go back")
                        .lore("<gray>Go back to the main menu")
                        .click((menu, event) -> openMain(player)))
                .animation(new WaveEastAnimation())
                .fillBackground(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
                .open(player);
    }

    private static void openExecutor(Player player, String alias) {
        AliasedCommand command = CommandFactory.getProcessor().get(alias);

        if (command == null) {
            return;
        }

        Executor executor = command.getExecutableBy();
        AtomicBoolean players = new AtomicBoolean(executor == Executor.PLAYER || executor == Executor.BOTH);
        AtomicBoolean console = new AtomicBoolean(executor == Executor.CONSOLE || executor == Executor.BOTH);

        new Menu(3, "&fExecutor of " + alias)
                .distributeRowEvenly(1)

                .item(9, new SliderItem()
                        .initial(players.get() ? 0 : 1)
                        .add(0, new Item(Material.LIME_STAINED_GLASS_PANE, "<green><bold>Players")
                                .lore("<gray>Players can execute this command"), (menu, event) -> players.set(true))
                        .add(1, new Item(Material.RED_STAINED_GLASS_PANE, "<red><bold>Players")
                                .lore("<gray>Players can't execute this command"), (menu, event) -> players.set(false)))

                .item(10, new SliderItem()
                        .initial(console.get() ? 0 : 1)
                        .add(0, new Item(Material.LIME_STAINED_GLASS_PANE, "<green><bold>Console")
                                .lore("<gray>Console can execute this command"), (menu, event) -> console.set(true))
                        .add(1, new Item(Material.RED_STAINED_GLASS_PANE, "<red><bold>Console")
                                .lore("<gray>Console can't execute this command"), (menu, event) -> console.set(false)))

                .item(26, new Item(Material.WRITABLE_BOOK, "<#2FBE6A><bold>Save changes")
                        .lore("<gray>Click to confirm.")
                        .click((menu, event) -> {
                            if (players.get() && console.get()) {
                                CommandFactory.getProcessor().editExecutableBy(alias, Executor.BOTH);
                                openEditor(player, alias);
                            } else if (players.get()) {
                                CommandFactory.getProcessor().editExecutableBy(alias, Executor.PLAYER);
                                openEditor(player, alias);
                            } else if (console.get()) {
                                CommandFactory.getProcessor().editExecutableBy(alias, Executor.CONSOLE);
                                openEditor(player, alias);
                            } else {
                                menu.item(26, new TimedItem(new Item(Material.BARRIER, "<dark_red><bold>Invalid arguments!")
                                        .lore("<gray>You need to have someone be able to execute this command!")
                                        .click((menu1, event1) -> {

                                        }), menu, event, 5 * 20));
                                menu.updateItem(26);
                            }
                        }))

                .animation(new SplitMiddleInAnimation())
                .fillBackground(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .open(player);
    }

    private static void openCooldown(Player player, String alias) {
        AliasedCommand command = CommandFactory.getProcessor().get(alias);

        if (command == null) {
            return;
        }

        long cooldown = command.getCooldownMs();
        Item save = new Item(Material.WRITABLE_BOOK, "<#2FBE6A><bold>Save changes")
                .lore("<green>Current total<gray>: " + formatDuration(cooldown), "<gray>Click to confirm.");

        // Init days for items
        Duration total = Duration.ofMillis(cooldown); // get the total cooldown in ms
        AtomicInteger days = new AtomicInteger((int) total.toDays());
        total = total.minusDays(days.get()); // remove days
        AtomicInteger hours = new AtomicInteger((int) total.toHours());
        total = total.minusHours(hours.get()); // remove hours
        AtomicInteger mins = new AtomicInteger((int) total.toMinutes());
        total = total.minusMinutes(mins.get()); // remove mins
        AtomicInteger secs = new AtomicInteger((int) total.getSeconds());
        total = total.minusSeconds(secs.get()); // remove secs
        AtomicInteger ms = new AtomicInteger((int) total.toMillis());

        // Init all items
        SliderItem daysItem = new SliderItem().initial(days.get() > -1 && days.get() < 31 ? days.get() : 0);
        SliderItem hoursItem = new SliderItem().initial(hours.get() > -1 && hours.get() < 24 ? hours.get() : 0);
        SliderItem minsItem = new SliderItem().initial(mins.get() > -1 && mins.get() < 60 ? mins.get() : 0);
        SliderItem secsItem = new SliderItem().initial(secs.get() > -1 && secs.get() < 60 ? secs.get() : 0);
        SliderItem msItem = new SliderItem().initial(ms.get() > -1 && ms.get() < 1000 ? (ms.get() / 50) : 0);

        for (int i = 0; i < 31; i++) {
            int finalIndex = i;
            daysItem.add(i, new Item(Material.RED_STAINED_GLASS_PANE, "<dark_red><bold>" + i + " <dark_red>day(s)")
                            .lore("<gray>Use <dark_red>left<gray> or <dark_red>right click<gray> to add or remove days"),
                    (menu, event) -> {
                        days.set(finalIndex);
                        save.lore("<green>Current total<gray>: " + formatDuration(getDuration(days, hours, mins, secs, ms)), "<gray>Click to confirm.");
                        menu.item(26, save);
                        menu.updateItem(26);
                    });
        }

        for (int i = 0; i < 24; i++) {
            int finalIndex = i;
            hoursItem.add(i, new Item(Material.ORANGE_STAINED_GLASS_PANE, "&6<bold>" + i + " &6hour(s)")
                            .lore("<gray>Use &6left<gray> or &6right click<gray> to add or remove hours"),
                    (menu, event) -> {
                        hours.set(finalIndex);
                        save.lore("<green>Current total<gray>: " + formatDuration(getDuration(days, hours, mins, secs, ms)), "<gray>Click to confirm.");
                        menu.item(26, save);
                        menu.updateItem(26);
                    });
        }

        for (int i = 0; i < 60; i++) {
            int finalIndex = i;
            minsItem.add(i, new Item(Material.YELLOW_STAINED_GLASS_PANE, "&e<bold>" + i + " &eminute(s)")
                            .lore("<gray>Use &eleft<gray> or &eright click<gray> to add or remove minutes"),
                    (menu, event) -> {
                        mins.set(finalIndex);
                        save.lore("<green>Current total<gray>: " + formatDuration(getDuration(days, hours, mins, secs, ms)), "<gray>Click to confirm.");
                        menu.item(26, save);
                        menu.updateItem(26);
                    });
        }

        for (int i = 0; i < 60; i++) {
            int finalIndex = i;
            secsItem.add(i, new Item(Material.GREEN_STAINED_GLASS_PANE, "&2<bold>" + i + " &2second(s)")
                    .lore("<gray>Use &2left<gray> or &2right click<gray> to add or remove seconds"),
                    (menu, event) -> {
                        secs.set(finalIndex);
                        save.lore("<green>Current total<gray>: " + formatDuration(getDuration(days, hours, mins, secs, ms)), "<gray>Click to confirm.");
                        menu.item(26, save);
                        menu.updateItem(26);
                    });
        }

        for (int i = 0; i < 20; i++) {
            int finalIndex = i * 50;
            msItem.add(i, new Item(Material.LIME_STAINED_GLASS_PANE, "<green><bold>" + finalIndex + " <green>millisecond(s)")
                    .lore("<gray>Use <green>left<gray> or <green>right click<gray> to add or remove milliseconds"),
                    (menu, event) -> {
                        ms.set(finalIndex);
                        save.lore("<green>Current total<gray>: " + formatDuration(getDuration(days, hours, mins, secs, ms)), "<gray>Click to confirm.");
                        menu.item(26, save);
                        menu.updateItem(26);
                    });
        }

        new Menu(3, "&fCooldown of " + alias)
                .item(9, daysItem)
                .item(10, hoursItem)
                .item(11, minsItem)
                .item(12, secsItem)
                .item(13, msItem)
                .item(26, save
                        .click((menu, event) -> {
                            CommandFactory.getProcessor().editCooldown(alias, getDuration(days, hours, mins, secs, ms));
                            openEditor(player, alias);
                        }))
                .distributeRowEvenly(1)
                .fillBackground(Material.CYAN_STAINED_GLASS_PANE)
                .animation(new SplitMiddleOutAnimation())
                .open(player);
    }

    public static void openSettings(Player player) {
        Menu settingsMenu = new Menu(3, "&fSettings");
        settingsMenu
                .distributeRowEvenly(1)
                .item(9, new SliderItem()
                        .initial(Option.VERBOSE.get() ? 0 : 1)

                        .add(0, new Item(Material.LIME_STAINED_GLASS_PANE, "<green><bold>Verbosing enabled")
                                        .lore("<gray>You will receive more",  "<gray>information in the console."),
                                (menu, event) -> {
                                    Option.VERBOSE = new ConfigOption<>(true);
                                    CommandFactory.setVerbosing(true);
                                })

                        .add(1, new Item(Material.RED_STAINED_GLASS_PANE, "<red><bold>Verbosing disabled")
                                        .lore("<gray>You will receive very little", "<gray>information in the console."),
                                (menu, event) -> {
                                    Option.VERBOSE = new ConfigOption<>(false);
                                    CommandFactory.setVerbosing(false);
                                }))

                .item(10, new Item(Material.CLOCK, "&b<bold>Reset cooldowns")
                        .lore("<gray>This will reset all active cooldowns.")
                        .click((menu, event) -> {
                            menu.item(event.getSlot(), new TimedItem(new Item(Material.BARRIER, "<red><bold>Are you sure?")
                                    .lore("<gray>If you click this item again,", "<gray>&nall cooldowns&r<gray> will be reset!")
                                    .click((menu1, event1) -> {
                                        CommandFactory.getProcessor().resetCooldowns();
                                        Message.send(player, CommandFactory.MESSAGE_PREFIX + "Reset all cooldowns!");
                                    }), menu, event, 20 * 5));
                            menu.updateItem(event.getSlot());
                        }))

                .item(11, new Item(Material.COMPARATOR, "&b<bold>Reload files")
                        .lore("<gray>This will reload all files.")
                        .click((menu, event) -> {
                            menu.item(event.getSlot(), new TimedItem(new Item(Material.BARRIER, "<red><bold>Are you sure?")
                                    .lore("<gray>If you click this item again,", "<gray>all files will be reloaded")
                                    .click((menu1, event1) -> {
                                        player.closeInventory();
                                        Time.timerStart("reload");
                                        CommandFactory.getProcessor().unregisterAll();
                                        CommandFactory.getConfiguration().reload(true);
                                        Message.send(player, CommandFactory.MESSAGE_PREFIX + "Reloaded files in "
                                                + Time.timerEnd("reload") + "ms!");
                                    }), menu, event, 20 * 5));
                            menu.updateItem(event.getSlot());
                        }))

                .item(26, new Item(Material.ARROW, "<red><bold>Go back")
                        .lore("<gray>Go back to the main menu")
                        .click((menu, event) -> openMain(player)))
                .fillBackground(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
                .animation(new RandomAnimation())
                .open(player);
    }

    private static String orNothing(@Nullable String string) {
        if (string == null) {
            return "nothing";
        }
        return string;
    }

    private static long getDuration(AtomicInteger days, AtomicInteger hrs, AtomicInteger mins, AtomicInteger secs, AtomicInteger ms) {
        return days.longValue() * Time.toMillis(Time.SECONDS_PER_DAY) +
                hrs.longValue() * Time.toMillis(Time.SECONDS_PER_HOUR) +
                mins.longValue() * Time.toMillis(Time.SECONDS_PER_MINUTE) +
                Time.toMillis(secs.intValue()) + ms.longValue();
    }

    // formats durations
    private static String formatDuration(long millis) {
        return DurationFormatUtils.formatDurationWords(millis, false, true);
    }
}
