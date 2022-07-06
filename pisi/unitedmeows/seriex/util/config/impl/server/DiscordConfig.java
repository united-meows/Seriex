package pisi.unitedmeows.seriex.util.config.impl.server;

import static pisi.unitedmeows.seriex.Seriex.*;

import net.dv8tion.jda.api.entities.Activity.ActivityType;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.config.util.ConfigValue;

@Cfg(name = "Discord" , manual = false , multi = false)
public class DiscordConfig extends Config {
	public DiscordConfig() {
		super("Discord");
	}

	@ConfigField
	public ConfigValue<String> BOT_TOKEN = new ConfigValue<>(this, "bot_token", !available() ? "private" : get().fileManager().PRIVATE);
	@ConfigField
	public ConfigValue<String> MAINTAINER_NAME = new ConfigValue<>(this, "maintainer_name", "$$$#0707");
	@ConfigField
	public ConfigValue<String> INVITE_LINK = new ConfigValue<>(this, "invite_link", "discord.gg/9js26X5B6v");
	@ConfigField
	public ConfigValue<String> BANNER = new ConfigValue<>(this, "banner", "https://waa.ai/fyKR");
	@ConfigField
	public ConfigValue<Boolean> AUTO_CONFIGURE = new ConfigValue<>(this, "auto_configure", true);
	@ConfigField
	public ConfigValue<Boolean> AUTO_CONFIGURE_MULT = new ConfigValue<>(this, "auto_configure_multiple", false);
	@ConfigField
	public ConfigValue<ActivityType> ACTIVITY_TYPE = new ConfigValue<>(this, "activity_type", ActivityType.PLAYING);
	@ConfigField
	public ConfigValue<String> ACTIVITY_MESSAGE = new ConfigValue<>(this, "activity_msg", "puhaha");
	@ConfigField
	public ConfigValue<String> ACTIVITY_URL = new ConfigValue<>(this, "activity_url", "https://yapsanaburayiersinpuhahauhauhauha.com");
	@ConfigField
	public ConfigValue<String> ID_GUILD = new ConfigValue<>(this, "id_guild", "");
	@ConfigField
	public ConfigValue<String> ID_LANGUAGE_CHANNEL = new ConfigValue<>(this, "id_language_channel", "");
	@ConfigField
	public ConfigValue<String> ID_REGISTER_LOGS = new ConfigValue<>(this, "id_register_logs", "");
	@ConfigField
	public ConfigValue<String> ID_SERVER_CHAT = new ConfigValue<>(this, "id_server_chat", "");

	@Override
	public void load() {
		internalLoad(this);
	}

	@Override
	public void loadDefaultValues() {
		internalDefaultValues(this);
	}
}
