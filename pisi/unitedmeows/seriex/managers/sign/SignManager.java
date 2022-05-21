package pisi.unitedmeows.seriex.managers.sign;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.sign.SignCommand;

import java.util.ArrayList;
import java.util.List;

public class SignManager extends Manager {

    private static List<SignCommand> signCommands;

    @Override
    public void start(Seriex seriex) {
        signCommands = new ArrayList<>();
    }

    public static SignCommand create(String trigger) {
        SignCommand signCommand =  new SignCommand(trigger);
        signCommands.add(signCommand);
        return signCommand;
    }

    public List<SignCommand> signCommands() {
        return signCommands;
    }
}
