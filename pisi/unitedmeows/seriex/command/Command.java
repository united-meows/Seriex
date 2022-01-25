package pisi.unitedmeows.seriex.command;

import org.bukkit.entity.Player;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.player.PlayerW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;

public class Command {

	private String[] triggers;
	private String description;
	private Consumer<ExecuteInfo> run;
	private Function<AutoCompleteInfo, String> autoComplete;
	private String[] inputs;

	protected Command(String[] _triggers) {
		triggers = _triggers;
		inputs = new String[0];
	}

	public static Command create(String... _triggers) {
		final Command command = new Command(_triggers);
		Seriex._self.commandSystem().registerCommand(command);
		return command;
	}

	public void execute(PlayerW _playerW, String _input) {
		StringBuilder builder = new StringBuilder();
		List<String> inputList = new ArrayList<>();
		boolean inQuotes = false;

		/* find all inputs and put them to list*/
		for (char character : _input.toCharArray()) {

			switch (character) {
				/* maybe put them in the same case? */
				case '\'': {
					inQuotes = !inQuotes;
					break;
				}
				case '"':
					inQuotes = !inQuotes;
					break;
				case ' ': {
					if (!inQuotes) {
						inputList.add(builder.toString());
						builder = new StringBuilder();
						break;
					}
					builder.append(character);
					break;
				}
				default:
					builder.append(character);
					break;
			}
		}

		/* put the last input to list
		 if we don't add this line last input will be ignored because
		 we add inputs after space character */
		if (builder.length() > 0) {
			inputList.add(builder.toString());
		}

		Map<String, String> inputMap = new HashMap<>();

		/* add inputs with their var names */
		for (int i = 0; i < inputs.length && i < inputList.size(); i++) {
			inputMap.put(inputs[i], inputList.get(i));
		}

		/* create the execute info object */
		final ExecuteInfo executeInfo = new ExecuteInfo(_playerW, _input, inputMap);

		/* run the command */
		run.accept(executeInfo);
	}

	public Command inputs(String... _inputs) {
		inputs = _inputs;
		return this;
	}

	public Command setDescription(String _description) {
		description = _description;
		return this;
	}

	public Command onRun(Consumer<ExecuteInfo> _run) {
		run = _run;
		return this;
	}

	public Command onAutoComplete(Function<AutoCompleteInfo, String> _autoComplete) {
		autoComplete = _autoComplete;
		return this;
	}

	public String description() {
		return description;
	}

	public String[] triggers() {
		return triggers;
	}

	public static class AutoCompleteInfo {

		private PlayerW playerW;
		private String input;

		public AutoCompleteInfo(PlayerW _playerw, String _input) {
			playerW = _playerw;
			input = _input;
		}

		public PlayerW playerW() {
			return playerW;
		}

		public String input() {
			return input;
		}
	}

	public static class ExecuteInfo {
		private PlayerW playerW;
		private Map<String, String> arguments;
		private String fullInput;

		public ExecuteInfo(PlayerW _playerw, String _fullInput, Map<String, String> _arguments) {
			playerW = _playerw;
			arguments = _arguments;
			fullInput = _fullInput;
		}

		public String fullInput() {
			return fullInput;
		}

		public PlayerW playerW() {
			return playerW;
		}

		public Map<String, String> arguments() {
			return arguments;
		}
	}

}
