package us.Myles.PWP.compatibility;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.UnknownDependencyException;

import us.Myles.PWP.FakePluginManager;

import com.ryanclancy000.plugman.PlugMan;

public class PlugManUtils extends com.ryanclancy000.plugman.utilities.Utilities {
	private static final String pre = new StringBuilder()
			.append(ChatColor.GRAY).append("[").append(ChatColor.GREEN)
			.append("PlugMan").append(ChatColor.GRAY).append("] ").toString();
	private static final String specifyPlugin = new StringBuilder()
			.append(ChatColor.RED).append("Must specify a plugin!").toString();
	private static final String pluginNotFound = new StringBuilder()
			.append(ChatColor.RED).append("Plugin not found!").toString();

	private PlugMan plugin;

	public PlugManUtils(PlugMan instance) {
		super(instance);
		this.plugin = instance;
	}

	public void unloadCommand(CommandSender sender, String[] args) {
		if (args.length > 1) {
			String pl = consolidateArgs(args);
			Plugin targetPlugin = getPlugin(pl);
			if (targetPlugin == null)
				sender.sendMessage(new StringBuilder().append(pre)
						.append(ChatColor.RED).append(pluginNotFound)
						.toString());
			else
				sender.sendMessage(unloadPlugin(pl));
		} else {
			sender.sendMessage(new StringBuilder().append(pre)
					.append(ChatColor.RED).append(specifyPlugin).toString());
		}
	}

	public void reloadCommand(CommandSender sender, String[] args) {
		if (args.length > 1) {
			if (("all".equalsIgnoreCase(args[1]))
					|| ("*".equalsIgnoreCase(args[1]))) {
				for (Plugin pl : this.plugin.getServer().getPluginManager()
						.getPlugins()) {
					if ((!this.plugin.getSkipped().contains(pl.getName()))
							&& (!this.plugin.getSkipped().isEmpty())) {
						String pn = pl.getName();
						sender.sendMessage(unloadPlugin(pn));
						sender.sendMessage(loadPlugin(pn));
					}
				}
				sender.sendMessage(new StringBuilder().append(pre)
						.append(ChatColor.GREEN)
						.append("All plugins reloaded!").toString());
				return;
			}
			String pl = consolidateArgs(args);
			if (getPlugin(pl) != null) {
				sender.sendMessage(unloadPlugin(pl));
				sender.sendMessage(loadPlugin(pl));
			} else {
				sender.sendMessage(new StringBuilder().append(pre)
						.append(ChatColor.RED).append(pluginNotFound)
						.toString());
			}
		} else {
			sender.sendMessage(new StringBuilder().append(pre)
					.append(ChatColor.RED).append(specifyPlugin).toString());
		}
	}

	private Plugin getPlugin(String p) {
		for (Plugin pl : this.plugin.getServer().getPluginManager()
				.getPlugins()) {
			if (pl.getDescription().getName().equalsIgnoreCase(p)) {
				return pl;
			}
		}
		return null;
	}

	private String loadPlugin(String pl) {
		Plugin targetPlugin = null;
		String msg = "";
		File pluginDir = new File("plugins");
		if (!pluginDir.isDirectory()) {
			return new StringBuilder().append(pre).append(ChatColor.RED)
					.append("Plugin directory not found!").toString();
		}
		File pluginFile = new File(pluginDir, new StringBuilder().append(pl)
				.append(".jar").toString());

		if (!pluginFile.isFile()) {
			for (File f : pluginDir.listFiles())
				try {
					if (f.getName().endsWith(".jar")) {
						PluginDescriptionFile pdf = this.plugin
								.getPluginLoader().getPluginDescription(f);

						if (pdf.getName().equalsIgnoreCase(pl)) {
							pluginFile = f;
							msg = "(via search) ";
							break;
						}
					}
				} catch (InvalidDescriptionException e) {
					return new StringBuilder()
							.append(pre)
							.append(ChatColor.RED)
							.append("Couldn't find file and failed to search descriptions!")
							.toString();
				}
		}
		try {
			this.plugin.getServer().getPluginManager().loadPlugin(pluginFile);
			targetPlugin = getPlugin(pl);
			targetPlugin.onLoad();
			this.plugin.getServer().getPluginManager()
					.enablePlugin(targetPlugin);
			return new StringBuilder().append(pre).append(ChatColor.GREEN)
					.append(getPlugin(pl)).append(" loaded ").append(msg)
					.append("and enabled!").toString();
		} catch (UnknownDependencyException e) {
			return new StringBuilder().append(pre).append(ChatColor.RED)
					.append("File exists, but is missing a dependency!")
					.toString();
		} catch (InvalidPluginException e) {
			this.plugin.getLogger().log(Level.SEVERE,
					"Tried to load invalid Plugin.\n", e);
			return new StringBuilder().append(pre).append(ChatColor.RED)
					.append("File exists, but isn't a loadable plugin file!")
					.toString();
		} catch (InvalidDescriptionException e) {
		}
		return new StringBuilder().append(pre).append(ChatColor.RED)
				.append("Plugin exists, but has an invalid description!")
				.toString();
	}

