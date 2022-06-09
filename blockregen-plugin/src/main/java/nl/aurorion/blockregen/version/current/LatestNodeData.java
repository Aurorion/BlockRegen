package nl.aurorion.blockregen.version.current;

import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import nl.aurorion.blockregen.version.api.NodeData;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.Stairs;

@Log
@NoArgsConstructor
public class LatestNodeData implements NodeData {

    private BlockFace facing;

    private Stairs.Shape stairShape;

    private Axis axis;

    private BlockFace rotation;

    private int age;

    private boolean farmland;

    @Override
    public void load(Block block) {
        BlockData data = block.getBlockData();

        log.fine(data.toString());

        if (data instanceof Directional) {
            this.facing = ((Directional) data).getFacing();
        }

        if (data instanceof Stairs) {
            this.stairShape = ((Stairs) data).getShape();
        }

        if (data instanceof Orientable) {
            this.axis = ((Orientable) data).getAxis();
        }

        if (data instanceof Rotatable) {
            this.rotation = ((Rotatable) data).getRotation();
        }

        if (data instanceof Ageable) {
            this.age = ((Ageable) data).getAge();
        }

        // Check for farmland under this block
        BlockData underData = block.getRelative(BlockFace.DOWN).getBlockData();

        if (underData instanceof Farmland) {
            this.farmland = true;
        }

        log.fine("Loaded block data " + this);
        log.fine(block.getType().toString());
    }

    @Override
    public void place(Block block) {
        BlockData blockData = block.getBlockData();

        if (blockData instanceof Directional) {
            ((Directional) blockData).setFacing(this.facing);
        }

        if (blockData instanceof Stairs) {
            ((Stairs) blockData).setShape(this.stairShape);
        }

        if (blockData instanceof Orientable) {
            ((Orientable) blockData).setAxis(this.axis);
        }

        if (blockData instanceof Rotatable) {
            ((Rotatable) blockData).setRotation(this.rotation);
        }

        if (blockData instanceof Ageable) {
            ((Ageable) blockData).setAge(this.age);
        }

        if (farmland) {
            block.getRelative(BlockFace.DOWN).setType(Material.FARMLAND);
        }

        block.setBlockData(blockData);
    }

    @Override
    public String toString() {
        return "LatestNodeData{" +
                "facing=" + facing +
                ", stairShape=" + stairShape +
                ", axis=" + axis +
                ", rotation=" + rotation +
                ", age=" + age +
                '}';
    }
}
