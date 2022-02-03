package dev.efnilite.commandfactory;

import dev.efnilite.commandfactory.command.CommandProcessor;
import dev.efnilite.commandfactory.command.plugin.FCommand;
import dev.efnilite.commandfactory.util.UpdateChecker;
import dev.efnilite.commandfactory.util.config.Configuration;
import dev.efnilite.commandfactory.util.config.Option;
import dev.efnilite.fycore.FyPlugin;
import dev.efnilite.fycore.util.Logging;
import dev.efnilite.fycore.util.Task;
import dev.efnilite.fycore.util.Time;
import dev.efnilite.fycore.util.Version;
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
public final class CommandFactory extends FyPlugin {

    private static CommandFactory instance;
    private static CommandProcessor factory;
    private static Configuration configuration;
    public static boolean IS_OUTDATED = false;

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

        configuration.read();

        registerListener(new Handler());
        registerCommand("commandfactory",  new FCommand());

        Metrics metrics = new Metrics(this, 13281);
        metrics.addCustomChart(new SingleLineChart("total_alias_count", () -> factory.getMappedSize()));

        Logging.info("Loaded CommandFactory in " + Time.timerEnd("enable") + "ms");
    }

    @Override
    public void disable() {
        configuration.save("commands");

        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    public static void setVerbosing(boolean verbosing) {
        FyPlugin.verbosing = verbosing;
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
