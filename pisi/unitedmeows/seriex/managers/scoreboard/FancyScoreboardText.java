package pisi.unitedmeows.seriex.managers.scoreboard;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;

public class FancyScoreboardText {
	private int place = 0;
	private final String text;
	private final List<ChatColor> colors;
	private String animatedText;

	public FancyScoreboardText(String text, ChatColor... colors) {
		this.place = 0;
		this.text = text;
		this.colors = Arrays.asList(colors);
	}

	private void updateText() {
		int spot = this.place;
		StringBuilder stringBuilder = new StringBuilder();
		for (char c : this.text.toCharArray()) {
			String letter = Character.toString(c);
			if (!" ".equals(letter)) {
				stringBuilder.append(this.colors.get(spot)).append(letter);
				if (spot == this.colors.size() - 1) {
					spot = 0;
				} else {
					++spot;
				}
			} else {
				stringBuilder.append(letter);
			}
		}
		this.animatedText = stringBuilder.toString();
	}

	public void moveRight() {
		if (this.place == 0) {
			this.place = this.colors.size() - 1;
		} else {
			--this.place;
		}
		this.updateText();
	}
	
	public String getAnimatedText() {
		return this.animatedText;
	}
}
