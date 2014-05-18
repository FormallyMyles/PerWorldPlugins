package us.Myles.PWP;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mcstats.Metrics;

import java.util.regex.Pattern;

import us.Myles.PWP.TransparentListeners.PerWorldPluginLoader;

public class Plugin extends JavaPlugin {
	public static Plugin instance;
	@SuppressWarnings("deprecation")
	public List<Class<?>> exemptEvents = Arrays.asList(new Class<?>[] { AsyncPlayerPreLoginEvent.class,
			PlayerJoinEvent.class, PlayerKickEvent.class, PlayerLoginEvent.class, PlayerPreLoginEvent.class,
			PlayerQuitEvent.class });
	private boolean isExemptEnabled = true;
	public String blockedMessage;
	public boolean isUpdatesEnabled = true;

	public void onLoad() {
		Plugin.instance = this;
		$("Registering Event Interceptor");
		PerWorldPluginLoader pwpLoader = new PerWorldPluginLoader(Bukkit.getServer());
		injectExistingPlugins(pwpLoader);
		cleanJavaPluginLoaders(pwpLoader);
	}

	private void injectExistingPlugins(PerWorldPluginLoader pwpLoader) {
		for (org.bukkit.plugin.Plugin p : Bukkit.getPluginManager().getPlugins()) {
			if (p instanceof JavaPlugin) {
				JavaPlugin jp = (JavaPlugin) p;
				try {
					Field f = JavaPlugin.class.getDeclaredField("loader");
					f.setAccessible(true);
					f.set(jp, pwpLoader);
				} catch (Exception e) {
					Bukkit.getServer()
							.getLogger()
							.log(Level.SEVERE,
									"PerWorldPlugins failed injecting " + jp.getDescription().getFullName()
											+ " with PluginLoader, contact the Dev on BukkitDev.", e);
				}
			}
		}
	}

	private void cleanJavaPluginLoaders(PerWorldPluginLoader pwpLoader) {
		PluginManager spm = Bukkit.getPluginManager();
		try {
			Field field = spm.getClass().getDeclaredField("fileAssociations");
			field.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			@SuppressWarnings("unchecked")
			Map<Pattern, PluginLoader> map = (Map<Pattern, PluginLoader>) field.get(spm);
			Iterator<Map.Entry<Pattern, PluginLoader>> iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Pattern, PluginLoader> entry = iter.next();
				if (entry.getValue() instanceof JavaPluginLoader) {
					entry.setValue(pwpLoader);
				}
			}
			field.set(spm, map);
		} catch (Exception e) {
			Bukkit.getServer()
					.getLogger()
					.log(Level.SEVERE,
							"PerWorldPlugins failed replacing the existing PluginLoader, contact the Dev on BukkitDev",
							e);
		}
	}

	public void onEnable() {
		getCommand("pwp").setExecutor(new PWPCommandExecutor());
		reloadConfig();
		loadConfig();
		setupMetrics();
		boolean isInjected = false;
		$("Enabled, Attempting to Inject CommandHandler");
		try {
			Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			if (f.getType().getClass().getPackage().getName().contains("Myles")) {
				Bukkit.getServer()
						.getLogger()
						.log(Level.SEVERE,
								"Looks like the FakeSimpleCommandMap has already been injected, If this is a reload please ignore.");
				return;
			}
			if (!isInjected) {
				f.setAccessible(true);
				SimpleCommandMap oldCommandMap = (SimpleCommandMap) f.get(Bukkit.getServer());
				f.set(Bukkit.getServer(), new FakeSimpleCommandMap(oldCommandMap));
			}
		} catch (Exception e) {
			Bukkit.getServer()
					.getLogger()
					.log(Level.SEVERE,
							"PerWorldPlugins failed replacing the existing PluginLoader, contact the Dev on BukkitDev",
							e);
		}
	}

	public void $(String s) {
		System.out.println("[PerWorldPlugins] " + s);
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
		if (!c.isBoolean("exempt-login-events") || !c.contains("exempt-login-events")
				|| !c.isSet("exempt-login-events")) {
			c.set("exempt-login-events", true);
		}
		isExemptEnabled = c.getBoolean("exempt-login-events", true);
		if (!c.isBoolean("check-for-updates") || !c.contains("check-for-updates") || !c.isSet("check-for-updates")) {
			c.set("check-for-updates", true);
		}
		isUpdatesEnabled = c.getBoolean("check-for-updates", true);
		if (!c.isString("blocked-msg") || !c.contains("blocked-msg") || !c.isSet("blocked-msg")) {
			c.set("blocked-msg", "&c[Error] This command cannot be performed in this world.");
		}
		blockedMessage = c.getString("blocked-msg", "&c[Error] This command cannot be performed in this world.");
		ConfigurationSection ul = c.getConfigurationSection("limit");
		if (ul == null) {
			ul = c.createSection("limit");
		}
		for (org.bukkit.plugin.Plugin plug : Bukkit.getPluginManager().getPlugins()) {
			if (plug.equals(this))
				continue;
			if (!ul.isList(plug.getDescription().getName())) {
				ul.set(plug.getDescription().getName(), new ArrayList<String>());
			}
		}
		saveConfig();
	}

	public boolean checkWorld(org.bukkit.plugin.Plugin plugin, World w) {
		ConfigurationSection limit = getConfig().getConfigurationSection("limit");
		if (limit.isList(plugin.getDescription().getName())) {
			List<String> worlds = limit.getStringList(plugin.getDescription().getName());
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

	public boolean checkWorld(org.bukkit.plugin.Plugin plugin, Event e) {
		if ((e instanceof PlayerEvent)) {
			PlayerEvent e1 = (PlayerEvent) e;
			if ((exemptEvents.contains(e.getClass())) && (instance.isExemptEnabled())) {
				return true;
			}
			return checkWorld(plugin, e1.getPlayer().getWorld());
		}
		if ((e instanceof BlockEvent)) {
			BlockEvent e1 = (BlockEvent) e;
			return checkWorld(plugin, e1.getBlock().getWorld());
		}
		if ((e instanceof InventoryEvent)) {
			InventoryEvent e1 = (InventoryEvent) e;
			return checkWorld(plugin, e1.getView().getPlayer().getWorld());
		}
		if ((e instanceof EntityEvent)) {
			EntityEvent e1 = (EntityEvent) e;
			return checkWorld(plugin, e1.getEntity().getWorld());
		}
		if ((e instanceof HangingEvent)) {
			HangingEvent e1 = (HangingEvent) e;
			return checkWorld(plugin, e1.getEntity().getWorld());
		}
		if ((e instanceof VehicleEvent)) {
			VehicleEvent e1 = (VehicleEvent) e;
			return checkWorld(plugin, e1.getVehicle().getWorld());
		}
		if ((e instanceof WeatherEvent)) {
			WeatherEvent e1 = (WeatherEvent) e;
			return checkWorld(plugin, e1.getWorld());
		}
		if ((e instanceof WorldEvent)) {
			WorldEvent e1 = (WorldEvent) e;
			return checkWorld(plugin, e1.getWorld());
		}
		if ((e instanceof ServerEvent)) {
			return true;
		}
		return true;
	}

	public boolean isExemptEnabled() {
		return this.isExemptEnabled;
	}
}
