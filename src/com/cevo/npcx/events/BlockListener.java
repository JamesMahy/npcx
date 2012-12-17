package com.cevo.npcx.events;

import net.gamerservices.npcx.npcx;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;


public class BlockListener implements Listener {
    private npcx parent;
    private Universe universe;

    public BlockListener(npcx parent) {
        this.parent = parent;
        this.universe = this.parent.getUniverse();
    }
    
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.isCancelled()) { return; }
        if (this.universe.nospread.equals("true")) {

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
