package nl.aurorion.blockregen.version.ancient;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import lombok.extern.java.Log;
import nl.aurorion.blockregen.version.api.Methods;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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

        Material type = xMaterial.parseMaterial();

        if (type == null) {
            log.warning("Type " + xMaterial.name() + " is not supported on this version.");
            return false;
        }

        byte data = xMaterial.getData();
        boolean result = XMaterial.matchXMaterial(block.getType()) == xMaterial && block.getData() == data;
        log.fine(String.format("Compared %s and (%s, %d), result: %b", xMaterial, block.getType().toString(),
                (int) block.getData(), result));
        return result;
    }

    @Override
    public ItemStack getItemInMainHand(@NotNull Player player) {
        return player.getInventory().getItemInHand();
    }
}
