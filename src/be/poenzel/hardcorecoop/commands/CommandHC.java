package be.poenzel.hardcorecoop.commands;

import be.poenzel.hardcorecoop.HardcoreCoop;
import be.poenzel.hardcorecoop.HealthUpdate;
import be.poenzel.hardcorecoop.StateHC;
import be.poenzel.hardcorecoop.TimerHC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CommandHC implements CommandExecutor {

    private HardcoreCoop main;

    public CommandHC(HardcoreCoop hc) {
        this.main = hc;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
        if(sender instanceof Player) {

            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("hc")) {

                if (args.length == 0) {
                    player.sendMessage("§4[Hardcore Co-op] : Arguments missing. To get help, type /hc help.");
                }

                if (args.length >= 1){
                    if (args[0].equalsIgnoreCase("start")){
                        if(main.isState(StateHC.RUNNING)){
                            player.sendMessage("§4[Hardcore Co-op] : The session is already running. To end it, type /hc end.");
                            return true;
                        }
                        if(main.isState(StateHC.WAITING)){
                            Bukkit.broadcastMessage("§4[Hardcore Co-op] §e: Creating worlds...");
                            createWorlds();
                            Bukkit.broadcastMessage("§7Teleporting players");
                            Location hc_spawn = main.getHcSpawn();
                            for (Player p : main.getPlayers()) {
                                p.getInventory().clear();
                                p.teleport(hc_spawn);
                                p.setGameMode(GameMode.SURVIVAL);
                                p.setRespawnLocation(hc_spawn);
                            }
                            main.setFoodLevel(20);
                            main.setHealth(20);
                        }

                        // Once FINISHED, state has to go back to WAITING before being launched again.
                        if(main.isState(StateHC.WAITING) || main.isState(StateHC.FROZEN)) {
                            main.setState(StateHC.RUNNING);
                            TimerHC task = new TimerHC(main);
                            task.runTaskTimer(main, 0, 20);
                            HealthUpdate healthTask = new HealthUpdate(main);
                            healthTask.runTaskTimer(main,0,1);
                        }

                    }

                    if (args[0].equalsIgnoreCase("freeze")){
                        main.setState(StateHC.FROZEN);
                        File folder = main.getDataFolder();
                        if(!folder.exists()){
                            folder.mkdirs();
                        }
                        setFrozenState("FROZEN");
                    }

                    if (args[0].equalsIgnoreCase("end")){
                        File folder = main.getDataFolder();
                        if(folder.exists()) {
                            setFrozenState("MELTED");
                        }
                        main.setHealth(20);
                        main.setFoodLevel(20);
                        main.setState(StateHC.WAITING);
                        for(Player p : main.getPlayers()){
                            p.teleport(main.getLobbySpawn());
                            p.getInventory().clear();
                            p.setGameMode(GameMode.ADVENTURE);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch(InterruptedException e){
                            e.printStackTrace();
                        }
                        deleteWorlds();

                    }

                    if (args[0].equalsIgnoreCase("help")){
                        player.sendMessage("§4[Hardcore Co-op] §e : Type /hc start to start playing in Hardcore. Type /hc freeze to save your progress and interrupt the session. Type /hc end to end it. If you die, the session will automatically be ended.");
                    }

                    if (args[0].equalsIgnoreCase("get_world")){
                        Bukkit.broadcastMessage("You are in " + player.getWorld().getName());
                    }

                }

            }
        }

        return false;
    }


    public void setFrozenState(String state) {
        File file = new File(main.getDataFolder(), "frozen_state.txt");

        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createWorlds(){
        WorldCreator wc = new WorldCreator("hc_world");
        wc.environment(World.Environment.NORMAL);
        wc.generateStructures(true);
        World hcWorld = wc.createWorld();
        hcWorld.setDifficulty(Difficulty.HARD);
        Location hc_spawn = hcWorld.getSpawnLocation();
        Block highest_block = hcWorld.getHighestBlockAt(hc_spawn);
        hc_spawn.setY(highest_block.getY() + 1);

        main.setHcSpawn(hc_spawn);
        Bukkit.broadcastMessage("§4[Hardcore Co-op] §e : OverWorld created. Generating Nether...");
        WorldCreator wc_nether = new WorldCreator("hc_nether");
        wc_nether.environment(World.Environment.NETHER);
        wc_nether.generateStructures(true);
        World netherWorld = wc_nether.createWorld();
        netherWorld.setDifficulty(Difficulty.HARD);
        Bukkit.broadcastMessage("§4[Hardcore Co-op] §e : Nether created. Generating The End...");
        WorldCreator wc_end = new WorldCreator("hc_end");
        wc_end.environment(World.Environment.THE_END);
        wc_end.generateStructures(true);
        World endWorld = wc_end.createWorld();
        endWorld.setDifficulty(Difficulty.HARD);
        Bukkit.broadcastMessage("§4[Hardcore Co-op] §a : Worlds have been created. Starting soon !");
    }

    public void deleteWorlds(){
        Bukkit.getServer().unloadWorld("hc_world", false);
        deleteWorld(new File("hc_world"));
        Bukkit.getServer().unloadWorld("hc_nether", false);
        deleteWorld(new File("hc_nether"));
        Bukkit.getServer().unloadWorld("hc_end", false);
        deleteWorld(new File("hc_end"));
    }

    public void deleteWorld(File folder){
        if(!folder.exists()) return;
        if(folder.isFile()){
            folder.delete();
            return;
        }
        for(File file : folder.listFiles()){
            deleteWorld(file);
        }
        folder.delete();
    }

}
