package nl.aurorion.blockregen.Configurations;

import lombok.Getter;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile {

    @Getter
    private final String path;

    @Getter
    private FileConfiguration fileConfiguration;

    @Getter
    private final File file;

    public ConfigFile(String path) {
        this.path = path.contains(".yml") ? path : path + ".yml";
        this.file = new File(path);

        load();
    }

    public void load() {
        if (!file.exists()) {
            BlockRegen.getInstance().saveResource(this.path, false);
            BlockRegen.getInstance().getServer().getConsoleSender().sendMessage(Utils.color("&6[&3BlockRegen&6] &aCreated " + this.path));
        }

        BlockRegen.getInstance().getServer().getConsoleSender().sendMessage(Utils.color("&6[&3BlockRegen&6] &7Loaded " + this.path));

        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            BlockRegen.getInstance().getServer().getConsoleSender().sendMessage(Utils.color("&6[&3BlockRegen&6] &cCould not save " + this.path));
        }
    }
}
