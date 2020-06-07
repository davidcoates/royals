package net.undergroundantics.Royals;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Royal extends ItemStack {

	public Royal() {
		this(1);
	}
	
	public Royal(int amount) {
		this(ROYAL_NAME, ROYAL_LORE, amount);
	}

	public Royal(String name, List<String> lore) {
		this(name, lore, 1);
	}
	
	public Royal(String name, List<String> lore, int amount) {
		super(Material.EMERALD, amount);
		addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		ItemMeta meta = getItemMeta();
		meta.setDisplayName(name);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.setLore(lore);
		setItemMeta(meta);
	}

	private static String ROYAL_NAME = "Royal";
	private static List<String> ROYAL_LORE = Arrays.asList("A valuable gemstone from an", "era long forgotten.");

	public static Boolean isRoyal(ItemStack itemStack) {
		return itemStack.getType() == Material.EMERALD && itemStack.getItemMeta().hasEnchant(Enchantment.DURABILITY);
	}
}
