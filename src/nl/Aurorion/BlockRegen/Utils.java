package nl.Aurorion.BlockRegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;

public class Utils {
	
	public static List<String> bypass = new ArrayList<String>();
	public static List<String> itemcheck = new ArrayList<String>();
	public static List<Color> colors = new ArrayList<Color>();
	public static List<Location> regenBlocks = new ArrayList<Location>();
	public static Map<String, Boolean> events = new HashMap<String, Boolean>();
	public static Map<String, BossBar> bars = new HashMap<String, BossBar>();
	public static Map<Location, BukkitTask> tasks = new HashMap<Location, BukkitTask>();
	public static Map<Location, Material> persist = new HashMap<Location, Material>();
	
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
    
	public static String blockToString(Block block){
    	return block.getType().name();
    }
    
    public static void fillFireworkColors(){
    	colors.add(Color.AQUA);
    	colors.add(Color.BLUE);
    	colors.add(Color.FUCHSIA);
    	colors.add(Color.GREEN);
    	colors.add(Color.LIME);
    	colors.add(Color.ORANGE);
    	colors.add(Color.WHITE);
    	colors.add(Color.YELLOW);
    }

}
