package dev.efnilite.commandfactory.command;

/**
 * Status notifications on registering a command.
 */
public enum RegisterNotification {

    /**
     * This alias already exists
     */
    ALIAS_ALREADY_EXISTS,

    /**
     * Warning: this command overrides an already-existing command
     */
    OVERRIDING_EXISTING,

    /**
     * The main command or alias is null
     */
    ARGUMENT_NULL

}
