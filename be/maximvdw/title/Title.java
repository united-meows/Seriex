package be.maximvdw.title;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.Seriex;

/**
 * Minecraft 1.8 (1.9) Title
 * 
 * @version 1.0.5
 *
 * @author Maxim Van de Wynckel
 */
@SuppressWarnings("all")
public class Title {
	/* Title packet */
	private static Class<?> packetTitle;
	/* Title packet actions ENUM */
	private static Class<?> packetActions;
	/* Chat serializer */
	private static Class<?> nmsChatSerializer;
	private static Class<?> chatBaseComponent;
	/* Title text and color */
	private String title = "";
	private ChatColor titleColor = ChatColor.WHITE;
	/* Subtitle text and color */
	private String subtitle = "";
	private ChatColor subtitleColor = ChatColor.WHITE;
	/* Title timings */
	private int fadeInTime = -1;
	private int stayTime = -1;
	private int fadeOutTime = -1;
	private boolean ticks = false;
	private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<>();

	public Title() {
		loadClasses();
	}

	/**
	 * Create a new 1.8 title
	 * 
	 * @param title
	 *              Title
	 */
	public Title(final String title) {
		this.title = title;
		loadClasses();
	}

	/**
	 * Create a new 1.8 title
	 * 
	 * @param title
	 *                 Title text
	 * @param subtitle
	 *                 Subtitle text
	 */
	public Title(final String title, final String subtitle) {
		this.title = title;
		this.subtitle = subtitle;
		loadClasses();
	}

	/**
	 * Copy 1.8 title
	 * 
	 * @param title
	 *              Title
	 */
	public Title(final Title title) {
		// Copy title
		this.title = title.getTitle();
		this.subtitle = title.getSubtitle();
		this.titleColor = title.getTitleColor();
		this.subtitleColor = title.getSubtitleColor();
		this.fadeInTime = title.getFadeInTime();
		this.fadeOutTime = title.getFadeOutTime();
		this.stayTime = title.getStayTime();
		this.ticks = title.isTicks();
		loadClasses();
	}

	/**
	 * Create a new 1.8 title
	 * 
	 * @param title
	 *                    Title text
	 * @param subtitle
	 *                    Subtitle text
	 * @param fadeInTime
	 *                    Fade in time
	 * @param stayTime
	 *                    Stay on screen time
	 * @param fadeOutTime
	 *                    Fade out time
	 */
	public Title(final String title, final String subtitle, final int fadeInTime, final int stayTime, final int fadeOutTime) {
		this.title = title;
		this.subtitle = subtitle;
		this.fadeInTime = fadeInTime;
		this.stayTime = stayTime;
		this.fadeOutTime = fadeOutTime;
		loadClasses();
	}

	/**
	 * Load spigot and NMS classes
	 */
	private void loadClasses() {
		if (packetTitle == null) {
			packetTitle = getNMSClass("PacketPlayOutTitle");
			packetActions = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
			chatBaseComponent = getNMSClass("IChatBaseComponent");
			nmsChatSerializer = getNMSClass("ChatComponentText");
		}
	}

	/**
	 * Set title text
	 * 
	 * @param title
	 *              Title
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Get title text
	 * 
	 * @return Title text
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Set subtitle text
	 * 
	 * @param subtitle
	 *                 Subtitle text
	 */
	public void setSubtitle(final String subtitle) {
		this.subtitle = subtitle;
	}

	/**
	 * Get subtitle text
	 * 
	 * @return Subtitle text
	 */
	public String getSubtitle() {
		return this.subtitle;
	}

	/**
	 * Set the title color
	 * 
	 * @param color
	 *              Chat color
	 */
	public void setTitleColor(final ChatColor color) {
		this.titleColor = color;
	}

	/**
	 * Set the subtitle color
	 * 
	 * @param color
	 *              Chat color
	 */
	public void setSubtitleColor(final ChatColor color) {
		this.subtitleColor = color;
	}

	/**
	 * Set title fade in time
	 * 
	 * @param time
	 *             Time
	 */
	public void setFadeInTime(final int time) {
		this.fadeInTime = time;
	}

