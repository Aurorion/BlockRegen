package nl.Aurorion.BlockRegen;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Utils {
	
	public static ArrayList<String> bypass = new ArrayList<String>();
	public static ArrayList<String> itemcheck = new ArrayList<String>();
	public static ArrayList<Color> colors = new ArrayList<Color>();
	public static HashMap<String, Boolean> events = new HashMap<String, Boolean>();
	public static HashMap<String, BossBar> bars = new HashMap<String, BossBar>();
	
	public static Chunk stringToChunk(String string) {
        String[] splits = string.split(";");
        return Bukkit.getWorld(splits[0]).getChunkAt(Integer.valueOf(splits[1]), Integer.valueOf(splits[2]));
    }
 
    public static String chunkToString(Chunk chunk) {
        return chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
    }
 
    public static String locationToString(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
    }
 
    public static Location stringToLocation(String str) {
        String[] strar = str.split(";");
        Location newLoc = new Location(Bukkit.getWorld(strar[0]), Double.valueOf(strar[1]).doubleValue(), Double.valueOf(strar[2]).doubleValue(), Double.valueOf(strar[3]).doubleValue(), Float.valueOf(strar[4]).floatValue(), Float.valueOf(strar[5]).floatValue());
        return newLoc.clone();
    }
    
	public static String blockToString(Block block){
    	return block.getType().name();
    }
    
    @SuppressWarnings("deprecation")
	public static ItemStack getHand(Player player){
    	String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        String minorVer = split[1]; //For 1.10 will be "10"
        if(Integer.valueOf(minorVer) >= 9){
        	return player.getInventory().getItemInMainHand();
        }else{
        	return player.getInventory().getItemInHand();
        }
    }
    
    public static Integer getVersion(){
    	String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        String minorVer = split[1]; //For 1.10 will be "10"
        if(Integer.valueOf(minorVer) >= 9){
        	return 9;
        }else{
        	return 8;
        }
    }
    
    String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
    String majorVer = split[0]; //For 1.10 will be "1"
    String minorVer = split[1]; //For 1.10 will be "10"
    String minorVer2 = split.length > 2 ? split[2]:"0"; //For 1.10 will be "0", for 1.9.4 will be "4"
    
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
