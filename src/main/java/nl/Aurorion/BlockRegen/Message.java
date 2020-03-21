package nl.Aurorion.BlockRegen;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

/**
 * Message system, loaded on enable & reload.
 *
 * @author Wertik1206
 * */
public enum Message {

    PREFIX("Prefix", "&6&l[&3BlockRegen&6&l] &f"),
    NO_PERM("Insufficient-Permission", "&cYou don't have the permissions to do this!"),
    NO_PLAYER("Console-Sender-Error", "&cI'm sorry but the console can not perform this command!"),
    RELOAD("Reload", "&a&lSuccessfully reloaded Settings.yml, Messages.yml, Blocklist.yml & re-filled the events!"),
    BYPASS_ON("Bypass-On", "&aBypass toggled on!"),
    BYPASS_OFF("Bypass-Off", "&cBypass toggled off!"),
    INVALID_COMMAND("Invalid-Command", "&cThis is not a valid command!"),
    DATA_CHECK("Data-Check", "&eThe correct name to enter in the config is: &d%block%"),
    DATA_CHECK_ON("Data-Check-On", "&aEntered Data-Check mode!"),
    DATA_CHECK_OFF("Data-Check-Off", "&cLeft Data-Check mode!"),
    NO_SELECTION("No-Region-Selected", "&cI'm sorry but you need to select a CUBOID region first!"),
    DUPLICATED_REGION("Duplicated-Region", "&cThere is already a region with that name!"),
    SET_REGION("Set-Region", "&aRegion successfully saved!"),
    REMOVE_REGION("Remove-Region", "&aRegion successfully removed!"),
    UNKNOWN_REGION("Unknown-Region", "&cThere is no region with that name!"),
    ACTIVATE_EVENT("Activate-Event", "&aYou activated the event: &2%event%"),
    DEACTIVATE_EVENT("De-Activate-Event", "&cYou de-activated the event: &4%event%"),
    EVENT_NOT_FOUND("Event-Not-Found", "&cThis event is not found in the system. Reminder: event names are case sensitive!"),
    EVENT_ALREADY_ACTIVE("Event-Already-Active", "&cThis event is already active!"),
    EVENT_NOT_ACTIVE("Event-Not_Active", "&cThis event is currently not active!"),
    TOOL_REQUIRED_ERROR("Tool-Required-Error", "&cYou can only break this block with the following tool(s): &b%tool%&c."),
    ENCHANT_REQUIRED_ERROR("Enchant-Required-Error", "&cYour tool need to have the following enchantment(s): &b%enchant%&c."),
    JOBS_REQUIRED_ERROR("Jobs-Error", "&cYou need to be a level &b%level% %job% &cto break this block."),
    PERMISSION_BLOCK_ERROR("Permission-Error", "&cYou don't have the permission to break this block.");

    @Getter
    private final String path;

    @Getter
    private final String defaultValue;

    @Setter
    private String value;

    public String get() {
        return Utils.color(this.value);
    }

    Message(String path, String defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public static void load() {
        for (Message msg : values()) {
            String str = Main.getInstance().getFiles().getMessages().getString("Messages." + msg.getPath());

            if (Strings.isNullOrEmpty(str)) {
                Main.getInstance().getFiles().getMessages().set(msg.getPath(), msg.getDefaultValue());
                continue;
            }

            msg.setValue(str);
        }
        Main.getInstance().getFiles().saveMessages();
    }
}