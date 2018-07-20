/**
 * Created on 17 May 2014 by _MylesC
 * Copyright 2014
 */
package us.Myles.PWP.TransparentListeners;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class PWPRegisteredListener extends RegisteredListener {
	public PWPRegisteredListener(Listener listener, EventExecutor executor, EventPriority priority, Plugin plugin,
			boolean ignoreCancelled) {
		super(listener, executor, priority, plugin, ignoreCancelled);
	}

	public void callEvent(Event event) throws EventException {
		/* PWP */
		if (!us.Myles.PWP.Plugin.instance.checkWorld(super.getPlugin(), event))
			return;
		/* PWP OVER */
		try {
			super.callEvent(event);
		} catch (Throwable ex) {
			Bukkit.getServer().getLogger().log(
					Level.SEVERE,
					"**** THIS IS NOT AN ISSUE TO DO WITH PER WORLD PLUGINS ****\nCould not pass event "
							+ event.getEventName() + " to " + getPlugin().getDescription().getFullName(), ex);
		}
		/* PWP OVER */
	}

}
