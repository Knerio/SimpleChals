package de.derioo.chals.server.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import de.derioo.chals.server.api.types.Mod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ChalsAPI implements Api {

  private Set<Mod> cachedMods;

  public ChalsAPI() {
    try {
      cachedMods = getMods();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Set<Mod> getMods() throws IOException {
    List<String> modNames = new ArrayList<>();

    URL url = new URL("http://127.0.0.1:3000/mods");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();


      JsonArray jsonArray = JsonParser.parseString(response.toString()).getAsJsonArray();
      for (int i = 0; i < jsonArray.size(); i++) {
        modNames.add(jsonArray.get(i).getAsString());
      }
    } else {
      throw new IOException("Failed to fetch data from API. Response code: " + responseCode);
    }

    connection.disconnect();

    return modNames.stream().map(Mod::new).collect(Collectors.toSet());
  }

  @Override
  public Set<Mod> mods() {
    return cachedMods;
  }

  @Override
  public Optional<Mod> getModByName(String name) {
    return mods().stream().filter(mod -> name.equalsIgnoreCase(mod.getName())).findFirst();
  }
}
