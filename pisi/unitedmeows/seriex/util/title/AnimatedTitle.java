package pisi.unitedmeows.seriex.util.title;

import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import be.maximvdw.title.Title;
import pisi.unitedmeows.seriex.Seriex;

public class AnimatedTitle {
	public static BukkitRunnable animatedTitle(final Player player, final String[] mainTitle, final String[] subTitle) {
		final int speed = 20;
		final Title title = new Title("");
		title.setFadeInTime(0);
		title.setStayTime(20 + speed);
		final BukkitRunnable bukkitRunnable = new BukkitRunnable() {
			int mainTicks = 0 , subTicks = 0;
			@Override
			public void run() {
				if (mainTicks + 1 < mainTitle.length) {
					mainTicks++;
				}
				if (subTitle != null && subTitle.length != 0) {
					if (subTicks + 1 < subTitle.length) {
						subTicks++;
					}
					title.setSubtitle(subTitle[subTicks]);
				}
				if (subTitle == null || subTitle.length == 0) {
					if (mainTicks == mainTitle.length - 1) {
						cancel();
					}
				} else if (mainTicks == mainTitle.length - 1 && subTicks == subTitle.length - 1) {
					cancel();
				}
				title.setTitle(mainTitle[mainTicks]);
				title.send(player);
			}
		};
		bukkitRunnable.runTaskTimer(Seriex.get(), 0, speed);
		return bukkitRunnable;
	}

	// TODO optimize this shit kekw
	public static String[] animateText(final String kek, final String highlightedWord, final String primaryColor, final String highlightColor) {
		final String cool = "&r&l&k!il&r";
		final String coolSuffix = " " + cool;
		final String coolPrefix = cool + " ";
		final char[] charArray = kek.toCharArray();
		final Set<String> frames = new LinkedHashSet<>();
		for (int i = 0; i < charArray.length; i++) {
			final String message = kek.substring(0, i + 1);
			final char[] highlightedChars = highlightedWord.toCharArray();
			if (charArray.length - 1 == i) {
				frames.add(primaryColor + kek);
			}
			for (int j = 0; j < highlightedChars.length; j++) {
				// with stringbuilder we get one less object :DDDDDDDDD
				final String highlightedBefore = new StringBuilder().append(primaryColor).append(highlightedWord.substring(0, j)).toString();
				final String highlightedChar = new StringBuilder().append(highlightColor).append(highlightedWord.substring(j, j + 1)).toString();
				final String highlightedAfter = new StringBuilder().append(primaryColor).append(highlightedWord.substring(j + 1)).toString();
				final String newHighlighted = new StringBuilder().append(highlightedBefore).append(highlightedChar).append(highlightedAfter).toString();
				final String string = i == charArray.length - 1 ? message.replace(highlightedWord, newHighlighted) : message;
				final String replace = new StringBuilder().append(primaryColor).append(string).toString();
				frames.add(replace);
			}
		}
		final Set<String> realFrames = new LinkedHashSet<>();
		frames.forEach(string -> realFrames.add(coolPrefix + string + coolSuffix));
		return realFrames.stream().toArray(String[]::new);
	}
}
