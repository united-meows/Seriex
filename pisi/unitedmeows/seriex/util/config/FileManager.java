package pisi.unitedmeows.seriex.util.config;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

import dev.derklaro.reflexion.FieldAccessor;
import dev.derklaro.reflexion.Reflexion;
import dev.derklaro.reflexion.matcher.FieldMatcher;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.config.multi.MultiConfig;
import pisi.unitedmeows.seriex.util.config.multi.impl.AreaConfig;
import pisi.unitedmeows.seriex.util.config.multi.impl.MinigameConfig;
import pisi.unitedmeows.seriex.util.config.multi.impl.TranslationsConfig;
import pisi.unitedmeows.seriex.util.config.multi.impl.WorldConfig;
import pisi.unitedmeows.seriex.util.config.multi.util.ConfigHandler;
import pisi.unitedmeows.seriex.util.config.multi.util.MultiConfigHandler;
import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.AuthConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.BanActionsConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.BannedCommandsConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.DatabaseConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.DiscordConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.MOTDConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.MaintainersConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.PacketLimiterConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.RanksConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ScoreboardConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Future;

public class FileManager extends Manager {
	private static final Object LOCK_OBJECT = new Object();
	private static final ReentrantLock LOCK = new ReentrantLock();

	public static final String EXTENSION = ".seriex";
	public static final String TODO = "$todo";

	private File pluginDirectory;
	private Map<Class<?>, IConfig> classToCfg;

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}


	@SuppressWarnings("resource")
	public FileManager(File pluginDirectory) {
		try {
			this.pluginDirectory = pluginDirectory;
			if (!pluginDirectory.exists())
				pluginDirectory.mkdir();

			this.classToCfg = new HashMap<>();

			BiConsumer<Class<?>, IConfig> consumer = (klass, config) -> {
				Seriex.get().logger().info("Loading config => '{}'", klass.getSimpleName());
				this.classToCfg.put(klass, config);
				sleep(50);
			};

			consumer.accept(AreaConfig.class, new AreaConfig());
			consumer.accept(AuthConfig.class, new AuthConfig());
			consumer.accept(BanActionsConfig.class, new BanActionsConfig());
			consumer.accept(DatabaseConfig.class, new DatabaseConfig());
			consumer.accept(DiscordConfig.class, new DiscordConfig());
			consumer.accept(MaintainersConfig.class, new MaintainersConfig());
			consumer.accept(MinigameConfig.class, new MinigameConfig());
			consumer.accept(MOTDConfig.class, new MOTDConfig());
			consumer.accept(RanksConfig.class, new RanksConfig());
			consumer.accept(ServerConfig.class, new ServerConfig());
			consumer.accept(TranslationsConfig.class, new TranslationsConfig());
			consumer.accept(WorldConfig.class, new WorldConfig());
			consumer.accept(BannedCommandsConfig.class, new BannedCommandsConfig());
			consumer.accept(PacketLimiterConfig.class, new PacketLimiterConfig());
			consumer.accept(ScoreboardConfig.class, new ScoreboardConfig());

			for (Map.Entry<Class<?>, IConfig> entry : this.classToCfg.entrySet()) {
				Seriex.get().logger().debug("Processing: {}", entry.getKey().getSimpleName());
				Class<?> klass = entry.getKey();
				IConfig config = entry.getValue();
				for (FieldAccessor accessor : Reflexion.on(klass).findFields(FieldMatcher.newMatcher().and(f -> f.isAnnotationPresent(ConfigField.class)))) {
					ConfigValue<?> value = accessor.<ConfigValue>getValue(config).getOrThrow();
					if (config instanceof SingleConfig singleConfig) {
						value.config(singleConfig.config());
						sleep(10);
						value.value();
					}
				}

				for (FieldAccessor fieldAccessor : Reflexion.on(klass).findFields(FieldMatcher.newMatcher().and(f -> f.isAnnotationPresent(ConfigHandler.class)))) {
					Field member = fieldAccessor.getMember();
					ConfigHandler annotation = member.getAnnotation(ConfigHandler.class);
					if (annotation.start()) {
						MultiConfigHandler handler = fieldAccessor.<MultiConfigHandler>getValue(config).getOrThrow();
						if (config instanceof MultiConfig multiConfig) {
							multiConfig.initialize(handler, multiConfig);
						}
					}
				}
			}
		} catch (Exception throwable) {
			// FIXME: investigate
			Seriex.get().logger().error("Error in FileManager \n => {}", throwable);
		} finally {
			for(long ms = 3; ms >= 0; ms -= 1) {
				Seriex.get().logger().info("Waiting for I/O operations: {}", ms);
				sleep(1000);
			}
		}
	}

	@Override
	public void post(Seriex seriex) {
		this.classToCfg.forEach((klass, config) -> Reflexion.on(klass)
					.findFields(FieldMatcher.newMatcher().and(f -> f.isAnnotationPresent(ConfigHandler.class)))
					.forEach(fieldAccessor -> {
						Field member = fieldAccessor.getMember();
						ConfigHandler annotation = member.getAnnotation(ConfigHandler.class);
						if (!annotation.start()) {
							MultiConfigHandler handler = fieldAccessor.<MultiConfigHandler>getValue(config).getOrThrow();
							if (config instanceof MultiConfig multiConfig) {
								multiConfig.initialize(handler, multiConfig);
							}
						}
					}));
	}

	public <X> X config(Class<? extends IConfig> cfgClass) {
		return (X) classToCfg.get(cfgClass);
	}

	public void writeString(final File file, final String bytes) {
		writeBytes(file, bytes.getBytes(UTF_8));
	}

	public void writeBytes(final File file, final byte[] bytes) {
		Future<Boolean> future = Async.async(() -> {
			try {
				Files.write(file.toPath(), bytes);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return Boolean.FALSE;
		});
		Seriex.get().futureManager().addFuture(future);
	}

	public File pluginDirectory() {
		return pluginDirectory;
	}

	public String todo() {
		return TODO;
	}
}
