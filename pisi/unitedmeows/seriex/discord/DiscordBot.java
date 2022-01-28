package pisi.unitedmeows.seriex.discord;

import org.bukkit.ChatColor;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import pisi.unitedmeows.seriex.Seriex;

public class DiscordBot {
	private JDA jda;
	private Guild seriexGuild;

	public DiscordBot(final String _token) {
		try {
			jda = JDABuilder.createDefault(_token).build();
		} catch (final Exception ex) {
			Seriex._self.logger().fatalf(ChatColor.RED + "Couldn`t start the discord bot! %s",
						ex.getMessage());
			return;
		}
		seriexGuild = jda.getGuildById(Seriex._self.serverConfig().botToken());
	}

	public Guild seriexGuild() { return seriexGuild; }
}
