package be.poenzel.hardcorecoop.listeners;

import be.poenzel.hardcorecoop.HardcoreCoop;
import be.poenzel.hardcorecoop.StateHC;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
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
        if(main.isState(StateHC.FROZEN)){
            Location current_location = player.getLocation();
            current_location.setWorld(Bukkit.getWorld("hc_world"));
            player.teleport(current_location);
        }
        if(!main.isState(StateHC.WAITING)) return;

        player.teleport(main.getLobbySpawn());
        player.setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onHealthRegain(EntityRegainHealthEvent event){
        if (!main.isState(StateHC.RUNNING)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Double amount = event.getAmount();
        if(event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) amount = amount/Math.max(main.getPlayers().size(),1);
        //if(event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.EATING)) amount = amount/Math.max(main.getPlayers().size()-1,1);
        Double targetHealth = player.getHealth();
        main.setHealth(min(main.getHealth()+amount,20));
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event){
        if (!main.isState(StateHC.RUNNING)) return;
        if (!main.getHunger()) return;
        Player player = (Player) event.getEntity();
        Integer foodLevel = player.getFoodLevel();
        //event.setCancelled(true);
        main.setFoodLevel(event.getFoodLevel());
        main.setSaturation(player.getSaturation());

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!main.isState(StateHC.RUNNING)) return;

        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();


        if(main.isHurt(player)){
            event.setCancelled(true);
            return;
        } else {
            main.addHurtPlayer(player);
        }


        Double absorbedDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
        Double damageValue = event.getFinalDamage() - absorbedDamage;

        Bukkit.broadcastMessage("§4[Hardcore Co-op] §8: §6" + player.getName() + "§8 took §c" + round(damageValue,1) + "§8 of damage");


        double health = main.getHealth();
        double absorption = main.getAbsorption();

        // If absorption = 0 => nothing is used from absorption and nothing is deduced from the damages. If absorption is 1 and Damage is 2, 1 is used. If absorption is 4 and Damage is 2, 2 is consumed.
        double usedFromAbsorption = Math.min(absorption, damageValue);
        absorption -= usedFromAbsorption;
        damageValue -= usedFromAbsorption;

        if (damageValue > 0) {
            health = Math.max(0, health - damageValue);
        }

        //Bukkit.broadcastMessage("Absorption : "+absorption +", Health : "+ health);

        if(health==0){
            PlayerInventory inv = player.getInventory();
            if(inv.getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING) || inv.getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)) {

                for(PotionEffect pe : player.getActivePotionEffects())
                    player.removePotionEffect(pe.getType());
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
                health = 1;
                absorption = 4;

                player.playEffect(EntityEffect.TOTEM_RESURRECT);

                if(inv.getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING)){
                    player.getInventory().setItemInMainHand(null);
                }
                else {
                    player.getInventory().setItemInOffHand(null);
                }

            }
        }

        main.setHealth(health);
        main.setAbsorption(absorption);
        main.setAbsorptionActive(absorption > 0);

        for(Player p : main.getPlayers()){
            if(absorption==0){
                p.removePotionEffect(PotionEffectType.ABSORPTION);
            }
            p.playHurtAnimation(0);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT,1f,1f);
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
        Bukkit.broadcastMessage("Type of portal event : "+ e.getCause().toString());
        Player player = (Player) e.getPlayer();
        switch (e.getCause()) {
            case NETHER_PORTAL:
                switch (fromWorld.getEnvironment()) {
                    case NORMAL:
                        //e.getTo().setWorld(Bukkit.getWorld("hc_nether"));
                        Location newTo = e.getTo();
                        newTo.setWorld(Bukkit.getWorld("hc_nether"));
                        e.setTo(newTo);
                        break;
                    case NETHER:
                        Location newToWorld = e.getFrom().multiply(8.0D); //Need to multiply X & Z only & get highest block at Y
                        newToWorld.setWorld(Bukkit.getWorld("hc_world"));
                        e.setTo(newToWorld);
                        break;
                    default :
                }
                break;
            case END_PORTAL:
                switch (fromWorld.getEnvironment()){
                    case NORMAL :
                        //e.getTo().setWorld(Bukkit.getWorld("hc_end"));
                        Location newTo = e.getTo();
                        newTo.setWorld(Bukkit.getWorld("hc_end"));
                        e.setTo(newTo);
                        break;
                    case THE_END:
                        //e.getTo().setWorld(Bukkit.getWorld("hc_world"));
                        Location newToWorld = e.getTo();
                        newToWorld.setWorld(Bukkit.getWorld("hc_world"));
                        e.setTo(newToWorld);
                        break;
                    default :

                }
                break;
            case UNKNOWN:
                if(fromWorld.getEnvironment() == World.Environment.THE_END){
                    Location newTo = player.getRespawnLocation();
                    if (!(newTo.getWorld().equals(Bukkit.getWorld("hc_world")))){
                        newTo = main.getHcSpawn();
                    }
                    newTo.setWorld(Bukkit.getWorld("hc_world"));
                    e.setTo(newTo);
                }
            default :
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){

        Player player = event.getPlayer();
        if(main.isState(StateHC.FINISHED)) {
            Location spectateLocation = player.getLocation();
            player.setRespawnLocation(spectateLocation);
            event.setRespawnLocation(spectateLocation);
            player.setGameMode(GameMode.SPECTATOR);

        }
        if(main.isState(StateHC.RUNNING) && event.getRespawnReason().equals(PlayerRespawnEvent.RespawnReason.END_PORTAL)) {
            Location respawnPlayer = player.getRespawnLocation();
            event.setRespawnLocation(main.getHcSpawn());
        }


    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
