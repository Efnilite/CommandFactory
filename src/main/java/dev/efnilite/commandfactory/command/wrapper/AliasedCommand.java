package dev.efnilite.commandfactory.command.wrapper;

import dev.efnilite.commandfactory.command.RegisterNotification;
import dev.efnilite.fycore.util.Logging;
import org.jetbrains.annotations.Nullable;

public class AliasedCommand {

    private String mainCommand;
    private @Nullable String permission;
    private @Nullable String permissionMessage;
    private @Nullable String executableBy;
    private @Nullable String executableByMessage;
    private @Nullable String cooldown;
    private @Nullable String cooldownMessage;
    private long cooldownMs;
    private final boolean containsReplaceableArguments;
    private @Nullable RegisterNotification notification;

    public AliasedCommand(String mainCommand, @Nullable String permission, @Nullable String permissionMessage,
                          @Nullable String executableBy, @Nullable String executableByMessage, @Nullable String cooldown,
                          @Nullable String cooldownMessage, boolean containsReplaceableArguments) {
        this.mainCommand = mainCommand;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        if (executableBy != null) {
            this.executableBy = executableBy.toLowerCase();
        }
        this.executableByMessage = executableByMessage;
        this.containsReplaceableArguments = containsReplaceableArguments;
        this.cooldown = cooldown;
        this.cooldownMessage = cooldownMessage;

        this.cooldownMs = -1;
    }

    public long parseCooldown() {
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

    public void setNotification(@Nullable RegisterNotification notification) {
        this.notification = notification;
    }

    public @Nullable RegisterNotification getNotification() {
        return notification;
    }

    @Override
    public AliasedCommand clone() {
        return new AliasedCommand(mainCommand, permission, permissionMessage, executableBy, executableByMessage,
                cooldown, cooldownMessage, containsReplaceableArguments);
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

    public void setExecutableBy(@Nullable String executableBy) {
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

    public @Nullable String getExecutableBy() {
        return executableBy;
    }

    public @Nullable String getExecutableByMessage() {
        return executableByMessage;
    }

    public @Nullable String getCooldown() {
        return cooldown;
    }

    public @Nullable String getCooldownMessage() {
        return cooldownMessage;
    }

    public boolean containsReplaceableArguments() {
        return containsReplaceableArguments;
    }
}