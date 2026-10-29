package com.abo47.questsandstuff.client.tablet.app;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TabletAppRegistry {
    public static final String APP_HOME = "home";
    public static final String APP_QUESTS = "quest";
    public static final String APP_TEAMS = "teams";
    public static final String APP_CHUNKCLAIMER = "chunkclaimer";
    public static final String APP_SETTINGS = "settings";

    private static final LinkedHashMap<String, AppDescriptor> APPS = new LinkedHashMap<>();

    private TabletAppRegistry() {
    }

    public static void register(AppDescriptor app) {
        APPS.put(app.id(), app);
    }

    public static AppDescriptor get(String id) {
        return APPS.get(id);
    }

    public static Map<String, AppDescriptor> all() {
        return Map.copyOf(APPS);
    }

    public static String saveKeyOf(String appId) {
        if (appId == null) {
            return null;
        }
        return switch (appId) {
            case APP_QUESTS -> "QUESTS";
            case APP_TEAMS -> "TEAMS";
            case APP_CHUNKCLAIMER -> "CHUNKCLAIMER";
            case APP_SETTINGS -> "SETTINGS";
            case APP_HOME -> "";
            default -> null;
        };
    }
}
