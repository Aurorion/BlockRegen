package nl.aurorion.blockregen.listeners;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.system.region.struct.RegionSelection;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerListener implements Listener {

    private final BlockRegen plugin;

    public PlayerListener(BlockRegen instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Ignore offhand events at 1.9+
        if (plugin.getVersionManager().isAbove("1_9", true) && event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        // Ignore other interacts.
        if ((event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) || event.getClickedBlock() == null) {
            return;
        }

        // Region selection

        // Use our own selection only if WorldEdit is not installed.

        if (player.hasPermission("blockregen.select") && plugin.getVersionManager().getMethods().getItemInMainHand(player).getType() == Material.WOODEN_AXE && plugin.getVersionManager().getWorldEditProvider() == null) {
            RegionSelection selection = plugin.getRegionManager().getOrCreateSelection(player);

            // Selecting first.
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                selection.setFirst(event.getClickedBlock().getLocation());

                player.sendMessage(Message.SELECT_FIRST.get(player)
                        .replace("%x%", String.format("%.0f", selection.getFirst().getX()))
                        .replace("%y%", String.format("%.0f", selection.getFirst().getY()))
                        .replace("%z%", String.format("%.0f", selection.getFirst().getZ())));
            } else {
                // Selecting second.
                selection.setSecond(event.getClickedBlock().getLocation());

                player.sendMessage(Message.SELECT_SECOND.get(player)
                        .replace("%x%", String.format("%.0f", selection.getSecond().getX()))
                        .replace("%y%", String.format("%.0f", selection.getSecond().getY()))
                        .replace("%z%", String.format("%.0f", selection.getSecond().getZ())));
            }

            event.setCancelled(true);
            return;
        }

        // Data check

        if (plugin.getRegenerationManager().hasDataCheck(player)) {
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