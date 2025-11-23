package be.poenzel.hardcorecoop;

import be.poenzel.hardcorecoop.listeners.PlayerListenersHC;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import be.poenzel.hardcorecoop.commands.CommandHC;

import java.util.ArrayList;
import java.util.List;

public class HardcoreCoop extends JavaPlugin {

    private StateHC state;
    private List<Player> players = new ArrayList<>();

    @Override
    public void onEnable() {
        System.out.println("The plugin HardcoreCoop is now running.");
        setState(StateHC.WAITING);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListenersHC(this), this);

        getCommand("hc").setExecutor(new CommandHC(this));


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
}
