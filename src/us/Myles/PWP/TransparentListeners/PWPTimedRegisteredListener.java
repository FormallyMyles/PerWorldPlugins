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
import org.bukkit.plugin.TimedRegisteredListener;

public class PWPTimedRegisteredListener extends TimedRegisteredListener {

	public PWPTimedRegisteredListener(Listener pluginListener, EventExecutor eventExecutor,
			EventPriority eventPriority, Plugin registeredPlugin, boolean listenCancelled) {
		super(pluginListener, eventExecutor, eventPriority, registeredPlugin, listenCancelled);
	}

	public void callEvent(Event event) throws EventException {
		/* PWP */
		if (!us.Myles.PWP.Plugin.instance.checkWorld(getPlugin(), event))
			return;
		try {
			super.callEvent(event);
		} catch (Throwable ex) {
			Bukkit.getServer().getLogger().log(
					Level.SEVERE,
					"**** THIS IS NOT AN ISSUE TODO WITH PER WORLD PLUGINS ****\nCould not pass event "
							+ event.getEventName() + " to " + getPlugin().getDescription().getFullName(), ex);
		}
		/* PWP OVER */
	}

}
