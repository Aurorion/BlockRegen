package nl.aurorion.blockregen.Events;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Crops;

public class PlayerInteract implements Listener {

    private final BlockRegen plugin;

    public PlayerInteract(BlockRegen instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getGetters().boneMealOverride() ||
                event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

		if (!player.getInventory().getItemInMainHand().getType().equals(Material.BONE_MEAL) ||
				event.getClickedBlock() == null)
			return;

		if (!(event.getClickedBlock().getState().getData() instanceof Crops))
            return;

        Location loc = event.getClickedBlock().getLocation();

        if (Utils.tasks.containsKey(loc))
            Bukkit.getScheduler().cancelTask(Utils.tasks.get(loc).getTaskId());

        Utils.regenBlocks.remove(loc);
    }
}