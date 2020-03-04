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

    public static List<String> bypass = new ArrayList<>();
    public static List<String> itemcheck = new ArrayList<>();
    public static List<Color> colors = new ArrayList<>();
    public static List<Location> regenBlocks = new ArrayList<>();
    public static Map<String, Boolean> events = new HashMap<>();
    public static Map<String, BossBar> bars = new HashMap<>();
    public static Map<Location, BukkitTask> tasks = new HashMap<>();
    public static Map<Location, Material> persist = new HashMap<>();

    public static Chunk stringToChunk(String string) {
        String[] splits = string.split(";");
        return Bukkit.getWorld(splits[0]).getChunkAt(Integer.valueOf(splits[1]), Integer.valueOf(splits[2]));
    }

    public static String chunkToString(Chunk chunk) {
        return chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
    }

    public static String locationToString(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ();
    }

    public static Location stringToLocation(String str) {
        String[] strar = str.split(";");
        Location newLoc = new Location(Bukkit.getWorld(strar[0]), Double.valueOf(strar[1]).doubleValue(), Double.valueOf(strar[2]).doubleValue(), Double.valueOf(strar[3]).doubleValue());
        return newLoc.clone();
    }

    public static String parse(String string, Player player) {
        return parse("", string, player);
    }

    public static String parse(String modifier, String string, Player player) {
        string = string.replace("%player" + modifier + "%", player.getName());
        string = string.replace("%player" + modifier + "Display%", player.getDisplayName());
        //string = string.replace("%player" + modifier + "Suffix%", Main.getChat().getPlayerSuffix(player));
        //string = string.replace("%player" + modifier + "Prefix%", Main.getChat().getPlayerPrefix(player));
        string = string.replace("%player" + modifier + "MaxHP%", String.valueOf(player.getMaxHealth()));
        string = string.replace("%player" + modifier + "HP%", String.valueOf(player.getHealth()));
        string = string.replace("%player" + modifier + "X%", String.valueOf(((int) player.getLocation().getX())));
        string = string.replace("%player" + modifier + "Y%", String.valueOf((int) player.getLocation().getY()));
        string = string.replace("%player" + modifier + "Z%", String.valueOf((int) player.getLocation().getZ()));
        string = string.replace("%player" + modifier + "World%", String.valueOf(player.getLocation().getWorld()));
        string = string.replace("%player" + modifier + "Food%", String.valueOf(player.getFoodLevel()));
        string = string.replace("%player" + modifier + "Level%", String.valueOf(player.getLevel()));
        return string;
    }

    public static List<String> parseList(String modifier, List<String> list, Player player) {
        List<String> outPut = new ArrayList<>();
        list.forEach(line -> outPut.add(parse(modifier, line, player)));
        return outPut;
    }

    public static List<String> parseList(List<String> list, Player player) {
        return parseList("", list, player);
    }

    /*public static String blockToString(Block block){
    	return block.getType().name();
    }*/

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
