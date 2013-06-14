package us.Myles.PWP;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;

public class FakePluginManager implements PluginManager {
	public PluginManager oldManager;
	public Boolean INJECTED_CLASS = true;

	public FakePluginManager(PluginManager oldManager) {
		this.oldManager = oldManager;
	}

	@Override
	public void addPermission(Permission arg0) {
		this.oldManager.addPermission(arg0);
	}

	@Override
	public void callEvent(Event event) {
		if (event.isAsynchronous()) {
			if (Thread.holdsLock(this)) {
				throw new IllegalStateException(
						event.getEventName()
								+ " cannot be triggered asynchronously from inside synchronized code.");
			}
			if (Bukkit.getServer().isPrimaryThread()) {
				throw new IllegalStateException(
						event.getEventName()
								+ " cannot be triggered asynchronously from primary server thread.");
			}
			fireEvent(event);
		} else {
			synchronized (this) {
				fireEvent(event);
			}
		}
	}

	private void fireEvent(Event event) {
		HandlerList handlers = event.getHandlers();
		RegisteredListener[] listeners = handlers.getRegisteredListeners();

		for (RegisteredListener registration : listeners)
			if (registration.getPlugin().isEnabled()) {
				try {
					// Control If We should call it, world dependent
					if (shouldCall(registration.getPlugin(), event)) {
						registration.callEvent(event);
					}
				} catch (AuthorNagException ex) {
					Plugin plugin = registration.getPlugin();
					if (plugin.isNaggable()) {
						plugin.setNaggable(false);
						Bukkit.getServer()
								.getLogger()
								.log(Level.SEVERE,
										String.format(
												"Nag author(s): '%s' of '%s' about the following: %s",
												new Object[] {
														plugin.getDescription()
																.getAuthors(),
														plugin.getDescription()
																.getFullName(),
														ex.getMessage() }));
					}
				} catch (Throwable ex) {
					Bukkit.getServer()
							.getLogger()
							.log(Level.SEVERE,
									"Could not pass event "
											+ event.getEventName()
											+ " to "
											+ registration.getPlugin()
													.getDescription()
													.getFullName(), ex);
				}
			}
	}

	private boolean shouldCall(Plugin plugin, Event e) {
		if (e instanceof PlayerEvent) {
			PlayerEvent e1 = (PlayerEvent) e;
			// Check if exempt
			if(us.Myles.PWP.Plugin.instance.exemptEvents.contains(e.getClass()) && us.Myles.PWP.Plugin.instance.isExemptEnabled()){
				return true;
			}
			return checkWorld(plugin, e1.getPlayer().getWorld());
		}
		if (e instanceof BlockEvent) {
			BlockEvent e1 = (BlockEvent) e;
			return checkWorld(plugin, e1.getBlock().getWorld());
		}
		if (e instanceof InventoryEvent) {
			InventoryEvent e1 = (InventoryEvent) e;
			return checkWorld(plugin, e1.getView().getPlayer().getWorld());
		}
		if (e instanceof EntityEvent) {
			EntityEvent e1 = (EntityEvent) e;
			return checkWorld(plugin, e1.getEntity().getWorld());
		}
		if (e instanceof HangingEvent) {
			HangingEvent e1 = (HangingEvent) e;
			return checkWorld(plugin, e1.getEntity().getWorld());
		}
		if (e instanceof VehicleEvent) {
			VehicleEvent e1 = (VehicleEvent) e;
			return checkWorld(plugin, e1.getVehicle().getWorld());
		}
		if (e instanceof WeatherEvent) {
			WeatherEvent e1 = (WeatherEvent) e;
			return checkWorld(plugin, e1.getWorld());
		}
		if (e instanceof WorldEvent) {
			WorldEvent e1 = (WorldEvent) e;
			return checkWorld(plugin, e1.getWorld());
		}
		if (e instanceof ServerEvent) {
			// We can't really control server events because they don't involve
			// the world!
			return true;
		}
		return true;
	}

