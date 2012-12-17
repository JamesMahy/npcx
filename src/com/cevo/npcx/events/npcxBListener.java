package com.cevo.npcx.events;

import net.gamerservices.npcx.npcx;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;


public class npcxBListener implements Listener {
    private npcx parent;

    public npcxBListener(npcx parent) {
        this.parent = parent;
    }
    
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.isCancelled()) { return; }
        if (this.parent.universe.nospread.equals("true")) {

            String cause = event.getCause().toString();
            if (cause.equals("SPREAD")) {
                event.setCancelled(true);
            }
            if (cause.equals("FLINT_AND_STEEL")) {
                event.setCancelled(true);
            }

        }
    }
}
