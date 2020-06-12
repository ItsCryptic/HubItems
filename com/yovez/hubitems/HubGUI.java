package com.yovez.hubitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class HubGUI implements InventoryHolder {

	private final HubItems plugin;
	private final String path;
	private Inventory inv;

	public HubGUI(HubItems plugin, String path) {
		this.plugin = plugin;
		this.path = path;
		inv = Bukkit.createInventory(this, plugin.getConfig().getInt(path + ".size", 9),
				ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(path + ".title")));
	}

	@Override
	public Inventory getInventory() {
		for (String s : plugin.getConfig().getConfigurationSection(path + ".items").getKeys(false)) {
			inv.setItem(plugin.getConfig().getInt(path + ".items." + s + ".slot"),
					plugin.getConfigItem(path + ".items." + s));
		}
		return inv;
	}

}
