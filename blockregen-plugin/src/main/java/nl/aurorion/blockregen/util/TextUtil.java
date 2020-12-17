package nl.aurorion.blockregen.util;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import org.bukkit.entity.Player;

@UtilityClass
public class TextUtil {

    public String parse(String string) {

        if (Strings.isNullOrEmpty(string))
            return string;

        string = string.replaceAll("(?i)%prefix%", Message.PREFIX.getValue());
        return string;
    }

    public String parse(String string, Player player) {
        string = parse(string);

        if (Strings.isNullOrEmpty(string)) return string;

        string = string.replaceAll("(?i)%player%", player.getName());
        if (BlockRegen.getInstance().isUsePlaceholderAPI())
            string = PlaceholderAPI.setPlaceholders(player, string);

        return string;
    }
}
