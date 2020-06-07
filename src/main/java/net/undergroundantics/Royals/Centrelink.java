package net.undergroundantics.Royals;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;

import net.md_5.bungee.api.ChatColor;

public class Centrelink {

	private Royals plugin = null;
	
	private static long PAY_PERIOD_MILLIS = 30 * 60 * 1000;
	
	private static String PAY_MESSAGE = ChatColor.LIGHT_PURPLE + "You have earned 1 Royal for playing on the server.";
	
	public static void run(Royals plugin) {
		Centrelink cenno = new Centrelink(plugin);
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				cenno.tick();
			}
		}, 0, 60 * 20);
	}
	
	private Centrelink(Royals plugin) {
		this.plugin = plugin;
	}
	
	private long lastTick = 0;
	private HashMap<Player, Long> playTimes = new HashMap<>();
	
	private void tick() {
		
		long thisTick = System.currentTimeMillis();
		if (lastTick == 0) {
			lastTick = thisTick;
			return;
		}
		
		long duration = thisTick - lastTick;
		lastTick = thisTick;
		
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			User user = plugin.getEssentials().getUser(player);
			if (user == null) {
				plugin.getLogger().severe(String.format("could not find essentials user for player %s", player.getName()));
			}
			if (user.isAfk()) {
				continue;
			}
			long playTime = playTimes.getOrDefault(player, 0L);
			playTime += duration;
			if (playTime >= PAY_PERIOD_MILLIS) {
				playTime -= PAY_PERIOD_MILLIS;
				plugin.messagePlayer(player, PAY_MESSAGE);
				plugin.deposit(player, 1);
			}
			playTimes.put(player, playTime);
		}
		
	}
}
