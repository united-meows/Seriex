package pisi.unitedmeows.seriex.managers.sign;

import org.bukkit.scheduler.BukkitRunnable;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.sign.IExec;
import pisi.unitedmeows.seriex.sign.SignCommand;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.parallel.IFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
