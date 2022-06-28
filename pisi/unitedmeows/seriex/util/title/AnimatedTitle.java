package pisi.unitedmeows.seriex.util.title;

import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import be.maximvdw.title.Title;
import pisi.unitedmeows.seriex.Seriex;

public class AnimatedTitle {
	public static BukkitRunnable animatedTitle(Player player, String[] mainTitle, String[] subTitle) {
		int speed = 1;
		Title title = new Title("");
		title.setFadeInTime(0);
		title.setStayTime(40 + speed);
		BukkitRunnable bukkitRunnable = new BukkitRunnable() {
			int mainTicks = 0 , subTicks = 0;
			boolean titleReverse = false , subReverse = false;

			@Override
			public void run() {
				if (mainTicks + 1 >= mainTitle.length) {
					titleReverse = true;
					mainTicks--;
				} else {
					if (titleReverse) {
						if (mainTicks <= 0) {
							titleReverse = false;
							mainTicks++;
						} else {
							mainTicks--;
						}
					} else {
						mainTicks++;
					}
				}
				if (subTitle != null && subTitle.length != 0) {
					if (subTicks + 1 >= subTitle.length) {
						subReverse = true;
						subTicks--;
					} else {
						if (titleReverse) {
							if (subTicks <= 0) {
								subReverse = false;
								subTicks++;
							} else {
								subTicks--;
							}
						} else {
							subTicks++;
						}
					}
					title.setSubtitle(subTitle[subTicks]);
				}
				title.setTitle(mainTitle[mainTicks]);
				title.send(player);
			}
		};
		bukkitRunnable.runTaskTimer(Seriex.get(), 0, speed);
		return bukkitRunnable;
	}

	// TODO optimize this shit kekw
	public static String[] animateText(String kek, String highlightedWord, String primaryColor, String highlightColor) {
		String cool = "&r&l&k!il&r";
		final String coolSuffix = " " + cool;
		final String coolPrefix = cool + " ";
		char[] charArray = kek.toCharArray();
		Set<String> frames = new LinkedHashSet<>();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			String message = kek.substring(0, i + 1);
			char[] highlightedChars = highlightedWord.toCharArray();
			if (charArray.length - 1 == i) {
				frames.add(primaryColor + kek);
			}
			for (int j = 0; j < highlightedChars.length; j++) {
				String highlightedBefore = primaryColor + highlightedWord.substring(0, j);
				String highlightedChar = highlightColor + highlightedWord.substring(j, j + 1);
				String highlightedAfter = primaryColor + highlightedWord.substring(j + 1);
				String newHighlighted = highlightedBefore + highlightedChar + highlightedAfter;
				String string = i == charArray.length - 1 ? message.replace(highlightedWord, newHighlighted) : message;
				String replace = primaryColor + string;
				frames.add(replace);
			}
		}
		Set<String> realFrames = new LinkedHashSet<>();
		frames.forEach(string -> realFrames.add(coolPrefix + string + coolSuffix));
		return realFrames.stream().toArray(String[]::new);
	}
}
