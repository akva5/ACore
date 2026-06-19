package akv5.acore.libs.hooks;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import net.Zrips.CMILib.Container.CMILocation;
import org.bukkit.Location;
import org.bukkit.Bukkit;

import java.util.List;

public class CMIHologramHook {

    private static boolean enabled = false;

    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("CMI") != null) {
            enabled = true;
        }
    }

    public static void createHologram(String id, Location loc, List<String> lines) {
        if (!enabled) return;

        try {
            removeHologram(id);

            CMILocation cmiLoc = new CMILocation(loc);
            CMIHologram hologram = new CMIHologram(id, cmiLoc);

            hologram.setLines(lines);

            CMI.getInstance().getHologramManager().addHologram(hologram);
            hologram.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeHologram(String id) {
        if (!enabled) return;
        try {
            CMIHologram holo = CMI.getInstance().getHologramManager().getByName(id);
            if (holo != null) {
                holo.remove();
            }
        } catch (Exception e) {
            //
        }
    }

    public static void updateHologramLines(String id, List<String> lines) {
        if (!enabled) return;
        try {
            CMIHologram holo = CMI.getInstance().getHologramManager().getByName(id);
            if (holo != null) {
                holo.setLines(lines);
                holo.update();
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
}