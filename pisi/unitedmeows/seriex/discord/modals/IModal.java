package pisi.unitedmeows.seriex.discord.modals;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;
import pisi.unitedmeows.seriex.discord.DiscordBot;

public interface IModal {
	String buttonName();

	String modalName();
	Modal createdModal();
	void modalInteraction(ModalInteractionEvent event, DiscordBot discordBot);
}
