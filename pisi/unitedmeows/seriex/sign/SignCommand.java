package pisi.unitedmeows.seriex.sign;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.parallel.IFunction;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SignCommand {

    private String trigger;
    private SignCommand instance;
    private String[] expectedParams;

    private BiConsumer<PlayerW, SignCommand> leftClick;
    private BiConsumer<PlayerW, SignCommand> rightClick;
    private Consumer<SignCommand> tick;


    private HashMap<String, Object> session = new HashMap<>();
    private int runnable;


    public SignCommand(String _trigger) {
        trigger = _trigger;
        instance = this;
    }

    public void runLeft(PlayerW playerW) {
        if (leftClick != null)
            leftClick.accept(playerW, this);
    }

    public void runRight(PlayerW playerW) {
        if (rightClick != null)
            rightClick.accept(playerW, this);
    }


    public SignCommand onLeft(BiConsumer<PlayerW, SignCommand> _leftClick) {
        leftClick = _leftClick;
        return this;
    }

    public SignCommand onRight(BiConsumer<PlayerW, SignCommand> _rightClick) {
        rightClick = _rightClick;
        return this;
    }

    public SignCommand tick(Consumer<SignCommand> _tick, int interval) {
        tick = _tick;
        if (runnable != -1) {
            Bukkit.getScheduler().cancelTask(runnable);
        }

        runnable = Bukkit.getScheduler().scheduleSyncRepeatingTask(Seriex.get(), new BukkitRunnable() {
            @Override
            public void run() {
                tick.accept(instance);
            }
        }, interval, interval);
        return this;
    }






    public void close() {
        if (runnable != -1) {
            Bukkit.getScheduler().cancelTask(runnable);
        }

    }

    public HashMap<String, Object> session() {
        return session;
    }


    protected static String get(int index, Sign sign) {
        return sign.getLine(index);
    }

    public String trigger() {
        return trigger;
    }
}
