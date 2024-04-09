package de.derioo.chals.server.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Config {

  private final JsonObject content;
  private final Plugin plugin;
  private final String name;

  public Config(Plugin plugin, String name) {
    this.plugin = plugin;
    this.name = name;
    new File("./plugins/sc/" + name).mkdirs();
    File configFile = new File(new File("./plugins/sc/" + name), "config.json");
    try {
      if (configFile.createNewFile()) {
        content = getDefault();
      } else {
        try (FileReader fileReader = new FileReader(configFile);) {
          content = JsonParser.parseString(IOUtils.toString(fileReader)).getAsJsonObject();
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    save();
  }

  public JsonObject get() {
    return content;
  }

  private JsonObject getDefault() throws IOException {
    InputStream resource = plugin.getResource("config.json");
    return JsonParser.parseString(IOUtils.toString(resource, StandardCharsets.UTF_8)).getAsJsonObject();
  }

  public void save() {
    File dir = new File("./plugins/sc/" + name);
    dir.mkdirs();
    File configFile = new File(dir, "config.json");
      try {
          configFile.createNewFile();
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
      writer.write(content.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


}
