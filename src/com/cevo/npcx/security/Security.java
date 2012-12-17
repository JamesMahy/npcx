package com.cevo.npcx.security;

import java.util.logging.Logger;

import net.gamerservices.npcx.PermissionsHandler;
import net.gamerservices.npcx.npcx;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Security {
		private PermissionsHandler Permissions = null;
		private npcx parent;
		private Logger logger;
		
		public Security(npcx parent){
			this.parent = parent;
			this.logger = parent.getLogger();
		}
		
		public boolean isAdmin(Player player) {
	        // first if they are an op they get access regardless
	        if (player.isOp()) { return true;}
	        try {
	            if (this.Permissions.has(player, "npcx.fulladmin")) { return true; }
	        } catch (Exception e) {}
	        return false;
	    }
		
		public void setupPermissions() {
	        Plugin test = this.parent.getServer().getPluginManager().getPlugin("Permissions");

	        if (this.Permissions == null) {
	            if (test != null) {
	                this.Permissions = new PermissionsHandler();
	            } else {
	                this.logger.info("Permission system not detected, defaulting to OP");
	            }
	        }
	    }
		
}
