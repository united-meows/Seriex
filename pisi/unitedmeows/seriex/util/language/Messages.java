package pisi.unitedmeows.seriex.util.language;

public enum Messages {
	AUTH_INCORRECT_PASSWORD("auth.incorrect_password" , "Incorrect password."),
	AUTH_COMMAND_NOT_ALLOWED("auth.command_not_allowed" , "The command '%s' is not allowed!"),
	AUTH_CHAT_NOT_ALLOWED("auth.chat_not_allowed" , " In order to chat you must be authenticated!"),
	AUTH_FORGOT_PASSWORD("auth.forgot_password" , "Did you forget your password?"),
	AUTH_CLICK_TO_LOGIN("auth.click_to_login" , "Click me to log in!"),
	AUTH_DEFAULT_GUI_TEXT("auth.default_gui_text" , "Type your password."),
	AUTH_LOGGED_IN("auth.logged_in" , "Succesfully logged in!"),
	AUTH_FORGOT_PASSWORD_ACTION("auth.forgot_password_action" , "Go to the #recovery channel in the discord server."),
	AUTH_GUI_FAILURE("auth.gui_failure" , "Failed to open your login GUI, please connect to the server again."),
	AUTH_2FA_SENT("auth.2fa_sent" , "2FA sent to your Discord DM with the bot!"),
	AUTH_TYPE_2FA("auth.type_2fa" , "Please enter your 2FA pin in the chat to continue."),
	AUTH_INCORRECT_2FA("auth.incorrect_2fa" , "Incorrect 2FA code."),
	AUTH_TYPE_LOGIN_CMD("auth.login_cmd" , "Please enter your password using: '/login password'"),

	PLAYER_RECEIVE_MONEY("player.receive_money" , "You received %s pawcoins!"),
	PLAYER_SPEND_MONEY("player.spend_money" , "You spent %s pawcoins!"),

	AREA_SPEEDTEST_ERROR("area.speedtest_error" , "No available speedtests found."),
	AREA_SPEEDTEST_QUEUED("area.speedtest_queued" , "Joined the queue for a speed-test!"),
	AREA_SPEEDTEST_STARTING("area.speedtest_starting" , "Starting in %s"),
	AREA_SPEEDTEST_STARTED("area.speedtest_started" , "GO!"),
	AREA_SPEEDTEST_TOOK_TOO_LONG("area.speedtest_took_too_long" , "You are too slow!"),
	AREA_SPEEDTEST_RESULT("area.speedtest_result" , "You finished in %s ticks. (%s blocks per tick)"),

	AUTO_JOIN_MESSAGE("auto.join_message" , "&7( &a+ &7) '%s' joined the server."),
	AUTO_LEAVE_MESSAGE("auto.leave_message" , "&7( &c- &7) '%s' left the server."),

	ANTICHEAT_CANT_SWITCH("anticheat.gui_enable" , "You can only change your anti-cheat in spawn."),
	ANTICHEAT_NOT_FOUND("anticheat.not_found" , "Anti-cheat with the name '%s' not found."),
	ANTICHEAT_SWITCH("anticheat.switched" , "Switched to anti-cheat '%s'."),

	SERVER_INTERNAL_ERROR("server.internal_error" , "We had an internal error processing some of your actions, sorry for the inconvenience!"),
	SERVER_KICK("server.kick" , "You have been kicked from the server."),
	SERVER_RESTART("server.restart" , "Restarting server..."),
	SERVER_DESYNC("server.desync" , "You have been desynced from the server, please contact a maintainer."),
	SERVER_NOT_IMPLEMENTED("server.not_implemented" , "'%s' has not been implemented, yet."),
	SERVER_WELCOME("server.welcome" , "Welcome to %s!"),
	SERVER_CLICK_TO_COPY("server.click_to_copy" , "Click me to copy '%s'!"),

	COMMAND_ANTICHEAT_NOT_ALLOWED("command.anticheat", "Not allowed anti-cheat: '%s'"),

	COMMAND_BANNED("command.banned" , "Command is not allowed."),
	COMMAND_CONSOLE_ONLY("command.console_only" , "Command is console only."),
	COMMAND_NOT_ALLOWED("command.not_allowed" , "Insufficient rank. (Required: '%s' or above.)"),

