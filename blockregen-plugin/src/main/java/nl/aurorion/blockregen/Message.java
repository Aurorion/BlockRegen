package nl.aurorion.blockregen;

import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Message system, loaded on enable & reload.
 *
 * @author Wertik1206
 */
public enum Message {

    PREFIX("Prefix", "&6[&3BlockRegen&6] &r"),

    UPDATE("Update", "&8&m     &r &3BlockRegen &8&m     \n" +
            "&6A new update was found!\n" +
            "&6Current version: &c%version%\n" +
            "&6New version: &a%newVersion%"),

    /**
     * Command general messages.
     */
    NO_PERM("Insufficient-Permission", "&cYou don't have the permissions to do this!"),
    ONLY_PLAYERS("Console-Sender-Error", "&cI'm sorry but the console can not perform this command!"),
    INVALID_COMMAND("Invalid-Command", "&cThis is not a valid command!"),

    TOO_MANY_ARGS("Too-Many-Arguments", "&cToo many arguments.\n&7Use: &f%help%"),
    NOT_ENOUGH_ARGS("Not-Enough-Arguments", "&cNot enough arguments.\n&7Use: &f%help%"),

    RELOAD("Reload", "&aSuccessfully reloaded Settings.yml, Messages.yml, Blocklist.yml & re-filled the events!"),

    /**
     * Bypass
     */
    BYPASS_ON("Bypass-On", "&aBypass toggled on!"),
    BYPASS_OFF("Bypass-Off", "&cBypass toggled off!"),

    /**
     * Debug
     */
    DEBUG_ON("Debug-On", "&aYou are now listening to debug messages."),
    DEBUG_OFF("Debug-Off", "&cYou are no longer listening to debug messages."),

    /**
     * Data check
     */
    DATA_CHECK("Data-Check", "&eThe correct name to enter in the config is: &d%block%"),
    DATA_CHECK_ON("Data-Check-On", "&aEntered Data-Check mode!"),
    DATA_CHECK_OFF("Data-Check-Off", "&cLeft Data-Check mode!"),


    /**
     * Regions
     */
    NO_SELECTION("No-Region-Selected", "&cSelect a region with WorldEdit first."),
    DUPLICATED_REGION("Duplicated-Region", "&cThere is already a region with that name!"),
    SET_REGION("Set-Region", "&aRegion successfully saved!"),
    REMOVE_REGION("Remove-Region", "&aRegion successfully removed!"),
    UNKNOWN_REGION("Unknown-Region", "&cThere is no region with that name!"),
    COULD_NOT_CREATE_REGION("Could-Not-Create-Region", "&cCould not created a region."),
    SELECT_FIRST("Select-First", "&7Set first position to &f%x%, %y%, %z%."),
    SELECT_SECOND("Select-Second", "&7Set second position to &f%x%, %y%, %z%."),

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
    ENCHANT_REQUIRED_ERROR("Enchant-Required-Error", "&cYour tool has to have at least one of the following enchantment(s): &b%enchant%&c."),
    JOBS_REQUIRED_ERROR("Jobs-Error", "&cYou need to reach following job levels in order to break this block: &b%job%"),
    PERMISSION_BLOCK_ERROR("Permission-Error", "&cYou don't have the permission to break this block.");

    @Getter
    private final String path;

    @Getter
    @Setter
    private String value;

    @Getter
    private static boolean insertPrefix = false;

    public String get() {
        return StringUtil.color(TextUtil.parse(insertPrefix ? "%prefix%" + this.value : this.value));
    }

    public String get(Player player) {
        return StringUtil.color(TextUtil.parse(insertPrefix ? "%prefix%" + this.value : this.value, player));
    }

    public void send(CommandSender target) {
        target.sendMessage(get());
    }

    public void send(Player player) {
        player.sendMessage(get(player));
    }

    Message(String path, String value) {
        this.path = path;
        this.value = value;
    }

    public static void load() {

        FileConfiguration messages = BlockRegen.getInstance().getFiles().getMessages().getFileConfiguration();

        if (!messages.contains("Insert-Prefix"))
            messages.set("Insert-Prefix", true);
        insertPrefix = messages.getBoolean("Insert-Prefix", true);

        for (Message msg : values()) {
            String str = messages.getString("Messages." + msg.getPath());

            if (str == null) {
                messages.set("Messages." + msg.getPath(), msg.getValue());
                continue;
            }

            msg.setValue(str);
        }

        BlockRegen.getInstance().getFiles().getMessages().save();
    }
}
