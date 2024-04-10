package de.derioo.chals.ampel;

import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Ampel extends JavaPlugin implements Listener {

  @Getter
  private AmpelObject ampel;


  @Override
  public void onEnable() {
    this.getServer().getPluginManager().registerEvents(new MoveListener(this), this);

    ampel = new AmpelObject();
  }

  @Override
  public void onDisable() {
    ampel.disable();
  }

  public class AmpelObject {
    @Getter
    private Color color;
    private final Random random = new Random();
    private final BossBar bossBar;

    private long nextYellowSwitch;
    private long lastSwitch = System.currentTimeMillis();

    public AmpelObject() {
      color = Color.GREEN;
      nextYellowSwitch = TimeUnit.MINUTES.toMillis(random.nextInt(3) + 1);
      bossBar = BossBar.bossBar(MiniMessage.miniMessage().deserialize(""), 1, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);

      Bukkit.getScheduler().runTaskTimer(getPlugin(Ampel.class), () -> {
        bossBar.addViewer(Bukkit.getServer());
        updateBossbar();
        if (color != Color.GREEN) return;
        if (lastSwitch + nextYellowSwitch < System.currentTimeMillis()) {
          color = Color.YELLOW;
          updateBossbar();
          Bukkit.getServer().playSound(Sound.sound().type(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING).build());
          Bukkit.getScheduler().runTaskLater(getPlugin(Ampel.class), () -> {
            Bukkit.getServer().playSound(Sound.sound().type(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING).build());
            color = Color.RED;
            updateBossbar();
            Bukkit.getScheduler().runTaskLater(getPlugin(Ampel.class), () -> {
              Bukkit.getServer().playSound(Sound.sound().type(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING).build());
              color = Color.GREEN;
              updateBossbar();
              nextYellowSwitch = TimeUnit.MINUTES.toMillis(random.nextInt(3) + 5);
              lastSwitch = System.currentTimeMillis();
            }, random.nextInt(70) + 5 * 20);
          }, random.nextInt(20) + 30);
        }

      }, 0, 5);
    }

    private void updateBossbar() {
      bossBar.color(switch (color) {
        case RED -> BossBar.Color.RED;
        case GREEN -> BossBar.Color.GREEN;
        case YELLOW -> BossBar.Color.YELLOW;
      });
      bossBar.name(MiniMessage.miniMessage().deserialize("<"+color.name().toLowerCase()+">" + "◼".repeat(30)));
    }

    public void disable() {
      bossBar.removeViewer(Bukkit.getServer());
    }

    public enum Color {
      GREEN,
      YELLOW,
      RED
    }
  }

}