	COMMAND_WRONG_USAGE("command.wrong_usage" , "Wrong arguments. Usage: '%s'"),
	COMMAND_WRONG_USAGES("command.wrong_usages" , "Wrong arguments. Usages:"),
	COMMAND_INVALID_ARGUMENT("command.not_enough_arguments" , "Invalid argument '%s' for type %s."),
	COMMAND_NO_PERMISSION("command.no_permission" , "You need the permissions '%s' to run this command!"),
	COMMAND_WRONG_STATE("command.wrong_state" , "You cant use this command while in a %s."),
	
	COMMAND_ENABLED("command.state_enabled" , "enabled"),
	COMMAND_DISABLED("command.state_disabled" , "disabled"),

	COMMAND_AFK_ENABLE("command.afk_enable" , "Enabled AFK mode."),
	COMMAND_AFK_DISABLE("command.afk_disable" , "Disabled AFK mode."),

	COMMAND_FLY_ENABLE("command.fly_enable" , "Enabled flight!"),
	COMMAND_FLY_DISABLE("command.fly_disable" , "Disabled flight!"),

	COMMAND_TELEPORT_SUCCESS("command.tp_success", "Teleported to %s!"),
	COMMAND_TELEPORT_FAILURE_UNKNOWN("command.tp_failure", "Couldnt TP, %s"),
	COMMAND_TELEPORT_FAILURE_TARGET_IN_DUEL("command.tp_in_duel", "Couldnt TP, player %s is in a duel!"),
	COMMAND_TELEPORT_FAILURE_TELEPORTER_IN_DUEL("command.tp_in_duel", "Couldnt TP, you are in a duel!"),

	COMMAND_TELEPORT_COORDS_SUCCESS("command.tp_coords_success", "Teleported to [%s, %s, %s]!"),
	COMMAND_TELEPORT_OTHER_COORDS_SUCCESS("command.tp_other_coords_success", "Teleported %s to [%s, %s, %s]!"),
	
	COMMAND_CLEAR_SUCCESS("command.clear_success" , "Cleared your inventory."),
	COMMAND_CLEAR_OTHER_SUCCESS("command.clear_other_success" , "Cleared player '%s'`s inventory."),

	COMMAND_FEED_SELF_SUCCESS("command.feed_self_success" , "You have fed yourself."),
	COMMAND_FEED_OTHER_SUCCESS("command.feed_other_success" , "You have fed %s."),
	
	COMMAND_HEAL_SELF_SUCCESS("command.healed_self_success" , "You have healed yourself."),
	COMMAND_HEAL_OTHER_SUCCESS("command.healed_other_success" , "You have healed %s."),
	
	COMMAND_KILL_SELF_SUCCESS("command.kill_self_success" , "You have killed yourself."),
	COMMAND_KILL_OTHER_SUCCESS("command.kill_other_success" , "You have killed %s."),
	
	COMMAND_PAY_INSUFFICIENT_FUNDS("command.pay_insufficient_funds" , "Insufficient funds."),
	
	COMMAND_IGNORE_IGNORING_PLAYER("command.ignore_ignoring_player" , "Now ignoring '%s'"),
	COMMAND_IGNORE_NO_LONGER_IGNORING_PLAYER("command.ignore_no_longer_ignoring_player" , "No longer ignoring '%s'"),
	COMMAND_IGNORE_IGNORED_PLAYERS("command.ignore_ignored_players" , "Ignored players: %s"),
	
	COMMAND_STACK_SUCCESS("command.stack_success" , "Stacked potions!"),
	COMMAND_STACK_FAIL("command.stack_fail" , "No potions to stack!"),

	COMMAND_ENCHANT_NO_ITEM_IN_HAND("command.enchant_no_item" , "Please hold an item to enchant."),
	COMMAND_ENCHANT_HAS_NO_ENCHANT("command.has_no_enchant" , "Enchant '%s' does not exist on the held item."),
	COMMAND_ENCHANT_HAS_ENCHANT("command.has_enchant_already" , "Enchant '%s' already exists on the held item."),
	COMMAND_ENCHANT_SUCCESS("command.enchant_success" , "Enchanted held item with '%s %s'!"),
	COMMAND_ENCHANT_REMOVE_TIP("command.enchant_remove_tip" , "You can enchant an item with the level '0' to remove the effect."),

