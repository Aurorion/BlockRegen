package nl.aurorion.blockregen;

import com.cryptomorin.xseries.XMaterial;

import org.jetbrains.annotations.NotNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BlockUtil {
    public boolean isWood(@NotNull XMaterial xMaterial) {
        for (int i = 0; i < xMaterial.getLegacy().length; i++) {
            if (xMaterial.getLegacy()[i] == "LOG" || xMaterial.getLegacy()[i] == "LOG_2") {
                return true;
            }
        }
        return false;
    }

    public boolean isWool(@NotNull XMaterial xMaterial) {
        for (int i = 0; i < xMaterial.getLegacy().length; i++) {
            if (xMaterial.getLegacy()[i] == "WOOL") {
                return true;
            }
        }
        return false;
    }
}
