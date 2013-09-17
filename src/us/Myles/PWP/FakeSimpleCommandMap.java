package us.Myles.PWP;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
				if (this.getClass().getSuperclass()
						.getDeclaredField(f.getName()) != null) {
					transferValue(f.getName(), oldMap);
				}
			} catch (NoSuchFieldException e) {
				System.out
						.println("Can't find field "
								+ f.getName()
								+ " in FakeSimpleCommandMap, bug MylesC to update his plugin!");
			} catch (SecurityException e) {

			}
		}
	}

	private void transferValue(String field, SimpleCommandMap oldMap) {
		// Happy Function yay! Transfer data from old PM to new!
		try {
			Field f = oldMap.getClass().getDeclaredField(field);
			f.setAccessible(true);
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			Object o = f.get(oldMap);
			Field f1 = this.getClass().getSuperclass().getDeclaredField(field);
			f1.setAccessible(true);
			Field modifiers1 = Field.class.getDeclaredField("modifiers");
			modifiers1.setAccessible(true);
			modifiers1.setInt(f1, f1.getModifiers() & ~Modifier.FINAL);
			f1.set(this, o);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean dispatch(CommandSender sender, String commandLine)
			throws CommandException {

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
			if (target instanceof PluginIdentifiableCommand
					&& sender instanceof Player) {
				Player p = (Player) sender;
				PluginIdentifiableCommand t = (PluginIdentifiableCommand) target;
				if (!Plugin.instance.checkWorld(t.getPlugin(), p.getWorld())) {
					p.sendMessage(Plugin.instance.blockedMessage
							.replace("%world%", p.getWorld().getName())
							.replace("%player%", p.getName())
							.replace("%plugin%", t.getPlugin().getName())
							.replace("&", ChatColor.COLOR_CHAR + ""));
					return true;
				}
			}
			target.execute(sender, sentCommandLabel, (String[]) Java15Compat
					.Arrays_copyOfRange(args, 1, args.length));
		} catch (CommandException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new CommandException("Unhandled exception executing '"
					+ commandLine + "' in " + target, ex);
		}

		return true;
	}
}
