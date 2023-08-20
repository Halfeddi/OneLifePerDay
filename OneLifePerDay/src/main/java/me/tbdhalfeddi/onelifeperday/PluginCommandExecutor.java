package me.tbdhalfeddi.onelifeperday;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PluginCommandExecutor implements org.bukkit.command.CommandExecutor
{
    private final OneLifePerDay plugin;
    public PluginCommandExecutor(OneLifePerDay plugin) {
        this.plugin = plugin;}
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

    //--------------------------------------------- COMMAND CALLER METHODS ---------------------------------------------

        if (command.getName().equalsIgnoreCase("restoreplayer")) {
            if (args.length == 1) {
                Player _player = Bukkit.getPlayer(args[0]);
                if (_player != null) {
                    String _playerUUID = _player.getUniqueId().toString();
                    restoreplayer(_playerUUID);
                    player.sendMessage("Restored player lives of " + args[0]);
                }else{
                    player.sendMessage("§cCited player was not found.");
                }


            } else {
                player.sendMessage("§cInvalid command usage. Usage: /restoreplayer <player>");
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("livesperplayer")) {
            if (args.length == 1) {
                try {
                    int count = Integer.parseInt(args[0]);
                    livesperplayer(count);
                    player.sendMessage("Lives per player set to " + count);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid argument. Usage: /livesperplayer <count>");
                }
            } else {
                player.sendMessage("§cInvalid command usage. Usage: /livesperplayer <count>");
            }
            return true;
        }
        else if (command.getName().equalsIgnoreCase("timezone")) {
            if (args.length == 1) {
                if (TimeZone.getTimeZone(args[0]).getID().equals("GMT")) {
                    player.sendMessage("§cInvalid time zone region ID. Please provide a valid region ID.");
                } else if (args[0].equals("local")) {
                    timezone(TimeZone.getDefault().getID());
                    player.sendMessage("Time zone set to " + TimeZone.getDefault().getID());
                } else {
                    timezone(args[0]);
                    player.sendMessage("Time zone set to " + args[0]);
                }
            }
            else {
                player.sendMessage("§cInvalid command usage. Usage: /timezone <regionID>");
            }
            return true;
        }
        else if (command.getName().equalsIgnoreCase("excludeplayer")) {
            if (args.length == 1) {
                if(!plugin.config.excludedPlayers.contains(args[0])) {
                    excludeplayer(args[0]);
                    player.sendMessage("Player excluded: " + args[0]);
                }else{
                    player.sendMessage("§cCited player is either already excluded or doesn't exists.");
                }
            } else {
                player.sendMessage("§cInvalid command usage. Usage: /excludeplayer <player>");
            }
            return true;
        }
        else if (command.getName().equalsIgnoreCase("includeplayer")) {
            if (args.length == 1) {
                if(plugin.config.excludedPlayers.contains(args[0])) {
                    includeplayer(args[0]);
                    player.sendMessage("Player included: " + args[0]);
                }else{
                    player.sendMessage("§cCited player is either already included or doesn't exists.");
                }
            } else {
                player.sendMessage("§cInvalid command usage. Usage: /includeplayer <player>");
            }
            return true;
        }
        // set LIFE RESTORE HOURS
        else if (command.getName().equalsIgnoreCase("livesrestorehours")) {
            if (args.length >= 1) {
                List<Integer> hours = new ArrayList<>();
                for (String arg : args) {
                    try {
                        int hour = Integer.parseInt(arg);
                        hours.add(hour);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid argument: " + arg);
                        return true;
                    }
                }
                livesrestorehours(hours);
                player.sendMessage("Life restore hours set to: " + hours);
            } else {
                player.sendMessage("§cInvalid command usage. Usage: /livesrestorehours <hour1> <hour2> ...");
            }
            return true;
        }
        // Add more else if blocks for additional commands

        return true;
    }


    

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////     C O M M A N D S     ////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void restoreplayer(String playerUUID){
        plugin.data.playerLives.put(playerUUID, plugin.config.livesPerPlayer);
        Bukkit.getBanList(BanList.Type.NAME).pardon(Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getName());
        plugin.dataYaml.set("data.playerLives", plugin.data.playerLives);
    }
    public void livesperplayer(Integer count) {
        plugin.config.livesPerPlayer = count;
        plugin.configYaml.set("config.livesPerPlayer", count);
        plugin.saveConfigFile();
    }
    public void timezone(String regionID) {
        plugin.config.timeZoneRegion = regionID;
        plugin.configYaml.set("config.timeZoneRegion", regionID);
        plugin.saveDataFile();
    }
    public void excludeplayer (String player) {
        plugin.config.excludedPlayers.add(player);
        plugin.configYaml.set("config.excludedPlayers", plugin.config.excludedPlayers);
        plugin.saveConfigFile();
    }
    public void includeplayer (String player) {
        plugin.config.excludedPlayers.remove(player);
        plugin.configYaml.set("config.excludedPlayers", plugin.config.excludedPlayers);
        plugin.saveConfigFile();
    }
    public void livesrestorehours(List<Integer> hours){
        plugin.config.lrHours = hours;
        plugin.getLiveRestoreDates();
        plugin.configYaml.set("config.lrHours", hours);
        plugin.dataYaml.set("data.lrDates", plugin.data.lrDates);
        plugin.saveConfigFile();
        plugin.saveDataFile();
    }

}
