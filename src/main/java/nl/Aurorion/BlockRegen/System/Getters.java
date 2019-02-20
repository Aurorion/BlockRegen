package nl.Aurorion.BlockRegen.System;

import nl.Aurorion.BlockRegen.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
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

    public boolean useRestorer() {
        return main.getFiles().getSettings().getBoolean("Use-Restorer");
    }

    // Getters Blocklist.yml

    public List<String> blockNames() {
        return new ArrayList<>(main.getFiles().getBlocklist().getConfigurationSection("Blocks").getKeys(false));
    }

    public Material replaceBlockMaterial(String blockName) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockName + ".replace-block") != null)
            return Material.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockName + ".replace-block").split(";")[0].toUpperCase());
        return null;
    }

    public Material blockMaterial(String blockName) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockName + ".block-type") != null)
            return Material.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockName + ".block-type").split(";")[0].toUpperCase());
        else
        {
            main.getLogger().warning("Block material of " + blockName + " is invalid.");
            return null;
        }
    }

    public byte blockData(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".block-type") != null)
            if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".block-type").contains(";"))
                return Byte.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockname + ".block-type").split(";")[1]);
        return 0;
    }

    public byte replaceBlockData(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".replace-block") != null)
            if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".replace-block").contains(";"))
                return Byte.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockname + ".replace-block").split(";")[1]);
        return 0;
    }

    public Integer replaceDelay(String blockname) {
        return main.getFiles().getBlocklist().getInt("Blocks." + blockname + ".regen-delay");
    }

    public Integer money(String blockname) {
        return main.getFiles().getBlocklist().getInt("Blocks." + blockname + ".money");
    }

    public List<String> consoleCommands(String blockname) {
        return main.getFiles().getBlocklist().getStringList("Blocks." + blockname + ".console-commands");
    }

    public List<String> playerCommands(String blockname) {
        return main.getFiles().getBlocklist().getStringList("Blocks." + blockname + ".player-commands");
    }

    public String jobsCheck(String blockname) {
        return main.getFiles().getBlocklist().getString("Blocks." + blockname + ".jobs-check");
    }

    public boolean naturalBreak(String blockname) {
        return main.getFiles().getBlocklist().getBoolean("Blocks." + blockname + ".natural-break");
    }

    public Material dropItemMaterial(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.material") != null) {
            return Material.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.material").split(";")[0]);
        }
        return null;
    }

    public byte dropItemData(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.material") != null)
            if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.material").contains(";"))
                return Byte.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.material").split(";")[1]);
        return 0;
    }

    public String dropItemName(String blockname) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.name") != null)
            return ChatColor.translateAlternateColorCodes('&', main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.name"));
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

    public List<Enchant> dropEnchants(String blockname) {
        List<Enchant> enchants = new ArrayList<>();
        if (main.getFiles().getBlocklist().getStringList("Blocks." + blockname + ".drop-item.enchants") != null)
            for (String enchantData : main.getFiles().getBlocklist().getStringList("Blocks." + blockname + ".drop-item.enchants")) {
                String[] splitter = enchantData.split(";");
                Enchantment enchantment;
                int enchantLevel;
                try {
                    enchantment = Enchantment.getByName(splitter[0]);
                    enchantLevel = Integer.valueOf(splitter[1]);
                } catch (Exception e) {
                    main.getLogger().warning("A mistake in config, Enchant levels/names of " + blockname + " are not valid.");
                    continue;
                }
                enchants.add(new Enchant(enchantment, enchantLevel));
            }
        return enchants;
    }

    public List<ItemFlag> dropFlags(String blockname) {
        List<ItemFlag> flags = new ArrayList<>();
        if (main.getFiles().getBlocklist().getString("Blocks." + blockname + ".drop-item.flags") != null) {
            for (String flagName : main.getFiles().getBlocklist().getStringList("Blocks." + blockname + ".drop-item.flags")) {
                ItemFlag flag;
                try {
                    flag = ItemFlag.valueOf(flagName);
                } catch (Exception e) {
                    main.getLogger().warning("A mistake in config, Flags of " + blockname + " are not valid.");
                    continue;
                }
                flags.add(flag);
            }
        }
        return flags;
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

    public List<String> regionBlocks(String regionName) {
        List<String> blockNames = new ArrayList<>();
        if (main.getFiles().getRegions().getConfigurationSection("Regions." + regionName).contains("blocks")){
            // Get the list in the save file

            if (main.getFiles().getRegions().getConfigurationSection("Regions." + regionName).contains("blocks")) {
                String blocks1 = main.getFiles().getRegions().getString("Regions." + regionName + ".blocks");
                if (blocks1.contains(","))
                    for (String block : blocks1.split(",")) {
                        blockNames.add(block);
                    }
                else
                    blockNames.add(blocks1);
            }
        }
            return blockNames;
    }

    // Tool getters

    public Material toolMaterial(String blockName) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockName + ".tool-required.material") != null)
            return Material.valueOf(main.getFiles().getBlocklist().getString("Blocks." + blockName + ".tool-required.material").split(";")[0].toUpperCase());
        return null;
    }

    public String toolName(String blockName) {
        if (main.getFiles().getBlocklist().getString("Blocks." + blockName + ".tool-required.name") != null)
            return ChatColor.translateAlternateColorCodes('&', main.getFiles().getBlocklist().getString("Blocks." + blockName + ".tool-required.name"));
        return null;
    }

    public List<String> toolLores(String blockName) {
        List<String> lores = new ArrayList<String>();
        for (String all : main.getFiles().getBlocklist().getStringList("Blocks." + blockName + ".tool-required.lores")) {
            lores.add(ChatColor.translateAlternateColorCodes('&', all));
        }
        return lores;
    }

    public List<Enchant> toolEnchants(String blockName) {
        List<Enchant> enchants = new ArrayList<>();
        if (main.getFiles().getBlocklist().getStringList("Blocks." + blockName + ".tool-required.enchants") != null)
            for (String enchantData : main.getFiles().getBlocklist().getStringList("Blocks." + blockName + ".tool-required.enchants")) {
                String[] splitter = enchantData.split(";");
                Enchantment enchantment;
                int enchantLevel;
                try {
                    enchantment = Enchantment.getByName(splitter[0]);
                    enchantLevel = Integer.valueOf(splitter[1]);
                } catch (Exception e) {
                    main.getLogger().warning("A mistake in config, Enchant levels/names for a tool of " + blockName + " are not valid.");
                    continue;
                }
                enchants.add(new Enchant(enchantment, enchantLevel));
            }
        return enchants;
    }

    public int toolAmount(String blockName) {
        if (main.getFiles().getBlocklist().getConfigurationSection("Blocks." + blockName + ".tool-required").contains("amount"))
            return main.getFiles().getBlocklist().getInt("Blocks." + blockName + ".tool-required.amount");
        return 1;
    }

    public List<ItemFlag> toolItemFlags(String blockName) {
        List<ItemFlag> flags = new ArrayList<>();
        if (main.getFiles().getBlocklist().getString("Blocks." + blockName + ".tool-required.flags") != null) {
            for (String flagName : main.getFiles().getBlocklist().getStringList("Blocks." + blockName + ".tool-required.flags")) {
                ItemFlag flag;
                try {
                    flag = ItemFlag.valueOf(flagName);
                } catch (Exception e) {
                    main.getLogger().warning("A mistake in config, Flags for tool of " + blockName + " are not valid.");
                    continue;
                }
                flags.add(flag);
            }
        }
        return flags;
    }
}
