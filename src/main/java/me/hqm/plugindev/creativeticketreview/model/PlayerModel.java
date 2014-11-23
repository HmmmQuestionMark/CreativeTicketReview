package me.hqm.plugindev.creativeticketreview.model;

import com.iciql.Db;
import com.iciql.Iciql;
import me.hqm.plugindev.creativeticketreview.CTRSetting;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.UUID;

@Iciql.IQTable(name = "ctr_users")
public class PlayerModel extends AbstractModel {

    // -- STATIC CONSTRUCTOR -- //

    public static PlayerModel of(Player player) {
        // Create the new object
        PlayerModel model = new PlayerModel();

        // Check if it exists
        boolean exists = existsFor(player);
        if (exists) {
            model = getFor(player.getUniqueId());
        } else {
            model.setMinecraftId(player.getUniqueId().toString());
        }

        // Always set these
        model.setLastKnownName(player.getName());
        model.setLastLoginTime(new Timestamp(System.currentTimeMillis()));

        // Update or insert into database
        if (exists) {
            model.update();
        } else {
            model.insert();
        }

        // Return the object
        return model;
    }

    // -- UTIL -- //

    public static boolean existsFor(Player player) {
        return getFor(player.getUniqueId()) != null;
    }

    public static PlayerModel getFor(UUID playerId) {
        PlayerModel alias = new PlayerModel();
        Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
        try {
            return db.from(alias).where(alias.minecraftId).is(playerId.toString()).selectFirst();
        } finally {
            db.close();
        }
    }

    // -- MODEL META -- //

    @Iciql.IQColumn(name = "player_id", primaryKey = true, length = 200)
    public String minecraftId;

    // -- DATA FIELDS -- //

    @Iciql.IQColumn(name = "last_known_name", length = 200)
    public String lastKnownName;

    @Iciql.IQColumn(name = "last_login_time")
    public Timestamp lastLoginTime;

    @Iciql.IQColumn(name = "last_logout_time")
    public Timestamp lastLogoutTime;

    // -- ADDITIONAL DATA -- //

    @Iciql.IQColumn
    public Integer points = 0;

    // -- GETTERS & SETTERS -- //

    public String getMinecraftId() {
        return minecraftId;
    }

    public void setMinecraftId(String minecraftId) {
        this.minecraftId = minecraftId;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Timestamp getLastLogoutTime() {
        return lastLogoutTime;
    }

    public void setLastLogoutTime(Timestamp lastLogoutTime) {
        this.lastLogoutTime = lastLogoutTime;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    // Synchronized to prevent errors when grabbing the offline player object.
    protected synchronized OfflinePlayer getOfflinePlayer() {
        UUID uuid = UUID.fromString(minecraftId);
        return Bukkit.getOfflinePlayer(uuid);
    }

    // -- DATABASE RELATED -- //

    @Override
    public void refresh() {
        PlayerModel fromDatabase = new PlayerModel();
        Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
        fromDatabase = db.from(fromDatabase).where(fromDatabase.minecraftId).is(minecraftId).selectFirst();
        db.close();

        // Refresh the data
        minecraftId = fromDatabase.getMinecraftId();
        lastKnownName = fromDatabase.getLastKnownName();
        lastLoginTime = fromDatabase.getLastLoginTime();
        lastLogoutTime = fromDatabase.getLastLogoutTime();
        points = fromDatabase.getPoints();
    }
}
