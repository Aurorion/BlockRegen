package nl.aurorion.blockregen.configuration;

import nl.aurorion.blockregen.BlockRegen;
import lombok.Getter;

public class Files {

    @Getter
    private ConfigFile settings;
    @Getter
    private ConfigFile messages;
    @Getter
    private ConfigFile blockList;
    @Getter
    private ConfigFile regions;

    public Files(BlockRegen plugin) {
        this.settings = new ConfigFile(plugin, "Settings.yml");
        this.messages = new ConfigFile(plugin, "Messages.yml");
        this.blockList = new ConfigFile(plugin, "Blocklist.yml");
        this.regions = new ConfigFile(plugin, "Regions.yml");
    }

    public void load() {
        this.settings.load();
        this.messages.load();
        this.blockList.load();
        this.regions.load();
    }
}