	/**
	 * Set title fade out time
	 * 
	 * @param time
	 *             Time
	 */
	public void setFadeOutTime(final int time) {
		this.fadeOutTime = time;
	}

	/**
	 * Set title stay time
	 * 
	 * @param time
	 *             Time
	 */
	public void setStayTime(final int time) {
		this.stayTime = time;
	}

	/**
	 * Set timings to ticks
	 */
	public void setTimingsToTicks() {
		ticks = true;
	}

	/**
	 * Set timings to seconds
	 */
	public void setTimingsToSeconds() {
		ticks = false;
	}

	/**
	 * Send the title to a player
	 * 
	 * @param player
	 *               Player
	 */
	public void send(final Player player) {
		if (packetTitle != null) {
			// First reset previous settings
			resetTitle(player);
			try {
				// Send timings first
				final Object handle = getHandle(player);
				if (handle == null) {
					Seriex.get().logger().error("Handle is null @ Title!");
					return;
				}
				final Field field = getField(handle.getClass(), "playerConnection");
				if (field == null) {
					Seriex.get().logger().error("Field is null @ Title!");
					return;
				}
				final Object connection = field.get(handle);
				final Object[] actions = packetActions.getEnumConstants();
				final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
				if (sendPacket == null) {
					Seriex.get().logger().error("Method is null @ Title!");
					return;
				}
				Object packet = packetTitle.getConstructor(packetActions, chatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(actions[2], null, fadeInTime * (ticks ? 1 : 20),
							stayTime * (ticks ? 1 : 20), fadeOutTime * (ticks ? 1 : 20));
				// Send if set
				if (fadeInTime != -1 && fadeOutTime != -1 && stayTime != -1) {
					sendPacket.invoke(connection, packet);
				}
				// Send title
				Object serialized = nmsChatSerializer.getConstructor(String.class).newInstance(ChatColor.translateAlternateColorCodes('&', title));
				packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[0], serialized);
				sendPacket.invoke(connection, packet);
				if (!"".equals(subtitle)) {
					// Send subtitle if present
					serialized = nmsChatSerializer.getConstructor(String.class).newInstance(ChatColor.translateAlternateColorCodes('&', subtitle));
					packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[1], serialized);
					sendPacket.invoke(connection, packet);
				}
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void updateTimes(final Player player) {
		if (Title.packetTitle != null) {
			try {
				final Object handle = getHandle(player);
				if (handle != null) {
					final Field field = getField(handle.getClass(), "playerConnection");
					if (field != null) {
						final Object connection = field.get(handle);
						final Object[] actions = Title.packetActions.getEnumConstants();
						final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
						final Object packet = Title.packetTitle.getConstructor(Title.packetActions, chatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(actions[2], null,
									Integer.valueOf(this.fadeInTime * (this.ticks ? 1 : 20)), Integer.valueOf(this.stayTime * (this.ticks ? 1 : 20)), Integer.valueOf(this.fadeOutTime * (this.ticks ? 1 : 20)));
						if (sendPacket != null && this.fadeInTime != -1 && this.fadeOutTime != -1 && this.stayTime != -1) {
							sendPacket.invoke(connection, packet);
						}
					}
				}
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void updateTitle(final Player player) {
		if (Title.packetTitle != null) {
			try {
				final Object handle = getHandle(player);
				final Object connection = getField(handle.getClass(), "playerConnection").get(handle);
				final Object[] actions = Title.packetActions.getEnumConstants();
				final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
				final Object serialized = nmsChatSerializer.getConstructor(String.class).newInstance(ChatColor.translateAlternateColorCodes('&', this.title));
				final Object packet = Title.packetTitle.getConstructor(Title.packetActions, chatBaseComponent).newInstance(actions[0], serialized);
				sendPacket.invoke(connection, packet);
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void updateSubtitle(final Player player) {
		if (Title.packetTitle != null) {
			try {
				final Object handle = getHandle(player);
				final Object connection = getField(handle.getClass(), "playerConnection").get(handle);
				final Object[] actions = Title.packetActions.getEnumConstants();
				final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
				final Object serialized = nmsChatSerializer.getConstructor(String.class).newInstance(ChatColor.translateAlternateColorCodes('&', this.subtitle));
				final Object packet = Title.packetTitle.getConstructor(Title.packetActions, chatBaseComponent).newInstance(actions[1], serialized);
				sendPacket.invoke(connection, packet);
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Broadcast the title to all players
	 */
	public void broadcast() {
		for (final Player p : Bukkit.getOnlinePlayers()) {
			send(p);
		}
	}

	/**
	 * Clear the title
	 * 
	 * @param player
	 *               Player
	 */
	public void clearTitle(final Player player) {
		try {
			// Send timings first
			final Object handle = getHandle(player);
			final Object connection = getField(handle.getClass(), "playerConnection").get(handle);
			final Object[] actions = packetActions.getEnumConstants();
			final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
			final Object packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[3], null);
			sendPacket.invoke(connection, packet);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reset the title settings
	 * 
	 * @param player
	 *               Player
	 */
	public void resetTitle(final Player player) {
		try {
			// Send timings first
			final Object handle = getHandle(player);
			final Object connection = getField(handle.getClass(), "playerConnection").get(handle);
			final Object[] actions = packetActions.getEnumConstants();
			final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
			final Object packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[4], null);
			sendPacket.invoke(connection, packet);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private Class<?> getPrimitiveType(final Class<?> clazz) {
		return CORRESPONDING_TYPES.containsKey(clazz) ? CORRESPONDING_TYPES.get(clazz) : clazz;
	}

	private Class<?>[] toPrimitiveTypeArray(final Class<?>[] classes) {
		final int a = classes != null ? classes.length : 0;
		final Class<?>[] types = new Class<?>[a];
		for (int i = 0; i < a; i++) {
			types[i] = getPrimitiveType(classes[i]);
		}
		return types;
	}

	private static boolean equalsTypeArray(final Class<?>[] a, final Class<?>[] o) {
		if (a.length != o.length) return false;
		for (int i = 0; i < a.length; i++) if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i])) return false;
		return true;
	}

	private Object getHandle(final Object obj) {
		try {
			return getMethod("getHandle", obj.getClass()).invoke(obj);
		}
		catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Method getMethod(final String name, final Class<?> clazz, final Class<?>... paramTypes) {
		final Class<?>[] t = toPrimitiveTypeArray(paramTypes);
		for (final Method m : clazz.getMethods()) {
			final Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
			if (m.getName().equals(name) && equalsTypeArray(types, t)) return m;
		}
		return null;
	}

	private String getVersion() {
		final String name = Bukkit.getServer().getClass().getPackage().getName();
		return name.substring(name.lastIndexOf('.') + 1) + ".";
	}

	private Class<?> getNMSClass(final String className) {
		final String fullName = "net.minecraft.server." + getVersion() + className;
		Class<?> clazz = null;
		try {
			clazz = Class.forName(fullName);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		return clazz;
	}

	private Field getField(final Class<?> clazz, final String name) {
		try {
			final Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		}
		catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Method getMethod(final Class<?> clazz, final String name, final Class<?>... args) {
		for (final Method m : clazz.getMethods()) if (m.getName().equals(name) && (args.length == 0 || ClassListEqual(args, m.getParameterTypes()))) {
			m.setAccessible(true);
			return m;
		}
		return null;
	}

	private boolean ClassListEqual(final Class<?>[] l1, final Class<?>[] l2) {
		boolean equal = true;
		if (l1.length != l2.length) return false;
		for (int i = 0; i < l1.length; i++) if (l1[i] != l2[i]) {
			equal = false;
			break;
		}
		return equal;
	}

	public ChatColor getTitleColor() {
		return titleColor;
	}

	public ChatColor getSubtitleColor() {
		return subtitleColor;
	}

	public int getFadeInTime() {
		return fadeInTime;
	}

	public int getFadeOutTime() {
		return fadeOutTime;
	}

	public int getStayTime() {
		return stayTime;
	}

	public boolean isTicks() {
		return ticks;
	}
}
