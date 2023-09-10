package pisi.unitedmeows.seriex.commands.arguments;

import java.util.List;

import org.bukkit.Server;
import org.bukkit.World;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import panda.std.Option;
import panda.std.Result;

@ArgumentName("world")
public class WorldArgument implements OneArgument<World> {

	private final Server server;

	public WorldArgument(Server server) {
		this.server = server;
	}

	@Override
	public Result<World, Object> parse(LiteInvocation invocation, String argument) {
		return Option.of(this.server.getWorld(argument)).toResult("World not found.");
	}

	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		return this.server.getWorlds().stream()
					.map(World::getName)
					.map(Suggestion::of)
					.toList();
	}
}
