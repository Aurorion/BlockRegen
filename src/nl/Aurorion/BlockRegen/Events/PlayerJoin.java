package nl.Aurorion.BlockRegen.Events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import nl.Aurorion.BlockRegen.Main;

public class PlayerJoin implements Listener {
	
	private Main main;
	
	public PlayerJoin(Main instance) {
		this.main = instance;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("blockregen.admin")) {
			if (main.newVersion != null) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&6&m-----&r &3&lBlockRegen &6&m-----"
						+ "\n&eA new update was found!"
						+ "\n&eCurrent verion: &c" + main.getDescription().getVersion()
						+ "\n&eNew version: &a" + main.newVersion
						+ "\n&6&m-----------------------"));
			}
		}
	}

}
