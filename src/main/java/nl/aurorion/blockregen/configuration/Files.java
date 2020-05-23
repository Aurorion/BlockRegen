package nl.aurorion.blockregen.configuration;

import lombok.Getter;
import nl.aurorion.blockregen.BlockRegen;

public class Files {

    @Getter
    private ConfigFile settings;
    @Getter
    private ConfigFile messages;
    @Getter
    private ConfigFile blocklist;
    @Getter
    private ConfigFile regions;
    @Getter
    private ConfigFile data;
    @Getter
    private ConfigFile recovery;

    public Files() {
        load();
    }

    public void load() {
        settings = new ConfigFile(BlockRegen.getInstance(), "Settings.yml");
        messages = new ConfigFile(BlockRegen.getInstance(), "Messages.yml");
        blocklist = new ConfigFile(BlockRegen.getInstance(), "Blocklist.yml");
        regions = new ConfigFile(BlockRegen.getInstance(), "Regions.yml");
        data = new ConfigFile(BlockRegen.getInstance(), "Data.yml");
    }

    public void checkRecovery() {
        if (settings.getFileConfiguration().getBoolean("Data-Recovery"))
            recovery = new ConfigFile(BlockRegen.getInstance(), "Recovery.yml");
    }
}