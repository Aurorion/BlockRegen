package nl.aurorion.blockregen.version.ancient;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;

import nl.aurorion.blockregen.BlockUtil;
import nl.aurorion.blockregen.version.api.Methods;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;
import org.bukkit.material.Wool;
import org.jetbrains.annotations.NotNull;

import lombok.extern.java.Log;

@Log
@SuppressWarnings("deprecation")
public class AncientMethods implements Methods {

    @Override
    public void setType(@NotNull Block block, @NotNull XMaterial xMaterial) {
        /* Raw data is set correctly through the #setType() method. */
        XBlock.setType(block, xMaterial);
    }

    @Override
    public boolean compareType(@NotNull Block block, @NotNull XMaterial xMaterial) {
        /*
         * Avoid using XBlock isSimilar, isType methods. They don't compare raw block
         * data on lower
         * versions.
         * 
         * return XBlock.isSimilar(block, xMaterial);
         */

        Material material = xMaterial.parseMaterial();

        if (material == null) {
            log.severe(String.format("Material %s not supported on this version.", xMaterial.name()));
            return false;
        }

        byte blockData = block.getData();

        log.fine(String.format("Block: %s:%d, Material: %s:%d", block.getType().name(), blockData, xMaterial.name(),
                xMaterial.getData()));

        if (block.getType() != material) {
            log.fine("Block type doesn't match.");
            return false;
        }

        BlockState state = block.getState();
        MaterialData materialData = state.getData();

        // We compare these by hand because byte data contains direction as well.

        // Compare colors

        // The only block that can be colored on 1.8 should be wool.
        if (BlockUtil.isWool(xMaterial)) {
            // The blocks are the same type, but just in case.
            if (!(materialData instanceof Wool)) {
                return false;
            }

            return xMaterial.getData() == block.getData();
        }

        // Compare wood types

        // Wood interface is named Tree on 1.8
        // Check by material

        if (BlockUtil.isWood(xMaterial)) {
            // The blocks are the same type, but just in case.
            if (!(materialData instanceof Tree)) {
                return false;
            }

            TreeSpecies species = ((Tree) materialData).getSpecies();
            return xMaterial.getData() == species.getData();
        }

        return true;
    }

    @Override
    public ItemStack getItemInMainHand(@NotNull Player player) {
        return player.getInventory().getItemInHand();
    }
}
