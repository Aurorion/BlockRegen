package nl.aurorion.blockregen.system.preset;

import org.bukkit.Material;

import java.util.List;

public class ItemDrop {

    private Material material;

    private Amount amount;

    private String displayName;

    private boolean dropNaturally;

    private List<String> lore;

    // Nullable
    private ExperienceDrop experienceDrop;
}
