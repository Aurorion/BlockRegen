package nl.Aurorion.BlockRegen.System;

import nl.Aurorion.BlockRegen.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Getters {

    private Main main;

    public Getters(Main instance) {
        this.main = instance;
    }

    // Player related
    public ItemStack getHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    // Getter Settings.yml
    public boolean updateChecker() {
        if (main.getFiles().getSettings().get("Update-Checker") != null) {
            return main.getFiles().getSettings().getBoolean("Update-Checker");
        }
        return true;
    }

    public boolean useRegions() {
        return main.getFiles().getSettings().getBoolean("Use-Regions");
    }

    public boolean useTowny() {
        return main.getFiles().getSettings().getBoolean("Towny-Support");
    }

    public boolean useGP() {
        return main.getFiles().getSettings().getBoolean("GriefPrevention-Support");
    }

    public boolean disableOtherBreak() {
        return main.getFiles().getSettings().getBoolean("Disable-Other-Break");
    }

    public boolean disableOtherBreakRegion() {
        return main.getFiles().getSettings().getBoolean("Disable-Other-Break-Region");
    }

    public boolean boneMealOverride() {
        return main.getFiles().getSettings().getBoolean("Bone-Meal-Override");
    }

    public boolean dataRecovery() {
        return main.getFiles().getSettings().getBoolean("Data-Recovery");
    }

    // Getters Blocklist.yml
    public Material replaceBlock(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".replace-block") != null) {
            return Material.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockname + ".replace-block").toUpperCase());
        }
        return null;
    }

    public Integer replaceDelay(String blockname) {
        return main.getFiles().getBlocklist().getInt("Blocks." + blockname + ".regen-delay");
    }

    public Integer money(String blockname) {
        return main.getFiles().getBlocklist().getInt("Blocks." + blockname + ".money");
    }

    public List<String> consoleCommands(String blockname) {
        List<String> consoleCommands = new ArrayList<>();

        String path = "Blocks." + blockname;
        if (main.getFiles().getBlocklist().getConfigurationSection(path).contains("console-commands"))
            path += ".console-commands";
        else if (main.getFiles().getBlocklist().getConfigurationSection(path).contains("console-command"))
            path += ".console-command";
        else return null;

        if (!main.getFiles().getBlocklist().getStringList(path).isEmpty())
            consoleCommands = main.getFiles().getBlocklist().getStringList(path);
        else
            consoleCommands.add(main.getFiles().getBlocklist().getString(path));

        return consoleCommands;
    }

    public List<String> playerCommands(String blockname) {
        List<String> playerCommands = new ArrayList<>();

        String path = "Blocks." + blockname;
        if (main.getFiles().getBlocklist().getConfigurationSection(path).contains("player-commands"))
            path += ".player-commands";
        else if (main.getFiles().getBlocklist().getConfigurationSection(path).contains("player-command"))
            path += ".player-command";
        else return null;

        if (!main.getFiles().getBlocklist().getStringList(path).isEmpty())
            playerCommands = main.getFiles().getBlocklist().getStringList(path);
        else
            playerCommands.add(main.getFiles().getBlocklist().getString(path));

        return playerCommands;
    }

    public String toolRequired(String blockname) {
        return main.getFiles().getBlocklist().getString("Blocks." + blockname + ".tool-required");
    }

    public String enchantRequired(String blockname) {
        return main.getFiles().getBlocklist().getString("Blocks." + blockname + ".enchant-required");
    }

    public String jobsCheck(String blockname) {
        return main.getFiles().getBlocklist().getString("Blocks." + blockname + ".jobs-check");
    }

    public String particleCheck(String blockname) {
        return main.getFiles().getBlocklist().getString("Blocks." + blockname + ".particles");
    }

    public boolean naturalBreak(String blockname) {
        return main.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".natural-break");
    }

    public Material dropItemMaterial(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.material") != null) {
            return Material.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.material"));
        }
        return null;
    }

    public String dropItemName(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.name") != null) {
            return ChatColor.translateAlternateColorCodes('&', main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.name"));
        }
        return null;
    }

    public List<String> dropItemLores(String blockname) {
        List<String> lores = new ArrayList<String>();
        for (String all : main.getFiles().getBlocklist().getStringList("Blocks." + blockname + ".drop-item.lores")) {
            lores.add(ChatColor.translateAlternateColorCodes('&', all));
        }
        return lores;
    }

    public boolean dropItemDropNaturally(String blockname) {
        return main.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".drop-item.drop-naturally");
    }

    public boolean dropItemExpDrop(String blockname) {
        return main.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".drop-item.exp.drop-naturally");
    }

    public Integer dropItemExpAmount(String blockname) {
        return main.getFiles().getBlocklist().getInt("Blocks." + blockname + ".drop-item.exp.amount");
    }

    public Integer dropItemAmount(String blockname, Player player) {
        int amounthigh = main.getFiles().getBlocklist().getInt("Blocks." + blockname + ".drop-item.amount.high");
        int amountlow = main.getFiles().getBlocklist().getInt("Blocks." + blockname + ".drop-item.amount.low");
        int amount = main.getRandom().nextInt((amounthigh - amountlow) + 1) + amountlow;
        if (getHand(player).hasItemMeta() && getHand(player).getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
            int enchantLevel = getHand(player).getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
            amount = amount + enchantLevel;
        }
        return amount;
    }

    public String eventName(String blockname) {
        return main.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.event-name");
    }

    public String eventBossbarName(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.bossbar.name") != null) {
            return ChatColor.translateAlternateColorCodes('&', main.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.bossbar.name"));
        }
        return null;
    }

    public BarColor eventBossbarColor(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.bossbar.color") != null) {
            return BarColor.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.bossbar.color").toUpperCase());
        }
        return null;
    }

    public boolean eventDoubleDrops(String blockname) {
        return main.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".event.double-drops");
    }

    public boolean eventDoubleExp(String blockname) {
        return main.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".event.double-exp");
    }

    public Material eventItemMaterial(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.custom-item.material") != null) {
            return Material.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.custom-item.material").toUpperCase());
        }
        return null;
    }

    public String eventItemName(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.custom-item.name") != null) {
            return ChatColor.translateAlternateColorCodes('&', main.getFiles().getBlocklist().getString("Blocks." + blockname + ".event.custom-item.name"));
        }
        return null;
    }

    public List<String> eventItemLores(String blockname) {
        List<String> lores = new ArrayList<String>();
        for (String all : main.getFiles().getBlocklist().getStringList("Blocks." + blockname + ".event.custom-item.lores")) {
            lores.add(ChatColor.translateAlternateColorCodes('&', all));
        }
        return lores;
    }

    public boolean eventItemDropNaturally(String blockname) {
        return main.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".event.custom-item.drop-naturally");
    }

    public Integer eventItemRarity(String blockname) {
        return main.getFiles().getBlocklist().getInt("Blocks." + blockname + ".event.custom-item.rarity");
    }

}
