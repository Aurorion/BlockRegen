package nl.aurorion.blockregen.provider;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import nl.aurorion.blockregen.BlockRegen;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardProvider {

    private final BlockRegen plugin;

    private final WorldGuardPlugin worldGuard;

    public WorldGuardProvider(BlockRegen plugin) {
        this.plugin = plugin;
        this.worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
    }

    public boolean canBreak(Player player, Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (location.getWorld() == null) return false;

        com.sk89q.worldedit.util.Location localLocation = new com.sk89q.worldedit.util.Location(
                BukkitAdapter.adapt(location.getWorld()),
                location.getX(),
                location.getY(),
                location.getZ());

        ApplicableRegionSet set = query.getApplicableRegions(localLocation);

        LocalPlayer localPlayer = worldGuard.wrapPlayer(player);

        return !(set.queryState(localPlayer, Flags.BLOCK_BREAK) == StateFlag.State.DENY || set.queryState(localPlayer, Flags.BUILD) == StateFlag.State.DENY);
    }
}
