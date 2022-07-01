package nl.aurorion.blockregen.version.legacy;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;
import nl.aurorion.blockregen.version.api.NodeData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Ageable;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;
import org.bukkit.material.Tree;

@Log
@ToString
@NoArgsConstructor
public class LegacyNodeData implements NodeData {

    private BlockFace facing;

    private BlockFace treeFacing;

    // Stairs

    private boolean inverted;

    private int age = -1;

    private boolean farmland;

    @Override
    public void load(Block block) {
        MaterialData data = block.getState().getData();

        if (data instanceof Directional) {
            this.facing = ((Directional) data).getFacing();
        }

        if (data instanceof Tree) {
            this.treeFacing = ((Tree) data).getDirection();
        }

        if (data instanceof Stairs) {
            this.inverted = ((Stairs) data).isInverted();
        }

        if (data instanceof Ageable) {
            this.age = ((Ageable) data).getAge();
        }

        // Check for farmland under this block
        Block underBlock = block.getRelative(BlockFace.DOWN);

        if (underBlock.getType() == Material.SOIL) {
            this.farmland = true;
        }

        log.fine("Loaded block data " + this);
        log.fine(block.getType().toString());
    }

    @Override
    public void place(Block block) {
        BlockState state = block.getState();
        MaterialData data = state.getData();

        if (data instanceof Directional && this.facing != null) {
            ((Directional) data).setFacingDirection(this.facing);
        }

        if (data instanceof Tree && this.treeFacing != null) {
            ((Tree) data).setDirection(this.treeFacing);
        }

        if (data instanceof Stairs && this.inverted) {
            ((Stairs) data).setInverted(true);
        }

        if (data instanceof Ageable && this.age != -1) {
            ((Ageable) data).setAge(this.age);
        }

        if (farmland) {
            block.getRelative(BlockFace.DOWN).setType(Material.SOIL);
        }

        state.setData(data);
        state.update(true);
    }

}
