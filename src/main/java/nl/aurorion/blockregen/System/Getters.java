package nl.aurorion.blockregen.System;

import com.google.common.base.Strings;
import me.clip.placeholderapi.PlaceholderAPI;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Getters {

    private final BlockRegen plugin;

    private FileConfiguration blocklist;
    private FileConfiguration settings;

    public Getters(BlockRegen plugin) {
        this.plugin = plugin;

        reload();
    }

    public void reload() {
        this.blocklist = plugin.getFiles().getBlocklist().getFileConfiguration();
        this.settings = plugin.getFiles().getSettings().getFileConfiguration();
    }

    public boolean updateChecker() {
        if (settings.contains("Update-Checker"))
            return settings.getBoolean("Update-Checker");
        return true;
    }

    public boolean useRegions() {
        return settings.getBoolean("Use-Regions");
    }


    public boolean useTowny() {
        return settings.getBoolean("Towny-Support", true);
    }

    public boolean useGriefPrevention() {
        return settings.getBoolean("GriefPrevention-Support", true);
    }

    public boolean useWorldGuard() {
        return settings.getBoolean("WorldGuard-Support", true);
    }

    public boolean useResidence() {
        return settings.getBoolean("Residence-Support", true);
    }

    public boolean useJobsRewards() {
        return settings.getBoolean("Jobs-Rewards", true);
    }

    public boolean useMMOItems() {
        return settings.getBoolean("MMOItems-Drops", true);
    }


    public boolean disableOtherBreak() {
        return settings.getBoolean("Disable-Other-Break");
    }

    public boolean disableOtherBreakRegion() {
        return settings.getBoolean("Disable-Other-Break-Region");
    }

    public boolean boneMealOverride() {
        return settings.getBoolean("Bone-Meal-Override");
    }

    public boolean dataRecovery() {
        return settings.getBoolean("Data-Recovery");
    }

    public Material pickRandomBlock(String blocks) {

        if (Strings.isNullOrEmpty(blocks)) return null;

        List<String> materials = new ArrayList<>();

        if (blocks.contains(";")) {
            materials.addAll(new ArrayList<>(Arrays.asList(blocks.split(";"))));
        } else materials.add(blocks);

        if (materials.isEmpty()) return null;
        else if (materials.size() == 1) {
            return Material.valueOf(materials.get(0));
        }

        List<String> valued = new ArrayList<>();
        String defaultMaterial = null;
        int total = 0;

        for (String material : materials) {
            if (!material.contains(":")) {
                defaultMaterial = material;
                continue;
            }

            int chance = Integer.parseInt(material.split(":")[1]);
            total += chance;

            for (int i = 0; i < chance; i++) valued.add(material.split(":")[0]);
        }

        if (defaultMaterial != null) {
            for (int i = 0; i < (100 - total); i++) valued.add(defaultMaterial);
        }

        String pickedMaterial = valued.get(plugin.getRandom().nextInt(valued.size())).toUpperCase();

        Material material;
        try {
            material = Material.valueOf(pickedMaterial);
        } catch (IllegalArgumentException e) {
            plugin.getConsoleOutput().err("Could not parse material " + pickedMaterial);
            return null;
        }

        return material;
    }

    public Material replaceBlock(String blockName) {
        if (blocklist.getString("Blocks." + blockName + ".replace-block") != null) {
            return pickRandomBlock(blocklist.getString("Blocks." + blockName + ".replace-block"));
        }
        return null;
    }

    public Material regenBlock(String blockName) {
        if (blocklist.getString("Blocks." + blockName + ".regenerate-into") != null) {
            Material picked = pickRandomBlock(blocklist.getString("Blocks." + blockName + ".regenerate-into"));
            return picked != null ? picked : Material.valueOf(blockName);
        }
        return Material.valueOf(blockName);
    }

    public int replaceDelay(String blockName) {
        return Amount.loadAmount(blocklist, "Blocks." + blockName + ".regen-delay", 1).getInt();
    }

    public int money(String blockName) {
        return Amount.loadAmount(blocklist, "Blocks." + blockName + ".money", 0).getInt();
    }

    public List<String> consoleCommands(String blockName) {
        List<String> consoleCommands = new ArrayList<>();

        String path = "Blocks." + blockName;

        if (!blocklist.contains("Blocks." + blockName))
            return new ArrayList<>();

        if (Objects.requireNonNull(blocklist.getConfigurationSection(path)).contains("console-commands"))
            path += ".console-commands";
        else if (Objects.requireNonNull(blocklist.getConfigurationSection(path)).contains("console-command"))
            path += ".console-command";
        else return consoleCommands;

        if (blocklist.isString(path))
            consoleCommands.add(blocklist.getString(path));
        else
            consoleCommands = blocklist.getStringList(path);

        return consoleCommands;
    }

    public List<String> playerCommands(String blockName) {
        List<String> playerCommands = new ArrayList<>();

        String path = "Blocks." + blockName;
        if (Objects.requireNonNull(blocklist.getConfigurationSection(path)).contains("player-commands"))
            path += ".player-commands";
        else if (Objects.requireNonNull(blocklist.getConfigurationSection(path)).contains("player-command"))
            path += ".player-command";
        else return playerCommands;

        if (blocklist.isString(path))
            playerCommands.add(blocklist.getString(path));
        else
            playerCommands = blocklist.getStringList(path);

        return playerCommands;
    }

    public String toolRequired(String blockName) {
        return blocklist.getString("Blocks." + blockName + ".tool-required");
    }

    public String enchantRequired(String blockName) {
        return blocklist.getString("Blocks." + blockName + ".enchant-required");
    }

    public String jobsCheck(String blockName) {
        return blocklist.getString("Blocks." + blockName + ".jobs-check");
    }

    public String particleCheck(String blockName) {
        return blocklist.getString("Blocks." + blockName + ".particles");
    }

    public boolean applyFortune(String blockName) {
        return blocklist.getBoolean("Blocks." + blockName + ".apply-fortune", true);
    }

    public boolean naturalBreak(String blockName) {
        return blocklist.getBoolean("Blocks." + blockName + ".natural-break", true);
    }

    public Material dropItemMaterial(String blockName) {
        if (!Strings.isNullOrEmpty(blocklist.getString("Blocks." + blockName + ".drop-item.material")))
            return Material.valueOf(blocklist.getString("Blocks." + blockName + ".drop-item.material").toUpperCase());
        return null;
    }

    public String dropItemName(String blockName, Player player) {

        if (blocklist.getString("Blocks." + blockName + ".drop-item.name") != null) {
            String displayName = blocklist.getString("Blocks." + blockName + ".drop-item.name");

            if (Strings.isNullOrEmpty(displayName))
                return null;

            if (BlockRegen.getInstance().isUsePlaceholderAPI())
                displayName = PlaceholderAPI.setPlaceholders(player, displayName);

            return Utils.color(Utils.parse(displayName, player));
        }

        return null;
    }

    public List<String> dropItemLore(String blockName, Player player) {
        List<String> lore = new ArrayList<>();

        for (String all : blocklist.getStringList("Blocks." + blockName + ".drop-item.lores")) {
            if (BlockRegen.getInstance().isUsePlaceholderAPI())
                all = PlaceholderAPI.setPlaceholders(player, all);

            lore.add(Utils.color(Utils.parse(all, player)));
        }

        return lore;
    }

    public boolean dropItemsNaturally(String blockName) {
        return blocklist.getBoolean("Blocks." + blockName + ".drop-naturally");
    }

    public boolean dropItemExpDrop(String blockName) {
        return blocklist.getBoolean("Blocks." + blockName + ".drop-item.exp.drop-naturally");
    }

    public int dropItemExpAmount(String blockName) {
        return Amount.loadAmount(blocklist, "Blocks." + blockName + ".drop-item.exp.amount", 1).getInt();
    }

    public int dropItemAmount(String blockName) {
        return Amount.loadAmount(blocklist, "Blocks." + blockName + ".drop-item.amount", 1).getInt();
    }

    public String eventName(String blockName) {
        return blocklist.getString("Blocks." + blockName + ".event.event-name");
    }

    public boolean eventDoubleDrops(String blockName) {
        return blocklist.getBoolean("Blocks." + blockName + ".event.double-drops");
    }

    public boolean eventDoubleExp(String blockName) {
        return blocklist.getBoolean("Blocks." + blockName + ".event.double-exp");
    }

    public int eventItemAmount(String blockName, Player player) {
        int amount = Amount.loadAmount(blocklist, "Blocks." + blockName + ".event.custom-item.amount", 1).getInt();

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
            int enchantLevel = Objects.requireNonNull(item.getItemMeta()).getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
            amount = amount + enchantLevel;
        }

        return amount;
    }

    public Material eventItemMaterial(String blockName) {
        if (blocklist.getString("Blocks." + blockName + ".event.custom-item.material") != null) {
            return Material.valueOf(Objects.requireNonNull(blocklist.getString("Blocks." + blockName + ".event.custom-item.material")).toUpperCase());
        }
        return null;
    }

    public String eventItemName(String blockName, Player player) {
        if (blocklist.getString("Blocks." + blockName + ".event.custom-item.name") != null) {
            String displayName = blocklist.getString("Blocks." + blockName + ".event.custom-item.name");

            if (Strings.isNullOrEmpty(displayName))
                return null;

            if (BlockRegen.getInstance().isUsePlaceholderAPI())
                displayName = PlaceholderAPI.setPlaceholders(player, displayName);

            return Utils.color(Utils.parse(displayName, player));
        }
        return null;
    }

    public List<String> eventItemLore(String blockName, Player player) {
        List<String> lore = new ArrayList<>();

        for (String all : blocklist.getStringList("Blocks." + blockName + ".event.custom-item.lores")) {
            if (BlockRegen.getInstance().isUsePlaceholderAPI())
                all = PlaceholderAPI.setPlaceholders(player, all);

            lore.add(Utils.color(Utils.parse(all, player)));
        }

        return lore;
    }

    public boolean eventItemDropNaturally(String blockName) {
        return blocklist.getBoolean("Blocks." + blockName + ".event.custom-item.drop-naturally");
    }

    public int eventItemRarity(String blockName) {
        return blocklist.getInt("Blocks." + blockName + ".event.custom-item.rarity");
    }
}