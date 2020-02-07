package coffee.weneed.bukkit.shuklerception.events;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import coffee.weneed.bukkit.shuklerception.BoxManager;
import coffee.weneed.bukkit.shuklerception.Shulkerception;
import coffee.weneed.bukkit.shuklerception.BoxManager.BoxData;

public class Inventory implements Listener {
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!Shulkerception.placed)
			return;
		if (!event.hasBlock() || !(event.getClickedBlock().getState() instanceof ShulkerBox) || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}

		if (event.getPlayer().isSneaking() && !event.isBlockInHand()) {
			event.setCancelled(true);
			return;
		}

		Player player = event.getPlayer();
		event.setCancelled(true);
		player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_OPEN, 1, 1);
		BoxData box = BoxManager.getManaged((event.getClickedBlock().getLocation()));
		if (box != null) {
			player.openInventory(box.getInventory());
			return;
		}
		player.openInventory(BoxManager.createShulkerBoxInventory(player, (ShulkerBox) event.getClickedBlock().getState()));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(final BlockBreakEvent event) {
		if (!Shulkerception.placed)
			return;
		if (!(event.getBlock().getState() instanceof ShulkerBox))
			return;
		BoxData box = BoxManager.getManaged(event.getBlock().getLocation());
		if (box == null)
			return;
		for (HumanEntity h : box.getInventory().getViewers()) {
			h.closeInventory();
		}
		BoxManager.boxes.remove(box.getID());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent event) {

		// ensure the Inventory is a Shulker Box Backpack Inventory
		if (!event.getView().getTitle().contains(BoxManager.IDENTIFIER) && !event.getView().getTitle().contains(BoxManager.IDENTIFIER2))
			return;
		boolean placed = event.getView().getTitle().contains(BoxManager.IDENTIFIER2);
		if (placed && !Shulkerception.placed)
			return;
		if (event.getCurrentItem() == null)
			return;
		if (!Shulkerception.nesting && Shulkerception.supportedMaterials.contains(event.getCurrentItem().getType())) {
			event.setCancelled(true);
			return;
		}

		ItemStack shulkerBox = event.getWhoClicked().getInventory().getItemInMainHand();

		// prevent duplication exploits on laggy servers by closing Inventory if no shulker box in hand on Inventory click
		if (shulkerBox == null && !placed) {
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
		}

		// prevent putting box inside itself (tests this by testing equal-ness for shulker boxes in hotbar
		if (event.getCurrentItem().equals(shulkerBox) && event.getRawSlot() >= 54 && !placed) {
			event.setCancelled(true);
			return;
		}

		// prevent nesting too far
		if (Shulkerception.supportedMaterials.contains(event.getCurrentItem().getType())) {
			if (event.getRawSlot() > 34) {
				if (Shulkerception.nesting && Shulkerception.nestingDepth > -1 && Shulkerception.getNestingDepth(event.getCurrentItem(), 1) >= Shulkerception.nestingDepth) {
					event.setCancelled(true);
					return;
				}
			}
		}

		// prevent swapping Inventory slot with shulker box (fixes dupe glitch)
		if (event.getAction().name().contains("HOTBAR") && !placed) {
			event.setCancelled(true);
			return;
		}

		if (placed) {
			String title = event.getView().getTitle();
			int id = Integer.parseInt(title.substring(title.indexOf(BoxManager.IDENTIFIER2) + BoxManager.IDENTIFIER2.length(), title.length()));
			ShulkerBox box = (ShulkerBox) BoxManager.boxes.get(id).getBlock().getState();

			box.getInventory().setContents(event.getInventory().getContents());
		} else {
			BlockStateMeta im = (BlockStateMeta) shulkerBox.getItemMeta();
			ShulkerBox shulker = (ShulkerBox) im.getBlockState();

			// set all contents minus most recent item
			shulker.getInventory().setContents(event.getInventory().getContents());

			// set most recent item
			// if (event.getAction() == InventoryAction.DROP_ALL_SLOT)
			//shulker.getInventory().setItem(event.getSlot(), event.getCurrentItem());

			im.setBlockState(shulker);
			shulkerBox.setItemMeta(im);
		}

	}

	//play shulker box close sound on Inventory close
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent event) {
		String title = event.getView().getTitle();
		if (!title.contains(BoxManager.IDENTIFIER) && !title.contains(BoxManager.IDENTIFIER2))
			return;
		boolean placed = title.contains(BoxManager.IDENTIFIER2);
		if (!Shulkerception.placed)
			return;
		Player player = (Player) event.getPlayer();

		if (placed) {

			int id = Integer.parseInt(title.substring(title.indexOf(BoxManager.IDENTIFIER2) + BoxManager.IDENTIFIER2.length(), title.length()));
			ShulkerBox box = (ShulkerBox) BoxManager.boxes.get(id).getBlock().getState();
			box.getInventory().setContents(event.getInventory().getContents());
			boolean ee = false;
			for (HumanEntity h : BoxManager.boxes.get(id).getInventory().getViewers()) {
				if (h.getOpenInventory().getTitle().contains(BoxManager.IDENTIFIER2)) {
					ee = true;
					break;
				}
			}
			if (!ee) {
				BoxManager.boxes.remove(id);
			}
		} else {
			ItemStack shulkerBox = player.getInventory().getItemInMainHand();
			BlockStateMeta im = (BlockStateMeta) shulkerBox.getItemMeta();
			ShulkerBox shulker = (ShulkerBox) im.getBlockState();

			//set all contents minus most recent item
			shulker.getInventory().setContents(event.getInventory().getContents());
			im.setBlockState(shulker);
			shulkerBox.setItemMeta(im);
		}

		player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_CLOSE, 1, 1);

	}

}
