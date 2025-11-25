package be.poenzel.hardcorecoop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class HealthUpdate extends BukkitRunnable {

    private int timer = 0;
    private HardcoreCoop main;

    public HealthUpdate(HardcoreCoop main){
        this.main = main;
    }

    @Override
    public void run() {

        if (!main.isState(StateHC.RUNNING)){
            cancel();
        }

        Integer amplifier = 0;
        Integer duration = 0;
        boolean new_absorption_effect = false;

        for(Player p : main.getPlayers()){
            // Will not be updated if the effect was already running => no reset of absorption level
            if((!main.isAbsorptionActive()) && p.hasPotionEffect(PotionEffectType.ABSORPTION)){
                amplifier = p.getPotionEffect(PotionEffectType.ABSORPTION).getAmplifier();
                duration = p.getPotionEffect(PotionEffectType.ABSORPTION).getDuration();
                main.setAbsorptionActive(true);
                new_absorption_effect = true;
                main.setAbsorption(p.getAbsorptionAmount());
            }
        }

        for(Player p : main.getPlayers()){
            p.setHealth(main.getHealth());
            if(main.getHunger()) p.setFoodLevel(main.getFoodLevel());
            if (main.isAbsorptionActive()){
                if(new_absorption_effect && !(p.hasPotionEffect(PotionEffectType.ABSORPTION))) p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, amplifier));
                p.setAbsorptionAmount(main.getAbsorption());
                //Bukkit.broadcastMessage("Current absorption level for " + p.getName() + " is : " + p.getAbsorptionAmount());
            }

            // Update if potion effect faded
            if(!p.hasPotionEffect(PotionEffectType.ABSORPTION)) {
                main.setAbsorptionActive(false);
            }

        }



    }
}
