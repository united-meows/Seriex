package pisi.unitedmeows.seriex.discord.utilities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.language.Language;

import java.awt.*;
import java.util.*;
import java.util.List;


public class DiscordCache {
	private static final Color VERIFIED_MEMBER_COLOR = new Color(42, 106, 209);

	private final Map<String, Map<Language, Role>> languageRoleCache;
	private final Map<String, Role> verifiedRoleCache;
	private final Map<String, Map<String, Member>> memberCache;


	private DiscordCache() {
		this.languageRoleCache = new HashMap<>();
		this.verifiedRoleCache = new HashMap<>();
		this.memberCache = new HashMap<>();
	}

	public static DiscordCache create() {
		return new DiscordCache();
	}

	public void cacheInitial(Guild guild) {
		this.cacheLanguages(guild);
		this.cacheInitialMembers(guild);
		this.cacheVerifiedRoles(guild);
	}

	public void onMemberJoin(Guild guild, Member member) {
		Map<String, Member> map = memberCache.get(guild.getId());
		map.put(member.getId(), member);
		memberCache.replace(guild.getId(), map);
	}

	public void onMemberQuit(Guild guild, Member member) {
		Map<String, Member> map = memberCache.get(guild.getId());
		map.remove(member.getId());
		memberCache.replace(guild.getId(), map);
	}

	private void cacheLanguages(Guild guild) {
		Map<Language, Role> map = new EnumMap<>(Language.class);
		Arrays.stream(Language.values()).forEach(language -> {
			List<Role> rolesByName = guild.getRolesByName(language.name(), false);
			if (rolesByName.isEmpty()) {
				Seriex.get().logger().info("Created language role {} for the guild {}!", language.name(), guild.getName());
				guild.createRole().setColor(-1).setMentionable(false).setName(language.name()).complete();
			} else {
				Optional<Role> optional = rolesByName.stream().findFirst();
				Seriex.get().logger().info("Created cache for the role {} in the guild {}!", language.name(), guild.getName());
				Role value = optional.get();
				map.put(language, value);
				languageRoleCache.put(guild.getId(), map);
			}
		});
	}

	private void cacheVerifiedRoles(Guild guild) {
		List<Role> rolesByName = guild.getRolesByName("verified", false);
		if (rolesByName.size() > 1) {
			Seriex.get().logger().error("There is more than 1 verified role?");
		}
		if (rolesByName.isEmpty()) {
			Seriex.get().logger().info("Created verified role verified for the guild{}!", guild.getName());
			guild.createRole().setColor(VERIFIED_MEMBER_COLOR).setMentionable(true).setName("verified").complete();
		} else {
			Seriex.get().logger().info("Created cache for the role verified in the guild {}!", guild.getName());
			verifiedRoleCache.put(guild.getId(), rolesByName.get(0));
		}
	}

	private void cacheInitialMembers(Guild guild) {
		memberCache.computeIfAbsent(guild.getId(), guildID -> {
			Map<String, Member> map = new HashMap<>();
			guild.findMembers(f -> true).onSuccess(memberList -> memberList.forEach(member -> {
				map.put(member.getId(), member);
			}));
			return map;
		});
	}

	public Map<String, Role> verifiedRoles() {
		return verifiedRoleCache;
	}

	public Map<String, Map<Language, Role>> languageRoles() {
		return languageRoleCache;
	}

	public Map<String, Role> members() {
		return verifiedRoleCache;
	}
}
