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
	public Boolean INJECTED_CLASS = true;
	public FakeSimpleCommandMap(SimpleCommandMap oldCommandMap) {
		super(Bukkit.getServer());
		oldMap = oldCommandMap;
		try {
			Field knownCMDs = oldMap.getClass().getDeclaredField("knownCommands");
			knownCMDs.setAccessible(true);
			Field modifiers = Field.class.getDeclaredField("modifiers");
		    modifiers.setAccessible(true);
			modifiers.setInt(knownCMDs, knownCMDs.getModifiers() & ~Modifier.FINAL);
			knownCMDs.set(this, knownCMDs.get(oldMap));
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Field aliasesField = oldMap.getClass().getDeclaredField("aliases");
			aliasesField.setAccessible(true);
			Field modifiers = Field.class.getDeclaredField("modifiers");
		    modifiers.setAccessible(true);
			modifiers.setInt(aliasesField, aliasesField.getModifiers() & ~Modifier.FINAL);
			aliasesField.set(this, aliasesField.get(oldMap));
		} catch (NoSuchFieldException | SecurityException e) {
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
			if (target instanceof PluginIdentifiableCommand && sender instanceof Player) {
				Player p = (Player) sender;
				PluginIdentifiableCommand t = (PluginIdentifiableCommand) target;
				if (!Plugin.instance.checkWorld(t.getPlugin(), p.getWorld())) {
					p.sendMessage(ChatColor.RED
							+ "[Error] This command cannot be performed in this world.");
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
