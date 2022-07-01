package nl.aurorion.blockregen.version.ancient;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import lombok.extern.java.Log;
import nl.aurorion.blockregen.version.api.Methods;
import org.bukkit.DyeColor;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.material.Leaves;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;
import org.jetbrains.annotations.NotNull;

@Log
@SuppressWarnings("deprecation")
public class AncientMethods implements Methods {

    @Override
    public void setType(@NotNull Block block, @NotNull XMaterial xMaterial) {
        /* Raw data is set correctly through the #setType() method. */
        XBlock.setType(block, xMaterial);
    }

    // Basically copy XBlock.getType(), but fix the 1.8 "Wood not found" and match only material outside of Colorable & Tree
    @Override
    public XMaterial getType(@NotNull Block block) {
        BlockState state = block.getState();
        MaterialData data = state.getData();

        byte dataValue;

        if (data instanceof Tree) {
            TreeSpecies species = ((Tree) data).getSpecies();
            dataValue = species.getData();
        } else if (data instanceof Leaves) {
            TreeSpecies species = ((Leaves) data).getSpecies();
            dataValue = species.getData();
        } else if (data instanceof Colorable) {
            DyeColor color = ((Colorable) data).getColor();
            dataValue = color.getWoolData();
        } else {
            // Ignore data
            dataValue = data.getData();
            log.fine(String.format("Ignoring material data: %s:%d", state.getType(), dataValue));

            return XMaterial.matchXMaterial(state.getType());
        }

        XMaterial xMaterial = XMaterial.matchXMaterial(String.format("%s:%d", block.getType().toString(), dataValue)).orElse(null);

        log.fine(String.format("Parsed material %s:%d into %s", state.getType(), dataValue, xMaterial));

        return xMaterial;
    }

    @Override
    public ItemStack getItemInMainHand(@NotNull Player player) {
        return player.getInventory().getItemInHand();
    }
}
