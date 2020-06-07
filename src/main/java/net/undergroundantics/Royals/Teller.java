package net.undergroundantics.Royals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.inventory.ClickType;

import net.citizensnpcs.api.trait.Trait;
import net.md_5.bungee.api.ChatColor;
import net.citizensnpcs.api.event.*;

public final class Teller extends Trait {

	public static String NAME = "teller";
	private static String INVENTORY_NAME = "Bank";

	private static String DEPOSIT_NAME = "Deposit Royals";
	private static List<String> DEPOSIT_LORE = Arrays.asList("Place Royals here to deposit", "them into your bank account.");
	private static Integer DEPOSIT_SLOT = 3;
	private static String WITHDRAW_NAME = "Withdraw Royals";
	private static List<String> WITHDRAW_LORE = Arrays.asList("Take Royals from here to withdraw", "them from your bank account.");
	private static Integer WITHDRAW_SLOT = 5;

	private static String INSUFFICIENT_FUNDS = ChatColor.RED + "Your balance is insufficient to withdraw any Royals.";
	
	private Inventory inventory;
	private Royals plugin = null;

	public Teller() {
		super(NAME);
		plugin = JavaPlugin.getPlugin(Royals.class);
		inventory = Bukkit.createInventory(null, 9, INVENTORY_NAME);
		ItemStack[] bank = new ItemStack[9];
		bank[DEPOSIT_SLOT] = new Royal(DEPOSIT_NAME, DEPOSIT_LORE);
		bank[WITHDRAW_SLOT] = new Royal(WITHDRAW_NAME, WITHDRAW_LORE);
		inventory.setContents(bank);
	}

	@EventHandler
	private void click(NPCRightClickEvent event) {

		if (event.getNPC() != this.getNPC()) {
			return;
		}

		Player player = event.getClicker();

		if (!player.hasPermission("royals.bankteller.use")) {
			return;
		}

		player.openInventory(inventory);
	}

	@EventHandler
	private void inventoryDrag(InventoryDragEvent e) {
		
		if (e.getInventory() != inventory) {
			return;
		}
		
		// Cancel drag events in the bank pane
		for(Integer slot : e.getInventorySlots()) {
			if (slot < 9) {
				plugin.getLogger().info(String.format("selected slot: %d", slot));
				e.setCancelled(true);
				return;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	private void inventoryClick(InventoryClickEvent e) {
		
		if (e.getInventory() != inventory) {
			return;
		}
		
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}
		
		Player player = (Player) e.getWhoClicked();

		Boolean isShiftClick = e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT;
		Boolean isCursorRoyal = e.getCursor() != null && Royal.isRoyal(e.getCursor());
		Boolean isSelectedRoyal = e.getCurrentItem() != null && Royal.isRoyal(e.getCurrentItem());
		
		int slot = e.getRawSlot();
		
		if ((slot >= 0 && slot < 9) || isShiftClick) {
			e.setCancelled(true);
		}
		
		if (slot >= 9 && isShiftClick && isSelectedRoyal) {
			
			int amount = e.getCurrentItem().getAmount();
			if (plugin.deposit(player, amount)) {
				e.setCurrentItem(new ItemStack(Material.AIR, 1));
			}
			
		} else if (slot == DEPOSIT_SLOT) {
			
			if (e.getCursor() == null || !Royal.isRoyal(e.getCursor())) {
				return;
			}
			
			int amount;
			switch (e.getClick())
			{
			case LEFT:
				amount = e.getCursor().getAmount();
				break;
			case RIGHT:
				amount = 1;
				break;
			default:
				return;
			}
			
			if (plugin.deposit(player, amount)) {
				e.getCursor().setAmount(e.getCursor().getAmount() - amount);
			}
			
		} else if (slot == WITHDRAW_SLOT && isShiftClick) {
			
			int amount = Math.min(64, (int) plugin.getEconomy().getBalance((player)));
			if (amount <= 0) {
				plugin.messagePlayer(player, INSUFFICIENT_FUNDS);
				return;
			}
			ItemStack royals = new Royal(amount);
			HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(royals);
			if (leftover.size() == 1) {
				amount -= leftover.get(0).getAmount();
				if (amount <= 0) {
					return;
				}
			}
			if (!plugin.withdraw(player, amount)) {
				player.getInventory().removeItem(new Royal(amount));
			}
			
		} else if (slot == WITHDRAW_SLOT) {
			
			if (e.getCursor().getType() != Material.AIR && !isCursorRoyal) {
				return;
			}
				
			int amount;
			switch (e.getClick())
			{
			case LEFT:
				amount = 8;
				break;
			case RIGHT:
				amount = 1;
				break;
			default:
				return;
			}
			
			// Augment the desired amount based on the capacity of the cursor
			if (isCursorRoyal) {
				amount = Math.min(64 - e.getCursor().getAmount(), amount);
				if (amount <= 0) {
					return;
				}
			}
			
			// Augment by the remaining balance of the player
			amount = Math.min(amount, (int) plugin.getEconomy().getBalance(player));
			if (amount <= 0) {
				plugin.messagePlayer(player, INSUFFICIENT_FUNDS);
				return;
			}
			
			if (plugin.withdraw(player, amount)) {
				if (isCursorRoyal) {
					e.getCursor().setAmount(e.getCursor().getAmount() + amount);
				} else { // AIR
					// Should be safe because the event is cancelled.
					e.setCursor(new Royal(amount));
				}
			}

		}
	}

	@Override
	public void onAttach() {
		plugin.getLogger().info(npc.getName() + " has been assigned to a Teller");
	}

}
