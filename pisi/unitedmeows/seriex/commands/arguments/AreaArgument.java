package pisi.unitedmeows.seriex.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import panda.std.Result;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.area.impl.Area;

import java.util.List;
import java.util.Optional;

@ArgumentName("area")
public class AreaArgument implements OneArgument<Area> {

	@Override
	public Result<Area, ?> parse(LiteInvocation invocation, String argument) {
		Optional<Area> areaOpt = Seriex.get().areaManager()
					.areaList
					.stream()
					.filter(a -> a.name.equals(argument))
					.findFirst();

		if(areaOpt.isEmpty())
			return Result.error("No area found");

		return Result.ok(areaOpt.get());
	}

	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		return Suggestion.of(Seriex.get()
					.areaManager()
					.areaList
					.stream()
					.map(area -> area.name)
					.toList());
	}
}
