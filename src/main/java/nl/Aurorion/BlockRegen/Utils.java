package nl.Aurorion.BlockRegen;

import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
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
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}