package pisi.unitedmeows.seriex.util.config.single.impl;

import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "PacketLimiter")
public class PacketLimiterConfig extends SingleConfig {
	@ConfigField public ConfigValue<Double> PACKET_THRESHOLD = new ConfigValue<>("packet_threshold", 500.0);
	@ConfigField public ConfigValue<Double> BUCKET_AMOUNT = new ConfigValue<>("bucket_amount", 150.0);
	@ConfigField public ConfigValue<Double> BUCKET_INTERVAL = new ConfigValue<>("bucket_interval", 7.0);
}
