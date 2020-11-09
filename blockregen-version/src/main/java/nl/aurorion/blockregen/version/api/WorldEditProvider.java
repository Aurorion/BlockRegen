package nl.aurorion.blockregen.version.api;


import nl.aurorion.blockregen.system.region.struct.RegenerationRegion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WorldEditProvider {

    @Nullable RegenerationRegion createFromSelection(@NotNull String name, Player player);
}
