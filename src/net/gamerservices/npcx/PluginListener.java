package net.gamerservices.npcx;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;

import com.iConomy.*;
import org.bukkit.plugin.Plugin;

public class PluginListener extends ServerListener {

	public npcx parent;
	public PluginListener(npcx parent) 
	{
		this.parent = parent;
	}
	

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if(parent.getiConomy() == null) {
            Plugin iConomy = parent.getBukkitServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if(iConomy.isEnabled()) {
                    parent.setiConomy((iConomy)iConomy);
                    System.out.println("npcx : Successfully linked with iConomy.");
                }
            }
        }
    }
}
