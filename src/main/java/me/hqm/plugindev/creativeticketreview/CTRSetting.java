package me.hqm.plugindev.creativeticketreview;

public class CTRSetting<T> {
    public static final CTRSetting<String> DATABASE_URL;
    public static final CTRSetting<Integer> REVIEW_RANGE;

    static {
        DATABASE_URL = new CTRSetting<>("jdbc:" + CTRPlugin.getInst().getConfig().getString("database-url"));
        REVIEW_RANGE = new CTRSetting<>(CTRPlugin.getInst().getConfig().getInt("review-range"));
    }

    private T value;

    CTRSetting(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
