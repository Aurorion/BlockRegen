package nl.Aurorion.BlockRegen;

import nl.Aurorion.BlockRegen.Configurations.Files;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public class Messages {

    public String prefix;
    public String noperm;
    public String noplayer;
    public String reload;
    public String bypasson;
    public String bypassoff;
    public String invalidcmd;
    public String datacheck;
    public String datacheckon;
    public String datacheckoff;
    public String noregion;
    public String dupregion;
    public String setregion;
    public String removeregion;
    public String unknownregion;
    public String activateEvent;
    public String deActivateEvent;
    public String eventNotFound;
    public String eventActive;
    public String eventNotActive;
    public String toolRequired;
    public String enchantRequired;
    public String jobsError;

    public Messages(Files files) {
        FileConfiguration messages = files.getMessages();
        prefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Prefix")));
        noperm = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Insufficient-Permission")));
        noplayer = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Console-Sender-Error")));
        reload = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Reload")));
        bypasson = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Bypass-On")));
        bypassoff = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Bypass-Off")));
        invalidcmd = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Invalid-Command")));
        datacheck = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Data-Check")));
        datacheckon = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Data-Check-On")));
        datacheckoff = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Data-Check-Off")));
        noregion = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.No-Region-Selected")));
        dupregion = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Duplicated-Region")));
        setregion = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Set-Region")));
        removeregion = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Remove-Region")));
        unknownregion = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Unknown-Region")));
        activateEvent = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Activate-Event")));
        deActivateEvent = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.De-Activate-Event")));
        eventNotFound = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Event-Not-Found")));
        eventActive = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Event-Already-Active")));
        eventNotActive = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Event-Not-Active")));
        toolRequired = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Tool-Required-Error")));
        enchantRequired = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Enchant-Required-Error")));
        jobsError = prefix + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("Messages.Jobs-Error")));
    }
}