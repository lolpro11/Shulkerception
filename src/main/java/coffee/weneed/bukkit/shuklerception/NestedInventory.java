package coffee.weneed.bukkit.shuklerception;

import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;

public class NestedInventory implements InventoryHolder {
	private ItemStack shulker;
	private NestedInventory parent;
	private Inventory inventory;
	private boolean closed = false;
	
	
	public NestedInventory(ItemStack shulker) {
		this(null, shulker);
	}
	
	public NestedInventory(NestedInventory parent, ItemStack shulker) {
		setShulker(shulker);
		setParent(parent);
		BlockStateMeta im = (BlockStateMeta) shulker.getItemMeta();
		ShulkerBox shulkerstate = (ShulkerBox) im.getBlockState();
		String name = shulker.getItemMeta().getDisplayName();
		if (name.equals("")) {
			name = shulker.getType().name();
		}
		Inventory inv = Bukkit.createInventory(this, 27, name + Shulkerception.IDENTIFIER);
		setInventory(inv);
		getInventory().setContents(shulkerstate.getInventory().getContents());
	}

	public ItemStack getShulker() {
		return shulker;
	}

	public void setShulker(ItemStack shulker) {
		this.shulker = shulker;
	}

	public void save() {
		BlockStateMeta im = (BlockStateMeta) getShulker().getItemMeta();
		ShulkerBox shulker = (ShulkerBox) im.getBlockState();

		//set all contents minus most recent item
		shulker.getInventory().setContents(getInventory().getContents());
		im.setBlockState(shulker);
		getShulker().setItemMeta(im);
		
		if (getParent() != null) {
			getParent().save();
		}
	}

	public boolean checkTree(ItemStack shulker) {
		if (getShulker().equals(shulker)) {
			return true;
		} else if (getParent() != null) {
			return getParent().checkTree(shulker);
		}
		return false;
	}
	public NestedInventory getParent() {
		return parent;
	}

	public void setParent(NestedInventory parent) {
		this.parent = parent;
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inventory;
	}
	
	public void setInventory(Inventory inv) {
		this.inventory = inv;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}
}