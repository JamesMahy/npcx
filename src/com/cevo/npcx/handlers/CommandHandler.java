package com.cevo.npcx.handlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


import net.gamerservices.npcx.myFactionEntry;
import net.gamerservices.npcx.myLoottable;
import net.gamerservices.npcx.myLoottable_entry;
import net.gamerservices.npcx.myMerchant;
import net.gamerservices.npcx.myMerchant_entry;
import net.gamerservices.npcx.myNPC;
import net.gamerservices.npcx.myPathgroup;

import net.gamerservices.npcx.myPlayer;
import net.gamerservices.npcx.mySpawngroup;
import net.gamerservices.npcx.myTriggerword;
import net.gamerservices.npcx.myZone;
import net.gamerservices.npcx.myZoneMember;
import net.gamerservices.npcx.npcx;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


import com.cevo.npcx.security.Security;
import com.cevo.npcx.universe.Universe;

public class CommandHandler {
	private Universe universe;
	private Security security;
	private npcx parent;
	private Logger logger;
	
	public CommandHandler(npcx parent){
		this.parent = parent;		
		this.universe = this.parent.getUniverse();
		this.security = this.parent.getSecurity();
		this.logger = this.parent.getLogger();
	}
	
	public Boolean doCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if (this.universe.nations.equals("true")) {
            if (command.getName().toLowerCase().equals("civ") || command.getName().toLowerCase().equals("c")) {
                if (!(sender instanceof Player)) {return false; }
                
                Player player = (Player) sender;
                if (args.length < 1) {
                    player.sendMessage("/civ buy  - buys a civilisations area");
                    player.sendMessage("/civ add playername - adds a player to a civilisation area");
                    player.sendMessage("/civ balance - lists your bank balance");
                    player.sendMessage("/civ research - lists available research");
                    player.sendMessage("/civ here - lists info about the civilisation area you are at");
                    player.sendMessage("/civ abandon - abandons the civilisation area");
                    player.sendMessage("/civ pay playername amount - pays a player an amount");
                    player.sendMessage("/civ name name - name a civilisation area");
                    player.sendMessage("/civ toggle - toggle on/off area info");
                    player.sendMessage("/civ faction - lists your faction status");

                    return false;
                }

                String subCommand = args[0].toLowerCase();

                if (subCommand.equals("faction")) {
                    this.universe.sendFactionList(player);
                }
                if (subCommand.equals("research")) {
                    int playerx = this.universe.getZoneCoord(player.getLocation().getX());
                    int playerz = this.universe.getZoneCoord(player.getLocation().getZ());
                    if (args.length < 2) {
                        player.sendMessage("Insufficient arguments /civ research list");
                        player.sendMessage("Insufficient arguments /civ research start researchname");
                        player.sendMessage("Insufficient arguments /civ research cancel researchname");
                        player.sendMessage("Insufficient arguments /civ research unlocked ");
                        return false;
                    }

                    if (args[1].equals("start")) {
                        if (args.length < 3) {
                            player.sendMessage("Insufficient arguments /civ research start researchname");

                        } else {
                            try {
                                int researchid = this.universe.getResearchID(args[2]);
                                if (this.universe.startResearch(player, researchid)) {

                                } else {
                                    player.sendMessage("Incorrect research name");
                                }
                            } catch (Exception e) {
                                player.sendMessage("Incorrect research name");
                                return false;
                            }
                        }
                    }

                    if (args[1].equals("cancel")) {
                        if (args.length < 3) {
                            player.sendMessage("Insufficient arguments /civ research cancel researchname");

                        } else {
                            try {
                                int researchid = this.universe.getResearchID(args[2]);
                                this.universe.cancelResearch(player, researchid);
                            } catch (Exception e) {
                                player.sendMessage("Incorrect research name");
                                return false;
                            }
                        }
                    }

                    if (args[1].equals("list")) {
                        this.universe.sendResearchList(player);
                    }
                    if (args[1].equals("unlocked")) {
                        this.universe.sendUnlockedList(player);
                    }

                }

                if (subCommand.equals("toggle")) {
                    player.sendMessage("* Toggled zone display to: " + this.universe.togglePlayerZoneInfo(player));
                }

                if (subCommand.equals("name")) {
                    int playerx = this.universe.getZoneCoord(player.getLocation().getX());
                    int playerz = this.universe.getZoneCoord(player.getLocation().getZ());
                    if (args.length < 2) {
                        player.sendMessage("Insufficient arguments /civ name name");
                        return false;
                    }
                    int count = 0;

                    String fullname = "";
                    int current = 2;
                    while (current <= args.length) {
                        fullname = fullname + args[current - 1] + " ";
                        current++;
                    }

                    fullname = fullname.substring(0, fullname.length() - 1);

                    for (myZone z : this.universe.zones) {
                        if (z.x == playerx && z.z == playerz) {
                            count++;
                            // are they the owner?
                            if (z.ownername.equals(player.getName())) {
                                this.universe.setZoneName(z.id, fullname);
                                player.sendMessage("Zone name set");
                                player.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "* The land [" + z.x + ":" + z.z + "] shall be forever known as " + fullname + " as decreed by " + player.getName());

                                return true;

                            } else {
                                player.sendMessage("You cannot name this civilization area as you are not the owner (" + this.universe.getZoneOwnerName(z.id) + ")!");
                                return false;
                            }
                        }
                    }
                    if (count == 0) {
                        player.sendMessage("That zone does not exist");

                    }
                }

                if (subCommand.equals("balance")) {
                    myPlayer player2 = this.universe.getmyPlayer(player);
                    double balance = player2.getPlayerBalance(player);
                    player.sendMessage("* Your balance is: " + balance);
                }

                if (subCommand.equals("pay")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /civ pay playername amount");
                        return false;
                    }
                    int amount = 0;
                    // Is their amount correct?
                    try {
                        amount = Integer.parseInt(args[2]);

                    } catch (NumberFormatException ex) {
                        player.sendMessage("Cannot pay that amount");
                        return false;
                    }

                    // Check amount and send
                    if (amount > 0) {
                        myPlayer me = this.universe.getmyPlayer(player);
                        myPlayer recipient = this.universe.getmyPlayer(args[1]);

                        if (recipient != null && me != null && recipient.player != null && me.player != null) {
                            double balanceme = me.getPlayerBalance(me.player);

                            if (balanceme >= amount) {
                                // Change accounts
                                recipient.addPlayerBalance(recipient.player, amount);
                                recipient.player.sendMessage(ChatColor.LIGHT_PURPLE + "* " + ChatColor.YELLOW + me.player.getName() + ChatColor.LIGHT_PURPLE + " just paid you " + ChatColor.YELLOW + amount);
                                me.subtractPlayerBalance(me.player, amount);
                                me.player.sendMessage(ChatColor.LIGHT_PURPLE + "* " + ChatColor.YELLOW + recipient.player.getName() + ChatColor.LIGHT_PURPLE + " just received your payment of:" + ChatColor.YELLOW + amount);

                            }

                        } else {
                            player.sendMessage("Sorry one of those accounts doesn't exist or is offline");
                        }
                    } else {
                        player.sendMessage("Cannot pay that amount");
                    }
                }

