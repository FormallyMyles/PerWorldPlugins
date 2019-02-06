package us.Myles.PWP;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PWPCommandExecutor implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (sender.isOp() || sender.hasPermission("pwp.admin")) {
			if (args.length == 0) {
				sender.sendMessage(Plugin.color("&c[&4PWP&c] &fUsage: &7/pwp reload|version"));
			} else {
				if (args[0].equalsIgnoreCase("reload")) {
					Plugin.instance.reload();
					sender.sendMessage(Plugin.color("&a[&2PWP&a] &fPerWorldPlugins successfully reloaded!"));
					if (sender instanceof Player){
						Player p = (Player) sender;
						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
					}
				}else
				if (args[0].equalsIgnoreCase("version")) {
					sender.sendMessage(Plugin.color("&a[&2PWP&a] &fYou are currently running version &l" + Plugin.instance.getDescription().getVersion() + "&f of PerWorldPlugins."));
				}else{
					sender.sendMessage(Plugin.color("&c[&4PWP&c] &fUsage: &7/pwp reload|version"));
				}
				if(args.length >= 2){
					sender.sendMessage(Plugin.color("&c[&4PWP&c] &fUsage: &7/pwp reload|version"));
				}
			}
		} else {
			sender.sendMessage(Plugin.color("&c[&4PWP&c] &fNo permission! &7(Required node: &opwp.admin&7)"));
		}
		return true;
	}

}
