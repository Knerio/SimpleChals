package de.derioo.chals.randomizer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.derioo.chals.randomizer.listener.BlockBreakListener;
import de.derioo.chals.server.api.Config;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Randomizer extends JavaPlugin implements Listener {

    @Getter
    private final Map<Material, Material> blocks = new HashMap<>();

    private Config config;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        config = new Config(this, "randomizer");
        for (Map.Entry<String, JsonElement> entry : config.get().get("blocks").getAsJsonObject().entrySet()) {
            blocks.put(Material.valueOf(entry.getKey()), Material.valueOf(entry.getValue().getAsString()));
        }

        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);
    }



    @Override
    public void onDisable() {
        saveJsonConfig();
    }


    public void saveJsonConfig() {
        for (Map.Entry<Material, Material> entry : blocks.entrySet()) {
            config.get().get("blocks").getAsJsonObject().addProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        config.save();
    }
}
