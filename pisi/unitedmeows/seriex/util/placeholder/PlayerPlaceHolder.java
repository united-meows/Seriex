package pisi.unitedmeows.seriex.util.placeholder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class PlayerPlaceHolder implements IPlaceHolder {
	/*
	    matches
	    welcome @player.slowcheet4h.name or
	    welcome @player.slowcheet4h.name@'s name
	
	    if there is text coming right after placeholder you should put @ at the end
	    otherwise its not required
	 */
	private static final Pattern pattern = Pattern.compile("\\@player.[a-zA-Z0-9]+.[a-zA-Z0-9]+(\\@|$| )");

	@Override
	public String compute(String input) {
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			do {
				String playerName = matcher.group(1);
				Player player = Bukkit.getPlayer(playerName);
				if (player == null) {
					break;
				}
				PlayerW playerW = Seriex.get().dataManager().addUser(player);
				input = matcher.replaceFirst(playerW.attribute(matcher.group(2)));
				matcher = pattern.matcher(input);
			} while (matcher.find());
		}
		return matcher.toString();
	}
}
