# OneLifePerDay
**What is this?**
A plugin that bans players when they die or lose all their lives, and brings them back at a specific time everyday.

**What is its purpose?**
Serves as an alternative for hardcore minecraft in multiplayer, but the punishment is less severe; rather temporal than permanent. With the speciality of automatically lifting up at a given hour everyday. That way you encourage players to play within a certain time span to avoid a longer penalty.

**Do I need this?**
If you have friends that don't suck at minecraft and wish a stressfull challenge, and you don't want them to complete the game in a single day, but at the same time, you don't want to destroy their mood with permanent death like in vanilla Hardcore mode, then this plugin is for you.

**How do I use this?**
It is as easy as installing the .jar file in your plugins folder and restart the server. You can modify many of the configurations through commands. The timezone is assigned by default according to the server's local timezone.

**This plugin best combines with:**
- **ReviveMe** plugin by FavioFlashMc: [⚔️ ReviveMe 1.14 - 1.20.1 | PAPI Support | No Dependencies⭕ | SpigotMC - High Performance Minecraft](https://www.spigotmc.org/resources/%E2%9A%94%EF%B8%8F-reviveme-1-14-1-20-1-papi-support-no-dependencies%E2%AD%95.99030/) or, if you're poor: [ReviveMe [Beta] | SpigotMC - High Performance Minecraft](https://www.spigotmc.org/resources/reviveme-beta.78184/)
-  Basically any "player graves" plugin or datapack.

**Planned features:**
I don't plan to extend this plugin at all, as it is a really simple mechanic, but I'd expect to add different kind of punishments, like switching the gamemode to Spectator and so... You tell me if you really want it..!!.. :]

# Note
Yeah this plugin is free, do whatever you want with it. Use it, edit it, sell it, idc.

# Commands
- onelifeperday.**restoreplayer:** Restores a player's lives and unbans them.
   `/restoreplayer` `<username>`
- onelifeperday.**livesperplayer:** Set how many lives each player starts with.
   `/livesperplayer` `<count>`
- onelifeperday.**timezone:** Set the time zone for life-restore hours.
   All available timezones (Java): https://docs.oracle.com/middleware/12211/wcs/tag-ref/MISC/TimeZones.html
   `/timezone` `<regionID>`
- onelifeperday.**excludeplayer:** Exclude a player from the consequences of death.
   `/excludeplayer` `<player>`
- onelifeperday.**includeplayer:** Include a player into the consequences of death.
   `/includeplayer` `<player>`
- onelifeperday.**livesrestorehours:** Set the hours in a defined time zone where all dead players' lives are restored.
   `/livesrestorehours` `<hour1>` `<hour2>` ...
- minecraft.**pardon:** Lift up ban of a player (Works the same for this plugin).
   `/pardon` `<player>`
