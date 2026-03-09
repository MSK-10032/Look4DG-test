package qrz.ba4ihr.look4dg;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import qrz.ba4ihr.look4dg.models.Config;

public class ConfigManager {
    private static final String PREF_NAME = "look4dg_config";
    private static final String KEY_CONFIG = "config";

    private SharedPreferences prefs;
    private Gson gson;

    public ConfigManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public Config loadConfig() {
        String json = prefs.getString(KEY_CONFIG, null);
        if (json != null) {
            return gson.fromJson(json, Config.class);
        }
        return new Config(); // 返回默认配置
    }

    public void saveConfig(Config config) {
        String json = gson.toJson(config);
        prefs.edit().putString(KEY_CONFIG, json).apply();
    }
}