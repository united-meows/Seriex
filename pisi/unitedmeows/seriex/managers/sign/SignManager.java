package pisi.unitedmeows.seriex.managers.sign;

import java.util.ArrayList;
import java.util.List;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.sign.impl.SignCommand;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class SignManager extends Manager {
	private static List<SignCommand> signCommands;

	@Override
	public void start(Seriex seriex) {
		signCommands = new ArrayList<>();
	}

	public static SignCommand create(String trigger) {
		SignCommand signCommand = new SignCommand(trigger);
		signCommands.add(signCommand);
		return signCommand;
	}

	public List<SignCommand> signCommands() {
		return signCommands;
	}

	@Override
	public void cleanup() throws SeriexException {
		signCommands.forEach(SignCommand::close);
	}
}
