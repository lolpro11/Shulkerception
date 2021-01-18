package coffee.weneed.bukkit.shuklerception.events;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import coffee.weneed.bukkit.shuklerception.NestedInventory;
import coffee.weneed.bukkit.shuklerception.Shulkerception;

public class InventoryListener implements Listener {
	//play shulker box close sound on Inventory close
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCloseInventory(InventoryCloseEvent event) {
		String title = event.getView().getTitle();
		if (!title.contains(Shulkerception.IDENTIFIER))
			return;

		Player player = (Player) event.getPlayer();
		((NestedInventory) event.getInventory().getHolder()).save();
		player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_CLOSE, 1, 1);
		//make sure there is no loop from opening the new inventory
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onOpenInventory(InventoryOpenEvent event) {
		String title = event.getView().getTitle();
		if (!title.contains(Shulkerception.IDENTIFIER))
			return;

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent event) {
		// ensure the Inventory is a Shulker Box Backpack Inventory
		if (!event.getWhoClicked().getGameMode().equals(GameMode.SURVIVAL))
			return;
		if (event.getWhoClicked().hasPermission("Shulkerception.use") && event.getCursor().getType().isAir()) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getAmount() == 1 && Shulkerception.supportedMaterials.contains(event.getCurrentItem().getType()) && event.getClick().equals(ClickType.MIDDLE)) {
				if ((event.getInventory().getHolder() instanceof NestedInventory || event.getInventory().getHolder() instanceof HumanEntity) && ((event.getInventory().getHolder() instanceof NestedInventory && event.getRawSlot() <= 27) || ((event.getInventory().getHolder() instanceof HumanEntity) && event.getRawSlot() > 8 && event.getRawSlot() <= 44) || (event.getRawSlot() <= 27 && ((NestedInventory) event.getInventory().getHolder()).getParent() != null))) {
					if (event.getInventory().getHolder() instanceof NestedInventory && ((NestedInventory) event.getInventory().getHolder()).checkTree(event.getCurrentItem())) {
						return;
					}
					Player player = (Player) event.getWhoClicked();
					player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_OPEN, 1, 1);
					event.getWhoClicked().openInventory(Shulkerception.createShulkerBoxInventory(event.getCurrentItem(), event.getInventory().getHolder() instanceof NestedInventory ? (NestedInventory) event.getInventory().getHolder() : null).getInventory());
					event.setCancelled(true);
					return;
				} else if (event.getInventory().getType().equals(InventoryType.ENDER_CHEST) && event.getRawSlot() <= 27) {
					Player player = (Player) event.getWhoClicked();
					player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_OPEN, 1, 1);
					event.getWhoClicked().openInventory(Shulkerception.createShulkerBoxInventory(event.getCurrentItem(), null).getInventory());
					event.setCancelled(true);
					return;
				}
			}
		}
		if (!event.getView().getTitle().contains(Shulkerception.IDENTIFIER)) {
			return;
		}
		
		
		if (event.getCurrentItem() == null) {
			if (((NestedInventory) event.getInventory().getHolder()).getParent() == null || !event.getWhoClicked().hasPermission("Shulkerception.use") || !event.getClick().equals(ClickType.MIDDLE)) {
				return;
			}
			event.getWhoClicked().openInventory(((NestedInventory) event.getInventory().getHolder()).getParent().getInventory());
			event.setCancelled(true);
			return;
		} else if (event.getInventory().getHolder() instanceof NestedInventory){
			NestedInventory n = (NestedInventory) event.getInventory().getHolder();
			while (n != null) {
				if (event.getCurrentItem().equals(n.getShulker())) {
					event.setCancelled(true);
					return;
				}
				n = n.getParent();
			}
		}
		if (Shulkerception.supportedMaterials.contains(event.getCurrentItem().getType()) && event.getAction().name().toLowerCase().contains("DROP".toLowerCase())) {
			event.setCancelled(true);
		}
		if (!Shulkerception.nesting && Shulkerception.supportedMaterials.contains(event.getCurrentItem().getType())) {
			event.setCancelled(true);
			return;
		}

		ItemStack shulkerBox = ((NestedInventory) event.getInventory().getHolder()).getShulker();
		// prevent duplication exploits on laggy servers by closing Inventory if no shulker box in hand on Inventory click
		if (shulkerBox == null) {
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
		}
		// prevent putting box inside itself (tests this by testing equal-ness for shulker boxes in hotbar
		if (event.getCurrentItem().equals(shulkerBox) && event.getRawSlot() >= 54) {
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
