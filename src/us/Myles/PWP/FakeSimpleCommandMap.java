package us.Myles.PWP;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.util.Java15Compat;

public class FakeSimpleCommandMap extends SimpleCommandMap {
	public SimpleCommandMap oldMap;
	private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", 16);

	public FakeSimpleCommandMap(SimpleCommandMap oldCommandMap) {
		super(Bukkit.getServer());
		oldMap = oldCommandMap;
		// Lazy mode activated!
		for (Field f : oldMap.getClass().getDeclaredFields()) {
			try {
				if (this.getClass().getSuperclass().getDeclaredField(f.getName()) != null) {
					transferValue(f.getName(), oldMap);
				}
			} catch (Exception e) {
				Bukkit.getServer().getLogger().log(Level.SEVERE,
						"PerWorldPlugins failed finding fields in the CommandMap, contact the Dev on BukkitDev", e);
			}
		}
	}

	private void transferValue(String field, SimpleCommandMap oldMap) {
		// Happy Function yay! Transfer data from old PM to new!
		try {
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);

			Field oldField = oldMap.getClass().getDeclaredField(field);
			oldField.setAccessible(true);
			modifiers.setInt(oldField, oldField.getModifiers() & ~Modifier.FINAL);
			Object oldObject = oldField.get(oldMap);
			Field newField = this.getClass().getSuperclass().getDeclaredField(field);
			newField.setAccessible(true);
			modifiers.setInt(newField, newField.getModifiers() & ~Modifier.FINAL);
			newField.set(this, oldObject);
		} catch (Exception e) {
			Bukkit.getServer().getLogger().log(Level.SEVERE,
					"PerWorldPlugins failed transferring CommandMap, contact the Dev on BukkitDev", e);
		}

	}

	@Override
	public boolean dispatch(CommandSender sender, String commandLine) throws CommandException {
		String[] args = PATTERN_ON_SPACE.split(commandLine);
		if (args.length == 0) {
			return false;
		}
		String sentCommandLabel = args[0].toLowerCase();
		Command target = getCommand(sentCommandLabel);
		if (target == null) {
			return false;
		}
		try {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				org.bukkit.plugin.Plugin plugin = null;
				if (target instanceof PluginIdentifiableCommand) {
					PluginIdentifiableCommand t = (PluginIdentifiableCommand) target;
					if (!Plugin.instance.checkWorld(t.getPlugin(), p.getWorld()))
						plugin = t.getPlugin();
				}
				/*
				 * HANDLE MCORE, This is really a workaround as they don't
				 * implement PluginIdentifiableCommand
				 */
				if (target.getClass().getSimpleName().equals("MCoreBukkitCommand")) {
					if (Bukkit.getPluginManager().getPlugin("mcore") != null)
						plugin = Bukkit.getPluginManager().getPlugin("mcore");
				}
				if (!Plugin.instance.checkWorld(plugin, p.getWorld())) {
					p.sendMessage(Plugin.instance.blockedMessage.replace("%world%", p.getWorld().getName()).replace(
							"%player%", p.getName()).replace("%plugin%", plugin.getName()).replace("&",
							ChatColor.COLOR_CHAR + ""));
					return true;
				}
			}
			target.execute(sender, sentCommandLabel, (String[]) Java15Compat.Arrays_copyOfRange(args, 1, args.length));
		} catch (CommandException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new CommandException("Unhandled exception executing '" + commandLine + "' in " + target, ex);
		}
		return true;
	}
}
