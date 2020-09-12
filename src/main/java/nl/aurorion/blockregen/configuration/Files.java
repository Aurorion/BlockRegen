package nl.aurorion.blockregen.configuration;

import lombok.Getter;
import nl.aurorion.blockregen.BlockRegen;

public class Files {

    private final BlockRegen plugin;

    @Getter
    private ConfigFile settings;
    @Getter
    private ConfigFile messages;
    @Getter
    private ConfigFile blockList;
    @Getter
    private ConfigFile regions;

    public Files(BlockRegen plugin) {
        load();
        this.plugin = plugin;
    }

    public void load() {
        settings = new ConfigFile(plugin, "Settings.yml");
        messages = new ConfigFile(plugin, "Messages.yml");
        blockList = new ConfigFile(plugin, "Blocklist.yml");
        regions = new ConfigFile(plugin, "Regions.yml");
    }
}