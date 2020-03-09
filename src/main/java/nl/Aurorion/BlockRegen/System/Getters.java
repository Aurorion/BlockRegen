package nl.Aurorion.BlockRegen.System;

import com.google.common.base.Strings;
import nl.Aurorion.BlockRegen.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public Material replaceBlock(String blockName) {
        if (plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".replace-block") != null) {
            return Material.valueOf(Objects.requireNonNull(plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".replace-block")).toUpperCase());
        }
        return null;
    }

    public int replaceDelay(String blockName) {
        return Amount.loadAmount(plugin.getFiles().getBlocklist(), "Blocks." + blockName + ".regen-delay", 1).getInt();
    }

    public int money(String blockName) {
        return Amount.loadAmount(plugin.getFiles().getBlocklist(), "Blocks." + blockName + ".money", 0).getInt();
    }

    public List<String> consoleCommands(String blockName) {
        List<String> consoleCommands = new ArrayList<>();

        String path = "Blocks." + blockName;

        if (!plugin.getFiles().getBlocklist().contains("Blocks." + blockName))
            return new ArrayList<>();

        if (Objects.requireNonNull(plugin.getFiles().getBlocklist().getConfigurationSection(path)).contains("console-commands"))
            path += ".console-commands";
        else if (Objects.requireNonNull(plugin.getFiles().getBlocklist().getConfigurationSection(path)).contains("console-command"))
            path += ".console-command";
        else return consoleCommands;

        if (plugin.getFiles().getBlocklist().isString(path))
            consoleCommands.add(plugin.getFiles().getBlocklist().getString(path));
        else
            consoleCommands = plugin.getFiles().getBlocklist().getStringList(path);

        return consoleCommands;
    }

    public List<String> playerCommands(String blockName) {
        List<String> playerCommands = new ArrayList<>();

        String path = "Blocks." + blockName;
        if (Objects.requireNonNull(plugin.getFiles().getBlocklist().getConfigurationSection(path)).contains("player-commands"))
            path += ".player-commands";
        else if (Objects.requireNonNull(plugin.getFiles().getBlocklist().getConfigurationSection(path)).contains("player-command"))
            path += ".player-command";
        else return playerCommands;

        if (plugin.getFiles().getBlocklist().isString(path))
            playerCommands.add(plugin.getFiles().getBlocklist().getString(path));
        else
            playerCommands = plugin.getFiles().getBlocklist().getStringList(path);

        return playerCommands;
    }

    public String toolRequired(String blockName) {
        return plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".tool-required");
    }

    public String enchantRequired(String blockName) {
        return plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".enchant-required");
    }

    public String jobsCheck(String blockName) {
        return plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".jobs-check");
    }

    public String particleCheck(String blockName) {
        return plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".particles");
    }

    public boolean naturalBreak(String blockName) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockName + ".natural-break");
    }

    public Material dropItemMaterial(String blockName) {
        if (!Strings.isNullOrEmpty(plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".drop-item.material")))
            return Material.valueOf(plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".drop-item.material"));
        return null;
    }

    public String dropItemName(String blockName) {
        if (plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".drop-item.name") != null) {
            return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".drop-item.name")));
        }
        return null;
    }

    public List<String> dropItemLores(String blockName) {
        List<String> lores = new ArrayList<>();
        for (String all : plugin.getFiles().getBlocklist().getStringList("Blocks." + blockName + ".drop-item.lores")) {
            lores.add(ChatColor.translateAlternateColorCodes('&', all));
        }
        return lores;
    }

    public boolean dropItemDropNaturally(String blockName) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockName + ".drop-item.drop-naturally");
    }

    public boolean dropItemExpDrop(String blockName) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockName + ".drop-item.exp.drop-naturally");
    }

    public int dropItemExpAmount(String blockName) {
        return Amount.loadAmount(plugin.getFiles().getBlocklist(), "Blocks." + blockName + ".drop-item.exp.amount", 1).getInt();
    }

    public int dropItemAmount(String blockName, Player player) {
        int amount = Amount.loadAmount(plugin.getFiles().getBlocklist(), "Blocks." + blockName + ".drop-item.amount", 1).getInt();

        if (getHand(player).hasItemMeta() && Objects.requireNonNull(getHand(player).getItemMeta()).hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
            int enchantLevel = Objects.requireNonNull(getHand(player).getItemMeta()).getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
            amount = amount + enchantLevel;
        }

        return amount;
    }

    public String eventName(String blockName) {
        return plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".event.event-name");
    }

    public boolean eventDoubleDrops(String blockName) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockName + ".event.double-drops");
    }

    public boolean eventDoubleExp(String blockName) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockName + ".event.double-exp");
    }

    public Material eventItemMaterial(String blockName) {
        if (plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".event.custom-item.material") != null) {
            return Material.valueOf(Objects.requireNonNull(plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".event.custom-item.material")).toUpperCase());
        }
        return null;
    }

    public String eventItemName(String blockName) {
        if (plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".event.custom-item.name") != null) {
            return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getFiles().getBlocklist().getString("Blocks." + blockName + ".event.custom-item.name")));
        }
        return null;
    }

    public List<String> eventItemLores(String blockName) {
        List<String> lores = new ArrayList<>();
        for (String all : plugin.getFiles().getBlocklist().getStringList("Blocks." + blockName + ".event.custom-item.lores")) {
            lores.add(ChatColor.translateAlternateColorCodes('&', all));
        }
        return lores;
    }

    public boolean eventItemDropNaturally(String blockName) {
        return plugin.getFiles().getBlocklist().getBoolean("Blocks." + blockName + ".event.custom-item.drop-naturally");
    }

    public int eventItemRarity(String blockName) {
        return plugin.getFiles().getBlocklist().getInt("Blocks." + blockName + ".event.custom-item.rarity");
    }
}