package pisi.unitedmeows.seriex.command;

import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.hook.YString;

import java.util.ArrayList;
import java.util.List;

public class CommandSystem {

    //TODO: make this prefix '/'
    private String prefix = "$";

    private List<Command> commandList = new ArrayList<>();

    public void registerCommand(Command command) {
        commandList.add(command);
    }

    public boolean execute(PlayerW player, String input) {

        /* remove the prefix from input */
        input = input.substring(prefix.length());

        final String name = input.split(" ")[0];
        Command command = null;
        for (Command cmd : commandList) {
            for (String trigger : cmd.triggers()) {
                if (name.equalsIgnoreCase(trigger)) {
                    command = cmd;
                    break;
                }
            }
        }


        if (command != null) {
            command.execute(player, input.length() == name.length() ? YString.EMPTY_R
                    : input.substring(name.length() + 1));
            return true;
        }

        return false;
    }


    public String prefix() {
        return prefix;
    }
}