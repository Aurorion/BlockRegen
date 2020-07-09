package nl.aurorion.blockregen.configuration;

import lombok.Getter;
import nl.aurorion.blockregen.BlockRegen;

public class Files {

    @Getter
    private ConfigFile settings;
    @Getter
    private ConfigFile messages;
    @Getter
    private ConfigFile blockList;
    @Getter
    private ConfigFile regions;

    public Files() {
        load();
    }

    public void load() {
        settings = new ConfigFile(BlockRegen.getInstance(), "Settings.yml");
        messages = new ConfigFile(BlockRegen.getInstance(), "Messages.yml");
        blockList = new ConfigFile(BlockRegen.getInstance(), "Blocklist.yml");
        regions = new ConfigFile(BlockRegen.getInstance(), "Regions.yml");
    }
}