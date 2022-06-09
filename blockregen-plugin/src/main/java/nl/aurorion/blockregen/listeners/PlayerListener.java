package nl.aurorion.blockregen.listeners;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final BlockRegen plugin;

    public PlayerListener(BlockRegen instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getClickedBlock() != null && Utils.dataCheck.contains(player.getUniqueId())) {
            // Ignore other interacts.
            if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            event.setCancelled(true);

            player.sendMessage(Message.DATA_CHECK.get(player).replace("%block%", event.getClickedBlock().getType().toString()));
        }
    }

    // Inform about a new version
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // newVersion will be null when the checker is disabled, or there are no new available
        if (player.hasPermission("blockregen.admin") && plugin.newVersion != null)
            player.sendMessage(Message.UPDATE.get(player)
                    .replaceAll("(?i)%newVersion%", plugin.newVersion)
                    .replaceAll("(?i)%version%", plugin.getDescription().getVersion()));

        // Add to bars if needed
        plugin.getEventManager().addBars(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getEventManager().removeBars(event.getPlayer());
    }
}