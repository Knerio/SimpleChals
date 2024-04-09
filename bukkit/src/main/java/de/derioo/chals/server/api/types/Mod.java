package de.derioo.chals.server.api.types;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystemException;

@Getter
public class Mod {

  private final String name;
  private Plugin plugin;

  public Mod(String name) {
    this.name = name;
      try {
          load();
      } catch (IOException | InvalidPluginException | InvalidDescriptionException e) {
          throw new RuntimeException(e);
      }
  }

  private void load() throws IOException, InvalidPluginException, InvalidDescriptionException {
    if (isDownloaded()) new File(getDownloadDir(), name + ".jar").delete();
    download();

    File downloadDir = getDownloadDir();
    File jar = new File(downloadDir, name + ".jar");

    try {
      File dest = new File("./plugins", this.name + ".jar");
      if (dest.exists()) dest.delete();
      FileUtils.copyFile(jar, dest);
    } catch (FileSystemException e) {
      e.printStackTrace();
    }

    plugin = Bukkit.getPluginManager().loadPlugin(getPluginFile());
    Bukkit.getPluginManager().disablePlugin(plugin);
  }

  public void enable() {
    Bukkit.getPluginManager().enablePlugin(plugin);
  }

  @NotNull
  private File getPluginFile() {
    return new File("./plugins", name + ".jar");
  }

  public void unload() {
    if (!Bukkit.getPluginManager().isPluginEnabled(plugin)) return;
    Bukkit.getPluginManager().disablePlugin(plugin);
  }

  public void delete() {
    unload();
    getPluginFile().delete();
  }

  @NotNull
  private static File getDownloadDir() {
    File downloadDir = new File("./SimpleChals/downloads");
    downloadDir.mkdirs();
    return downloadDir;
  }

  public boolean isDownloaded() {
    File downloadDir = getDownloadDir();
    return new File(downloadDir, name + ".jar").exists();
  }

  public void download() {
    File downloadDir = getDownloadDir();
    try {
      String serverUrl = "http://127.0.0.1:3000/latest/" + this.name;

      HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl).openConnection();
      connection.connect();

      try (InputStream inputStream = connection.getInputStream()) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(downloadDir.toPath() + "/" + this.name + ".jar")) {

          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
          }

          fileOutputStream.close();
          inputStream.close();
          connection.disconnect();
        }
      }



      System.out.println("Jar file downloaded successfully");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
