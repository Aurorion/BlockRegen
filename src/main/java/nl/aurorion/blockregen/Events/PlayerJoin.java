package nl.aurorion.blockregen.Events;

import nl.aurorion.blockregen.BlockRegen;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final BlockRegen plugin;

    public PlayerJoin(BlockRegen instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("blockregen.admin"))
            return;

        if (plugin.newVersion == null)
            return;

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&6&m-----&r &3&lBlockRegen &6&m-----"
                        + "\n&eA new update was found!"
                        + "\n&eCurrent verion: &c" + plugin.getDescription().getVersion()
                        + "\n&eNew version: &a" + plugin.newVersion
                        + "\n&6&m-----------------------"));
    }
}