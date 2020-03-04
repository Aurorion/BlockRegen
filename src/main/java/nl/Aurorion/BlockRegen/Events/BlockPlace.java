package nl.Aurorion.BlockRegen.Events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.Utils;

public class BlockPlace implements Listener {
	
	private Main main;
	
	public BlockPlace(Main instance) {
		this.main = instance;
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		if(!Utils.restorer.containsKey(block.getLocation()) && main.getGetters().useRestorer()) {
			Utils.restorer.put(block.getLocation(), Material.AIR);
		}
	}

}
