package de.derioo.chals.server.api;

import de.derioo.chals.server.api.types.Mod;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Optional;
import java.util.Set;

public interface Api {


  Set<Mod> mods();

  Optional<Mod> getModByName(String name);

}
