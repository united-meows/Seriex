package pisi.unitedmeows.seriex.managers.sign.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class SignCommand {
	private String trigger;
	private SignCommand instance;
	private String[] expectedParams;
	private BiConsumer<PlayerW, SignCommand> leftClick;
	private BiConsumer<PlayerW, SignCommand> rightClick;
	private Consumer<SignCommand> tick;
	private Cache<Sign, Map<String, Object>> session;
	private Cache<String, Object> global;
	private int runnable, countdownRunnable, countdown;
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
			if (counting) {
				int cooldown = (int) session(sign).getOrDefault("cooldown", 0);
				if (cooldown == 0) {
					session(sign).put("cooldown", countdown);
					leftClick.accept(playerW, this);
				}
			} else leftClick.accept(playerW, this);
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
				int cooldown = (int) session(sign).getOrDefault("cooldown", 0);
				if (cooldown == 0) {
					session(sign).put("cooldown", countdown);
					rightClick.accept(playerW, this);
				}
			} else rightClick.accept(playerW, this);
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
		runnable = Bukkit.getScheduler().scheduleSyncRepeatingTask(Seriex.get().plugin(), new BukkitRunnable() {
			@Override
			public void run() {
				tick.accept(instance);
			}
		}, interval, interval);
		return this;
	}

	public void close() {
		if (runnable != -1) {
			Bukkit.getScheduler().cancelTask(runnable);
		}
		if (countdownRunnable != -1) {
			Bukkit.getScheduler().cancelTask(countdownRunnable);
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
		this.counting = true;
		this.countdownRunnable = Bukkit.getScheduler().scheduleSyncRepeatingTask(Seriex.get().plugin(), new BukkitRunnable() {
			@Override
			public void run() {
				session().asMap().forEach((Sign sign, Map<String, Object> data) -> {
					int cooldown = (int) data.getOrDefault("cooldown", -1);
					if(cooldown < 0) {
						Seriex.get().logger().error("Cooldown is not present for the sign at: {}", sign.getBlock().getLocation());
						return;
					}

					if (cooldown > 0) {
						data.replace("cooldown", cooldown - 1);
						sign.setLine(3, String.format("In cooldown... (%d)", cooldown - 1));
					} else sign.setLine(3, "");

					Seriex.get().run(sign::update);
				});
			}
		}, 20, 1);
		return this;
	}
}
