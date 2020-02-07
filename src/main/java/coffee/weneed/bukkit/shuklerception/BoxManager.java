package coffee.weneed.bukkit.shuklerception;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class BoxManager {
	public static class BoxData {
		private Inventory inventory;
		private Block block;
		private int id;

		public BoxData(Inventory inv, int id, Block bl) {
			inventory = inv;
			block = bl;
			this.id = id;
		}

		public Block getBlock() {
			return block;
		}

		public int getID() {
			return id;
		}

		public Inventory getInventory() {
			return inventory;
		}

		public Location getLoc() {
			return block.getLocation();
		}

	}

	public static final String IDENTIFIER = ChatColor.BLUE + "+" + ChatColor.RESET;
	public static final String IDENTIFIER2 = ChatColor.DARK_GREEN + "+" + ChatColor.RESET;

	public static Map<Integer, BoxData> boxes = new HashMap<>();

	public static Inventory createShulkerBoxInventory(Player player, ItemStack shulkerBoxItemStack) {

		if (shulkerBoxItemStack.getItemMeta() instanceof BlockStateMeta) {
			BlockStateMeta im = (BlockStateMeta) shulkerBoxItemStack.getItemMeta();
			if (im.getBlockState() instanceof ShulkerBox) {
				ShulkerBox shulker = (ShulkerBox) im.getBlockState();

				String name = shulkerBoxItemStack.getItemMeta().getDisplayName();
				if (name.equals("")) {
					name = shulkerBoxItemStack.getType().name();
				}
				Inventory inv = Bukkit.createInventory(null, 27, name + IDENTIFIER);
				inv.setContents(shulker.getInventory().getContents());
				//player.openInventory(inv);

				return inv;
			}
		}

		//should never happen, because function is only called for shulker boxes
		return null;
	}

	public static Inventory createShulkerBoxInventory(Player player, ShulkerBox shulker) {

		String name = shulker.getCustomName();
		if (name == null || name.equals("")) {
			name = shulker.getType().name();
		}
		int id = getAvailableID();
		Inventory inv = Bukkit.createInventory(null, 27, name + IDENTIFIER2 + id);
		inv.setContents(shulker.getInventory().getContents());
		boxes.put(id, new BoxManager.BoxData(inv, id, shulker.getBlock()));
		//player.openInventory(inv);

		return inv;

	}

	public static int getAvailableID() {
		int r = -1;
		for (int i = 0; i < boxes.size(); i++) {
			if (!boxes.containsKey(i)) {
				r = i;
				break;
			}
		}

		if (r == -1) {
			int i = 0;
			while (true) {
				if (!boxes.containsKey(i)) {
					r = i;
					break;
				}
				i++;
			}
		}
		return r;
	}

	public static BoxData getManaged(Location loc) {
		for (BoxData box : boxes.values()) {
			if (box.getLoc().equals(loc))
				return box;
		}
		return null;
	}

	public static boolean isManaged(Location loc) {
		return getManaged(loc) != null;
	}
}
