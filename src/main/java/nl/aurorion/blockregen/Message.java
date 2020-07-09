package nl.aurorion.blockregen;

import lombok.Getter;
import lombok.Setter;

/**
 * Message system, loaded on enable & reload.
 *
 * @author Wertik1206
 */
public enum Message {

    PREFIX("Prefix", "&6[&3BlockRegen&6] &r"),

    UPDATE("Update", "&6&m-----&r &3&lBlockRegen &6&m-----\n" +
            "&eA new update was found!\n" +
            "&eCurrent version: &c%version%\n" +
            "&eNew version: &a%newVersion%\n" +
            "&6&m-----------------------"),

    /**
     * Command general messages.
     */
    NO_PERM("Insufficient-Permission", "&cYou don't have the permissions to do this!"),
    NO_PLAYER("Console-Sender-Error", "&cI'm sorry but the console can not perform this command!"),
    INVALID_COMMAND("Invalid-Command", "&cThis is not a valid command!"),

    RELOAD("Reload", "&aSuccessfully reloaded Settings.yml, Messages.yml, Blocklist.yml & re-filled the events!"),

    /**
     * Bypass
     */
    BYPASS_ON("Bypass-On", "&aBypass toggled on!"),
    BYPASS_OFF("Bypass-Off", "&cBypass toggled off!"),

    /**
     * Debug
     */
    DEBUG_ON("Debug-On", "&aYou are now listening to info, warn, error and if enabled, debug messages."),
    DEBUG_OFF("Debug-Off", "&cYou are no longer listening to console output."),

    /**
     * Data check
     */
    DATA_CHECK("Data-Check", "&eThe correct name to enter in the config is: &d%block%"),
    DATA_CHECK_ON("Data-Check-On", "&aEntered Data-Check mode!"),
    DATA_CHECK_OFF("Data-Check-Off", "&cLeft Data-Check mode!"),


    /**
     * Regions
     */
    WORLD_EDIT_NOT_INSTALLED("WorldEdit-Not-Installed", "&cRegion functions require World Edit."),
    NO_SELECTION("No-Region-Selected", "&cI'm sorry but you need to select a CUBOID region first!"),
    DUPLICATED_REGION("Duplicated-Region", "&cThere is already a region with that name!"),
    SET_REGION("Set-Region", "&aRegion successfully saved!"),
    REMOVE_REGION("Remove-Region", "&aRegion successfully removed!"),
    UNKNOWN_REGION("Unknown-Region", "&cThere is no region with that name!"),

    /**
     * Events
     */
    ACTIVATE_EVENT("Activate-Event", "&aYou activated the event: &2%event%"),
    DEACTIVATE_EVENT("De-Activate-Event", "&cYou de-activated the event: &4%event%"),
    EVENT_NOT_FOUND("Event-Not-Found", "&cThis event is not found in the system. Reminder: event names are case sensitive!"),
    EVENT_ALREADY_ACTIVE("Event-Already-Active", "&cThis event is already active!"),
    EVENT_NOT_ACTIVE("Event-Not-Active", "&cThis event is currently not active!"),

    /**
     * Messages on block break errs.
     */
    TOOL_REQUIRED_ERROR("Tool-Required-Error", "&cYou can only break this block with the following tool(s): &b%tool%&c."),
    ENCHANT_REQUIRED_ERROR("Enchant-Required-Error", "&cYour tool has to have one of the following enchantment(s): &b%enchant%&c."),
    JOBS_REQUIRED_ERROR("Jobs-Error", "&cYou need to reach following job levels in order to break this block: &b%job%"),
    PERMISSION_BLOCK_ERROR("Permission-Error", "&cYou don't have the permission to break this block.");

    @Getter
    private final String path;

    @Getter
    @Setter
    private String value;

    public String get() {
        return Utils.color(this.value);
    }

    Message(String path, String value) {
        this.path = path;
        this.value = value;
    }

    public static void load() {
        for (Message msg : values()) {
            String str = BlockRegen.getInstance().getFiles().getMessages().getFileConfiguration().getString("Messages." + msg.getPath());

            if (str == null) {
                BlockRegen.getInstance().getFiles().getMessages().getFileConfiguration().set("Messages." + msg.getPath(), msg.getValue());
                continue;
            }

            msg.setValue(str);
        }

        BlockRegen.getInstance().getFiles().getMessages().save();
    }
}