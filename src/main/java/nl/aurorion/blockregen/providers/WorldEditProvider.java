package nl.aurorion.blockregen.providers;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.system.region.struct.RegenerationRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class WorldEditProvider {

    private final BlockRegen plugin;

    private final WorldEditPlugin worldEdit;

    public WorldEditProvider(BlockRegen plugin) {
        this.plugin = plugin;
        this.worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
    }

    public Region getSelection(Player player) {
        Region selection;
        try {
            selection = worldEdit.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (IncompleteRegionException e) {
            if (BlockRegen.getInstance().getConsoleOutput().isDebug())
                e.printStackTrace();
            return null;
        }
        return selection;
    }

    @Nullable
    public RegenerationRegion createFromSelection(String name, Region selection) {

        if (selection == null || selection.getWorld() == null)
            return null;

        World world = BukkitAdapter.adapt(selection.getWorld());

        Location min = BukkitAdapter.adapt(world, selection.getMinimumPoint());
        Location max = BukkitAdapter.adapt(world, selection.getMaximumPoint());

        return plugin.getRegionManager().createRegion(name, min, max);
    }
}