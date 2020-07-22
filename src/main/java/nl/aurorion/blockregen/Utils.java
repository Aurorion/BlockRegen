package nl.aurorion.blockregen;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class Utils {

    public final List<String> bypass = new ArrayList<>();
    public final List<String> dataCheck = new ArrayList<>();

    public final List<Color> colors = new ArrayList<>();

    static {
        colors.add(Color.AQUA);
        colors.add(Color.BLUE);
        colors.add(Color.FUCHSIA);
        colors.add(Color.GREEN);
        colors.add(Color.LIME);
        colors.add(Color.ORANGE);
        colors.add(Color.WHITE);
        colors.add(Color.YELLOW);
    }

    public final Map<String, Boolean> events = new HashMap<>();
    public final Map<String, BossBar> bars = new HashMap<>();

    public String locationToString(Location loc) {
        World world = loc.getWorld();
        return world == null ? "" : world.getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ();
    }

    public Location stringToLocation(String str) {
        String[] arr = str.split(";");
        Location newLoc = new Location(Bukkit.getWorld(arr[0]), Double.parseDouble(arr[1]), Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
        return newLoc.clone();
    }

    public String parse(String string) {
        string = string.replaceAll("(?i)%prefix%", Message.PREFIX.getValue());
        return string;
    }

    public String parse(String string, Player player) {
        string = string.replaceAll("(?i)%player%", player.getName());
        if (BlockRegen.getInstance().isUsePlaceholderAPI())
            string = PlaceholderAPI.setPlaceholders(player, string);
        return string;
    }

    public String stripColor(String msg) {
        return msg != null ? ChatColor.stripColor(msg) : null;
    }

    @Nullable
    public String color(@Nullable String msg) {
        return color(msg, '&');
    }

    @Nullable
    public String color(@Nullable String msg, char colorChar) {
        return msg == null ? null : ChatColor.translateAlternateColorCodes(colorChar, msg);
    }

    @Nullable
    public Material parseMaterial(@Nullable String input) {
        if (Strings.isNullOrEmpty(input)) return null;

        try {
            return Material.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            BlockRegen.getInstance().getConsoleOutput().debug("Could not parse material " + input);
            return null;
        }
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    private int quantityDropped(Material mat) {
        return mat == Material.LAPIS_ORE ? 4 + BlockRegen.getInstance().getRandom().nextInt(5) : 1;
    }

    /**
     * Get the quantity dropped based on the given fortune level
     */
    public int applyFortune(Material mat, ItemStack tool) {
        if (tool.getItemMeta() == null || !tool.getItemMeta().hasEnchants() || !tool.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS))
            return 0;

        int fortune = tool.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);

        if (fortune > 0) {
            int i = BlockRegen.getInstance().getRandom().nextInt(fortune + 2) - 1;

            if (i < 0) i = 0;

            return quantityDropped(mat) * i;
        } else return quantityDropped(mat);
    }
}