package us.Myles.PWP;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class HandleListeners implements Listener {
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		if(e.getPlayer().isOp() || e.getPlayer().hasPermission("pwp.admin")){
			if(Plugin.instance.hasUpdate()){
				e.getPlayer().sendMessage(ChatColor.GREEN + "[PerWorldPlugins] A New Update is avaliable type /pwp update to automatically update.");
			}
		}
	}
}
