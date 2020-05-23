package nl.aurorion.blockregen.listeners;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
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

        Player player = event.getPlayer();

        if (event.getAction().toString().toUpperCase().contains("BLOCK") && Utils.dataCheck.contains(player.getName())) {
            if (event.getClickedBlock() == null) return;

            event.setCancelled(true);
            player.sendMessage(Message.DATA_CHECK.get().replace("%block%", event.getClickedBlock().getType().toString()));
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.getGetters().boneMealOverride()) {
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
}