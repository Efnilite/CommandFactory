package dev.efnilite.commandfactory.command.wrapper;

import com.google.gson.annotations.Expose;
import dev.efnilite.commandfactory.CommandFactory;
import dev.efnilite.commandfactory.command.Executor;
import dev.efnilite.commandfactory.command.RegisterNotification;
import dev.efnilite.vilib.util.Logging;
import dev.efnilite.vilib.util.Task;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Class containing all data for registered commands.
 */
public class AliasedCommand {

    @Expose
    private String mainCommand;

    @Expose
    @Nullable
    private String permission;

    @Expose
    @Nullable
    private String permissionMessage;

    @Expose
    @Nullable
    private String executableByMessage;

    @Expose
    @Nullable
    private String cooldown;

    @Expose
    @Nullable
    private String cooldownMessage;

    private long cooldownMs;
    private Executor executableBy;
    @Nullable
    private RegisterNotification notification;

    private final String id;
    private final boolean containsReplaceableArguments;

    public AliasedCommand(String id, String mainCommand, @Nullable String permission, @Nullable String permissionMessage,
                          @Nullable String executableBy, @Nullable String executableByMessage, @Nullable String cooldown,
                          @Nullable String cooldownMessage, boolean containsReplaceableArguments) {
        this.id = id;
        this.mainCommand = mainCommand;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        if (executableBy != null) {
            this.executableBy = Executor.valueOf(executableBy.toUpperCase());
        }
        this.executableByMessage = executableByMessage;
        this.containsReplaceableArguments = containsReplaceableArguments;
        this.cooldown = cooldown;
        this.cooldownMessage = cooldownMessage;

        this.cooldownMs = -1;
    }

    /**
     * Saves the command file
     */
    public void save() {
        new Task()
                .execute(() -> {
                    File file = new File(CommandFactory.getInstance().getDataFolder(), "commands/" + id + ".json");

                    file.mkdirs();
                    try {
                        file.createNewFile();

                        FileWriter writer = new FileWriter(file);
                        CommandFactory.getGson().toJson(this, writer);
                        writer.flush();
                        writer.close();
                    } catch (Throwable throwable) {
                        Logging.stack("Error while trying to save command file", "Please report this error", throwable);
                    }
                })
                .async()
                .run();
    }

    /**
     * Deletes this command file
     */
    public void delete() {
        new Task()
                .execute(() -> {
                    File file = new File(CommandFactory.getInstance().getDataFolder(), "commands/" + id + ".json");

                    try {
                        file.delete();
                    } catch (Throwable throwable) {
                        Logging.stack("Error while trying to delete command file", "Please report this error", throwable);
                    }
                })
                .async()
                .run();
    }

    /**
     * Reads a command file
     *
     * @param   file
     *          The file
     *
     * @return the {@link AliasedCommand} instance
     */
    @Nullable
    public static AliasedCommand read(@NotNull File file) {
        if (file.exists()) {
            try {
                FileReader reader = new FileReader(file);
                AliasedCommand command = CommandFactory.getGson().fromJson(reader, AliasedCommand.class);
                reader.close();
                return command;
            } catch (Throwable throwable) {
                Logging.stack("Error while trying to save command file", "Please report this error", throwable);
            }
        }
        return null;
    }

    /**
     * Gets the cooldown in ms
     *
     * @return the ms
     */
    public long getCooldownMs() {
        if (cooldown == null) {
            return 0;
        }
        if (cooldownMs > -1) {
            return cooldownMs;
        }

        long total = 0;
        String[] elements = cooldown.split(" ");
        for (String element : elements) {
            if (element.contains("ms")) {
                String ms = element.replace("ms", "");
                total += (Long.parseLong(ms)); // ms
            } else if (element.contains("d")) {
                String days = element.replace("d", "");
                total += (Long.parseLong(days) * 86400 * 1000); // days to seconds to ms
            } else if (element.contains("h")) {
                String hrs = element.replace("h", "");
                total += (Long.parseLong(hrs) * 3600 * 1000); // hours to seconds to ms
            } else if (element.contains("m")) {
                String mins = element.replace("m", "");
                total += (Long.parseLong(mins) * 60 * 1000); // mins to seconds to ms
            } else if (element.contains("s")) {
                String secs = element.replace("s", "");
                total += (Long.parseLong(secs) * 1000); // seconds to ms
            } else {
                Logging.error("Invalid time measurement: " + element);
            }
        }
        cooldownMs = total;
        return cooldownMs;
    }

    /**
     * Gets the cooldown string, usable for configs
     *
     * @return the cooldown string but updated
     */
    public String getCooldownString() {
        String[] values = DurationFormatUtils.formatDuration(cooldownMs, "dddd:HHHH:mmmm:ssss:SSSS", false).split(":");
        StringBuilder cooldown = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int val = Integer.parseInt(values[i]);
            switch (i) {
                case 0:
                    cooldown.append(val).append("d ");
                    break;
                case 1:
                    cooldown.append(val).append("h ");
                    break;
                case 2:
                    cooldown.append(val).append("m ");
                    break;
                case 3:
                    cooldown.append(val).append("s ");
                    break;
                case 4:
                    cooldown.append(val).append("ms");
                    break;
            }
        }
        this.cooldown = cooldown.toString();
        return cooldown.toString();
    }

    public void setNotification(@Nullable RegisterNotification notification) {
        this.notification = notification;
    }

    public @Nullable RegisterNotification getNotification() {
        return notification;
    }

    @Override
    public AliasedCommand clone() {
        return new AliasedCommand(id, mainCommand, permission, permissionMessage, executableBy == null ? null : executableBy.name().toLowerCase(),
                executableByMessage, cooldown, cooldownMessage, containsReplaceableArguments);
    }

    public void setCooldownMs(long cooldownMs) {
        this.cooldownMs = cooldownMs;
    }

    public void setMainCommand(String mainCommand) {
        this.mainCommand = mainCommand;
    }

    public void setPermission(@Nullable String permission) {
        this.permission = permission;
    }

    public void setPermissionMessage(@Nullable String permissionMessage) {
        this.permissionMessage = permissionMessage;
    }

    public void setExecutableBy(@Nullable Executor executableBy) {
        this.executableBy = executableBy;
    }

    public void setExecutableByMessage(@Nullable String executableByMessage) {
        this.executableByMessage = executableByMessage;
    }

    public void setCooldownMessage(@Nullable String cooldownMessage) {
        this.cooldownMessage = cooldownMessage;
    }

    public String getMainCommand() {
        return mainCommand;
    }

    public @Nullable String getPermission() {
        return permission;
    }

    public @Nullable String getPermissionMessage() {
        return permissionMessage;
    }

    public Executor getExecutableBy() {
        if (executableBy == null) {
            return Executor.BOTH;
        }
        return executableBy;
    }

    public @Nullable String getExecutableByMessage() {
        return executableByMessage;
    }

    public @Nullable String getCooldownMessage() {
        return cooldownMessage;
    }

    public boolean containsReplaceableArguments() {
        return containsReplaceableArguments;
    }

    public String getId() {
        return id;
    }
}