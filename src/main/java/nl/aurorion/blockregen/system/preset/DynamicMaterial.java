package nl.aurorion.blockregen.system.preset;

import com.google.common.base.Strings;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DynamicMaterial {

    private boolean fixed = false;

    private final List<Material> valuedMaterialsCache = new ArrayList<>();

    private Material defaultMaterial;

    public DynamicMaterial(String input) {
        if (Strings.isNullOrEmpty(input)) throw new IllegalArgumentException("Input string cannot be null");

        input = input.replace(" ", "").trim().toUpperCase();

        List<String> materials;

        if (input.contains(";")) {
            materials = new ArrayList<>(new ArrayList<>(Arrays.asList(input.split(";"))));
        } else {
            defaultMaterial = Utils.parseMaterial(input);

            if (defaultMaterial == null)
                throw new IllegalArgumentException("Default material cannot be null");

            fixed = true;
            return;
        }

        if (materials.isEmpty()) throw new IllegalArgumentException("Dynamic material doesn't have the correct syntax");
        else if (materials.size() == 1) {
            defaultMaterial = Utils.parseMaterial(materials.get(0));

            if (defaultMaterial == null)
                throw new IllegalArgumentException("Default material cannot be null");

            fixed = true;
            return;
        }

        int total = 0;

        for (String material : materials) {

            if (!material.contains(":")) {
                defaultMaterial = Utils.parseMaterial(material);

                if (defaultMaterial == null)
                    throw new IllegalArgumentException("Default material cannot be null");

                continue;
            }

            int chance = Integer.parseInt(material.split(":")[1]);
            total += chance;

            for (int i = 0; i < chance; i++) {
                Material mat = Utils.parseMaterial(material.split(":")[0]);

                if (mat == null) {
                    BlockRegen.getInstance().getConsoleOutput().debug("Invalid material " + material.split(":")[0] + " skipped");
                    continue;
                }

                valuedMaterialsCache.add(mat);
            }
        }

        if (defaultMaterial != null) {
            for (int i = 0; i < (100 - total); i++) valuedMaterialsCache.add(defaultMaterial);
        }
    }

    @NotNull
    public Material get() {
        if (fixed) return defaultMaterial;
        Material pickedMaterial = valuedMaterialsCache.get(BlockRegen.getInstance().getRandom().nextInt(valuedMaterialsCache.size()));
        return pickedMaterial != null ? pickedMaterial : defaultMaterial;
    }
}
