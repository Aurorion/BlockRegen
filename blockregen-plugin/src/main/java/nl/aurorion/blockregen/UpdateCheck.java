package nl.aurorion.blockregen;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UpdateCheck {

    private URL checkURL;
    @Getter
    private String latestVersion;
    private final JavaPlugin plugin;

    public UpdateCheck(JavaPlugin plugin, int projectID) {
        this.plugin = plugin;
        this.latestVersion = plugin.getDescription().getVersion();
        try {
            this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectID);
        } catch (MalformedURLException ignored) {
        }
    }

    public boolean checkForUpdates() throws Exception {
        URLConnection con = checkURL.openConnection();

        latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
        int newVersionValue = Integer.parseInt(latestVersion.replace(".", ""));
        int currentVersion = Integer.parseInt(plugin.getDescription().getVersion().split("-")[0].replace(".", ""));

        return newVersionValue > currentVersion;
    }
}