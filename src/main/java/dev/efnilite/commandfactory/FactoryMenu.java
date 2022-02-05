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
            commands.add(new Item(Material.WRITABLE_BOOK, "&#1F85DE&l" + alias)
                    .lore("&7Click to edit this command.")
                    .click((menu, event) -> openEditor(player, alias)));
        }

        mainMenu.
                displayRows(0, 1)
                .addToDisplay(commands)

                .nextPage(35, new Item(Material.LIME_DYE, "&a&lNext page")
                        .click((menu, event) -> mainMenu.page(1)))
                .prevPage(27, new Item(Material.RED_DYE, "&c&lPrevious page")
                        .click((menu, event) -> mainMenu.page(-1)))

                .item(30, new Item(Material.PAPER, "&#2055B8&lNew command")
                        .lore("&7Create a new command")
                        .click((menu, event) -> initNew(player)))

                .item(31, new Item(Material.NOTE_BLOCK, "&#5F9DAD&lSettings")
                        .lore("&7Open the settings menu")
                        .click((menu, event) -> openSettings(player)))

                .item(32, new Item(Material.ARROW, "&c&lClose").click((menu, event) -> player.closeInventory()))

                .fillBackground(Material.GRAY_STAINED_GLASS_PANE)
                .animation(new SnakeSingleAnimation())
                .open(player);
    }

    public static void initNew(Player player) {
        new ChatAnswer(player, "cancel")
                .pre((pl) -> {
                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "Please enter the alias. Type 'cancel' to cancel.");
                    pl.closeInventory();
                })
                .post((pl, alias) -> new ChatAnswer(player, "cancel")
                        .pre((pl1) -> {
                            Message.send(pl1, CommandFactory.MESSAGE_PREFIX + "Please enter the main command. Type 'cancel' to cancel.");
                            pl1.closeInventory();
                        })
                        .post((pl1, main) -> {
                            RegisterNotification notification = CommandFactory.getProcessor().register(alias, main);

                            if (notification != null) {
                                switch (notification) {
                                    case ARGUMENT_NULL:
                                        Message.send(pl, CommandFactory.MESSAGE_PREFIX + "&cYou entered a value which is null!");
                                        return;
                                    case ALIAS_ALREADY_EXISTS:
                                        Message.send(pl, CommandFactory.MESSAGE_PREFIX + "&cThat alias already exists!");
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
                        .item(31, new Item(Material.REDSTONE_TORCH, "&#B61616&lWarning!")
                                .lore("&7This command overrides another command.",
                                        "&7This may cause issues with the server,",
                                        "&7or the plugin that owns this command.", "",
                                        "&7Please &#CB7575don't report problems or errors&7",
                                        "&7with this command if you see this warning."));
            }
        }

        editor
                .distributeRowEvenly(1)
                .item(9, new Item(Material.COMMAND_BLOCK, "&#91AEE2&lMain command")
                        .lore("&#7285A9Currently&7: " + orNothing(command.getMainCommand()), "&7Set the main command by typing it.")
                        .click((menu, event) -> new ChatAnswer(player, "cancel")
                                .pre((pl) -> {
                                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "&7Please enter a command. Type 'cancel' to cancel.");
                                    pl.closeInventory();
                                })
                                .post((pl, msg) -> {
                                    CommandFactory.getProcessor().editMainCommand(alias, msg);
                                    openEditor(pl, alias);
                                })
                                .cancel((pl) -> openEditor(pl, alias))))

                .item(10, new Item(Material.NAME_TAG, "&#91AEE2&lPermission")
                        .lore("&#7285A9Currently&7: " + orNothing(command.getPermission()), "&7Set the permission by typing it.")
                        .click((menu, event) -> new ChatAnswer(player, "cancel")
                                .pre((pl) -> {
                                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "&7Please enter a permission. Type 'cancel' to cancel.");
                                    pl.closeInventory();
                                })
                                .post((pl, msg) -> {
                                    CommandFactory.getProcessor().editPermission(alias, msg);
                                    openEditor(pl, alias);
                                })
                                .cancel((pl) -> openEditor(pl, alias))))

                .item(11, new Item(Material.IRON_HORSE_ARMOR, "&#91AEE2&lPermission message")
                        .lore("&#7285A9Currently&7: " + orNothing(command.getPermissionMessage()), "&7Set the permission message by typing it.",
                                "&7This is the message players get when", "&7they don't have enough permissions.")
                        .click((menu, event) -> new ChatAnswer(player, "cancel")
                                .pre((pl) -> {
                                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "Please enter a permission message. " +
                                            "Use '&' or '<name>' for colours. Hex colours are supported ('&#abcde'). Type 'cancel' to cancel.");
                                    pl.closeInventory();
                                })
                                .post((pl, msg) -> {
                                    CommandFactory.getProcessor().editPermissionMessage(alias, msg);
                                    openEditor(pl, alias);
                                })
                                .cancel((pl) -> openEditor(pl, alias))))

                .item(12, new Item(Material.CLOCK, "&#91AEE2&lCooldown")
                        .lore("&#7285A9Currently&7: " + orNothing(formatDuration(command.getCooldownMs())), "&7Set the cooldown.")
                        .click((menu, event) -> openCooldown(player, alias)))

                .item(13, new Item(Material.GOLDEN_HORSE_ARMOR, "&#91AEE2&lCooldown message")
                        .lore("&#7285A9Currently&7: " + orNothing(command.getCooldownMessage()), "&7Set the cooldown message by typing it.",
                                "&7This is the message players get when", "&7they still have a cooldown.")
                        .click((menu, event) -> new ChatAnswer(player, "cancel")
                                .pre((pl) -> {
                                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "Please enter a cooldown message. " +
                                            "Use '&' or '<name>' for colours. Hex colours are supported ('&#abcde'). Use '%time%' for the remaining time." +
                                            "Use '%cooldown%' for the total cooldown time. Type 'cancel' to cancel.");
                                    pl.closeInventory();
                                })
                                .post((pl, msg) -> {
                                    CommandFactory.getProcessor().editCooldownMessage(alias, msg);
                                    openEditor(pl, alias);
                                })
                                .cancel((pl) -> openEditor(pl, alias))))

                .item(14, new Item(Material.PLAYER_HEAD, "&#91AEE2&lExecutor")
                        .lore("&#7285A9Currently&7: " + command.getExecutableBy().name().toLowerCase(), "&7Set who can execute this command.")
                        .click((menu, event) -> openExecutor(player, alias)))

                .item(15, new Item(Material.DIAMOND_HORSE_ARMOR, "&#91AEE2&lExecutor message")
                        .lore("&#7285A9Currently&7: " + orNothing(command.getExecutableByMessage()), "&7Set the executor message by typing it.",
                                "&7This is the message players/console get when", "&7they can't execute this command.")
                        .click((menu, event) -> new ChatAnswer(player, "cancel")
                                .pre((pl) -> {
                                    Message.send(pl, CommandFactory.MESSAGE_PREFIX + "Please enter an executor message. " +
                                            "Use '&' or '<name>'. Hex colours are supported ('&#abcde'). Type 'cancel' to cancel.");
                                    pl.closeInventory();
                                })
                                .post((pl, msg) -> {
                                    CommandFactory.getProcessor().editExecutableByMessage(alias, msg);
                                    openEditor(pl, alias);
                                })
                                .cancel((pl) -> openEditor(pl, alias))))

                .item(30, new Item(Material.BUCKET, "&#C43131&lDelete")
                        .lore("&7Deletes this command forever.")
                        .click((menu, event) -> {
                            menu.item(event.getSlot(), new TimedItem(new Item(Material.BARRIER, "&#980F0F&lAre you sure?")
                                    .lore("&7If you click this item again,", "&7this command will be &npermanently deleted&r&7!")
                                    .click((menu1, event1) -> {
                                        Message.send(player, CommandFactory.MESSAGE_PREFIX + "Deleted command '" + alias + "'!");
                                        CommandFactory.getProcessor().unregister(alias);
                                        openMain(player);
                                    }), menu, event, 5 * 20));
                            menu.updateItem(event.getSlot());
                        }))

                .item(32, new Item(Material.ARROW, "&c&lGo back")
                        .lore("&7Go back to the main menu")
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
                        .add(0, new Item(Material.LIME_STAINED_GLASS_PANE, "&a&lPlayers")
                                .lore("&7Players can execute this command"), (menu, event) -> players.set(true))
                        .add(1, new Item(Material.RED_STAINED_GLASS_PANE, "&c&lPlayers")
                                .lore("&7Players can't execute this command"), (menu, event) -> players.set(false)))

                .item(10, new SliderItem()
                        .initial(console.get() ? 0 : 1)
                        .add(0, new Item(Material.LIME_STAINED_GLASS_PANE, "&a&lConsole")
                                .lore("&7Console can execute this command"), (menu, event) -> console.set(true))
                        .add(1, new Item(Material.RED_STAINED_GLASS_PANE, "&c&lConsole")
                                .lore("&7Console can't execute this command"), (menu, event) -> console.set(false)))

                .item(26, new Item(Material.WRITABLE_BOOK, "&#2FBE6A&lSave changes")
                        .lore("&7Click to confirm.")
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
                                menu.item(26, new TimedItem(new Item(Material.BARRIER, "&4&lInvalid arguments!")
                                        .lore("&7You need to have someone be able to execute this command!")
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
        Item save = new Item(Material.WRITABLE_BOOK, "&#2FBE6A&lSave changes")
                .lore("&aCurrent total&7: " + formatDuration(cooldown), "&7Click to confirm.");

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
            daysItem.add(i, new Item(Material.RED_STAINED_GLASS_PANE, "&4&l" + i + " &4day(s)")
                            .lore("&7Use &4left&7 or &4right click&7 to add or remove days"),
                    (menu, event) -> {
                        days.set(finalIndex);
                        save.lore("&aCurrent total&7: " + formatDuration(getDuration(days, hours, mins, secs, ms)), "&7Click to confirm.");
                        menu.item(26, save);
                        menu.updateItem(26);
                    });
        }

        for (int i = 0; i < 24; i++) {
            int finalIndex = i;
            hoursItem.add(i, new Item(Material.ORANGE_STAINED_GLASS_PANE, "&6&l" + i + " &6hour(s)")
                            .lore("&7Use &6left&7 or &6right click&7 to add or remove hours"),
                    (menu, event) -> {
                        hours.set(finalIndex);
                        save.lore("&aCurrent total&7: " + formatDuration(getDuration(days, hours, mins, secs, ms)), "&7Click to confirm.");
                        menu.item(26, save);
                        menu.updateItem(26);
                    });
        }

        for (int i = 0; i < 60; i++) {
            int finalIndex = i;
            minsItem.add(i, new Item(Material.YELLOW_STAINED_GLASS_PANE, "&e&l" + i + " &eminute(s)")
                            .lore("&7Use &eleft&7 or &eright click&7 to add or remove minutes"),
                    (menu, event) -> {
                        mins.set(finalIndex);
                        save.lore("&aCurrent total&7: " + formatDuration(getDuration(days, hours, mins, secs, ms)), "&7Click to confirm.");
                        menu.item(26, save);
                        menu.updateItem(26);
                    });
        }

        for (int i = 0; i < 60; i++) {
            int finalIndex = i;
            secsItem.add(i, new Item(Material.GREEN_STAINED_GLASS_PANE, "&2&l" + i + " &2second(s)")
                    .lore("&7Use &2left&7 or &2right click&7 to add or remove seconds"),
                    (menu, event) -> {
                        secs.set(finalIndex);
                        save.lore("&aCurrent total&7: " + formatDuration(getDuration(days, hours, mins, secs, ms)), "&7Click to confirm.");
                        menu.item(26, save);
                        menu.updateItem(26);
                    });
        }

        for (int i = 0; i < 20; i++) {
            int finalIndex = i * 50;
            msItem.add(i, new Item(Material.LIME_STAINED_GLASS_PANE, "&a&l" + finalIndex + " &amillisecond(s)")
                    .lore("&7Use &aleft&7 or &aright click&7 to add or remove milliseconds"),
                    (menu, event) -> {
                        ms.set(finalIndex);
                        save.lore("&aCurrent total&7: " + formatDuration(getDuration(days, hours, mins, secs, ms)), "&7Click to confirm.");
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

                        .add(0, new Item(Material.LIME_STAINED_GLASS_PANE, "&a&lVerbosing enabled")
                                        .lore("&7You will receive more",  "&7information in the console."),
                                (menu, event) -> {
                                    Option.VERBOSE = new ConfigOption<>(true);
                                    CommandFactory.setVerbosing(true);
                                })

                        .add(1, new Item(Material.RED_STAINED_GLASS_PANE, "&c&lVerbosing disabled")
                                        .lore("&7You will receive very little", "&7information in the console."),
                                (menu, event) -> {
                                    Option.VERBOSE = new ConfigOption<>(false);
                                    CommandFactory.setVerbosing(false);
                                }))

                .item(10, new Item(Material.CLOCK, "&b&lReset cooldowns")
                        .lore("&7This will reset all active cooldowns.")
                        .click((menu, event) -> {
                            menu.item(event.getSlot(), new TimedItem(new Item(Material.BARRIER, "&c&lAre you sure?")
                                    .lore("&7If you click this item again,", "&7&nall cooldowns&r&7 will be reset!")
                                    .click((menu1, event1) -> {
                                        CommandFactory.getProcessor().resetCooldowns();
                                        Message.send(player, CommandFactory.MESSAGE_PREFIX + "Reset all cooldowns!");
                                    }), menu, event, 20 * 5));
                            menu.updateItem(event.getSlot());
                        }))

                .item(11, new Item(Material.COMPARATOR, "&b&lReload files")
                        .lore("&7This will reload all files.")
                        .click((menu, event) -> {
                            menu.item(event.getSlot(), new TimedItem(new Item(Material.BARRIER, "&c&lAre you sure?")
                                    .lore("&7If you click this item again,", "&7all files will be reloaded")
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

                .item(26, new Item(Material.ARROW, "&c&lGo back")
                        .lore("&7Go back to the main menu")
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
