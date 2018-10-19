package nl.Aurorion.BlockRegen;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import com.sk89q.worldedit.Vector;

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
    
    public static String vectorToString(Vector vec) {
    	String str = vec.toString().replaceAll(" ", "").replaceAll(",", ";").replace("(", "").replace(")", "");
        return str;
    }
    
    public static Vector stringToVector(String str) {
        String[] strar = str.split(";");
        Vector newVec = new Vector(Double.valueOf(strar[0]), Double.valueOf(strar[1]), Double.valueOf(strar[2]));
        return newVec;
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
