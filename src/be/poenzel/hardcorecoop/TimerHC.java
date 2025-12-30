package be.poenzel.hardcorecoop;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import java.util.concurrent.TimeUnit;

public class TimerHC extends BukkitRunnable {

    private int timer = 0;
    private HardcoreCoop main;

    public TimerHC(HardcoreCoop main){
        this.main = main;
        this.timer = (int) main.getCurrentTimer();
    }

    // Current Hardcore Session Timer
    @Override
    public void run() {

        if (!main.isState(StateHC.RUNNING)){
            // RESET for FINISHED & WAITING in case /hc end is performed before the game actually ends by itself
            if(main.isState(StateHC.FINISHED) || main.isState(StateHC.WAITING)) timer = 0;
            main.setCurrentTimer(timer);
            main.saveCurrentTimer(main.getCurrentTimer());
            cancel();
        }

        for(Player p : main.getPlayers()){
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(secondsToFormat(timer)));
        }
        timer++;

    }

    public String secondsToFormat(long seconds){
        Duration d = Duration.ofSeconds(seconds);
        long h = d.toHours();
        long m = d.toMinutes() % 60;
        long s = d.getSeconds() % 60;
        return String.format("%02d:%02d:%02d",h, m, s);
    }


}
