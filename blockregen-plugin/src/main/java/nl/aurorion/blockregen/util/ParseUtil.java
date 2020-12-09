package nl.aurorion.blockregen.util;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.ConsoleOutput;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@UtilityClass
public class ParseUtil {

    /**
     * Attempt to parse an integer, return -1 if a NumberFormatException was thrown.
     */
    public int parseInteger(String input) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    @Nullable
    public XEnchantment parseEnchantment(String input) {
        if (Strings.isNullOrEmpty(input))
            return null;

        Optional<XEnchantment> xEnchantment = XEnchantment.matchXEnchantment(input);
        if (!xEnchantment.isPresent()) {
            ConsoleOutput.getInstance().warn("Could not parse enchantment " + input);
            return null;
        }

        Enchantment enchantment = xEnchantment.get().parseEnchantment();
        if (enchantment == null) {
            ConsoleOutput.getInstance().warn("Could not parse enchantment " + input);
            return null;
        }

        return xEnchantment.get();
    }

    @Nullable
    public XMaterial parseMaterial(String input, boolean... blocksOnly) {

        if (Strings.isNullOrEmpty(input))
            return null;

        Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(input);

        if (!xMaterial.isPresent()) {
            ConsoleOutput.getInstance().debug("Could not parse material " + input);
            return null;
        }

        Material material = xMaterial.get().parseMaterial();

        if (material != null && blocksOnly.length > 0 && blocksOnly[0] && !material.isBlock()) {
            BlockRegen.getInstance().getConsoleOutput().debug("Material " + input + " is not a block.");
            return null;
        }

        return xMaterial.get();
    }

    public <T> T nullOrDefault(Supplier<T> supplier, T def, Consumer<Throwable> exceptionCallback) {
        try {
            T t = supplier.get();
            return t == null ? def : t;
        } catch (Exception e) {
            exceptionCallback.accept(e);
            return def;
        }
    }

    public <T> T nullOrDefault(Supplier<T> supplier, T def) {
        try {
            T t = supplier.get();
            return t == null ? def : t;
        } catch (Exception e) {
            return def;
        }
    }
}
