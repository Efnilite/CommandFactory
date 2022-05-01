package dev.efnilite.commandfactory;

import dev.efnilite.commandfactory.command.CommandProcessor;
import dev.efnilite.commandfactory.legacy.LegacyCommandsReader;
import dev.efnilite.commandfactory.util.config.Configuration;
import dev.efnilite.commandfactory.util.config.Option;
import dev.efnilite.vilib.ViPlugin;
import dev.efnilite.vilib.util.Logging;
import dev.efnilite.vilib.util.Time;
import dev.efnilite.vilib.util.Version;
import dev.efnilite.vilib.util.elevator.GitElevator;
import dev.efnilite.vilib.util.elevator.VersionComparator;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

/**
 * Main class
 *
 * @author Efnilite
 * (c) MMXXI - MMXXII
 */
public final class CommandFactory extends ViPlugin {

    private static GitElevator elevator;
    private static CommandFactory instance;
    private static CommandProcessor factory;
    private static Configuration configuration;
    public static final String NAME = "<gradient:#7F00FF>CommandFactory</gradient:#007FFF>";
    public static final String MESSAGE_PREFIX = NAME + " <#7B7B7B>» <gray>";

    @Override
    public void enable() {
        instance = this;
        Time.timerStart("enable");

        logging.info("Registered under version " + Version.getPrettyVersion());

        configuration = new Configuration(this);
        Option.init();

        elevator = new GitElevator("Efnilite/CommandFactory", this, VersionComparator.FROM_SEMANTIC, Option.AUTO_UPDATER);

        factory = new CommandProcessor();

        registerListener(new Handler());
        registerCommand("commandfactory", new MainCommand());

        Metrics metrics = new Metrics(this, 14168);
        metrics.addCustomChart(new SingleLineChart("total_alias_count", () -> factory.getMappedSize()));

        LegacyCommandsReader.check();

        logging.info("Loaded CommandFactory in " + Time.timerEnd("enable") + "ms");
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    /**
     * Returns the {@link Logging} belonging to this plugin.
     *
     * @return this plugin's {@link Logging} instance.
     */
    public static Logging logging() {
        return getPlugin().logging;
    }

    /**
     * Returns this plugin instance.
     *
     * @return the plugin instance.
     */
    public static CommandFactory getPlugin() {
        return instance;
    }

    public static GitElevator getElevator() {
        return elevator;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static CommandProcessor getProcessor() {
        return factory;
    }
}
