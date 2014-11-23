package me.hqm.plugindev.creativeticketreview.model;

import com.iciql.Db;
import com.iciql.Iciql;
import me.hqm.plugindev.creativeticketreview.CTRSetting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.UUID;

@Iciql.IQTable(name = "ctr_tickets")
public class TicketModel extends AbstractModel {

    // -- STATIC CONSTRUCTOR -- //

    public static TicketModel of(Player player) {
        // Check if it exists
        boolean exists = existsFor(player);
        if (exists) {
            return getFor(player.getUniqueId());
        }

        // Add the attributes
        Location location = player.getLocation();
        TicketModel ticket = new TicketModel();
        ticket.setName(player.getName());
        ticket.setCreatorId(player.getUniqueId().toString());
        ticket.setLocWorld(location.getWorld().getName());
        ticket.setLocX(location.getX());
        ticket.setLocY(location.getY());
        ticket.setLocZ(location.getZ());
        ticket.setOpenTime(new Timestamp(System.currentTimeMillis()));
        ticket.setState(State.OPEN);

        // Insert into the database
        ticket.insert();

        // Return the object
        return ticket;
    }

    // -- UTIL -- //

    public static boolean existsFor(Player player) {
        return getFor(player.getUniqueId()) != null;
    }

    public static TicketModel getFor(UUID playerId) {
        TicketModel alias = new TicketModel();
        Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
        try {
            return db.from(alias).where(alias.creatorId).is(playerId.toString()).selectFirst();
        } finally {
            db.close();
        }
    }

    public static TicketModel getFor(String ticketName) {
        TicketModel alias = new TicketModel();
        Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
        try {
            return db.from(alias).where(alias.ticketName).is(ticketName).selectFirst();
        } finally {
            db.close();
        }
    }

    // -- ENUMS -- //

    @Iciql.IQEnum
    public enum State {
        OPEN, ACCEPTED, DENIED
    }

    // -- MODEL META -- //

    @Iciql.IQColumn(name = "name", primaryKey = true, length = 200)
    public String ticketName;

    // -- DATA FIELDS -- //

    @Iciql.IQColumn(name = "player_id", length = 200)
    public String creatorId;

    @Iciql.IQColumn(name = "opened")
    public Timestamp openTime;

    @Iciql.IQColumn(name = "closed")
    public Timestamp closeTime;

    @Iciql.IQColumn(name = "location_world_name", length = 200)
    public String locWorld;

    @Iciql.IQColumn(name = "location_x")
    public Double locX;

    @Iciql.IQColumn(name = "location_y")
    public Double locY;

    @Iciql.IQColumn(name = "location_z")
    public Double locZ;

    // -- ADDITIONAL DATA -- //

    @Iciql.IQColumn
    @Iciql.IQEnum
    public State state;

    @Iciql.IQColumn(name = "moderator_id")
    public String moderatorId;

    // -- GETTERS & SETTERS -- //

    public void setName(String ticketName) {
        this.ticketName = ticketName;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setOpenTime(Timestamp openTime) {
        this.openTime = openTime;
    }

    public void setCloseTime(Timestamp closeTime) {
        this.closeTime = closeTime;
    }

    public void setLocWorld(String locWorld) {
        this.locWorld = locWorld;
    }

    public void setLocX(Double locX) {
        this.locX = locX;
    }

    public void setLocY(Double locY) {
        this.locY = locY;
    }

    public void setLocZ(Double locZ) {
        this.locZ = locZ;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setModeratorId(String moderatorId) {
        this.moderatorId = moderatorId;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(locWorld);
        if (world == null) {
            throw new NullPointerException("Ticket's world is null.");
        }
        return new Location(world, locX, locY, locZ);
    }

    // -- DATABASE RELATED -- //

    @Override
    public void refresh() {
        TicketModel fromDatabase = new TicketModel();
        Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
        fromDatabase = db.from(fromDatabase).where(fromDatabase.ticketName).is(ticketName).selectFirst();
        db.close();

        // Refresh the data
        openTime = fromDatabase.openTime;
        closeTime = fromDatabase.closeTime;
        locWorld = fromDatabase.locWorld;
        locX = fromDatabase.locX;
        locY = fromDatabase.locY;
        locZ = fromDatabase.locZ;
        state = fromDatabase.state;
        moderatorId = fromDatabase.moderatorId;
    }
}
