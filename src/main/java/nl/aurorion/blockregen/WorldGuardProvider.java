package nl.aurorion.blockregen;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardProvider {

    private final BlockRegen plugin;

    public WorldGuardProvider(BlockRegen plugin) {
        this.plugin = plugin;
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

        LocalPlayer localPlayer = plugin.getWorldGuard().wrapPlayer(player);

        return !(set.queryState(localPlayer, Flags.BLOCK_BREAK) == StateFlag.State.DENY || set.queryState(localPlayer, Flags.BUILD) == StateFlag.State.DENY);
    }
}
