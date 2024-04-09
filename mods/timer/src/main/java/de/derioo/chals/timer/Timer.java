package de.derioo.chals.timer;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import de.derioo.chals.server.api.Config;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.players;

@DefaultQualifier(NonNull.class)
public final class Timer extends JavaPlugin implements Listener {

  private boolean running = false;
  private long timer;

  private Config config;


  @Override
  public void onEnable() {
    this.getServer().getPluginManager().registerEvents(this, this);

    config = new Config(this, "timer");
    timer = config.get().get("timer").getAsLong();

    this.registerTimerCommand();
    this.runTimerTask();
  }

  private void runTimerTask() {
    Bukkit.getScheduler().runTaskTimer(this, () -> {
      if (running) timer++;
      Bukkit.getServer().sendActionBar(MiniMessage.miniMessage().deserialize("<gradient:#d450b8:#737ddb>" + (timer != 0 ?
        TimerConverter.convert(timer, TimeUnit.SECONDS) :
        "Idle")));
    }, 0, 20);
    Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
      config.save();
    }, 20 * 20, 20 * 20);
  }

  private void registerTimerCommand() {
    this.registerPluginBrigadierCommand(
      "timer",
      literal -> literal.requires(stack -> stack.getBukkitSender().hasPermission("sc.timer"))
        .then(literal("reset")
          .executes(ctx -> {
            timer = 0L;
            running = false;
            ctx.getSource().getBukkitSender().sendMessage(text("Timer wird geresetet"));
            return Command.SINGLE_SUCCESS;
          }))
        .then(literal("stop")
          .executes(ctx -> {
            running = false;
            ctx.getSource().getBukkitSender().sendMessage(text("Timer wird gestoppt"));
            return Command.SINGLE_SUCCESS;
          }))
        .then(literal("start").executes((ctx -> {
          running = true;
          ctx.getSource().getBukkitSender().sendMessage(text("Timer wird gestarted"));
          return Command.SINGLE_SUCCESS;
        })))
    );
  }

  @Override
  public void onDisable() {
    config.get().addProperty("timer", timer);
    config.save();
  }

  private PluginBrigadierCommand registerPluginBrigadierCommand(final String label, final Consumer<LiteralArgumentBuilder<CommandSourceStack>> command) {
    final PluginBrigadierCommand pluginBrigadierCommand = new PluginBrigadierCommand(this, label, command);
    this.getServer().getCommandMap().register(this.getName(), pluginBrigadierCommand);
    ((CraftServer) this.getServer()).syncCommands();
    return pluginBrigadierCommand;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @EventHandler
  public void onCommandRegistered(final CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
    if (!(event.getCommand() instanceof PluginBrigadierCommand pluginBrigadierCommand)) {
      return;
    }
    final LiteralArgumentBuilder<CommandSourceStack> node = literal(event.getCommandLabel());
    pluginBrigadierCommand.command().accept(node);
    event.setLiteral((LiteralCommandNode) node.build());
  }
}
