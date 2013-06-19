package us.Myles.PWP;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.ryanclancy000.plugman.PlugMan;

import us.Myles.PWP.compatibility.PlugManUtils;

public class Plugin extends JavaPlugin {
	public static Plugin instance;
	@SuppressWarnings("deprecation")
	public List<Class<?>> exemptEvents = Arrays.asList(new Class<?>[] {
			AsyncPlayerPreLoginEvent.class, PlayerJoinEvent.class,
			PlayerKickEvent.class, PlayerLoginEvent.class,
			PlayerPreLoginEvent.class, PlayerQuitEvent.class });
	private boolean isExemptEnabled = true;
	public String blockedMessage;

	public void onEnable() {
		Plugin.instance = this;
		getCommand("pwp").setExecutor(new PWPCommandExecutor());
		reloadConfig();
		loadConfig();
		setupMetrics();
		executeCompatibilityLayer();
		boolean isInjected = false;
		$("Enabled, Attempting to Inject PluginManager");
		if (Bukkit.getPluginManager().getClass().getPackage().getName()
				.contains("Myles")) {
			Bukkit.getServer()
					.getLogger()
					.log(Level.SEVERE,
							"Looks like the FakePluginManager has already been injected, If this is a reload please ignore.");
			isInjected = true;
		}
		try {
			Field f = Bukkit.getServer().getClass()
					.getDeclaredField("pluginManager");
			f.setAccessible(true);
			PluginManager oldManager = (PluginManager) f
					.get(Bukkit.getServer());
			if (isInjected) {
				f.set(Bukkit.getServer(),
						new FakePluginManager((PluginManager) oldManager
								.getClass().getDeclaredField("oldManager")
								.get(oldManager)));
			} else {
				f.set(Bukkit.getServer(), new FakePluginManager(oldManager));
			}

		} catch (NoSuchFieldException | SecurityException e) {
			System.out
					.println("[Error] Failed to inject, please notify the author on bukkitdev. (Type: FieldNotFound, PluginManager)");
		} catch (IllegalArgumentException e) {
			System.out
					.println("[Error] Failed to inject, please notify the author on bukkitdev. (Type: IllegalArgument, PluginManager)");
		} catch (IllegalAccessException e) {
			System.out
					.println("[Error] Failed to inject, please notify the author on bukkitdev. (Type: AccessError, PluginManager)");
		}
		$("Enabled, Attempting to Inject CommandHandler");
		try {
			Field f = Bukkit.getServer().getClass()
					.getDeclaredField("commandMap");
			if (f.getType().getClass().getPackage().getName().contains("Myles")) {
				Bukkit.getServer()
						.getLogger()
						.log(Level.SEVERE,
								"Looks like the FakeSimpleCommandMap has already been injected, If this is a reload please ignore.");
				return;
			}
			if (!isInjected) {
				f.setAccessible(true);
				SimpleCommandMap oldCommandMap = (SimpleCommandMap) f
						.get(Bukkit.getServer());
				f.set(Bukkit.getServer(), new FakeSimpleCommandMap(
						oldCommandMap));
			}
		} catch (NoSuchFieldException | SecurityException e) {
			System.out
					.println("[Error] Failed to inject, please notify the author on bukkitdev. (Type: FieldNotFound, SimpleCommandMap)");
		} catch (IllegalArgumentException e) {
			System.out
					.println("[Error] Failed to inject, please notify the author on bukkitdev. (Type: IllegalArgument, SimpleCommandMap)");
		} catch (IllegalAccessException e) {
			System.out
					.println("[Error] Failed to inject, please notify the author on bukkitdev. (Type: AccessError, SimpleCommandMap)");
		}
	}
	public void $(String s){
		System.out.println("[PerWorldPlugins] " + s);
	}
	private void executeCompatibilityLayer() {
		// Check for PlugMan
		try {
			$("Found PlugMan Adding Compatibility Layer");
			Class<?> c = Class.forName("com.ryanclancy000.plugman.PlugMan");
			Field f = c.getDeclaredField("utils");
			f.setAccessible(true);
			f.set(this.getServer().getPluginManager().getPlugin("PlugMan"), new PlugManUtils((PlugMan) this.getServer().getPluginManager().getPlugin("PlugMan")));
		} catch (ClassNotFoundException e) {
			// No PlugMan
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setupMetrics() {
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
	}

	public void loadConfig() {
		this.saveDefaultConfig();
		FileConfiguration c = getConfig();
		if (!c.isBoolean("exempt-login-events")
				|| !c.contains("exempt-login-events")
				|| !c.isSet("exempt-login-events")) {
			c.set("exempt-login-events", true);
		}
		isExemptEnabled = c.getBoolean("exempt-login-events", true);
		if (!c.isString("blocked-msg") || !c.contains("blocked-msg")
				|| !c.isSet("blocked-msg")) {
			c.set("blocked-msg", "&c[Error] This command cannot be performed in this world.");
		}
		blockedMessage = c.getString("blocked-msg", "&c[Error] This command cannot be performed in this world.");
		ConfigurationSection ul = c.getConfigurationSection("limit");
		if (ul == null) {
			ul = c.createSection("limit");
		}
		for (org.bukkit.plugin.Plugin plug : Bukkit.getPluginManager()
				.getPlugins()) {
			if (plug.equals(this))
				continue;
			if (!ul.isList(plug.getDescription().getName())) {
				ul.set(plug.getDescription().getName(), new ArrayList<String>());
			}
		}
		saveConfig();
	}

	public boolean checkWorld(org.bukkit.plugin.Plugin plugin, World w) {
		ConfigurationSection limit = getConfig().getConfigurationSection(
				"limit");
		if (limit.isList(plugin.getDescription().getName())) {
			List<String> worlds = limit.getStringList(plugin.getDescription()
					.getName());
			if (worlds.size() == 0) {
				return true;
			} else {
				for (String s : worlds) {
					if (w.getName().equalsIgnoreCase(s)) {
						return true;
					}
				}
				return false;
			}
		} else {
			return true;
		}
	}

	public boolean isExemptEnabled() {
		return this.isExemptEnabled;
	}
}
