package coffee.weneed.bukkit.shuklerception.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import coffee.weneed.bukkit.shuklerception.Shulkerception;

public class reload implements CommandExecutor {

    public Shulkerception main = null;
    public reload(Shulkerception main){
        this.main = main;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender.hasPermission("Shulkerception.reload")){
            main.reloadConfig();
            main.loadConfiguration();

            sender.sendMessage("[Shulkerception] config reloaded!");
        }

        return true;
    }

}
