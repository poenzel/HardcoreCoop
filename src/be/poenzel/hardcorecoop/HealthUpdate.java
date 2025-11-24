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

        boolean absorption = false;
        Integer amplifier = 0;
        Integer duration = 0;

        for(Player p : main.getPlayers()){
            if(p.hasPotionEffect(PotionEffectType.ABSORPTION)){
                amplifier = p.getPotionEffect(PotionEffectType.ABSORPTION).getAmplifier();
                duration = p.getPotionEffect(PotionEffectType.ABSORPTION).getDuration();
                absorption = true;
                main.setAbsorption(p.getAbsorptionAmount());
            }
        }

        for(Player p : main.getPlayers()){
            p.setHealth(main.getHealth());
            if(main.getHunger()) p.setFoodLevel(main.getFoodLevel());
            if (absorption){
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, amplifier));
            }
        }
    }
}