	COMMAND_POTION_INVENTORY_FULL("command.potion_inventory_full" , "Inventory full, could not add %s amount of potions to inventory."),
	COMMAND_POTION_ADDED_TO_INVENTORY("command.potion_added_to_inventory" , "Gave %s amount of potion %s!"),

	COMMAND_GIVE_INVENTORY_FULL("command.give_inventory_full" , "Inventory full, could not add item to inventory."),
	COMMAND_GIVE_ADDED_TO_INVENTORY("command.give_added_to_inventory" , "Gave the item '%s:%s' with the stack size '%s'!"),

	COMMAND_RANK_MODIFIED("command.rank_modified" , "Modified rank with the command: '%s %s %s'."),
	COMMAND_RANK_CHANGED("command.rank_changed" , "Changed '%s'`s rank to '%s'."),

	COMMAND_BAN_PLAYER_ALREADY_BANNED("command.player_already_banned" , "Player '%s' is already banned."),
	COMMAND_BAN_PLAYER_ALREADY_NOT_BANNED("command.player_already_not_banned" , "Player '%s' is not banned."),
	COMMAND_BAN_PLAYER_UNBANNED("command.player_unbanned" , "Player '%s' is now unbanned."),
	COMMAND_BAN_PLAYER_BANNED("command.player_banned" , "Player '%s' is now banned."),
	COMMAND_BAN_PLAYER_DISCORD_MESSAGE("command.player_ban_discord_message" , "Your account '%s' has been banned with the reason: '%s'"),
	COMMAND_UNBAN_PLAYER_DISCORD_MESSAGE("command.player_unban_discord_message" , "Your account '%s' has been unbanned."),

	// TODO
	COMMAND_MAINTENANCE("command.maintenance" , "Maintenance mode is currently: %s."),
	COMMAND_PLUGINS("command.plugins" , "We use the following plugins: %s"),
	COMMAND_KIT_EQUIP("command.kit_equipped" , "Equipped the default kit."),
	COMMAND_GM("command.gm", "Set gamemode to %s"),
	COMMAND_PLAYTIME("command.playtime" , "You have played for: %s"),

	LOGIN_ABORT_MCP("login_abort.mcp" , "Currently, MCP development accounts are not allowed."),

	MINIGAME_WINSTREAK("minigame.winstreak" , "(%s) '%s' has gotten a '%s' killstreak"),
	MINIGAME_KILLED("minigame.killed" , "(%s) '%s' has been killed by '%s'!"),
	MINIGAME_DEATH("minigame.death" , "(%s) '%s' died mysteriously."),
	MINIGAME_CANT_SWITCH_IN_DUEL("minigame.cant_switch_in_duel" , "You cannot join minigames while in a duel."),
	MINIGAME_OUT_OF_BOUNDS("minigame.out_of_bounds" , "You went out of bounds, teleporting you to spawn."),

	DUEL_CANT_ACCEPT("duel.cant_accept" , "You can only accept a duel in spawn!"),
	DUEL_CANT_ACCEPT_OTHER("duel.cant_accept_other" , "Player '%s' isnt in spawn!"),
	DUEL_CANT_SEND("duel.cant_send" , "You can only send a duel request in spawn!"),
	DUEL_CANT_SEND_OTHER("duel.cant_send_other" , "Player '%s' isnt in spawn!"),
	DUEL_CANT_SPECTATE("duel.cant_spectate" , "You can only spectate a duel in spawn!"),

	AREA_TELEPORTING("area.teleport" , "Teleporting you to the %s test area..."),
	AREA_ONLY_IN_SPAWN("area.only_in_spawn" , "You can only teleport to areas while in spawn."),
	AREA_NOT_IMPLEMENTED_YET("area.not_implemented_yet" , "Test area '%s' is not finished yet."),

	SCOREBOARD_INFO_TITLE("scoreboard.title_info" , "Info"),
	SCOREBOARD_INFO_SERVER("scoreboard.title_server" , "Server"),

	BAN_ACTIONS_ANNOUNCE_IP("ban_actions.announce_ip" , "Player '%s' (IP: '%s') tried to join the server, but is banned.");

	public String cfgString, defaultValue;

	Messages(String cfg, String defaultValue) {
		this.cfgString = cfg;
		this.defaultValue = defaultValue;
	}
}
