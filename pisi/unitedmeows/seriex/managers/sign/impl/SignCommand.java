package pisi.unitedmeows.seriex.managers.sign.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

// lol get formatted
public class SignCommand {
	private String trigger;
	private SignCommand instance;
	private String[] expectedParams;
	private BiConsumer<PlayerW, SignCommand> leftClick;
	private BiConsumer<PlayerW, SignCommand> rightClick;
	private Consumer<SignCommand> tick;
	private Cache<Sign, Map<String, Object>> session;
	private Cache<String, Object> global;
	private int runnable , countdown;
	private boolean counting;

	public SignCommand(String _trigger) {
		session = Caffeine.newBuilder().maximumSize(50).expireAfterWrite(Duration.ofMinutes(2)).build();
		global = Caffeine.newBuilder().maximumSize(50).expireAfterWrite(Duration.ofMinutes(10)).build();
		trigger = _trigger;
		instance = this;
	}

	public void runLeft(PlayerW playerW, Sign sign, org.bukkit.material.Sign signMaterial) {
		if (leftClick != null) {
			if (session.getIfPresent(sign) == null) {
				session.put(sign, new HashMap<>());
			}
			global.put("last_clicked", playerW);
			global.put("current_sign", sign);
			global.put("current_signMaterial", signMaterial);
			global.put("last_use", System.currentTimeMillis());
			leftClick.accept(playerW, this);
		}
	}

	public void runRight(PlayerW playerW, Sign sign, org.bukkit.material.Sign signMaterial) {
		if (rightClick != null) {
			if (session.getIfPresent(sign) == null) {
				session.put(sign, new HashMap<>());
			}
			global.put("last_clicked", playerW);
			global.put("current_sign", sign);
			global.put("current_signMaterial", signMaterial);
			global.put("last_use", System.currentTimeMillis());
			if (counting) {
				final Sign block = (Sign) global().getIfPresent("current_sign");
				int cooldown = (int) session(block).getOrDefault("cooldown", 0);
				if (cooldown == 0) {
					session(block).put("cooldown", countdown);
				}
			}
			rightClick.accept(playerW, this);
		}
	}

	public SignCommand onLeft(BiConsumer<PlayerW, SignCommand> _leftClick) {
		leftClick = _leftClick;
		return this;
	}

	public SignCommand onRight(BiConsumer<PlayerW, SignCommand> _rightClick) {
		rightClick = _rightClick;
		return this;
	}

	public SignCommand tick(Consumer<SignCommand> _tick, int interval) {
		tick = _tick;
		if (runnable != -1) {
			Bukkit.getScheduler().cancelTask(runnable); // why?
		}
		runnable = Bukkit.getScheduler().scheduleSyncRepeatingTask(Seriex.get(), new BukkitRunnable() {
			@Override
			public void run() {
				if (counting) {
					try (Stream<Map<String, Object>> filter = session().asMap().values().stream().filter(map -> map.getOrDefault("cooldown", null) != null)) {
						Optional<Map<String, Object>> optional = filter.findFirst();
						if (optional.isPresent()) {
							Map<String, Object> map = optional.get();
							int cooldown = (int) map.get("cooldown");
							if (cooldown > 0) {
								map.put("cooldown", cooldown - 1);
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						Seriex.logger().fatal("Couldnt create stream!");
					}
				}
				tick.accept(instance);
			}
		}, interval, interval);
		return this;
	}

	public void close() {
		if (runnable != -1) {
			Bukkit.getScheduler().cancelTask(runnable);
		}
	}

	public Map<String, Object> session(Sign sign) {
		return session.getIfPresent(sign);
	}

	public Cache<Sign, Map<String, Object>> session() {
		return session;
	}

	public Cache<String, Object> global() {
		return global;
	}

	protected static String get(int index, Sign sign) {
		return sign.getLine(index);
	}

	public String trigger() {
		return trigger;
	}

	public SignCommand counting(int cooldown) {
		this.countdown = cooldown;
		counting ^= true;
		return this;
	}
}
