package net.undergroundantics.Royals;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import com.earth2me.essentials.Essentials;

import java.text.DecimalFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Royals extends JavaPlugin {

	private Economy economy = null;
	
	public Economy getEconomy() {
		return economy;
	}
	
	private Essentials essentials = null;
	
	public Essentials getEssentials() {
		return essentials;
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Disabled");
	}

	@Override
	public void onEnable() {

		if (!setupEconomy()) {
			getLogger().severe(String.format("[%s] Disabled due to no Vault found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (getServer().getPluginManager().getPlugin("Citizens") == null || getServer().getPluginManager().getPlugin("Citizens").isEnabled() == false) {
			getLogger().severe(String.format("[%s] Disabled due to no Citizens 2.0 found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		if (!setupEssentials()) {
			getLogger().severe(String.format("[%s] Disabled due to no Essentials found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(Teller.class).withName(Teller.NAME));
		
		Centrelink.run(this);	
	}
	
	private boolean setupEssentials() {	
		Plugin plugin = getServer().getPluginManager().getPlugin("Essentials");
		if (plugin == null || !plugin.isEnabled()) {
			return false;
		}
		essentials = (Essentials) plugin;
		return true;
	}

	private boolean setupEconomy() {
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		if (vault== null || !vault.isEnabled()) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}

	public void messagePlayer(Player player, String message) {
		player.sendMessage(message);
	}

	public Boolean deposit(Player player, int amount) {
		EconomyResponse er = economy.depositPlayer(player, amount);
		if (er.transactionSuccess()) {
			getLogger().info(String.format("deposited %d Royals into the account of %s", amount, player.getName()));
			displayBalance(player);
			return true;
		} else {
			getLogger().severe(String.format("failed to deposit %d Royals into the account of %s: %s", amount, player.getName(), er.errorMessage));
			return false;
		}
	}
	
	public Boolean withdraw(Player player, int amount) {
		EconomyResponse er = economy.withdrawPlayer(player, amount);		
		if (er.transactionSuccess()) {
			getLogger().info(String.format("withdrew %d Royals from the account of %s", amount, player.getName()));
			displayBalance(player);
			return true;
		} else {
			getLogger().severe(String.format("failed to withdraw %d Royals from the account of %s: %s", amount, player.getName(), er.errorMessage));
			return false;
		}
	}
	
	private void displayBalance(Player player) {
		messagePlayer(player, ChatColor.LIGHT_PURPLE + String.format("Your balance is now: %s Royals", new DecimalFormat("#.##").format(economy.getBalance(player))));
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		if (command.getLabel().equalsIgnoreCase("bankteller")) {
			NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Bank Teller");
			npc.addTrait(new Teller());
			npc.spawn(player.getLocation());
			getLogger().info(String.format("%s created a bank teller", player.getName()));
			return true;
		} else {
			return false;
		}
	}

}
