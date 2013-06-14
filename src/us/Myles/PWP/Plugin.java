package us.Myles.PWP;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class Plugin extends JavaPlugin {
	public static Plugin instance;

	public void onEnable() {
		Plugin.instance = this;
		reloadConfig();
		loadConfig();
		setupMetrics();
		// Enabled
		boolean isInjected = false;
		System.out.println("Enabled, Attempting to Inject PluginManager");
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
		System.out.println("Enabled, Attempting to Inject CommandHandler");
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

	private void setupMetrics() {
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
	}

	private void loadConfig() {
		this.saveDefaultConfig();
		FileConfiguration c = getConfig();
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
}
