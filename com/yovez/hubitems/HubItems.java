package com.yovez.hubitems;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class HubItems extends JavaPlugin implements Listener {

	private List<ItemStack> hubItems;
	private List<ItemStack> hubGUIItems;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		Bukkit.getPluginManager().registerEvents(this, this);
		hubItems = new ArrayList<>();
		hubGUIItems = new ArrayList<>();
		for (String s : getConfig().getConfigurationSection("hubitems.items").getKeys(false)) {
			hubItems.add(getConfigItem("hubitems.items." + s));
		}
		for (String s : getConfig().getConfigurationSection("hubitems.items").getKeys(false)) {
			if (getConfig().getConfigurationSection("hubitems.items." + s + ".gui.items") != null)
				for (String ss : getConfig().getConfigurationSection("hubitems.items." + s + ".gui.items")
						.getKeys(false)) {
					hubGUIItems.add(getConfigItem("hubitems.items." + s + ".gui.items." + ss));
				}
		}
	}

	public void giveItems(Player p) {
		for (String s : getConfig().getConfigurationSection("hubitems.items").getKeys(false)) {
			if (getConfigItem("hubitems.items." + s) != null)
				p.getInventory().setItem(getConfig().getInt("hubitems.items." + s + ".slot"),
						getConfigItem("hubitems.items." + s));
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (getConfig().getBoolean("hubitems.settings.clear_on_join", true) == true)
			if (getConfig().getBoolean("hubitems.settings.clear_all", false) == true)
				e.getPlayer().getInventory().clear();
			else
				for (ItemStack i : e.getPlayer().getInventory()) {
					if (hubItems.contains(i))
						e.getPlayer().getInventory().remove(i);
				}
		if (getConfig().getBoolean("hubitems.settings.give_on_join", true) == true)
			giveItems(e.getPlayer());
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (getConfig().getBoolean("hubitems.settings.clear_on_respawn", true) == true)
			if (getConfig().getBoolean("hubitems.settings.clear_all", false) == true)
				e.getPlayer().getInventory().clear();
			else
				for (ItemStack i : e.getPlayer().getInventory()) {
					if (hubItems.contains(i))
						e.getPlayer().getInventory().remove(i);
				}
		if (getConfig().getBoolean("hubitems.settings.give_on_respawn", true) == true)
			giveItems(e.getPlayer());
	}

	@EventHandler
	public void onArrowLand(ProjectileHitEvent e) {
		if (e.getEntity().getType().equals(EntityType.ARROW)) {
			Arrow item = (Arrow) e.getEntity();
			if (item.getShooter() instanceof Player) {
				Player p = (Player) item.getShooter();
				if (p.getInventory().getItemInMainHand().equals(getConfigItem("hubitems.items.tp_bow"))) {
					Location loc = item.getLocation();
					loc.setDirection(p.getLocation().getDirection());
					loc.setPitch(p.getLocation().getPitch());
					loc.setYaw(p.getLocation().getYaw());
					p.teleport(loc);
					p.playSound(p.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 100, 100);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (getConfig().getBoolean("hubitems.settings.disable_drop", true) == true)
			if (hubItems.size() > 0)
				e.getDrops().removeAll(hubItems);
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		if (getConfig().getBoolean("hubitems.settings.disable_drop", true) == true)
			e.setCancelled(true);
	}

	@EventHandler
	public void onItemMove(InventoryMoveItemEvent e) {
		if (e.getInitiator().getHolder() instanceof PlayerInventory)
			if (getConfig().getBoolean("hubitems.settings.disable_move", true) == true)
				e.setCancelled(true);
	}

	@EventHandler
	public void onItemClick(InventoryClickEvent e) {
		if (e.getClickedInventory() != null) {
			if (e.getClickedInventory().getHolder().equals(e.getWhoClicked()))
				if (getConfig().getBoolean("hubitems.settings.disable_move", true) == true)
					e.setCancelled(true);
			if (e.getClickedInventory().getHolder() instanceof HubGUI)
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		for (String s : getConfig().getConfigurationSection("hubitems.items").getKeys(false)) {
			if (getConfig().getConfigurationSection("hubitems.items." + s + ".gui.items") != null
					&& e.getItem().equals(getConfigItem("hubitems.items." + s))) {
				HubGUI gui = new HubGUI(this, "hubitems.items." + s + ".gui");
				e.getPlayer().openInventory(gui.getInventory());
			}
		}
	}

	public ItemStack getConfigItem(String path) {
		if (Material.matchMaterial(getConfig().getString(path + ".material")) == null)
			return null;
		ItemStack item = new ItemStack(Material.matchMaterial(getConfig().getString(path + ".material")),
				getConfig().getInt(path + ".amount", 1));
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(
				ChatColor.translateAlternateColorCodes('&', getConfig().getString(path + ".display_name", "")));
		List<String> lore = new ArrayList<>();
		if (getConfig().getStringList(path + ".lore") != null)
			for (String s : getConfig().getStringList(path + ".lore"))
				lore.add(ChatColor.translateAlternateColorCodes('&', s));
		meta.setLore(lore);
		if (getConfig().getBoolean(path + ".unbreakable", false) == true)
			meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
		if (getConfig().getConfigurationSection(path + ".enchantments") != null)
			for (String s : getConfig().getConfigurationSection(path + ".enchantments").getKeys(false)) {
				if (Enchantment.getByName(s.toUpperCase()) != null)
					meta.addEnchant(Enchantment.getByName(s.toUpperCase()),
							getConfig().getInt(path + ".enchantments." + s), true);
			}
		item.setItemMeta(meta);
		return item;
	}

}
