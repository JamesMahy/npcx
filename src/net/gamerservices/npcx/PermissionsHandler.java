package net.gamerservices.npcx;
import java.util.HashMap;

import org.bukkit.entity.Player;

import com.platymuus.bukkit.permissions.*;

public class PermissionsHandler {
	private PermissionsPlugin p;
	
	public PermissionsHandler(){
		this.p = new PermissionsPlugin();
	}
	
	public boolean has(Player player, String permission){
		if(player.isOp()){return true;}
		PermissionInfo info = this.p.getPlayerInfo(player.getName());
		
		if(info.getPermissions().containsKey(permission)){
			return info.getPermissions().get(permission);
		}
		return false;
	}
}
