package nl.aurorion.blockregen;

import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static final List<String> bypass = new ArrayList<>();
    public static final List<String> dataCheck = new ArrayList<>();

    public static final List<Color> colors = new ArrayList<>();
    public static final List<Location> regenBlocks = new ArrayList<>();

    public static final Map<String, Boolean> events = new HashMap<>();
    public static final Map<String, BossBar> bars = new HashMap<>();

    public static final Map<Location, BukkitTask> tasks = new HashMap<>();
    public static final Map<Location, Material> persist = new HashMap<>();

    public static String locationToString(Location loc) {
        World world = loc.getWorld();
        return world == null ? "" : world.getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ();
    }

    public static Location stringToLocation(String str) {
        String[] arr = str.split(";");
        Location newLoc = new Location(Bukkit.getWorld(arr[0]), Double.parseDouble(arr[1]), Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
        return newLoc.clone();
    }

    public static String parse(String string, Player player) {
        string = string.replace("%player%", player.getName());
        return string;
    }

    public static void fillFireworkColors() {
        colors.add(Color.AQUA);
        colors.add(Color.BLUE);
        colors.add(Color.FUCHSIA);
        colors.add(Color.GREEN);
        colors.add(Color.LIME);
        colors.add(Color.ORANGE);
        colors.add(Color.WHITE);
        colors.add(Color.YELLOW);
    }

    public static String color(String msg) {
        return msg != null ? ChatColor.translateAlternateColorCodes('&', msg) : null;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    private static int quantityDropped(Material mat) {
        return mat == Material.LAPIS_ORE ? 4 + BlockRegen.getInstance().getRandom().nextInt(5) : 1;
    }

    /**
     * Get the quantity dropped based on the given fortune level
     */
    public static int applyFortune(Material mat, ItemStack tool) {
        if (tool.getItemMeta() == null || !tool.getItemMeta().hasEnchants() || !tool.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS))
            return 0;

        int fortune = tool.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);

        if (fortune > 0) {
            int i = BlockRegen.getInstance().getRandom().nextInt(fortune + 2) - 1;

            if (i < 0)
                i = 0;

            return quantityDropped(mat) * (i + 1);
        } else return quantityDropped(mat);
    }
}