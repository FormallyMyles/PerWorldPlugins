package us.Myles.PWP;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PWPCommandExecutor implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] args) {
		if(sender.isOp() || sender.hasPermission("pwp.admin")){
			if(args.length == 0){
				sender.sendMessage("Usage: /pwp reload|version|update");
			}else
			{
				if(args[0].equalsIgnoreCase("reload")){
					Plugin.instance.reloadConfig();
					Plugin.instance.loadConfig();
					sender.sendMessage(ChatColor.GREEN + "Reloaded PerWorldPlugins Config!");
				}
				if(args[0].equalsIgnoreCase("version")){
					sender.sendMessage(ChatColor.GREEN + "Running v" + Plugin.instance.getDescription().getVersion() + " of PerWorldPlugins.");
				}
				if(args[0].equalsIgnoreCase("update")){
					if(Plugin.instance.hasUpdate()){
						sender.sendMessage(ChatColor.GREEN + "Started downloading, Check console for results then reload/reboot.");
						Plugin.instance.update();
					}else
					{
						sender.sendMessage(ChatColor.GREEN + "No updates currently avaliable");
					}
				}
			}
		}else
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to use this command. (Permission node: pwp.admin)");
		}
		return true;
	}

}
