package be.poenzel.hardcorecoop.commands;

import be.poenzel.hardcorecoop.HardcoreCoop;
import be.poenzel.hardcorecoop.HealthUpdate;
import be.poenzel.hardcorecoop.StateHC;
import be.poenzel.hardcorecoop.TimerHC;
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
                    player.sendMessage("Arguments missing. To get help, type /hc help.");
                }

                if (args.length >= 1){
                    if (args[0].equalsIgnoreCase("start")){
                        if(main.isState(StateHC.RUNNING)){
                            player.sendMessage("The session is already running. To end it, type /hc end.");
                            return true;
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
                        main.setState(StateHC.WAITING);
                    }

                    if (args[0].equalsIgnoreCase("help")){
                        player.sendMessage("Type /hc start to start playing in hardcore. Type /hc freeze to save your progress and quit the session. Type /hc end to end it. If you die, the session will automatically be ended.");
                    }

                    if (args[0].equalsIgnoreCase("current_timer")){
                        player.sendMessage("Current timer is : " + main.getCurrentTimer());
                    }

                    // Method to check setAbsorptionAmount behavior -> only works when Absorption is active. Capped to level of Absorption.
                    if (args[0].equalsIgnoreCase("absorption")){
                        if(args.length == 1){
                            player.sendMessage("Missing value for absorption.");
                        }
                        String absorption = args[1];
                        player.setAbsorptionAmount(Integer.parseInt(absorption));
                    }

                    if (args[0].equalsIgnoreCase("hunger")){
                        // TODO : Figure out of Hunger can be toggled on/off during session. Shouldn't have any impact.
                        /*
                        if(main.isState(StateHC.RUNNING)){
                            player.sendMessage("Cannot change hunger setting while in game.");
                            return false;
                        }
                        */
                        if(args.length == 1){
                            player.sendMessage("Missing value for hunger. Type /hc hunger false if you want the hunger bar not to be shared.");
                        }
                        String hungerValue = args[1];
                        main.setHunger(Boolean.parseBoolean(hungerValue));
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



}
