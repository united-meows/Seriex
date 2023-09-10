package pisi.unitedmeows.seriex.util.inventories.ui;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerSettings;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerWallet;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.animation.Tail;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class PlayerInfoGUI implements InventoryProvider {

	public static final SmartInventory INVENTORY = SmartInventory.builder()
				.id("playerinfo_gui")
				.provider(new PlayerInfoGUI())
				.size(3, 9)
				.title(ChatColor.LIGHT_PURPLE + "Player Info GUI")
				.build();

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

	private String formatMS(long ms) {
		LocalDateTime datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneOffset.UTC);
		return DATE_TIME_FORMATTER.format(datetime);
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerW user = Seriex.get().dataManager().user(player);
		contents.setProperty("tail", new Tail());

		contents.set(1, 1, ClickableItem.of(ItemBuilder.of(Material.ARMOR_STAND).name("&fEntity information").build(), e -> {}));

		List<String> discordStruct = new ArrayList<>();
		StructPlayerDiscord playerDiscord = user.playerDiscord();
		discordStruct.add("&fDiscord ID: " + playerDiscord.snowflake);
		discordStruct.add("&fJoined as: " + playerDiscord.joinedAs);
		discordStruct.add("&fLanguages: " + Language.getLanguages(playerDiscord.languages).stream().map(Language::name).collect(Collectors.joining(",")));
		contents.set(1, 3, ClickableItem.of(ItemBuilder.head("566fda81aa30cdd2079db7cc90dae6ee346ce4aabf9e6a87f1f51aeb1a440d").name("&bDiscord")
					.lore(discordStruct)
					.build(), e -> {}));

		List<String> loginDates = new ArrayList<>();

		var logins = user.playerLogins();
		loginDates.add("&dFirst login: " + formatMS(logins.get(0).ms));
		loginDates.add("&5Last login: " + formatMS(logins.get(logins.size() - 1).ms));
		contents.set(1, 4, ClickableItem.of(ItemBuilder.of(Material.SIGN)
					.name("&fLogin dates")
					.lore(loginDates)
					.build(), e -> {}));

		List<String> settingsList = new ArrayList<>();
		StructPlayerSettings settings = user.playerSettings();
		settingsList.add("&fFlags: " + coloredBoolean(settings.flags));
		settingsList.add("&fHunger: " + coloredBoolean(settings.hunger));
		settingsList.add("&fFall damage: " + coloredBoolean(settings.fall_damage));
		settingsList.add("&fAnticheat: " + settings.anticheat);
		settingsList.add("&fSelected language: " + settings.selectedLanguage);
		contents.set(1, 5, ClickableItem.of(ItemBuilder.of(Material.NETHER_STAR)
					.name("&fSettings")
					.lore(settingsList)
					.build(), e -> {}));

		List<String> walletList = new ArrayList<>();
		StructPlayerWallet wallet = user.playerWallet();
		walletList.add("&dPawcoins: &f" + wallet.coins);
		walletList.add("&dPawcoins wallet: &f" + wallet.player_wallet);
		contents.set(1, 7, ClickableItem.of(ItemBuilder.of(Material.EMERALD)
					.name("&fWallet")
					.lore(walletList)
					.build(), e -> {}));
	}

	public String coloredBoolean(boolean b) {
		return b ? "&ayes" : "&cno";
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		List<String> playerInfo = new ArrayList<>();
		Location location = player.getLocation();
		playerInfo.add(String.format("&fLocation: [%d,%d,%d]",
					location.getBlockX(),
					location.getBlockY(),
					location.getBlockZ()));
		playerInfo.add("&fUUID: " + player.getUniqueId());
		playerInfo.add("&fOnground: " + coloredBoolean(player.isOnGround()));
		playerInfo.add("&fSneaking: " + coloredBoolean(player.isSneaking()));
		playerInfo.add("&fSprinting: " + coloredBoolean(player.isSprinting()));

		contents.set(1, 1, ClickableItem.of(ItemBuilder.of(Material.ARMOR_STAND)
					.name("Entity information")
					.lore(playerInfo)
					.build(), e -> {}));

		int state = contents.property("state", 0);
		contents.setProperty("state", state + 1);

		if (state % GuiSettings.TAIL_DELAY != 0)
			return;

		Tail tail = contents.property("tail");
		tail.update(contents);
	}
}