	private boolean checkWorld(Plugin plugin, World world) {
		return us.Myles.PWP.Plugin.instance.checkWorld(plugin, world);
	}

	@Override
	public void clearPlugins() {
		this.oldManager.clearPlugins();
	}

	@Override
	public void disablePlugin(Plugin arg0) {
		this.oldManager.disablePlugin(arg0);
	}

	@Override
	public void disablePlugins() {
		this.oldManager.disablePlugins();
	}

	@Override
	public void enablePlugin(Plugin arg0) {
		this.oldManager.enablePlugin(arg0);
	}

	@Override
	public Set<Permissible> getDefaultPermSubscriptions(boolean arg0) {
		return this.oldManager.getDefaultPermSubscriptions(arg0);
	}

	@Override
	public Set<Permission> getDefaultPermissions(boolean arg0) {
		return this.oldManager.getDefaultPermissions(arg0);
	}

	@Override
	public Permission getPermission(String arg0) {
		return this.oldManager.getPermission(arg0);
	}

	@Override
	public Set<Permissible> getPermissionSubscriptions(String arg0) {
		return this.oldManager.getPermissionSubscriptions(arg0);
	}

	@Override
	public Set<Permission> getPermissions() {
		return this.oldManager.getPermissions();
	}

	@Override
	public Plugin getPlugin(String arg0) {
		return this.oldManager.getPlugin(arg0);
	}

	@Override
	public Plugin[] getPlugins() {
		return this.oldManager.getPlugins();
	}

	@Override
	public boolean isPluginEnabled(String arg0) {
		return this.oldManager.isPluginEnabled(arg0);
	}

	@Override
	public boolean isPluginEnabled(Plugin arg0) {
		return this.oldManager.isPluginEnabled(arg0);
	}

	@Override
	public Plugin loadPlugin(File arg0) throws InvalidPluginException,
			InvalidDescriptionException, UnknownDependencyException {
		return this.oldManager.loadPlugin(arg0);
	}

	@Override
	public Plugin[] loadPlugins(File arg0) {
		return this.oldManager.loadPlugins(arg0);
	}

	@Override
	public void recalculatePermissionDefaults(Permission arg0) {
		this.oldManager.recalculatePermissionDefaults(arg0);
	}

	@Override
	public void registerEvent(Class<? extends Event> arg0, Listener arg1,
			EventPriority arg2, EventExecutor arg3, Plugin arg4) {
		this.oldManager.registerEvent(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public void registerEvent(Class<? extends Event> arg0, Listener arg1,
			EventPriority arg2, EventExecutor arg3, Plugin arg4, boolean arg5) {
		this.oldManager.registerEvent(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	@Override
	public void registerEvents(Listener arg0, Plugin arg1) {
		this.oldManager.registerEvents(arg0, arg1);
	}

	@Override
	public void registerInterface(Class<? extends PluginLoader> arg0)
			throws IllegalArgumentException {
		this.oldManager.registerInterface(arg0);
	}

	@Override
	public void removePermission(Permission arg0) {
		this.oldManager.removePermission(arg0);
	}

	@Override
	public void removePermission(String arg0) {
		this.oldManager.removePermission(arg0);
	}

	@Override
	public void subscribeToDefaultPerms(boolean arg0, Permissible arg1) {
		this.oldManager.subscribeToDefaultPerms(arg0, arg1);
	}

	@Override
	public void subscribeToPermission(String arg0, Permissible arg1) {
		this.oldManager.subscribeToPermission(arg0, arg1);
	}

	@Override
	public void unsubscribeFromDefaultPerms(boolean arg0, Permissible arg1) {
		this.oldManager.unsubscribeFromDefaultPerms(arg0, arg1);
	}

	@Override
	public void unsubscribeFromPermission(String arg0, Permissible arg1) {
		this.oldManager.unsubscribeFromPermission(arg0, arg1);
	}

	@Override
	public boolean useTimings() {
		return this.oldManager.useTimings();
	}

}
