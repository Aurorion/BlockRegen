package nl.aurorion.blockregen.listeners;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {

    private final BlockRegen plugin;

    public PlayerInteract(BlockRegen instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (event.getClickedBlock() != null && Utils.dataCheck.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(Message.DATA_CHECK.get(player).replace("%block%", event.getClickedBlock().getType().toString()));
        }
    }
}