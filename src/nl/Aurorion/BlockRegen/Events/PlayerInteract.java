package nl.Aurorion.BlockRegen.Events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Crops;

import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.Utils;

public class PlayerInteract implements Listener {

	private Main main;

	public PlayerInteract(Main instance) {
		this.main = instance;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (main.getFiles().getSettings().getBoolean("Bone-Meal-Override")) {
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Player player = event.getPlayer();
				if (player.getInventory().getItemInMainHand().getType().equals(Material.BONE_MEAL)) {
					if (event.getClickedBlock().getState().getData() instanceof Crops) {
						Location loc = event.getClickedBlock().getLocation();
						if (Utils.tasks.containsKey(loc)) {
							Bukkit.getScheduler().cancelTask(Utils.tasks.get(loc).getTaskId());
						}
						if (Utils.regenBlocks.contains(loc)) {
							Utils.regenBlocks.remove(loc);
						}
					}
				}
			}
		}
	}
}
