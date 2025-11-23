package be.poenzel.hardcorecoop.commands;

import be.poenzel.hardcorecoop.HardcoreCoop;
import be.poenzel.hardcorecoop.StateHC;
import be.poenzel.hardcorecoop.TimerHC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                        main.setState(StateHC.RUNNING);
                        TimerHC task = new TimerHC(main);
                        task.runTaskTimer(main, 0, 20);
                    }
                    if (args[0].equalsIgnoreCase("end")){
                        main.setState(StateHC.FINISHED);
                    }

                    if (args[0].equalsIgnoreCase("help")){
                        player.sendMessage("Type /hc start to start playing in hardcore. Type /hc save to save your progress. Type /hc end to end it. If you die, the session will automatically be ended.");
                    }
                }


            }
        }

        return false;
    }
}
