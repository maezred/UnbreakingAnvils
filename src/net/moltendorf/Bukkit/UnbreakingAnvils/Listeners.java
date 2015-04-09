package net.moltendorf.Bukkit.UnbreakingAnvils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener class.
 *
 * @author moltendorf
 */
public class Listeners implements Listener {
	protected Map<UUID, Location> anvils = new HashMap<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void PlayerInteractEventMonitor(final PlayerInteractEvent event) {
		final Block block = event.getClickedBlock();

		if (block.getType() == Material.ANVIL) {
			anvils.put(event.getPlayer().getUniqueId(), block.getLocation());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void InventoryCloseEventMonitor(final InventoryCloseEvent event) {
		final Inventory inventory = event.getInventory();

		if (inventory.getType() == InventoryType.ANVIL) {
			anvils.remove(event.getPlayer().getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void InventoryClickEventMonitor(final InventoryClickEvent event) {
		final Inventory inventory = event.getInventory();

		// Is it an anvil?
		if (inventory.getType() == InventoryType.ANVIL && event.getSlotType() == InventoryType.SlotType.RESULT) {
			final Location location = anvils.get(event.getWhoClicked().getUniqueId()); // Using hacks to get the anvil that is in use.

			if (location == null) {
				// The player probably had an anvil open before the plugin was loaded. Can't do anything about it now.
				return;
			}

			final Block block = location.getBlock();
			final BlockState blockState = block.getState();

			if (blockState.getType() != Material.ANVIL) {
				// Ghost anvil.
				return;
			}

			MaterialData materialData = blockState.getData();
			byte data = materialData.getData(); // No anvil class.

			if (data > 2) {
				// Fix it now.
				materialData.setData((byte)2);
				block.setType(materialData.getItemType());
			} else {
				// Check after the event to see if it took damage.
				Plugin.instance.getServer().getScheduler().runTask(Plugin.instance, () -> {
					final BlockState newBlockState = block.getState();

					if (newBlockState.getType() == Material.ANVIL) {
						MaterialData newMaterialData = newBlockState.getData();

						if (newMaterialData.getData() > 2) {
							newMaterialData.setData((byte)2);
							block.setType(newMaterialData.getItemType());
						}
					}
				});
			}
		}
	}


}
