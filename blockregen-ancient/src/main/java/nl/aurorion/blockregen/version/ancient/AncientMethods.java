package nl.aurorion.blockregen.version.ancient;

import com.cryptomorin.xseries.XMaterial;
import nl.aurorion.blockregen.ConsoleOutput;
import nl.aurorion.blockregen.version.api.Methods;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AncientMethods implements Methods {

    @Override
    public void setType(@NotNull Block block, @NotNull XMaterial xMaterial) {
        Material type = xMaterial.parseMaterial();
        byte data = xMaterial.getData();

        if (type == null) {
            ConsoleOutput.getInstance().warn("Type " + xMaterial.name() + " is not supported on this version.");
            return;
        }

        block.setType(type);
        block.setData(data);
    }

    @Override
    public boolean compareType(@NotNull Block block, @NotNull XMaterial xMaterial) {
        Material type = xMaterial.parseMaterial();
        if (type == null) {
            ConsoleOutput.getInstance().warn("Type " + xMaterial.name() + " is not supported on this version.");
            return false;
        }
        byte data = xMaterial.getData();
        return block.getType() == type && block.getData() == data;
    }

    @Override
    public ItemStack getItemInMainHand(@NotNull Player player) {
        return player.getInventory().getItemInHand();
    }
}
