package de.derioo.chals.randomizer.listener;

import de.derioo.chals.randomizer.Randomizer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlockBreakListener implements Listener {

    private final Randomizer plugin;

    public BlockBreakListener(Randomizer plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack itemStack : event.getLoot()) {
            this.handleType(itemStack.getType());
            itemStack.setType(plugin.getBlocks().get(itemStack.getType()));
            items.add(itemStack);
        }
        event.setLoot(items);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material item = event.getBlock().getType();
        this.handleType(item);
        event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(plugin.getBlocks().get(item)));
        event.getBlock().setType(Material.AIR);
        event.setDropItems(false);
    }

    private void handleType(Material m) {
        if (!plugin.getBlocks().containsKey(m)) {
            List<Material> values = new ArrayList<>(Arrays.stream(Material.values()).toList());
            Collections.shuffle(values);
            Material material = values.get(values.size() - 1);
            while (!material.isItem()) {
                material = values.get(values.size() - 1);
                Collections.shuffle(values);
            }
            plugin.getBlocks().put(m, material);
        }
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, plugin::saveJsonConfig);
    }
}
