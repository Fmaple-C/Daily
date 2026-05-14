package com.maple.daily.data;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemePreferences {
    private static final String PREFS_NAME = "daily_maple_ui_prefs";
    private static final String NIGHT_THEME_KEY = "night_theme";

    private final SharedPreferences preferences;

    public ThemePreferences(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isNightTheme() {
        return preferences.getBoolean(NIGHT_THEME_KEY, false);
    }

    public void setNightTheme(boolean enabled) {
        preferences.edit().putBoolean(NIGHT_THEME_KEY, enabled).apply();
    }
}
