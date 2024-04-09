package de.derioo.chals.server;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.derioo.chals.server.api.ChalsAPI;
import de.derioo.chals.server.api.Config;
import de.derioo.chals.server.api.Unsafe;
import de.derioo.chals.server.api.types.Mod;
import de.derioo.inventoryframework.interfaces.InventoryContents;
import de.derioo.inventoryframework.interfaces.InventoryProvider;
import de.derioo.inventoryframework.objects.InventoryFramework;
import de.derioo.inventoryframework.objects.SmartItem;
import de.derioo.inventoryframework.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.commands.CommandSourceStack;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@DefaultQualifier(NonNull.class)
public final class ServerCore extends JavaPlugin implements Listener {
  Config global;

  @Override
  public void onLoad() {
    global = new Config(this, "global");
    if (global.get().get("shouldreset").getAsBoolean()) {
      this.resetWorld();
    }
  }

  @Override
  public void onEnable() {
    this.getServer().getPluginManager().registerEvents(this, this);


    Unsafe.setApi(new ChalsAPI());
    Unsafe.getApi().getModByName("timer").get().enable();

    new InventoryFramework(this);

    registerModCommand();
    registerWorldResetCommand();
  }

  private void registerWorldResetCommand() {
    registerPluginBrigadierCommand("worldreset", builder -> {
      builder.requires(stack -> stack.getBukkitSender().hasPermission("world.reset"))
        .executes(ctx -> {
          ctx.getSource().getBukkitSender().sendMessage(Component.text("Resetting world"));
          global.get().addProperty("shouldreset", true);
          global.save();
          Bukkit.shutdown();
          return Command.SINGLE_SUCCESS;
        });
    });
  }


  private void registerModCommand() {
    registerPluginBrigadierCommand("mod", (builder -> {
      RequiredArgumentBuilder<CommandSourceStack, String> modNameBuilder = argument("modName", StringArgumentType.word()).suggests((ctx, suggestionsBuilder) -> {
        for (Mod cachedMod : Unsafe.getApi().mods()) {
          suggestionsBuilder.suggest(cachedMod.getName());
        }
        return suggestionsBuilder.buildFuture();
      });
      builder.
        requires(stack -> stack.getBukkitSender().hasPermission("sc.manage"))
        .executes((ctx) -> {
          ChestGui gui = new ChestGui(6, "Mods");
          PaginatedPane paginatedPage = new PaginatedPane(0, 0, 9, 5);
          populate(paginatedPage);
          gui.addPane(paginatedPage);

          OutlinePane background = new OutlinePane(0, 5, 9, 1);
          background.addItem(new GuiItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)));
          background.setRepeat(true);
          background.setPriority(Pane.Priority.LOWEST);

          gui.addPane(background);

          StaticPane navigation = new StaticPane(0, 5, 9, 1);
          navigation.addItem(new GuiItem(new ItemBuilder(Material.ARROW).name(MiniMessage.miniMessage().deserialize("<!i>Vorherige Seite")).build(), event -> {
            if (paginatedPage.getPage() > 0) {
              paginatedPage.setPage(paginatedPage.getPage() - 1);

              gui.update();
            }
          }), 0, 0);

          navigation.addItem(new GuiItem(new ItemBuilder(Material.ARROW).name(MiniMessage.miniMessage().deserialize("<!i>Nächste Seite")).build(), event -> {
            if (paginatedPage.getPage() < paginatedPage.getPages() - 1) {
              paginatedPage.setPage(paginatedPage.getPage() + 1);

              gui.update();
            }
          }), 8, 0);
          gui.addPane(navigation);

          gui.setOnGlobalClick(inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
          });

          gui.show((HumanEntity) ctx.getSource().getBukkitSender());

          return Command.SINGLE_SUCCESS;
        })
        .then(modNameBuilder.then(literal("stop").executes(ctx -> {
          for (Mod mod : Unsafe.getApi().mods()) {
            if (!mod.getName().equalsIgnoreCase(ctx.getArgument("modName", String.class))) continue;
            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(getClass()), () -> {
              mod.delete();

              ctx.getSource().getBukkitSender().sendMessage(Component.text("Unloaded mod"));
            });
          }


          return Command.SINGLE_SUCCESS;
        })).then(literal("start").executes(commandContext -> {
          for (Mod mod : Unsafe.getApi().mods()) {
            if (!mod.getName().equalsIgnoreCase(commandContext.getArgument("modName", String.class))) continue;
            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(getClass()), () -> {
              mod.enable();

              commandContext.getSource().getBukkitSender().sendMessage(Component.text("Loaded and started mod!"));
            });
          }
          return Command.SINGLE_SUCCESS;
        })));
    }));
  }

  private void populate(PaginatedPane pane) {
    pane.populateWithGuiItems(
      Unsafe.getApi().mods().stream()
        .map(mod -> new GuiItem(
          new ItemBuilder(Material.CLOCK)
            .name(MiniMessage.miniMessage().deserialize("<!i><white>" + mod.getName()))
            .addLore(
              MiniMessage.miniMessage().deserialize("<!i><gray>" + mod.getPlugin().getPluginMeta().getDescription()),
              MiniMessage.miniMessage().deserialize("<!i><gray>Die Mod ist aktuell " + (mod.getPlugin().isEnabled() ? "<green>aktiv" : "<red>nicht aktiv")))
            .build(),
          inventoryClickEvent -> {
            if (mod.getPlugin().isEnabled()) {
              mod.unload();
            } else {
              mod.enable();
            }
            Bukkit.dispatchCommand(inventoryClickEvent.getWhoClicked(), "mod");
          }
        )).toList()
    );
  }

  @Override
  public void onDisable() {
    global.save();
    Unsafe.getApi().mods().forEach(Mod::delete);
  }

  private void resetWorld() {
    try {
      File world = new File(Bukkit.getWorldContainer(), "world");
      File nether = new File(Bukkit.getWorldContainer(), "world_nether");
      File end = new File(Bukkit.getWorldContainer(), "world_the_end");
      File[] files = new File[]{world, nether, end};
      for (File worldDir : files) {
        FileUtils.deleteDirectory(worldDir);
        worldDir.mkdirs();
        (new File(worldDir, "data")).mkdirs();
        (new File(worldDir, "datapacks")).mkdirs();
        (new File(worldDir, "playerdata")).mkdirs();
        (new File(worldDir, "poi")).mkdirs();
        (new File(worldDir, "region")).mkdirs();
      }
      FileUtils.deleteDirectory(new File("plugins/sc"));
      global.get().addProperty("shouldreset", false);
      global.save();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void registerPluginBrigadierCommand(final String label, final Consumer<LiteralArgumentBuilder<CommandSourceStack>> command) {
    final PluginBrigadierCommand pluginBrigadierCommand = new PluginBrigadierCommand(this, label, command);
    this.getServer().getCommandMap().register(this.getName(), pluginBrigadierCommand);
    ((CraftServer) this.getServer()).syncCommands();
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
