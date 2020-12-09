package nl.aurorion.blockregen.system.region.struct;

import lombok.Getter;
import lombok.Setter;
import nl.aurorion.blockregen.util.Utils;
import org.bukkit.Location;

public class RawRegion {

    @Getter
    private final String name;
    @Getter
    private final String min;
    @Getter
    private final String max;

    @Getter
    @Setter
    private boolean reattempt = false;

    public RawRegion(String name, String min, String max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    public RegenerationRegion build() {
        Location min = Utils.stringToLocation(this.min);
        Location max = Utils.stringToLocation(this.max);

        if (min == null || max == null)
            return null;

        return new RegenerationRegion(name, min, max);
    }
}
