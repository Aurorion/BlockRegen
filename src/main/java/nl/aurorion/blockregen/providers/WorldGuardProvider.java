package nl.aurorion.blockregen.providers;

import com.sk89q.worldguard.bukkit.ProtectionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import nl.aurorion.blockregen.BlockRegen;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardProvider {

    private final WorldGuardPlugin worldGuard;

    public WorldGuardProvider() {
        this.worldGuard = (WorldGuardPlugin) BlockRegen.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
    }

    public boolean canBreak(Player player, Location location) {
        ProtectionQuery protectionQuery = worldGuard.createProtectionQuery();
        return protectionQuery.testBlockBreak(player, location.getBlock());
    }
}