                if (subCommand.equals("add")) {
                    int playerx = this.universe.getZoneCoord(player.getLocation().getX());
                    int playerz = this.universe.getZoneCoord(player.getLocation().getZ());
                    if (args.length < 2) {
                        player.sendMessage("Insufficient arguments /civ add playername");
                        return false;
                    }
                    int count = 0;
                    if (!args[1].equals(player.getName())) {
                        player.sendMessage("Searching for zones...");

                        for (myZone z : this.universe.zones) {
                            if (z.x == playerx && z.z == playerz) {
                                count++;
                                player.sendMessage("Located your zone.. checking privileges...");
                                // are they the owner?
                                if (z.ownername.equals(player.getName())) {
                                    player.sendMessage("You are the owner");

                                    // are they in alraedy
                                    if (this.universe.isZoneMember(z.id, args[1])) {
                                        player.sendMessage("Sorry that player is already in this civilisation!");
                                        return false;

                                    } else {
                                        this.universe.addZoneMember(z.id, args[1]);
                                        player.sendMessage("Player added to civilization!!");
                                        return true;
                                    }
                                } else {
                                    player.sendMessage("You cannot add someone to this civilization as you are not the owner (" + this.universe.getZoneOwnerName(z.id) + ")!");
                                }
                            }
                        }
                        if (count == 0) {
                            player.sendMessage("That zone does not exist");

                        }
                    } else {
                        player.sendMessage("You cannot be a member of a town you own!");
                        return false;
                    }
                }

