package nl.aurorion.blockregen.listeners;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.Utils;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final BlockRegen plugin;

    public PlayerJoin(BlockRegen instance) {
        this.plugin = instance;
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
        if (!Utils.bars.isEmpty()) {
            for (String bars : Utils.bars.keySet()) {
                BossBar bar = Utils.bars.get(bars);
                bar.addPlayer(player);
            }
        }
    }
}