	private String consolidateArgs(String[] args) {
		String pl = args[1];
		if (args.length > 2) {
			for (int i = 2; i < args.length; i++) {
				pl = new StringBuilder().append(pl).append(" ").append(args[i])
						.toString();
			}
		}
		return pl;
	}

	private String unloadPlugin(String pl) {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		SimplePluginManager spm = (SimplePluginManager) ((FakePluginManager) pm).oldManager;
		SimpleCommandMap cmdMap = null;
		List<?> plugins = null;
		Map<?, ?> names = null;
		Map<?, ?> commands = null;
		if (spm != null) {
			try {
				Field pluginsField = spm.getClass().getDeclaredField("plugins");
				pluginsField.setAccessible(true);
				plugins = (List<?>) pluginsField.get(spm);

				Field lookupNamesField = spm.getClass().getDeclaredField(
						"lookupNames");
				lookupNamesField.setAccessible(true);
				names = (Map<?, ?>) lookupNamesField.get(spm);
				try {
					Field listenersField = spm.getClass().getDeclaredField(
							"listeners");
					listenersField.setAccessible(true);
				} catch (Exception e) {
				}

				Field commandMapField = spm.getClass().getDeclaredField(
						"commandMap");
				commandMapField.setAccessible(true);
				cmdMap = (SimpleCommandMap) commandMapField.get(spm);

				Field knownCommandsField = cmdMap.getClass().getDeclaredField(
						"knownCommands");
				knownCommandsField.setAccessible(true);
				commands = (Map<?, ?>) knownCommandsField.get(cmdMap);
			} catch (NoSuchFieldException e) {
				return new StringBuilder().append(pre).append(ChatColor.RED)
						.append("Failed to unload plugin!").toString();
			} catch (IllegalAccessException e) {
				return new StringBuilder().append(pre).append(ChatColor.RED)
						.append("Failed to unload plugin!").toString();
			}
		}

		String tp = "";
		for (Plugin p : Bukkit.getServer().getPluginManager().getPlugins()) {
			if (p.getDescription().getName().equalsIgnoreCase(pl)) {
				pm.disablePlugin(p);
				tp = new StringBuilder().append(tp).append(p.getName())
						.append(" ").toString();
				if ((plugins != null) && (plugins.contains(p))) {
					plugins.remove(p);
				}

				if ((names != null) && (names.containsKey(pl))) {
					names.remove(pl);
				}

				/*
				 * if ((listeners != null) && (reloadlisteners)) { for (Object s
				 * : listeners.values()) { for (SortedSet set :
				 * listeners.values()){ RegisteredListener value =
				 * (RegisteredListener) it .next();
				 * 
				 * if (value.getPlugin() == p) it.remove(); } } }
				 */
				Iterator<?> it1;
				if (cmdMap != null) {
					for (it1 = commands.entrySet().iterator(); it1.hasNext();) {
						Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it1.next();
						if ((entry.getValue() instanceof PluginCommand)) {
							PluginCommand c = (PluginCommand) entry.getValue();
							if (c.getPlugin() == p) {
								c.unregister(cmdMap);
								it1.remove();
							}
						}
					}
				}
			}
		}
		return new StringBuilder().append(pre).append(ChatColor.GREEN)
				.append(tp).append("has been unloaded and disabled!")
				.toString();
	}
}
