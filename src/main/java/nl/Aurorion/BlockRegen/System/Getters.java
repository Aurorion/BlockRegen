package nl.Aurorion.BlockRegen.System;

import nl.Aurorion.BlockRegen.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Getters {

    private final Main plugin;

    public Getters(Main instance) {
        this.plugin = instance;
    }

    // Player related
    public ItemStack getHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    // Getter Settings.yml
    public boolean updateChecker() {
        if (plugin.getFiles().getSettings().get("Update-Checker") != null) {
            return plugin.getFiles().getSettings().getBoolean("Update-Checker");
        }
        return true;
    }

    public boolean useRegions() {
        return plugin.getFiles().getSettings().getBoolean("Use-Regions");
    }

    public boolean useTowny() {
        return plugin.getFiles().getSettings().getBoolean("Towny-Support");
    }

    public boolean useGP() {
        return plugin.getFiles().getSettings().getBoolean("GriefPrevention-Support");
    }

    public boolean disableOtherBreak() {
        return plugin.getFiles().getSettings().getBoolean("Disable-Other-Break");
    }

    public boolean disableOtherBreakRegion() {
        return plugin.getFiles().getSettings().getBoolean("Disable-Other-Break-Region");
    }

    public boolean boneMealOverride() {
        return plugin.getFiles().getSettings().getBoolean("Bone-Meal-Override");
    }

    public boolean dataRecovery() {
        return plugin.getFiles().getSettings().getBoolean("Data-Recovery");
    }

    public Material replaceBlock(String blockname) {
        if (plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".replace-block") != null) {
            return Material.valueOf(plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".replace-block").toUpperCase());
        }
        return null;
    }

    public Integer replaceDelay(String blockname) {
        return Amount.loadAmount(plugin.getFiles().getBlocklist(), "Blocks." + blockname + ".regen-delay", 0).getInt();
    }

    public Integer money(String blockname) {
        return Amount.loadAmount(plugin.getFiles().getBlocklist(), "Blocks." + blockname + ".money", 0).getInt();
    }

    public List<String> consoleCommands(String blockname) {
        List<String> consoleCommands = new ArrayList<>();

        String path = "Blocks." + blockname;

        if (!plugin.getFiles().getBlocklist().contains("Blocks." + blockname))
            return new ArrayList<>();

        if (plugin.getFiles().getBlocklist().getConfigurationSection(path).contains("console-commands"))
            path += ".console-commands";
        else if (plugin.getFiles().getBlocklist().getConfigurationSection(path).contains("console-command"))
            path += ".console-command";
        else return consoleCommands;

        if (plugin.getFiles().getBlocklist().isString(path))
            consoleCommands.add(plugin.getFiles().getBlocklist().getString(path));
        else
            consoleCommands = plugin.getFiles().getBlocklist().getStringList(path);

        return consoleCommands;
    }

    public List<String> playerCommands(String blockname) {
        List<String> playerCommands = new ArrayList<>();

        String path = "Blocks." + blockname;
        if (plugin.getFiles().getBlocklist().getConfigurationSection(path).contains("player-commands"))
            path += ".player-commands";
        else if (plugin.getFiles().getBlocklist().getConfigurationSection(path).contains("player-command"))
            path += ".player-command";
        else return playerCommands;

        if (plugin.getFiles().getBlocklist().isString(path))
            playerCommands.add(plugin.getFiles().getBlocklist().getString(path));
        else
            playerCommands = plugin.getFiles().getBlocklist().getStringList(path);

        return playerCommands;
    }

    public String toolRequired(String blockname) {
        return plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".tool-required");
    }

    public String enchantRequired(String blockname) {
        return plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".enchant-required");
    }

    public String jobsCheck(String blockname) {
        return plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".jobs-check");
    }

    public String particleCheck(String blockname) {
        return plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".particles");
    }

    public boolean naturalBreak(String blockname) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".natural-break");
    }

    public Material dropItemMaterial(String blockname) {
        if (plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.material") != null) {
            return Material.valueOf(plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.material"));
        }
        return null;
    }

    public String dropItemName(String blockname) {
        if (plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.name") != null) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.name"));
        }
        return null;
    }

    public List<String> dropItemLores(String blockname) {
        List<String> lores = new ArrayList<>();
        for (String all : plugin.getFiles().getBlocklist().getStringList("Blocks." + blockname + ".drop-item.lores")) {
            lores.add(ChatColor.translateAlternateColorCodes('&', all));
        }
        return lores;
    }

    public boolean dropItemDropNaturally(String blockname) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".drop-item.drop-naturally");
    }

    public boolean dropItemExpDrop(String blockname) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".drop-item.exp.drop-naturally");
    }

    public Integer dropItemExpAmount(String blockname) {
        return plugin.getFiles().getBlocklist().getInt("Blocks." + blockname + ".drop-item.exp.amount");
    }

    public Integer dropItemAmount(String blockname, Player player) {
        int amount = Amount.loadAmount(plugin.getFiles().getBlocklist(), "Blocks." + blockname + ".drop-item.amount", 1).getInt();

        if (getHand(player).hasItemMeta() && getHand(player).getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
            int enchantLevel = getHand(player).getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
            amount = amount + enchantLevel;
        }

        return amount;
    }

    public String eventName(String blockname) {
        return plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.event-name");
    }

    public boolean eventDoubleDrops(String blockname) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".event.double-drops");
    }

    public boolean eventDoubleExp(String blockname) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".event.double-exp");
    }

    public Material eventItemMaterial(String blockname) {
        if (plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.custom-item.material") != null) {
            return Material.valueOf(plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.custom-item.material").toUpperCase());
        }
        return null;
    }

    public String eventItemName(String blockname) {
        if (plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.custom-item.name") != null) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.custom-item.name"));
        }
        return null;
    }

    public List<String> eventItemLores(String blockname) {
        List<String> lores = new ArrayList<>();
        for (String all : plugin.getFiles().getBlocklist().getStringList("Blocks." + blockname + ".event.custom-item.lores")) {
            lores.add(ChatColor.translateAlternateColorCodes('&', all));
        }
        return lores;
    }

    public boolean eventItemDropNaturally(String blockname) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".event.custom-item.drop-naturally");
    }

    public Integer eventItemRarity(String blockname) {
        return plugin.getFiles().getBlocklist().getInt("Blocks." + blockname + ".event.custom-item.rarity");
    }
}