package com.esperia09.bukkit.hello;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class HelloPlugin extends JavaPlugin {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            ItemStack diamond = new ItemStack(Material.DIAMOND);
            ItemStack bricks = new ItemStack(Material.BRICK);
            bricks.setAmount(20);

            player.getInventory().addItem(bricks, diamond);
        }

//        Server server = Bukkit.getServer();
//        Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
        return super.onCommand(sender, command, label, args);
    }
}
