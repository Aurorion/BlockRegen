package nl.aurorion.blockregen.providers;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import org.bukkit.entity.Player;

public class WorldEditProvider {

    private final WorldEditPlugin worldEdit;

    public WorldEditProvider() {
        this.worldEdit = (WorldEditPlugin) BlockRegen.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
    }

    public Region getSelection(Player player) {
        Region selection;
        try {
            selection = worldEdit.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (IncompleteRegionException e) {
            player.sendMessage(Message.NO_SELECTION.get(player));
            if (BlockRegen.getInstance().getConsoleOutput().isDebug())
                e.printStackTrace();
            return null;
        }
        return selection;
    }
}