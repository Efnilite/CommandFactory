package dev.efnilite.commandfactory;

import dev.efnilite.commandfactory.command.CommandProcessor;
import dev.efnilite.commandfactory.command.plugin.MainCommand;
import dev.efnilite.commandfactory.util.UpdateChecker;
import dev.efnilite.commandfactory.util.config.Configuration;
import dev.efnilite.commandfactory.util.config.Option;
import dev.efnilite.vilib.ViPlugin;
import dev.efnilite.vilib.util.Logging;
import dev.efnilite.vilib.util.Task;
import dev.efnilite.vilib.util.Time;
import dev.efnilite.vilib.util.Version;
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

    private static CommandFactory instance;
    private static CommandProcessor factory;
    private static Configuration configuration;
    public static boolean IS_OUTDATED = false;

    public static final String NAME = "<gradient:#7F00FF>CommandFactory</gradient:#007FFF>";
    public static final String MESSAGE_PREFIX = NAME + " <#7B7B7B>» <gray>";

    @Override
    public void enable() {
        instance = this;
        verbosing = true;
        Time.timerStart("enable");

        Logging.info("Registered under version " + Version.getPrettyVersion());

        configuration = new Configuration(this);
        Option.init();
        verbosing = Option.VERBOSE.get();

        if (Option.UPDATER.get()) {
            UpdateChecker checker = new UpdateChecker();
            new Task()
                    .repeat(8 * 72000)
                    .execute(checker::check)
                    .run(); // 8 hours
        }

        factory = new CommandProcessor();

        registerListener(new Handler());
        registerCommand("commandfactory", new MainCommand());

        Metrics metrics = new Metrics(this, 14168);
        metrics.addCustomChart(new SingleLineChart("total_alias_count", () -> factory.getMappedSize()));

        Logging.info("Loaded CommandFactory in " + Time.timerEnd("enable") + "ms");
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    public static void setVerbosing(boolean verbosing) {
        ViPlugin.verbosing = verbosing;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static CommandProcessor getProcessor() {
        return factory;
    }

    public static CommandFactory getInstance() {
        return instance;
    }
}
