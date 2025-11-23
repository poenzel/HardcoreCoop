package be.poenzel.hardcorecoop;

import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.List;

import java.util.concurrent.TimeUnit;

public class TimerHC extends BukkitRunnable {

    private int timer = 0;
    private HardcoreCoop main;

    public TimerHC(HardcoreCoop main){
        this.main = main;
    }

    @Override
    public void run() {

        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),"title @a actionbar '" + secondsToFormat(timer) +"'");
        timer++;
        if (!main.isState(StateHC.RUNNING)){
            timer = 0;
            cancel();
        }

    }

    public String secondsToFormat(long seconds){
        Duration d = Duration.ofSeconds(seconds);
        long h = d.toHours();
        long m = d.toMinutes() % 60;
        long s = d.getSeconds() % 60;
        return String.format("%02d:%02d:%02d",h, m, s);
    }
}
