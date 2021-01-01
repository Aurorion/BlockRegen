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
    public int parseInteger(String input, int def) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException exception) {
            return def;
        }
    }

    public int parseInteger(String input) {
        return parseInteger(input, -1);
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

    public <E extends Enum<E>> E parseEnum(String str, Class<E> clazz) {
        return parseEnum(str, clazz, null);
    }

    public <E extends Enum<E>> E parseEnum(String str, Class<E> clazz, Consumer<Throwable> exceptionCallback) {

        if (Strings.isNullOrEmpty(str))
            return null;

        try {
            return E.valueOf(clazz, str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            if (exceptionCallback != null)
                exceptionCallback.accept(e);
            return null;
        }
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
