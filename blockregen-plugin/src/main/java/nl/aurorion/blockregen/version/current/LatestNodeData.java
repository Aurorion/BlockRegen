package nl.aurorion.blockregen.version.current;

import com.bekvon.bukkit.residence.selection.SelectionManager.Direction;
import com.cryptomorin.xseries.XMaterial;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import lombok.NoArgsConstructor;
import nl.aurorion.blockregen.version.api.NodeData;

@NoArgsConstructor
public class LatestNodeData implements NodeData {

    private BlockFace facing;

    @Override
    public void load(Block block) {
        BlockData data = block.getBlockData();

        if (data instanceof Directional) {
            this.facing = ((Directional) data).getFacing();
        }
    }

    @Override
    public void place(Block block) {
        
    }
}
