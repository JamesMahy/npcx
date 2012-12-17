package net.gamerservices.npcx;

import java.sql.PreparedStatement;

import org.bukkit.Chunk;

import com.cevo.npcx.universe.Universe;

public class myZone {
    public Universe parent;
    public String name = "The Wild";
    public String ownername = "";
    public boolean wild = true;
    public Chunk chunk;
    public int x;
    public int z;
    public int id;

    public myZone(Universe universe, int id, Chunk chunk, int x, int z) {
        this.id = id;
        this.parent = universe;
        this.chunk = chunk;
        this.x = x;
        this.z = z;
    }

    public void setOwner(String name2) {
        // TODO Auto-generated method stub

        try {
            // System.out.println("npcx :myZone:setOwner:"+name2+":"+this.id);
            PreparedStatement stmt = this.parent.this.universe.conn.prepareStatement("UPDATE zone set name=?,ownername=? WHERE id = ?");
            stmt.setString(1, name2 + "s land");
            stmt.setString(2, name2);
            stmt.setInt(3, this.id);

            stmt.executeUpdate();
            stmt.close();

            this.ownername = name2;
            this.name = name2 + "s land";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setName(String string) {
        // TODO Auto-generated method stub
        try {
            // System.out.println("npcx :myZone:setOwner:"+name2+":"+this.id);
            PreparedStatement stmt = this.parent.this.universe.conn.prepareStatement("UPDATE zone set name=? WHERE id = ?");
            stmt.setString(1, string);
            stmt.setInt(2, this.id);

            stmt.executeUpdate();
            stmt.close();

            this.name = string;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
