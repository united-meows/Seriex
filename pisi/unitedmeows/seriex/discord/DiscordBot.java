package pisi.unitedmeows.seriex.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import pisi.unitedmeows.seriex.Seriex;

public class DiscordBot {

	private JDA jda;
	private Guild seriexGuild;

	public DiscordBot(String _token) {
		try {
			jda = JDABuilder.createDefault(_token).build();
		} catch (Exception ex) {
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED +
					String.format("Couldn't start the discord bot! ", ex.getMessage()));
			return;
		}

		seriexGuild = jda.getGuildById(Seriex._self.serverConfig().botToken());
	}

	public Guild seriexGuild() {
		return seriexGuild;
	}
}
