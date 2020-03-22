package nl.aurorion.blockregen.Configurations;

import lombok.Getter;

public class Files {

    @Getter
    private ConfigFile settings, messages, blocklist, regions, data, recovery;

    public Files() {
        load();
    }

    public void load() {
        settings = new ConfigFile("Settings.yml");
        messages = new ConfigFile("Messages.yml");
        blocklist = new ConfigFile("Blocklist.yml");
        regions = new ConfigFile("Regions.yml");
        data = new ConfigFile("Data.yml");

        if (settings.getFileConfiguration().getBoolean("Data-Recovery"))
            recovery = new ConfigFile("Recovery.yml");
    }
}