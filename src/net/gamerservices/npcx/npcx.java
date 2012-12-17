package net.gamerservices.npcx;

import org.bukkit.plugin.Plugin;
import net.gamerservices.npclibfork.BasicHumanNpcList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.Server;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cevo.npcx.events.BlockListener;
import com.cevo.npcx.events.EntityListener;
import com.cevo.npcx.events.PlayerListener;
import com.cevo.npcx.events.ServerListener;
import com.cevo.npcx.events.WorldListener;
import com.cevo.npcx.handlers.CommandHandler;
import com.cevo.npcx.security.Security;
import com.cevo.npcx.universe.Universe;
import com.iCo6.*;

public class npcx extends JavaPlugin {

    private final Logger logger = Logger.getLogger("Minecraft");
    
    private EntityListener mEntityListener;
    private PlayerListener mPlayerListener;
    private WorldListener mWorldListener;
    private BlockListener mBlockListener;
    private ServerListener mServerListener;
    private Universe universe;
    private Security security;
    
    private iConomy iconomy = null;
    boolean useiConomy;
    private Server server = null;

    public BasicHumanNpcList npclist = new BasicHumanNpcList();
    private Timer tick = new Timer();
    private Timer longtick = new Timer();

    public boolean checkchunks = false;
    
    public Logger getLogger(){return this.logger;}
    public Security getSecurity(){return this.security;}
    public Universe getUniverse(){return this.universe;}

    public double getDistance(double d, double e) {return d - e;}
    
    @Override
    public void onDisable() {
        // Reload or shutdown event
        try {
            this.universe.commitPlayerFactions();
            this.universe.onDisable();

            PluginDescriptionFile pdfFile = this.getDescription();
            logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " disabled.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "npcx : error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    public Server getBukkitServer() {return this.server;}
    public iConomy getiConomy() {return this.iconomy;}

    public boolean setiConomy(iConomy plugin) {
        if (this.iconomy == null && this.useiConomy) {this.iconomy = plugin;} 
        else {return false;}
        return true;
    }

    public boolean EventsSetup() {
        try {
            System.out.println("npcx : registering monitored events");
            
            PluginManager pm = this.server.getPluginManager();

            mEntityListener = new EntityListener(this);
            mPlayerListener = new npcxPListener(this);
            mWorldListener = new WorldListener(this);
            mBlockListener = new BlockListener(this);
            mServerListener = new ServerListener(this);            
            
            pm.registerEvents(mWorldListener,this);
            pm.registerEvents(mPlayerListener,this);
            pm.registerEvents(mEntityListener,this);
            pm.registerEvents(mServerListener,this);

            return true;
        } catch (NoSuchFieldError e) {
            System.out.println("npcx : *****************************************************");
            System.out.println("npcx : *            FAILED TO LOAD NPCX !                  *");
            System.out.println("npcx : * This version of NPCX is built for Bukkit RB 602   *");
            System.out.println("npcx : *            FAILED TO LOAD NPCX !                  *");
            System.out.println("npcx : *****************************************************");
            return false;
        }

    }

    @Override
    public void onEnable() {

        universe = new Universe(this);
        universe.checkSetup();

        if (!universe.loadSetup()) return;
        
        this.security = new Security(this);
        
        this.server = getServer();        
        if (EventsSetup() == false) {
            System.out.println("Events not loading correctly");
            return;
        }

        universe.checkDbSetup();
        universe.checkUpdates();

        universe.loadData();

        PluginDescriptionFile pdfFile = this.getDescription();
        logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled.");

        this.security.setupPermissions();

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	CommandHandler handler = new CommandHandler(this.universe);
    	return handler.doCommand(sender, command, commandLabel, args);
    }    
}
