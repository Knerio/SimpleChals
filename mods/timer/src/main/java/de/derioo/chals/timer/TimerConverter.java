package de.derioo.chals.timer;


import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class TimerConverter {
  public String convert(long time, TimeUnit unit) {
    final var stringBuilder = new StringBuilder();
    final var seconds = TimeUnit.SECONDS.convert(time, unit);

    long remainingSeconds = seconds % 60;
    long remainingMinutes = (seconds / 60) % 60;
    long remainingHours = (seconds / 3600);

    if (remainingHours > 0) stringBuilder.append(remainingHours).append(getTimeSuffix(TimeUnit.HOURS)).append(" ");
    if (remainingMinutes > 0 && remainingHours >= 0) stringBuilder.append(remainingMinutes).append(getTimeSuffix(TimeUnit.MINUTES)).append(" ");
    return stringBuilder.append(remainingSeconds).append(getTimeSuffix(TimeUnit.SECONDS)).toString();
  }

  private String getTimeSuffix(TimeUnit suffix) {
    return switch (suffix) {
      case HOURS -> "h";
      case MINUTES -> "m";
      case SECONDS -> "s";
      default -> throw new IllegalArgumentException(suffix + " is not supported");
    };
  }
}