                if (subCommand.equals("buy")) {
                    int cost = 25000;
                    myPlayer mp = this.universe.getmyPlayer(player);

                    if (cost <= mp.getPlayerBalance(player)) {

                        Chunk c = player.getLocation().getWorld().getChunkAt(player.getLocation());

                        myZone z = this.universe.getZoneFromChunkAndLoc(this.universe.getZoneCoord(player.getLocation().getX()), this.universe.getZoneCoord(player.getLocation().getZ()), player.getLocation().getWorld());
                        if (z != null) {
                            if (z.ownername.equals("")) {
                                z.setOwner(player.getName());
                                z.name = player.getName() + "s land";
                                player.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "* " + player.getName() + "'s civilisation borders have expanded at [" + z.x + ":" + z.z + "]");
                                player.sendMessage("Thanks! That's " + ChatColor.YELLOW + cost + ChatColor.WHITE + " total coins!");
                                mp.subtractPlayerBalance(player, cost);
                                player.sendMessage("You just bought region: [" + ChatColor.LIGHT_PURPLE + z.x + "," + z.z + "" + ChatColor.WHITE + "]!");

                                this.universe.setPlayerLastChunkX(player, z.x);
                                this.universe.setPlayerLastChunkZ(player, z.z);
                                this.universe.setPlayerLastChunkName(player, z.name);

                            } else {

                                player.sendMessage("Sorry this zone has already been purchased by another Civ");
                            }

                        } else {
                            player.sendMessage("Failed to buy zone at your location - target zone does not exist");

                        }
                    } else {
                        player.sendMessage("You don't have enough to buy this plot (" + ChatColor.YELLOW + "25000" + ChatColor.WHITE + ")!");
                    }
                }

                if (subCommand.equals("abandon")) {
                    myZone z = this.universe.getZoneFromChunkAndLoc(this.universe.getZoneCoord(player.getLocation().getX()), this.universe.getZoneCoord(player.getLocation().getZ()), player.getLocation().getWorld());
                    if (z != null) {
                        if (z.ownername.equals(player.getName())) {
                            z.setOwner("");
                            z.name = "Abandoned land";

                            for (myZoneMember zm : this.universe.zonemembers.values()) {
                                if (zm.zoneid == z.id) {
                                    // member of town
                                    this.universe.removeZoneMember(zm.id);
                                    zm = null;

                                }
                            }

                            player.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "* " + player.getName() + " has lost one of his civilizations!");
                            player.sendMessage("Thanks! Here's " + ChatColor.YELLOW + 5000 + ChatColor.WHITE + " coin from the sale of our land!");
                            myPlayer mp = this.universe.getmyPlayer(player);
                            mp.addPlayerBalance(player, 5000);
                            player.sendMessage("You just released region: [" + ChatColor.LIGHT_PURPLE + z.x + "," + z.z + "" + ChatColor.WHITE + "]!");

                            this.universe.setPlayerLastChunkX(player, z.x);
                            this.universe.setPlayerLastChunkZ(player, z.z);
                            this.universe.setPlayerLastChunkName(player, "Abandoned land");

                        } else {

                            player.sendMessage("Sorry this zone is not yours to abandon");
                        }

                    } else {
                        player.sendMessage("Failed to abandon zone at your location - target zone does not exist");

                    }
                }

                if (subCommand.equals("here")) {
                    int playerx = this.universe.getZoneCoord(player.getLocation().getX());
                    int playerz = this.universe.getZoneCoord(player.getLocation().getZ());

                    for (myZone z : this.universe.zones) {
                        if (z.x == playerx && z.z == playerz) {
                            player.sendMessage("[" + ChatColor.LIGHT_PURPLE + "" + z.x + "," + z.z + "" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "" + z.name);
                            player.sendMessage("Owner: " + ChatColor.YELLOW + "" + z.ownername);

                            for (myZoneMember ze : this.universe.zonemembers.values()) {
                                if (ze.zoneid == z.id) {
                                    player.sendMessage("[" + ChatColor.LIGHT_PURPLE + "" + ze.playename + "" + ChatColor.WHITE + " - member");
                                } else {
                                    // zone not in list
                                }
                            }

                        } else {
                            // zone not in list
                        }
                    }

                }

            }
        }

        // END OF CIV MENU

        // ops only

        try {

            // 
            // NPCX COMMAND MENU
            //

            if (!command.getName().toLowerCase().equals("npcx") && !command.getName().toLowerCase().equals("n")) {

            return false; }

            if (!(sender instanceof Player)) {

            return false; }

            if (this.security.isAdmin(((Player) sender)) == false) {return false; }

            Player player = (Player) sender;

            if (args.length < 1) {
                player.sendMessage("Insufficient arguments /npcx spawngroup");
                player.sendMessage("Insufficient arguments /npcx faction");
                player.sendMessage("Insufficient arguments /npcx loottable");
                player.sendMessage("Insufficient arguments /npcx npc");
                player.sendMessage("Insufficient arguments /npcx pathgroup");
                player.sendMessage("Insufficient arguments /npcx merchant");
                player.sendMessage("Insufficient arguments /npcx civ");

                return false;
            }

            String subCommand = args[0].toLowerCase();
            // debug: logger.log(Level.WARNING, "npcx : " +
            // command.getName().toLowerCase() + "(" + subCommand + ")");

            Location l = player.getLocation();

            if (subCommand.equals("debug")) {

            }

            if (subCommand.equals("research")) {

                if (args[1].equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx research create researchname prereqresearchname hourstocomplete cost");
                        return false;

                    } else {

                        this.universe.createResearch(args[2], args[3], args[4], args[5]);
                        player.sendMessage("debug: created");
                        return true;
                    }
                }

                if (args[1].equals("list")) {
                    this.universe.sendAllResearchList(player);

                }

            }

            if (subCommand.equals("spawngroup")) {

                // Overview:
                // A spawngroup is like a container. It contains many npcs and
                // any one of them could spawn randomly.
                // If you placed just one npc in the group only one npc would
                // spawn. This allows you to create 'rare' npcs

                // Spawngroups need to be assigned to a location with
                // 'spawngroup place' Once assigned that group
                // will spawn in that location and remain stationary

                // If a path is assigned to the spawn group, the npc will follow
                // the path continuously after spawning
                // at the location of 'spawngroup place'

                // todo: functionality
                // creates a new spawngroup with name
                // adds an npc to a spawngroup with a chance
                // makes the spawngroup spawn at your location
                // assigns a path to the spawngroup

                if (args.length < 2) {
                    player.sendMessage("Insufficient arguments /npcx spawngroup create spawngroupname");
                    player.sendMessage("Insufficient arguments /npcx spawngroup add spawngroupid npcid");
                    player.sendMessage("Insufficient arguments /npcx spawngroup pathgroup spawngroupid pathgroupid");
                    player.sendMessage("Insufficient arguments /npcx spawngroup list [name]");
                    player.sendMessage("Insufficient arguments /npcx spawngroup updatepos spawngroupid");
                    player.sendMessage("Insufficient arguments /npcx spawngroup delete spawngroupid");
                    player.sendMessage("Insufficient arguments /npcx version");

                    return false;

                }

                if (args[1].equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx spawngroup create spawngroupname");

                    } else {
                        player.sendMessage("Created spawngroup: " + args[2]);

                        double x = player.getLocation().getX();
                        double y = player.getLocation().getY();
                        double z = player.getLocation().getZ();
                        float pitch = player.getLocation().getPitch();
                        float yaw = player.getLocation().getYaw();

                        PreparedStatement stmt = this.universe.conn.prepareStatement("INSERT INTO spawngroup (name,x,y,z,pitch,yaw,world) VALUES (?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
                        stmt.setString(1, args[2]);
                        stmt.setString(2, Double.toString(x));
                        stmt.setString(3, Double.toString(y));
                        stmt.setString(4, Double.toString(z));
                        stmt.setString(5, Double.toString(pitch));
                        stmt.setString(6, Double.toString(yaw));
                        stmt.setString(7, player.getLocation().getWorld().getName());

                        stmt.executeUpdate();
                        ResultSet keyset = stmt.getGeneratedKeys();
                        int key = 0;
                        if (keyset.next()) {
                            // Retrieve the auto generated key(s).
                            key = keyset.getInt(1);

                        }
                        stmt.close();

                        player.sendMessage("Spawngroup ID [" + key + "] now active at your position");
                        System.out.println("npcx : + cached new spawngroup(" + args[2] + ")");

                    }

                }

                if (args[1].equals("delete")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx spawngroup delete spawngroupid");

                    } else {
                        int count = 0;
                        for (mySpawngroup spawngroup : this.universe.spawngroups.values()) {
                            if (spawngroup.id == Integer.parseInt(args[2])) {
                                spawngroup.DBDelete();
                                count++;
                            }
                        }
                        player.sendMessage("Deleted cached " + count + " spawngroups.");

                    }

                }

                if (args[1].equals("pathgroup")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx spawngroup pathgroup spawngroupid pathgroupid");

                    } else {

                        PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE spawngroup SET pathgroupid = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        stmt.executeUpdate();

                        player.sendMessage("Updated pathgroup ID:" + args[3] + " on spawngroup ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("add")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx spawngroup add spawngroup npcid");

                    } else {
                        player.sendMessage("Added to spawngroup " + args[2] + "<" + args[3] + ".");

                        // add to database

                        PreparedStatement s2 = this.universe.conn.prepareStatement("INSERT INTO spawngroup_entries (spawngroupid,npcid) VALUES (?,?);", Statement.RETURN_GENERATED_KEYS);
                        s2.setString(1, args[2]);
                        s2.setString(2, args[3]);

                        s2.executeUpdate();
                        player.sendMessage("NPC ID [" + args[3] + "] added to group [" + args[2] + "]");

                        // add to cached spawngroup
                        for (mySpawngroup sg : universe.spawngroups.values()) {
                            if (sg.id == Integer.parseInt(args[2])) {

                                PreparedStatement stmtNPC = this.universe.conn.prepareStatement("SELECT * FROM npc WHERE id = ?;");
                                stmtNPC.setString(1, args[3]);
                                stmtNPC.executeQuery();
                                ResultSet rsNPC = stmtNPC.getResultSet();
                                int count = 0;
                                while (rsNPC.next()) {
                                    Location loc = new Location(sg.world, 0, 0, 0, 0, 0);
                                    count++;
                                }
                                rsNPC.close();
                                stmtNPC.close();
                            }
                        }

                                                // close db
                        s2.close();

                    }

                }

                if (args[1].equals("updatepos")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx spawngroup updatepos spawngroupid");

                    } else {

                        Location loc = player.getLocation();
                        PreparedStatement s2 = this.universe.conn.prepareStatement("UPDATE spawngroup SET x=?,y=?,z=?,yaw=?,pitch=? WHERE id = ?;");
                        s2.setString(1, Double.toString(loc.getX()));
                        s2.setString(2, Double.toString(loc.getY()));
                        s2.setString(3, Double.toString(loc.getZ()));
                        s2.setString(4, Double.toString(loc.getYaw()));
                        s2.setString(5, Double.toString(loc.getPitch()));
                        s2.setString(6, args[2]);

                        s2.executeUpdate();
                        player.sendMessage("Updated Spawngroup " + args[2] + " to your position");
                        player.sendMessage("Warning: This does not change the world of the spawngroup");

                        // Update cached spawngroups
                        for (mySpawngroup sg : this.universe.spawngroups.values()) {
                            if (sg.id == Integer.parseInt(args[2])) {
                                // update the spawngroup
                                sg.x = loc.getX();
                                sg.y = loc.getY();
                                sg.z = loc.getZ();
                                sg.yaw = loc.getYaw();
                                sg.pitch = loc.getPitch();

                                

                                // Found the spawngroup, lets make sure the NPCs
                                // have their spawn values set right

                                for (myNPC np : sg.npcs.values()) {
                                    if (np.npc != null) {

                                        np.npc.spawnx = sg.x;
                                        np.npc.spawny = sg.y;
                                        np.npc.spawnz = sg.z;
                                        np.npc.spawnyaw = sg.yaw;
                                        np.npc.spawnpitch = sg.pitch;
                                        Location locnpc = new Location(sg.world, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                        np.npc.forceMove(locnpc);

                                    }

                                }

                            }
                        }

                        // close statement
                        s2.close();

                    }

                }

                if (args[1].equals("list")) {
                    player.sendMessage("Spawngroups:");
                    PreparedStatement sglist;

                    if (args.length < 3) {
                        sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM spawngroup ORDER BY ID DESC LIMIT 10");
                    } else {

                        sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM spawngroup WHERE name LIKE '%" + args[2] + "%'");
                    }
                    sglist.executeQuery();
                    ResultSet rs = sglist.getResultSet();

                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        String catVal = rs.getString("category");
                        player.sendMessage("id = " + idVal + ", name = " + nameVal + ", category = " + catVal);
                        ++count;
                    }
                    rs.close();
                    sglist.close();
                    player.sendMessage(count + " rows were retrieved");

                }

            }

            //
            // START CIV
            //

            if (subCommand.equals("civ")) {
                if (args.length < 2) {
                    player.sendMessage("Insufficient arguments /npcx civ givemoney playername amount");
                    player.sendMessage("Insufficient arguments /npcx civ money playername");

                    player.sendMessage("Insufficient arguments /npcx civ unclaim");
                    return false;
                }

                if (args[1].matches("money")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx civ money playername");
                        return false;

                    } else {
                        for (myPlayer p : this.universe.players.values()) {
                            if (p.player.getName().matches(args[2])) {
                                player.sendMessage("Balance: " + p.getNPCXBalance());
                            }
                        }
                    }

                }

                if (args[1].matches("givemoney")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx civ givemoney playername amount");
                        return false;

                    } else {
                        for (myPlayer p : this.universe.players.values()) {
                            if (p.player.getName().matches(args[2])) {
                                p.addPlayerBalance(p.player, Integer.parseInt(args[3]));
                                // use iconomy too!
                                // p.setNPCXBalance(p.getNPCXBalance() +
                                // (Integer.parseInt(args[3])));
                                player.sendMessage("Added to balance " + args[2] + "<" + args[3]);
                            }
                        }
                    }

                }
                if (args[1].matches("unclaim")) {
                    myZone z = this.universe.getZoneFromChunkAndLoc(this.universe.getZoneCoord(player.getLocation().getX()), this.universe.getZoneCoord(player.getLocation().getZ()), player.getLocation().getWorld());
                    if (z != null) {

                        z.setOwner("");
                        z.name = "Refurbished land";
                        player.sendMessage("You just released region: [" + ChatColor.LIGHT_PURPLE + z.x + "," + z.z + "" + ChatColor.WHITE + "]!");

                        for (myZoneMember zm : this.universe.zonemembers.values()) {
                            if (zm.zoneid == z.id) {
                                // member of town
                                this.universe.removeZoneMember(zm.id);
                                zm = null;

                            }
                        }

                        this.universe.setPlayerLastChunkX(player, z.x);
                        this.universe.setPlayerLastChunkZ(player, z.z);
                        this.universe.setPlayerLastChunkName(player, "Refurbished land");

                    } else {
                        player.sendMessage("Failed to buy zone at your location - target zone does not exist");

                    }
                }

            }

            //
            // START LOOTTABLE
            //

            if (subCommand.equals("loottable")) {

                if (args.length < 2) {
                    player.sendMessage("Insufficient arguments /npcx loottable create loottablename");
                    player.sendMessage("Insufficient arguments /npcx loottable list");
                    player.sendMessage("Insufficient arguments /npcx loottable add loottableid itemid amount");
                    return false;

                }

                if (args[1].equals("add")) {
                    if (args.length < 5) {
                        player.sendMessage("Insufficient arguments /npcx loottable add loottableid itemid amount");
                        return false;

                    } else {
                        player.sendMessage("Added to loottable " + args[2] + "<" + args[3] + "x" + args[4] + ".");

                        // add to database

                        PreparedStatement s2 = this.universe.conn.prepareStatement("INSERT INTO loottable_entries (loottable_id,item_id,amount) VALUES (?,?,?);", Statement.RETURN_GENERATED_KEYS);
                        s2.setString(1, args[2]);
                        s2.setString(2, args[3]);
                        s2.setString(3, args[4]);

                        s2.executeUpdate();
                        player.sendMessage("NPC ID [" + args[3] + "x" + args[4] + "] added to group [" + args[2] + "]");

                        // add to cached loottable
                        for (myLoottable lt : this.universe.loottables) {
                            if (lt.id == Integer.parseInt(args[2])) {

                                myLoottable_entry entry = new myLoottable_entry();
                                entry.id = Integer.parseInt(args[2]);
                                entry.itemid = Integer.parseInt(args[3]);
                                entry.amount = Integer.parseInt(args[4]);

                                
                                lt.loottable_entries.add(entry);

                            }
                        }
                        // close statement
                        s2.close();

                    }

                }

                if (args[1].equals("create")) {
                    if (args.length < 2) {
                        player.sendMessage("Insufficient arguments /npcx loottable create loottablename");
                        return false;
                    } else {

                        try {
                            PreparedStatement stmt = this.universe.conn.prepareStatement("INSERT INTO loottables (name) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
                            stmt.setString(1, args[2]);

                            stmt.executeUpdate();
                            ResultSet keyset = stmt.getGeneratedKeys();
                            int key = 0;
                            if (keyset.next()) {
                                // Retrieve the auto generated key(s).
                                key = keyset.getInt(1);

                            }
                            stmt.close();

                            player.sendMessage("Loottable [" + key + "] now active");                            

                        } catch (IndexOutOfBoundsException e) {
                            player.sendMessage("Insufficient arguments");
                        }

                    }

                }

                if (args[1].equals("list")) {
                    player.sendMessage("Loottables:");

                    Statement s = this.universe.conn.createStatement();
                    s.executeQuery("SELECT id, name FROM loottables");
                    ResultSet rs = s.getResultSet();
                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        player.sendMessage("id = " + idVal + ", name = " + nameVal);

                        Statement sFindEntries = this.universe.conn.createStatement();
                        sFindEntries.executeQuery("SELECT * FROM loottable_entries WHERE loottable_id = " + idVal);
                        ResultSet rsEntries = sFindEntries.getResultSet();
                        int countentries = 0;
                        while (rsEntries.next()) {

                            int id = rsEntries.getInt("id");
                            int itemid = rsEntries.getInt("item_id");
                            int loottableid = rsEntries.getInt("loottable_id");
                            int amount = rsEntries.getInt("amount");

                            player.sendMessage(" + id = " + id + ", loottableid = " + loottableid + ", itemid = " + itemid + ", amount = " + amount);

                            countentries++;

                        }
                        player.sendMessage(countentries + " entries in this set");
                        ++count;
                    }
                    rs.close();
                    s.close();
                    player.sendMessage(count + " loottables were retrieved");

                }

            }

            // END LOOTTABLE

            //
            // START FACTION
            //

            if (subCommand.equals("faction")) {

                if (args.length < 2) {
                    player.sendMessage("Insufficient arguments /npcx faction create baseamount factionname");
                    player.sendMessage("Insufficient arguments /npcx faction add factionid targetfactionid amount");
                    player.sendMessage("Insufficient arguments /npcx faction list");
                    return false;

                }

                if (args[1].equals("add")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx faction add factionid targetfactionid amount");

                    } else {

                        // add to database

                        PreparedStatement s2 = this.universe.conn.prepareStatement("INSERT INTO faction_entries (faction_id,target_faction_id,amount) VALUES (?,?,?);", Statement.RETURN_GENERATED_KEYS);
                        s2.setInt(1, Integer.parseInt(args[2]));
                        s2.setInt(2, Integer.parseInt(args[3]));
                        s2.setInt(3, Integer.parseInt(args[4]));
                        s2.executeUpdate();
                        ResultSet keyset = s2.getGeneratedKeys();
                        int key = 0;
                        if (keyset.next()) {
                            // Retrieve the auto generated key(s).
                            key = keyset.getInt(1);

                        }
                        s2.close();
                        myFactionEntry fe = new myFactionEntry();
                        fe.id = key;
                        fe.factionid = Integer.parseInt(args[2]);
                        fe.targetfactionid = Integer.parseInt(args[3]);
                        fe.amount = Integer.parseInt(args[4]);

                        player.sendMessage("Added to faction entries [" + key + "] " + args[2] + "<" + args[3] + "=" + args[4] + ".");
                        this.universe.factionentries.put(Integer.toString(fe.id), fe);

                    }

                }

                if (args[1].equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx faction create baseamount factionname");

                    } else {

                        try {
                            PreparedStatement stmt = this.universe.conn.prepareStatement("INSERT INTO faction_list (name,base) VALUES (?,?);", Statement.RETURN_GENERATED_KEYS);
                            stmt.setString(1, args[3]);
                            stmt.setInt(2, Integer.parseInt(args[2]));

                            stmt.executeUpdate();
                            ResultSet keyset = stmt.getGeneratedKeys();
                            int key = 0;
                            if (keyset.next()) {
                                // Retrieve the auto generated key(s).
                                key = keyset.getInt(1);

                            }
                            stmt.close();

                            player.sendMessage("Faction [" + key + "] now active");
                            
                        } catch (IndexOutOfBoundsException e) {
                            player.sendMessage("Insufficient arguments");
                        }

                    }

                }

                if (args[1].equals("list")) {
                    player.sendMessage("Factions:");

                    Statement s = this.universe.conn.createStatement();
                    s.executeQuery("SELECT id, name, base FROM faction_list");
                    ResultSet rs = s.getResultSet();
                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        String baseVal = rs.getString("base");
                        player.sendMessage("id = " + idVal + ", name = " + nameVal + ", base = " + baseVal);

                        for (myFactionEntry fe : this.universe.factionentries.values()) {
                            if (fe.factionid == idVal) {
                                
                            }
                        }

                        ++count;
                    }
                    rs.close();
                    s.close();
                    player.sendMessage(count + " rows were retrieved");

                }

            }

            // END FACTION

            if (subCommand.equals("pathgroup")) {
                if (args.length < 2) {
                    // todo: need to implement npc types here ie: 0 = default 1
                    // = banker 2 = merchant
                    // todo: need to implement '/npcx npc edit' here
                    player.sendMessage("Insufficient arguments /npcx pathgroup create name");

                    // todo needs to force the player to provide a search term
                    // to not spam them with lots of results in the event of a
                    // huge npc list
                    player.sendMessage("Insufficient arguments /npcx pathgroup list");
                    player.sendMessage("Insufficient arguments /npcx pathgroup add pathgroupid order");
                    player.sendMessage("Insufficient arguments /npcx pathgroup inspect pathgroupid");

                    return false;
                }

                if (args[1].equals("inspect")) {

                    player.sendMessage("Pathgroup Entries:");

                    if (args.length >= 3) {

                        PreparedStatement pginspect = this.universe.conn.prepareStatement("SELECT id,s,x,y,z,pathgroup,name FROM pathgroup_entries WHERE pathgroup = ? ORDER BY s ASC");
                        pginspect.setInt(1, Integer.parseInt(args[2]));
                        pginspect.executeQuery();
                        ResultSet rspginspect = pginspect.getResultSet();

                        int count = 0;
                        while (rspginspect.next()) {
                            int idVal = rspginspect.getInt("id");
                            String nameVal = rspginspect.getString("name");

                            int s = rspginspect.getInt("s");
                            int pgid = rspginspect.getInt("pathgroup");
                            String x = rspginspect.getString("x");
                            String y = rspginspect.getString("y");
                            String z = rspginspect.getString("z");

                            player.sendMessage("s: " + s + " pgid: " + pgid + " XYZ: " + x + "," + y + "," + z);
                            ++count;
                        }
                        rspginspect.close();
                        pginspect.close();
                        player.sendMessage(count + " rows were retrieved");

                    } else {
                        player.sendMessage("Insufficient arguments /npcx pathgroup inspect pathgroupid");
                    }

                }

                if (args[1].equals("add")) {
                    if (args.length < 5) {
                        player.sendMessage("Insufficient arguments /npcx pathgroup add pathgroupid order name");

                    } else {
                        player.sendMessage("Added " + args[4] + " to pathgroup " + args[2] + "<" + args[3] + ".");

                        // add to database

                        PreparedStatement s2 = this.universe.conn.prepareStatement("INSERT INTO pathgroup_entries (pathgroup,s,x,y,z,pitch,yaw,name) VALUES (?,?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
                        s2.setString(1, args[2]);
                        s2.setString(2, args[3]);
                        s2.setDouble(3, player.getLocation().getX());
                        s2.setDouble(4, player.getLocation().getY());
                        s2.setDouble(5, player.getLocation().getZ());
                        s2.setFloat(6, player.getLocation().getPitch());
                        s2.setFloat(7, player.getLocation().getYaw());
                        s2.setString(8, args[4]);

                        s2.executeUpdate();
                        player.sendMessage("Pathing Position [" + args[3] + "] added to pathgroup [" + args[2] + "]");

                        // add to cached spawngroup
                        for (myPathgroup pg : this.universe.pathgroups) {
                            if (pg.id == Integer.parseInt(args[2])) {

                                int dpathgroupid = Integer.parseInt(args[2]);
                                int dspot = Integer.parseInt(args[3]);
                                String name = args[4];
                                
                            }
                        }

                        // close db
                        s2.close();

                    }

                }

                if (args[1].equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx pathgroup create name");

                    } else {

                        PreparedStatement statementPCreate = this.universe.conn.prepareStatement("INSERT INTO pathgroup (name,world) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
                        statementPCreate.setString(1, args[2]);
                        statementPCreate.setString(2, player.getLocation().getWorld().getName());

                        statementPCreate.executeUpdate();

                        ResultSet keyset = statementPCreate.getGeneratedKeys();

                        int key = 0;
                        if (keyset.next()) {
                            // Retrieve the auto generated key(s).
                            key = keyset.getInt(1);

                        }

                        myPathgroup pathgroup = new myPathgroup();
                        pathgroup.id = key;
                        pathgroup.world = player.getWorld();

                        this.universe.pathgroups.add(pathgroup);

                        statementPCreate.close();
                        player.sendMessage("Created pathgroup [" + key + "]: " + args[2]);

                    }

                }

                if (args[1].equals("list")) {

                    player.sendMessage("Pathgroups:");
                    PreparedStatement sglist;

                    if (args.length < 3) {
                        sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM pathgroup ORDER BY ID DESC LIMIT 10");
                    } else {

                        sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM pathgroup WHERE name LIKE '%" + args[2] + "%'");
                    }
                    sglist.executeQuery();
                    ResultSet rs = sglist.getResultSet();

                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        String catVal = rs.getString("category");
                        player.sendMessage("id = " + idVal + ", name = " + nameVal + ", category = " + catVal);
                        ++count;
                    }
                    rs.close();
                    sglist.close();
                    player.sendMessage(count + " rows were retrieved");

                }
            }

            if (subCommand.equals("merchant")) {
                if (args.length < 2) {
                    // todo: need to implement npc types here ie: 0 = default 1
                    // = banker 2 = merchant
                    // todo: need to implement '/npcx npc edit' here
                    player.sendMessage("Insufficient arguments /npcx merchant create name");

                    // todo needs to force the player to provide a search term
                    // to not spam them with lots of results in the event of a
                    // huge npc list
                    player.sendMessage("Insufficient arguments /npcx merchant list");
                    player.sendMessage("Insufficient arguments /npcx merchant add merchantid item amount pricebuyat pricesellat");
                    player.sendMessage("Insufficient arguments /npcx merchant inspect merchantid");
                    player.sendMessage("Insufficient arguments /npcx merchant category merchantid category");

                    return false;
                }
                if (args[1].equals("category")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx merchant category merchantid category");

                    } else {

                        PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE merchant SET category = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        stmt.executeUpdate();

                        for (myMerchant n : universe.merchants) {
                            if (n.id == Integer.parseInt(args[2])) {

                                n.category = args[3];
                                player.sendMessage("npcx : Updated merchant to cached category (" + args[3] + "): " + n.category);
                                // when faction changes reset aggro and follow
                                // status

                            }
                        }

                        player.sendMessage("Updated merchant category :" + args[3] + " on Merchant ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("inspect")) {

                    player.sendMessage("Merchant Entries:");

                    if (args.length >= 3) {

                        PreparedStatement pginspect = this.universe.conn.prepareStatement("SELECT id,merchantid,itemid,amount,pricebuy,pricesell FROM merchant_entries WHERE merchantid = ? ORDER BY id ASC");
                        pginspect.setInt(1, Integer.parseInt(args[2]));
                        pginspect.executeQuery();
                        ResultSet rspginspect = pginspect.getResultSet();

                        int count = 0;
                        while (rspginspect.next()) {
                            int idVal = rspginspect.getInt("id");
                            int merchantid = rspginspect.getInt("merchantid");
                            int itemid = rspginspect.getInt("itemid");
                            int amount = rspginspect.getInt("amount");
                            int pricebuy = rspginspect.getInt("pricebuy");
                            int pricesell = rspginspect.getInt("pricesell");

                            player.sendMessage("EID:" + idVal + ":MID:" + merchantid + " Item:" + itemid + " - Amount: " + amount + " B: " + pricebuy + "S: " + pricesell);
                            ++count;
                        }
                        rspginspect.close();
                        pginspect.close();
                        player.sendMessage(count + " rows were retrieved");

                    } else {
                        player.sendMessage("Insufficient arguments /npcx merchant inspect merchantid");
                    }

                }

                if (args[1].equals("add")) {
                    if (args.length < 6) {
                        player.sendMessage("Insufficient arguments /npcx merchant add merchantid itemid amount pricebuyat pricesellat");

                    } else {

                        // add to database

                        PreparedStatement s2 = this.universe.conn.prepareStatement("INSERT INTO merchant_entries (merchantid,itemid,amount,pricebuy,pricesell) VALUES (?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);

                        s2.setInt(1, Integer.parseInt(args[2]));
                        s2.setInt(2, Integer.parseInt(args[3]));
                        s2.setInt(3, Integer.parseInt(args[4]));
                        s2.setInt(4, Integer.parseInt(args[5]));
                        s2.setInt(5, Integer.parseInt(args[6]));

                        s2.executeUpdate();
                        player.sendMessage("Merchant Item [" + args[3] + "x" + args[4] + "@" + args[5] + "/" + args[6] + "] added to Merchant: [" + args[2] + "]");

                        // add to cached spawngroup
                        for (myMerchant pg : this.universe.merchants) {
                            if (pg.id == Integer.parseInt(args[2])) {

                                int dmerchantid = Integer.parseInt(args[2]);
                                int itemid = Integer.parseInt(args[3]);
                                int amount = Integer.parseInt(args[4]);
                                int pricebuy = Integer.parseInt(args[5]);
                                int pricesell = Integer.parseInt(args[6]);

                                myMerchant_entry pge = new myMerchant_entry(pg, dmerchantid, itemid, amount, pricebuy, pricesell);
                                

                                // add new merchant entry object to the
                                // merchants entry list
                                pg.merchantentries.add(pge);
                                player.sendMessage("Added to merchant " + args[2] + "<" + args[3] + "x" + args[4] + "@" + args[5] + ".");

                            }
                        }

                        // close db
                        s2.close();

                    }

                }

                if (args[1].equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx merchant create name");

                    } else {

                        PreparedStatement statementPCreate = this.universe.conn.prepareStatement("INSERT INTO merchant (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                        statementPCreate.setString(1, args[2]);
                        statementPCreate.executeUpdate();

                        ResultSet keyset = statementPCreate.getGeneratedKeys();

                        int key = 0;
                        if (keyset.next()) {
                            // Retrieve the auto generated key(s).
                            key = keyset.getInt(1);

                        }

                        statementPCreate.close();
                        player.sendMessage("Created merchant [" + key + "]: " + args[2]);

                    }

                }

                if (args[1].equals("list")) {

                    player.sendMessage("merchants:");
                    PreparedStatement sglist;

                    if (args.length < 3) {
                        sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM merchant ORDER BY ID DESC LIMIT 10");
                    } else {

                        sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM merchant WHERE name LIKE '%" + args[2] + "%'");
                    }
                    sglist.executeQuery();
                    ResultSet rs = sglist.getResultSet();

                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        player.sendMessage("id = " + idVal + ", name = " + nameVal + ", category = " + rs.getString("category"));
                        ++count;
                    }
                    rs.close();
                    sglist.close();
                    player.sendMessage(count + " rows were retrieved");

                }
            }

            if (subCommand.equals("version"))

            {
                
            }

            if (subCommand.equals("npc")) {
                // Overview:
                // NPCs are just that, definitions of the mob you want to appear
                // in game. There can be multiple of the same
                // npc in many spawngroups, for example if you wanted a custom
                // npc called 'Thief' to spawn in several locations
                // you would put the npc into many spawn groups

                // In the future these npcs will support npctypes which
                // determines how the npc will respond to right click, attack,
                // etc events
                // ie for: bankers, normal npcs, merchants etc

                // Also loottables will be assignable

                // todo: functionality
                // creates a new npc with name

                if (args.length < 2) {
                    player.sendMessage("Insufficient arguments: /npcx npc");
                    player.sendMessage("list | triggerword | faction | loottable | category");
                    player.sendMessage("merchant | weapon | helmet | chest | legs | boots");
                    return false;
                }

                if (args[1].equals("merchant")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc merchant npcid merchantid");
                        return false;
                    } else {
                        if (Integer.parseInt(args[3]) == 0) {

                            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET merchantid = null WHERE id = ?;");
                            stmt.setString(1, args[2]);
                            stmt.executeUpdate();
                            stmt.close();

                        } else {
                            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET merchantid = ? WHERE id = ?;");
                            stmt.setString(1, args[3]);
                            stmt.setString(2, args[2]);
                            stmt.executeUpdate();
                            stmt.close();
                        }
                        int count = 0;
                        for (myNPC sg : universe.npcs.values()) {
                            // player.sendMessage("npcx : Checking: "+sg.name);
                            if (sg.id.matches(args[2])) {
                                if (Integer.parseInt(args[3]) != 0) {
                                    count++;
                                } else {
                                    sg.merchant = null;
                                    player.sendMessage("npcx : Updated NPCs cached merchant (0)");
                                    count++;

                                }

                            }
                        }

                        player.sendMessage("Updated " + count + " entries.");

                    }
                }

                if (args[1].equals("triggerword")) {
                    if (args.length < 6) {
                        player.sendMessage("Insufficient arguments /npcx npc triggerword add npcid triggerword response");

                    } else {

                        String reply = "";
                        int current = 6;
                        while (current <= args.length) {
                            reply = reply + args[current - 1] + " ";
                            current++;
                        }

                        reply = reply.substring(0, reply.length() - 1);

                        PreparedStatement statementTword = this.universe.conn.prepareStatement("INSERT INTO npc_triggerwords (npcid,triggerword,reply) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
                        statementTword.setString(1, args[3]);
                        statementTword.setString(2, args[4]);
                        statementTword.setString(3, reply);

                        statementTword.executeUpdate();
                        ResultSet keyset = statementTword.getGeneratedKeys();
                        int key = 0;
                        if (keyset.next()) {
                            // Retrieve the auto generated key(s).
                            key = keyset.getInt(1);

                        }
                        player.sendMessage("Added (" + universe.npcs.values().size() + ") triggerword [" + key + "] to npc " + args[3]);

                        // add it to any spawned npcs
                        for (myNPC npc : universe.npcs.values()) {
                            
                            if (npc.id.equals(args[3])) {
                             
                                myTriggerword tw = new myTriggerword();
                                tw.word = args[4];
                                tw.id = key;
                                tw.response = reply;
                                player.sendMessage("Added triggerword to Active npc " + args[3]);
                                npc.triggerwords.put(Integer.toString(tw.id), tw);

                            }
                        }

                    }

                }

                if (args[1].equals("chest")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc chest npcid itemid");

                    } else {

                        PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET chest = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        // TODO not in schema yet
                        // stmt.executeUpdate();

                        for (myNPC n : universe.npcs.values()) {
                            if (n.id.matches(args[2])) {

                                n.chest = Integer.parseInt(args[3]);
                                ItemStack i = new ItemStack(n.chest, 1);
                                i.setTypeId(Integer.parseInt(args[3]));

                                n.npc.getBukkitEntity().getInventory().setChestplate(i);
                                player.sendMessage("npcx : Updated living npc to cached chest (" + args[3] + "): " + n.chest);
                                stmt.executeUpdate();

                            }
                        }

                        player.sendMessage("Updated npc chest: item ID:" + args[3] + " on NPC ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("helmet")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc helmet npcid itemid");

                    } else {

                        PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET helmet = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        // TODO not in schema yet
                        // stmt.executeUpdate();

                        for (myNPC n : universe.npcs.values()) {
                            if (n.id.matches(args[2])) {

                                n.helmet = Integer.parseInt(args[3]);
                                ItemStack i = new ItemStack(n.helmet, 1);
                                i.setTypeId(Integer.parseInt(args[3]));
                                n.npc.getBukkitEntity().getInventory().setHelmet(i);
                                player.sendMessage("npcx : Updated living npc to cached helmet (" + args[3] + "): " + n.helmet);
                                stmt.executeUpdate();
                            }
                        }

                        player.sendMessage("Updated npc helmet: item ID:" + args[3] + " on NPC ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("weapon")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc weapon npcid itemid");

                    } else {

                        PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET weapon = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        // TODO not in schema yet
                        // stmt.executeUpdate();

                        for (myNPC n : universe.npcs.values()) {
                            if (n.id.matches(args[2])) {

                                n.weapon = Integer.parseInt(args[3]);
                                ItemStack i = new ItemStack(n.weapon, 1);
                                i.setTypeId(Integer.parseInt(args[3]));

                                n.npc.getBukkitEntity().getInventory().setItemInHand(i);
                                player.sendMessage("npcx : Updated living npc to cached weapon (" + args[3] + "): " + n.weapon);
                                stmt.executeUpdate();

                            }
                        }

                        player.sendMessage("Updated npc weapon: item ID:" + args[3] + " on NPC ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("boots")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc boots npcid itemid");

                    } else {

                        PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET boots = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        // TODO not in schema yet
                        // stmt.executeUpdate();

                        for (myNPC n : universe.npcs.values()) {
                            if (n.id.matches(args[2])) {

                                n.boots = Integer.parseInt(args[3]);
                                ItemStack i = new ItemStack(n.boots, 1);
                                i.setTypeId(Integer.parseInt(args[3]));

                                n.npc.getBukkitEntity().getInventory().setBoots(i);
                                player.sendMessage("npcx : Updated living npc to cached boots (" + args[3] + "): " + n.boots);

                                stmt.executeUpdate();

                            }
                        }

                        player.sendMessage("Updated npc boots: item ID:" + args[3] + " on NPC ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("legs")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc legs npcid itemid");

                    } else {

                        PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET legs = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        // TODO not in schema yet
                        // stmt.executeUpdate();

                        for (myNPC n : universe.npcs.values()) {
                            if (n.id.matches(args[2])) {

                                n.legs = Integer.parseInt(args[3]);
                                ItemStack i = new ItemStack(n.legs, 1);
                                i.setTypeId(Integer.parseInt(args[3]));

                                n.npc.getBukkitEntity().getInventory().setLeggings(i);
                                player.sendMessage("npcx : Updated living npc to cached legs (" + args[3] + "): " + n.legs);

                                stmt.executeUpdate();

                            }
                        }

                        player.sendMessage("Updated npc legs: item ID:" + args[3] + " on NPC ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("faction")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc faction npcid factionid");

                    } else {

                        PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET faction_id = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        stmt.executeUpdate();

                        for (myNPC n : universe.npcs.values()) {
                            if (n.id.matches(args[2])) {

                                // when faction changes reset aggro and follow
                                // status
                                n.npc.setAggro(null);
                                n.npc.setFollow(null);
                            }
                        }

                        player.sendMessage("Updated npc faction ID:" + args[3] + " on NPC ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("category")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc category npcid category");

                    } else {

                        PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET category = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        stmt.executeUpdate();

                        for (myNPC n : universe.npcs.values()) {
                            if (n.id.matches(args[2])) {
                                // when faction changes reset aggro and follow
                                // status

                            }
                        }

                        player.sendMessage("Updated npc category :" + args[3] + " on NPC ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("list")) {
                    player.sendMessage("Npcs:");
                    PreparedStatement sglist;

                    if (args.length < 3) {
                        sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM npc ORDER BY ID DESC LIMIT 10");
                    } else {

                        sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM npc WHERE name LIKE '%" + args[2] + "%'");
                    }
                    sglist.executeQuery();
                    ResultSet rs = sglist.getResultSet();

                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        String catVal = rs.getString("category");
                        player.sendMessage("id = " + idVal + ", name = " + nameVal + ", category = " + catVal);
                        ++count;
                    }
                    rs.close();
                    sglist.close();
                    player.sendMessage(count + " rows were retrieved");

                }

                if (args[1].equals("loottable")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc loottable npcid loottableid");

                    } else {

                        PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET loottable_id = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        stmt.executeUpdate();

                        for (myNPC n : universe.npcs.values()) {
                            if (n.id.matches(args[2])) {

                                
                            }
                        }

                        player.sendMessage("Updated npc loottable ID:" + args[3] + " on NPC ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx npc create npcname");

                    } else {

                        Statement s2 = this.universe.conn.createStatement();

                        PreparedStatement stmt = this.universe.conn.prepareStatement("INSERT INTO npc (name,weapon,helmet,chest,legs,boots) VALUES (?,'267','0','307','308','309');", Statement.RETURN_GENERATED_KEYS);
                        stmt.setString(1, args[2]);
                        stmt.executeUpdate();
                        ResultSet keyset = stmt.getGeneratedKeys();
                        int key = 0;
                        if (keyset.next()) {
                            // Retrieve the auto generated key(s).
                            key = keyset.getInt(1);

                        }
                        player.sendMessage("Created npc : " + args[2] + " NPC ID:[" + key + "]");

                        s2.close();

                    }

                }
            }

        } catch (Exception e) {
            sender.sendMessage("An error occured.");
            this.logger.log(Level.WARNING, "npcx: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return true;
        }
        return true;
	}
}
