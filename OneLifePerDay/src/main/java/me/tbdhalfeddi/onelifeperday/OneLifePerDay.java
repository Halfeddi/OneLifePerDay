package me.tbdhalfeddi.onelifeperday;

import org.bukkit.BanList;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class OneLifePerDay extends JavaPlugin implements Listener{

/////////////////////////////////////////////////// CONFIGURATION //////////////////////////////////////////////////////
    public static class Config {
        public List<Integer> lrHours = new ArrayList<>(Arrays.asList(0, 6, 12, 18)); // Hours: 12:00am, 6:00am, 12:00pm, 6:00pm
        public List<String> excludedPlayers = new ArrayList<>();
        public int livesPerPlayer = 1;
        //public enum Punishment { Ban, TurnToSpectator } public Punishment punishment = Punishment.Ban;
        public String timeZoneRegion = TimeZone.getDefault().getID();
    }
    public Config config = new Config();    public FileConfiguration configYaml;    private File configFile;
////////////////////////////////////////////////////// DATA ////////////////////////////////////////////////////////////
    public static class Data {
        public List<Date> lrDates = new ArrayList<>();
        public Map<String, Integer> playerLives = new HashMap<>();
    }
    public Data data = new Data();    public FileConfiguration dataYaml;   private File dataFile;
//    public void PutPlNameOrUUID(String nameOrUUID, int value){
//        Player player = null;
//        try {
//            UUID playerUUID = UUID.fromString(nameOrUUID);
//            player = Bukkit.getPlayer(playerUUID);
//            System.out.println("Valid UUID: " + playerUUID);
//        } catch (IllegalArgumentException e) {
//            System.out.println("Invalid UUID format");
//            // Check if the input corresponds to a valid online player's name
//            try{
//                player = Bukkit.getPlayerExact(nameOrUUID);
//                System.out.println("Valid player name: " + nameOrUUID);
//            }catch(NullPointerException e1){
//                System.out.println("Invalid player name");
//            }
//        }
//        if (player != null)
//            data.playerLives.put(player.getUniqueId() + ", " + player.getName() , value);
//        else
//            getLogger().severe("Username or UUID \"" + nameOrUUID + "\" are not valid.");
//    }
    public String getPlNameOrUUID(String nameOrUUID) {
        for (Map.Entry<String, Integer> entry : data.playerLives.entrySet()) {
            String[] parts = entry.getKey().split(", ");
                if (nameOrUUID.equals(parts[0])) return parts[0]; // Return the UUID part
                else if (nameOrUUID.equals(parts[1])) return parts[1]; // Return the username part
        }
        getLogger().info("Username or UUID \"" + nameOrUUID + "\" was not found in PlayerLives registry. Probably it is a new user.");
        return ""; // No matching key found
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onEnable()
    {
        getLogger().warning("OneLifePerDay plugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);

        loadConfigFile();
        loadDataFile();

        CommandExecutor executor = new PluginCommandExecutor(this);
            registerCommand("restoreplayer", executor);
            registerCommand("livesperplayer", executor);
            registerCommand("timezone", executor);
            registerCommand("excludeplayer", executor);
            registerCommand("includeplayer", executor);
            registerCommand("livesrestorehours", executor);

        getLiveRestoreDates();
        scheduleDailyRestoreDates();
    }
    private void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            getLogger().warning("Failed to register command: " + commandName);
        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//--------------------------------------------- LOAD PLUGIN FILES  -----------------------------------------------------
    private void loadConfigFile() {
        configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            saveDefaultConfigFile();
        }

        configYaml = YamlConfiguration.loadConfiguration(configFile);

        // Load data from the config.yml into the Config object
        config.lrHours = configYaml.getIntegerList("config.lrHours");
        config.excludedPlayers = configYaml.getStringList("config.excludedPlayers");
        config.livesPerPlayer = configYaml.getInt("config.livesPerPlayer");
        config.timeZoneRegion = configYaml.getString("config.timeZoneRegion");
    }
    private void loadDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");

        if (!dataFile.exists()) {
            saveDefaultDataFile();
        }

        dataYaml = YamlConfiguration.loadConfiguration(dataFile);

        // Load data from the data.yml into the Data object
        data.lrDates.clear(); // Clear the list before adding to it
        List<Long> lrDatesTimestamps = dataYaml.getLongList("data.lrDates");
        for (Long timestamp : lrDatesTimestamps) {
            data.lrDates.add(new Date(timestamp));
        }

        data.playerLives.clear(); // Clear the map before adding to it
        ConfigurationSection playerLivesSection = dataYaml.getConfigurationSection("data.playerLives");
        if (playerLivesSection != null) {
            for (String key : playerLivesSection.getKeys(false)) {
                int lives = playerLivesSection.getInt(key);
                data.playerLives.put(key, lives);
            }
        }
    }
//--------------------------------------------- SAVE PLUGIN FILES  -----------------------------------------------------
    public void saveConfigFile() {
        configYaml.set("config.lrHours", config.lrHours);
        configYaml.set("config.excludedPlayers", config.excludedPlayers);
        configYaml.set("config.livesPerPlayer", config.livesPerPlayer);
        configYaml.set("config.timeZoneRegion", config.timeZoneRegion);

        try {
            configYaml.save(configFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error loading config", e);
        }
    }
    private void saveDefaultConfigFile() {
        configYaml = YamlConfiguration.loadConfiguration(configFile);
        saveConfigFile();
    }
    public void saveDataFile() {
        dataYaml.set("data.lrDates", data.lrDates);
        ConfigurationSection playerLivesSection = dataYaml.createSection("data.playerLives");
        for (Map.Entry<String, Integer> entry : data.playerLives.entrySet()) {
            playerLivesSection.set(entry.getKey(), entry.getValue());
        }

        try {
            dataYaml.save(dataFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error loading data", e);
        }
    }
    private void saveDefaultDataFile() {
        dataYaml = YamlConfiguration.loadConfiguration(dataFile);
        saveDataFile();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void scheduleDailyRestoreDates() {
        // Calculate the ticks until 00:00 AM (midnight)
        long currentTicks = getServer().getCurrentTick();
        long ticksUntilMidnight = (currentTicks + 24000 - (24000 - 18000)) % 24000;

        // Create a BukkitRunnable to run getLiveRestoreDates() asynchronously
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                getLiveRestoreDates();
            }
        };
        // Schedule the task to run once every day at 00:00 AM (midnight)
        task.runTaskTimerAsynchronously(this, ticksUntilMidnight, 24000);
        getLogger().info("Successfully updated the RestoreDates for today and tomorrow!");
    }
    public void getLiveRestoreDates() {
        TimeZone timeZone = TimeZone.getTimeZone(config.timeZoneRegion);
        Calendar cal = Calendar.getInstance(timeZone);
        data.lrDates.clear();   // Clear the existing lrDates list

        for (int dayOffset = 0; dayOffset <= 1; dayOffset++) {
            // Set the calendar to the current day or the next day
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, dayOffset);
            // Loop through the configured lrHours
            for (Integer hour : config.lrHours) {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                data.lrDates.add(cal.getTime());
            }
        }
        saveDataFile();
    }
    @Override
    public  void onDisable()
    {
        saveConfigFile();
        saveDataFile();
        getLogger().warning("OneLifePerDay plugin has been disabled!");
    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event){
        Player player = event.getPlayer();
        Date lrDate = getNextLrDate();
        String formattedTime = getTimeUntilNextLrDate(lrDate);

        if (Bukkit.getBanList(BanList.Type.NAME).isBanned(player.getName())) {
            // The player is banned
            String banMessage = config.livesPerPlayer > 1 ?
                    "You are out of lives! You will be able to rejoin after " + (ChatColor.BLUE + formattedTime)
                    : "You died! You will be able to rejoin after " + (ChatColor.BLUE + formattedTime);
            event.setKickMessage(banMessage);
            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();
        Date lrDate = getNextLrDate();
        String formattedTime = getTimeUntilNextLrDate(lrDate);

        if (!config.excludedPlayers.contains(playerName))
        {
            if (!data.playerLives.containsKey(playerUUID + ", " + playerName)) {
            // IF the player joins for the first time.
                    if (!isByPassingBan(player)) {
                    // IF the player is not bypassing a ban.
                        data.playerLives.put(playerUUID + ", " + playerName, config.livesPerPlayer);
                        player.sendMessage("[First time logged] You spare " + (config.livesPerPlayer > 1 ? config.livesPerPlayer + " lives." : "one life."));
                        saveDataFile();
                    }
                }
            else if (data.playerLives.get(playerUUID + ", " + playerName) == 0) {
            // IF the player joins but has no lives left.
                    terminatePlayer(player, false);
                }
            else if (data.playerLives.get(playerUUID + ", " + playerName) == -1){
            // IF the player joins back after being terminated.
                    data.playerLives.put(playerUUID + ", " + playerName , config.livesPerPlayer);
                    player.sendMessage("Back for more? You spare " + (config.livesPerPlayer > 1 ? config.livesPerPlayer + " lives to spare." : "one life."));
                    saveDataFile();
            }else{
            // IF the player joins normally.
                    player.sendMessage("Welcome back. You spare " + (data.playerLives.get(playerUUID + ", " + playerName) > 1 ? data.playerLives.get(playerUUID + ", " + playerName) + " lives left." : "one life."));
            }
        }
    //------------------------------------------------------------------------------------------------------------------
        if (data.playerLives.get(playerUUID + ", " + playerName) == -2) {
            // The player is punished to Spectator mode
            String deathMessage = config.livesPerPlayer > 1 ?
                    "You are still out of lives! You will respawn after " + (ChatColor.BLUE + formattedTime)
                    : "You are still dead! You will respawn after " + (ChatColor.BLUE + formattedTime);
            player.sendMessage(deathMessage);
        }
    }
    private boolean isByPassingBan (Player player) {
        String playerName = getPlNameOrUUID(player.getUniqueId().toString());
        try {
            if (Bukkit.getBanList(BanList.Type.NAME).isBanned(playerName)) {
                terminatePlayer(player, true);
                return true;
            }else
                return false;
        }catch (IllegalArgumentException e){
            return  false;
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();

        if (data.playerLives.containsKey(playerUUID + ", " + playerName) && !config.excludedPlayers.contains(playerName))
        {
            int remainingLives = data.playerLives.get(playerUUID + ", " + playerName);
            if (remainingLives > 0) {
                data.playerLives.put(playerUUID + ", " + playerName , remainingLives - 1);
                player.sendMessage((remainingLives > 1) ?
                        ChatColor.RED + "You died! Remaining lives: " + (remainingLives - 1)
                        : ChatColor.RED + "You died! Sending into the Shadow Realm...");
                getLogger().info(player.getName() + " has died! Remaining lives: " + (remainingLives - 1));
            }
        }
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();
        if (data.playerLives.get(playerUUID + ", " + playerName) == 0){         // IF the player respawns with no lives left
            terminatePlayer(player, false);
        }
    }
//----------------------------------------------------------------------------------------------------------------------
    private void terminatePlayer(Player player, Boolean triedToBypass)
    {
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();
        getLogger().info(player.getName() + " has been terminated!");
        Date lrDate = getNextLrDate();
        String formattedTime = getTimeUntilNextLrDate(lrDate);

        //if (config.punishment == Config.Punishment.Ban) {
            String kickMessage =
                (!triedToBypass)
                ? ((config.livesPerPlayer > 1)
                    ? "You are out of lives! You will be able to rejoin after " + (ChatColor.BLUE + formattedTime)
                    : "You died! You will be able to rejoin after " + (ChatColor.BLUE + formattedTime))
                : "You really tried... huh? You will be able to rejoin after " + (ChatColor.BLUE + formattedTime);

            Bukkit.getBanList(BanList.Type.NAME).addBan(playerName, kickMessage, lrDate, null);
            player.kickPlayer(kickMessage);
            data.playerLives.put(playerUUID + ", " + playerName, -1);

        //}
        //else if (config.punishment == Config.Punishment.TurnToSpectator) {
        //    String deathMessage = config.livesPerPlayer > 1 ?
        //        "You are out of lives! You will respawn after " + (ChatColor.BLUE + formattedTime)
        //        : "You died! You will respawn after " + (ChatColor.BLUE + formattedTime);
        //    player.setGameMode(GameMode.SPECTATOR);
        //    player.sendMessage(deathMessage);
        //    data.playerLives.put(playerUUID + ", " + playerName, -2);
        //}
        saveDataFile();
    }

//----------------------------------------------------------------------------------------------------------------------
    private Date getNextLrDate()
    {
        Date currentDate = new Date();
        TimeZone timeZone = TimeZone.getTimeZone(config.timeZoneRegion);
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(currentDate);

        // Find the next restore time based on the predefined hourDates array
        for (Date date : data.lrDates) {
            if (date.after(currentDate)) {  cal.setTime(date);  return cal.getTime();   }
        }
        // If no future restore time is found, return 00:00 of the next day.
        cal.setTime(data.lrDates.get(0));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }
    private String getTimeUntilNextLrDate(Date nextlrDate) {
        Date currentTime = new Date();
        long timeDifferenceInMillis = nextlrDate.getTime() - currentTime.getTime();

        long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceInMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceInMillis - TimeUnit.HOURS.toMillis(hours));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceInMillis - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes));

        return String.format("%02dh:%02dm:%02ds", hours, minutes, seconds);
    }
}

