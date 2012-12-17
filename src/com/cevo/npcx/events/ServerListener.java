package com.cevo.npcx.events;

import net.gamerservices.npcx.npcx;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import com.iCo6.*;

public class ServerListener implements Listener {
    private npcx parent;
    private iConomy iconomy;
    
    public ServerListener(npcx parent) {
        this.parent = parent;
        this.iconomy = this.parent.getiConomy();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if (this.iconomy == null && this.parent.useiConomy) {
            Plugin iConomy = parent.getServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if (iConomy.isEnabled() && iConomy.getClass().getName().equals("com.iConomy.iConomy")) {
                    this.iconomy = (iConomy) iConomy;
                    System.out.println("[npcx] hooked into iConomy.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if (this.iconomy != null && parent.useiConomy) {
            if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                this.iconomy = null;
                System.out.println("[npcx] un-hooked from iConomy.");
            }
        }
    }
}
