package pisi.unitedmeows.seriex.util.config.single.impl;

import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "Discord")
public class DiscordConfig extends SingleConfig {

	@ConfigField public ConfigValue<String> BOT_TOKEN = new ConfigValue<>("bot_token", todo());
	@ConfigField public ConfigValue<String> MAINTAINER_NAME = new ConfigValue<>("maintainer_name", "$$$#0707");
	@ConfigField public ConfigValue<String> INVITE_LINK = new ConfigValue<>("invite_link", "discord.gg/9js26X5B6v");
	@ConfigField public ConfigValue<String> BANNER = new ConfigValue<>("banner", "https://waa.ai/fyKR");
	@ConfigField public ConfigValue<Boolean> AUTO_CONFIGURE = new ConfigValue<>("auto_configure.enabled", true);
	@ConfigField public ConfigValue<Boolean> AUTO_CONFIGURE_MULT = new ConfigValue<>("auto_configure.multiple", false);
	/**
	 * Possible values: PLAYING, STREAMING, LISTENING, WATCHING, CUSTOM_STATUS, COMPETING
	 * 
	 * @see net.dv8tion.jda.api.entities.Activity#ActivityType ActivityType
	 */
	@ConfigField public ConfigValue<String> ACTIVITY_TYPE = new ConfigValue<>("activity.type", "PLAYING");
	@ConfigField public ConfigValue<String> ACTIVITY_MESSAGE = new ConfigValue<>("activity.msg", "puhaha");
	@ConfigField public ConfigValue<String> ACTIVITY_URL = new ConfigValue<>("activity.url", "https://yapsanaburayiersinpuhahauhauhauha.com");
	@ConfigField public ConfigValue<String> ID_GUILD = new ConfigValue<>("id.guild", "0");
	@ConfigField public ConfigValue<String> ID_LANGUAGE_CHANNEL = new ConfigValue<>("id.language_channel", "0");
	@ConfigField public ConfigValue<String> ID_REGISTER_LOGS = new ConfigValue<>("id.register_logs", "0");
	@ConfigField public ConfigValue<String> ID_SERVER_CHAT = new ConfigValue<>("id.server_chat", "0");
	@ConfigField public ConfigValue<String> ID_BAN_LOGS = new ConfigValue<>("id.ban_logs", "0");

}
