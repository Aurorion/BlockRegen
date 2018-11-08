package nl.Aurorion.BlockRegen.Events;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import nl.Aurorion.BlockRegen.Utils;

public class BlockExplode implements Listener {
	
	@EventHandler
	public void onExplode(BlockExplodeEvent event) {
		Utils.explode.addAll(event.blockList());
		for (Block block : Utils.explode) {
			Utils.restorer.put(block.getLocation(), block.getType());
		}
		Utils.explode.clear();
	}

}
