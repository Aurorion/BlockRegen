package nl.Aurorion.BlockRegen.Events;

import nl.Aurorion.BlockRegen.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private Main main;

    public PlayerJoin(Main instance) {
        this.main = instance;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("blockregen.admin")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&6&m-----&r &3&lBlockRegen &6&m-----"
                            + "\n&eCurrent verion: &c" + main.getDescription().getVersion()
                            + "\n&eUpdater &cdisabled."
                            + "\n&eForked with &d<3 &eby Wertik1206"
                            + "\n&6&m-----------------------"));
        }
    }

}
