package me.hqm.plugindev.creativeticketreview.model;

import com.iciql.Db;
import me.hqm.plugindev.creativeticketreview.CTRSetting;

public abstract class AbstractModel {
    public abstract void refresh();

    public void insert() {
        Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
        db.insert(this);
        db.close();
    }

    public void update() {
        Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
        db.update(this);
        db.close();
    }

    public void delete() {
        Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
        db.delete(this);
        db.close();
    }
}
