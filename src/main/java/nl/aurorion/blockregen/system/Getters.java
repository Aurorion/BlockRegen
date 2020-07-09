package nl.aurorion.blockregen.system;

import com.google.common.base.Strings;
import me.clip.placeholderapi.PlaceholderAPI;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import nl.aurorion.blockregen.system.preset.Amount;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Getters {

    // TODO: Remove completely

    private final BlockRegen plugin;

    private FileConfiguration blocklist;

    public Getters(BlockRegen plugin) {
        this.plugin = plugin;

        load();
    }

    public void load() {
        this.blocklist = plugin.getFiles().getBlockList().getFileConfiguration();
    }

    // moved
    public String eventName(String blockName) {
        return blocklist.getString("Blocks." + blockName + ".event.event-name");
    }

    // moved
    public boolean eventDoubleDrops(String blockName) {
        return blocklist.getBoolean("Blocks." + blockName + ".event.double-drops");
    }

    // moved
    public boolean eventDoubleExp(String blockName) {
        return blocklist.getBoolean("Blocks." + blockName + ".event.double-exp");
    }

    // moved
    public int eventItemAmount(String blockName, Player player) {
        int amount = Amount.loadAmount(blocklist, "Blocks." + blockName + ".event.custom-item.amount", 1).getInt();

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
            int enchantLevel = Objects.requireNonNull(item.getItemMeta()).getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
            amount = amount + enchantLevel;
        }

        return amount;
    }

    // moved
    public Material eventItemMaterial(String blockName) {
        if (blocklist.getString("Blocks." + blockName + ".event.custom-item.material") != null) {
            return Material.valueOf(Objects.requireNonNull(blocklist.getString("Blocks." + blockName + ".event.custom-item.material")).toUpperCase());
        }
        return null;
    }

    // moved
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

    // moved
    public List<String> eventItemLore(String blockName, Player player) {
        List<String> lore = new ArrayList<>();

        for (String all : blocklist.getStringList("Blocks." + blockName + ".event.custom-item.lores")) {
            if (BlockRegen.getInstance().isUsePlaceholderAPI())
                all = PlaceholderAPI.setPlaceholders(player, all);

            lore.add(Utils.color(Utils.parse(all, player)));
        }

        return lore;
    }

    // moved
    public boolean eventItemDropNaturally(String blockName) {
        return blocklist.getBoolean("Blocks." + blockName + ".event.custom-item.drop-naturally");
    }

    // moved
    public int eventItemRarity(String blockName) {
        return blocklist.getInt("Blocks." + blockName + ".event.custom-item.rarity");
    }
}