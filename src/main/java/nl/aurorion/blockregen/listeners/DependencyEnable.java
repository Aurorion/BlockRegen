package nl.aurorion.blockregen.listeners;

import nl.aurorion.blockregen.BlockRegen;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class DependencyEnable implements Listener {

    private final BlockRegen plugin;

    public DependencyEnable(BlockRegen plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnable(final PluginEnableEvent event) {
        plugin.checkDependencies();
    }
}