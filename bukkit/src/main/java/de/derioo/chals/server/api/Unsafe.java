package de.derioo.chals.server.api;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Unsafe {

  @Getter
  private Api api;

  public void setApi(Api newApi) {
    Preconditions.checkNotNull(newApi);
    if (api != null) throw new IllegalStateException("Cannot set api twice");
    api = newApi;
  }

}
