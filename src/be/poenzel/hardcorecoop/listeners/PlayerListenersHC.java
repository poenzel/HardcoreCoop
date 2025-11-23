package be.poenzel.hardcorecoop.listeners;

import be.poenzel.hardcorecoop.HardcoreCoop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerListenersHC implements Listener {

    private HardcoreCoop main;

    public PlayerListenersHC(HardcoreCoop main){
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(!main.getPlayers().contains(player)) main.getPlayers().add(player);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event){

    }

}
