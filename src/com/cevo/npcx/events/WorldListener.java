package com.cevo.npcx.events;

import java.util.HashMap;

import net.gamerservices.npcx.myNPC;
import net.gamerservices.npcx.npcx;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;


// Citizens Mod: http://forums.bukkit.org/threads/7173/

public class WorldListener implements Listener {
    private npcx parent;
    private Universe universe;
    
    public WorldListener(npcx parent) {
        this.parent = parent;
        this.universe = this.parent.getUniverse();
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        parent.deregisterChunk(e.getChunk());
        try {
            // System.out.println("debug : closing chunk " + e.getChunk());
            for (myNPC npc : this.universe.npcs.values()) {
                if (this.universe.npcs != null) {
                    if (npc.npc != null) {
                        if (e.getChunk().getWorld().getChunkAt(npc.npc.getBukkitEntity().getLocation()).equals(e.getChunk())) {
                            npc.npc.chunkinactive(npc.npc.getBukkitEntity().getLocation());
                        }
                    }
                }
            }
        } catch (Exception e2) {
            // looks like a table was locked, we need to mark this to try again
            this.parent.checkchunks = true;
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {

        parent.registerChunk(e.getChunk());
        try {
            // System.out.println("debug : loading chunk " + e.getChunk());
            for (myNPC npc : this.universe.npcs.values()) {
                if (this.universe.npcs != null) {
                    if (npc.npc != null) {
                        if (e.getChunk().getWorld().getChunkAt(npc.npc.getBukkitEntity().getLocation()).equals(e.getChunk())) {
                            npc.npc.chunkactive(npc.npc.getBukkitEntity().getLocation());
                        }
                    }
                }
            }
        } catch (Exception e2) {
            // looks like a table was locked, we need to mark this to try again
            this.parent.checkchunks = true;
        }
    }
}