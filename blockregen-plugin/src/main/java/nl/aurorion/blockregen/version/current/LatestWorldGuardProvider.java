package nl.aurorion.blockregen.version.current;

import com.sk89q.worldguard.bukkit.ProtectionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import nl.aurorion.blockregen.version.api.WorldGuardProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LatestWorldGuardProvider implements WorldGuardProvider {

    private final WorldGuardPlugin worldGuard;

    public LatestWorldGuardProvider(WorldGuardPlugin worldGuard) {
        this.worldGuard = worldGuard;
    }

    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        ProtectionQuery protectionQuery = worldGuard.createProtectionQuery();
        return protectionQuery.testBlockBreak(player, location.getBlock());
    }
}