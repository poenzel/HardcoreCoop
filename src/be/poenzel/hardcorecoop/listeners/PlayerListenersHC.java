package be.poenzel.hardcorecoop.listeners;

import be.poenzel.hardcorecoop.HardcoreCoop;
import be.poenzel.hardcorecoop.StateHC;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

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
    public void onHealthRegain(EntityRegainHealthEvent event){
        if (!main.isState(StateHC.RUNNING)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Double amount = event.getAmount();
        if(event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) amount = amount/Math.max(main.getPlayers().size(),1);
        Double targetHealth = player.getHealth();
        main.setHealth(min(main.getHealth()+amount,20));
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event){
        if (!main.isState(StateHC.RUNNING)) return;
        if (!main.getHunger()) return;
        Player player = (Player) event.getEntity();
        Integer foodLevel = player.getFoodLevel();
        event.setCancelled(true);
        main.setFoodLevel(event.getFoodLevel());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!main.isState(StateHC.RUNNING)) return;

        //TODO : Check if damage kill?

        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        // Double absorptionValue = event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
        Double absorbedDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
        //Double rawDamage = event.getDamage();
        Double damageValue = event.getFinalDamage() - absorbedDamage;

        // damageValue + absorption = value of dmg taken. In practice,FinalDamage = # of red hearts lost

        //Bukkit.broadcastMessage("Raw Original Damage : "+ rawDamage + ", AbsorbedDamage : "+ absorbedDamage + ", Final Damage : "+ event.getFinalDamage());
        Bukkit.broadcastMessage("§4[Hardcore Co-op] §8: §6" + player.getName() + "§8 took §c" + round(damageValue,1) + "§8 of damage");


        double health = main.getHealth();
        double absorption = main.getAbsorption();

        // If absorption = 0 => nothing is used from absorption and nothing is deduced from the damages. If absorption is 1 and Damage is 2, 1 is used. If absorption is 4 and Damage is 2, 2 is consumed.
        // Due to Absorption, FinalDamage is set to 0 if Absorption > damageValue. If > Absorption => translates normally. This is therefore hard to compute.
        double usedFromAbsorption = Math.min(absorption, damageValue);
        absorption -= usedFromAbsorption;
        damageValue -= usedFromAbsorption;

        if (damageValue > 0) {
            health = Math.max(0, health - damageValue);
        }

        //Bukkit.broadcastMessage("Absorption : "+absorption +", Health : "+ health);

        main.setHealth(health);
        main.setAbsorption(absorption);
        main.setAbsorptionActive(absorption > 0);

        for(Player p : main.getPlayers()){
            if(absorption==0){
                p.removePotionEffect(PotionEffectType.ABSORPTION);
            }
            p.playHurtAnimation(0);
        }

        if(health==0){
            main.setState(StateHC.FINISHED);
            Bukkit.broadcastMessage("Game Over. To restart, type /hc end to go back to lobby.");
        }
        event.setCancelled(true);


    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {

        if(!main.isState(StateHC.RUNNING)) return;
        World fromWorld = e.getFrom().getWorld();
        switch (e.getCause()) {
            case NETHER_PORTAL:
                switch (fromWorld.getEnvironment()) {
                    case NORMAL:
                        e.getTo().setWorld(Bukkit.getWorld("hc_nether"));
                        break;
                    case NETHER:
                        Location newTo = e.getFrom().multiply(8.0D);
                        newTo.setWorld(Bukkit.getWorld("hc_world"));
                        e.setTo(newTo);
                        break;
                }
            case END_PORTAL:
                switch (fromWorld.getEnvironment()){
                    case NORMAL :
                        e.getTo().setWorld(Bukkit.getWorld("hc_end"));
                        break;
                    case THE_END:
                        e.getTo().setWorld(Bukkit.getWorld("hc_world"));
                        break;
                }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        if(!main.isState(StateHC.FINISHED)) return;
        Player player = event.getPlayer();
        player.setGameMode(GameMode.SPECTATOR);
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
