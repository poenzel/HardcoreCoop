package be.poenzel.hardcorecoop;

import be.poenzel.hardcorecoop.listeners.PlayerListenersHC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import be.poenzel.hardcorecoop.commands.CommandHC;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HardcoreCoop extends JavaPlugin {

    private StateHC state;
    private List<Player> players = new ArrayList<>();
    private long timer = 0;
    private boolean hunger = true;
    private double health = 20;
    private Integer foodLevel = 20;
    private double absorption = 0;
    private boolean absorptionActive = false;
    private Location hcSpawn;
    private Location lobbySpawn;

    @Override
    public void onEnable() {
        System.out.println("The plugin HardcoreCoop is now running.");

        // TODO : read file to know if it was frozen
        File file = new File(this.getDataFolder(),"frozen_state.txt");

        if(file.exists()){
           try{
               List<String> lines = Files.readAllLines(file.toPath());
               StringBuilder file_txt = new StringBuilder();
               System.out.println("THIS IS CHECKING IF THE FILE EXISTS");
               for(String line: lines){
                   file_txt.append(line + " ");
               }
               if(file_txt.toString().contains("FROZEN")){
                   setState(StateHC.FROZEN);
                   System.out.println("THE PREVIOUS SESSION WAS FROZEN");
                   this.setCurrentTimer(readCurrentTimer());
               }
               else{
                   setState(StateHC.WAITING);
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
        }
        else {
            setState(StateHC.WAITING);
        }
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListenersHC(this), this);

        getCommand("hc").setExecutor(new CommandHC(this));
        World lobby = Bukkit.getWorld("world");
        Location spawnLobby = Bukkit.getWorld("world").getSpawnLocation();
        spawnLobby.setY(lobby.getHighestBlockYAt(spawnLobby));
        this.lobbySpawn = spawnLobby;
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),"gamerule logAdminCommands false");

    }


    @Override
    public void onDisable() {
        System.out.println("The plugin HardcoreCoop has stopped working.");
    }

    public void setState(StateHC state){
        this.state = state;
    }

    public boolean isState(StateHC state){
        return this.state == state;
    }

    public List<Player> getPlayers(){
        return this.players;
    }

    public void setCurrentTimer(long seconds){
        this.timer = seconds;
    }

    public long getCurrentTimer(){
        return this.timer;
    }

    public double getHealth(){
        return this.health;
    }

    public void setHealth(double health){
        this.health = health;
    }

    public double getAbsorption(){
        return this.absorption;
    }

    public void setAbsorption(double absorption){
        this.absorption = absorption;
    }

    public Integer getFoodLevel(){
        return this.foodLevel;
    }

    public void setFoodLevel(Integer foodLevel){
        this.foodLevel = foodLevel;
    }
    public boolean getHunger(){
        return this.hunger;
    }

    public void setHunger(boolean hunger){
        this.hunger = hunger;
    }

    public boolean isAbsorptionActive(){
        return this.absorptionActive;
    }

    public void setAbsorptionActive(boolean absorptionActive){
        this.absorptionActive = absorptionActive;
    }

    public void setHcSpawn(Location loc){
        this.hcSpawn=loc;
    }

    public Location getHcSpawn(){
        return this.hcSpawn;
    }

    public Location getLobbySpawn(){
        return this.lobbySpawn;
    }

    public void saveCurrentTimer(long seconds){
        File file = new File(this.getDataFolder(), "current_timer.txt");

        try (FileWriter writer = new FileWriter(file, false)){
            writer.write(Long.toString(seconds));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public long readCurrentTimer(){
        File file = new File(this.getDataFolder(), "current_timer.txt");
        if(file.exists()){
            try{
                List<String> lines = Files.readAllLines(file.toPath());
                if(lines.size() > 1) {
                    System.out.println("Too many lines in current_timer.txt file.");
                    return 0;
                }

                String line = lines.getFirst().trim(); // remove spaces/newline

                if (line.isEmpty()) {
                    System.out.println("current_timer.txt is empty.");
                    return 0;
                }

                return Long.parseLong(line);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return 0;
        }
        return 0;
    }

}
