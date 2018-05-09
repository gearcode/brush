package com.gearcode.brush.client.util;

import java.util.prefs.Preferences;

/**
 * Created by jason on 2018/5/9.
 */
public class PrefConfig {

    static final String PATH_NAME = "/brushclient";

    public static String retrieve(String key) {
        Preferences root = Preferences.userRoot();
        Preferences brushClientRoot = root.node(PATH_NAME);

        return brushClientRoot.get(key, null);
    }

    public static void save(String key, String value) {
        Preferences root = Preferences.userRoot();
        Preferences brushClientRoot = root.node(PATH_NAME);

        brushClientRoot.put(key, value);

    }

    public static void main(String[] args) {
        System.out.println(retrieve("password"));
        save("password", "jason123");
        System.out.println(retrieve("password"));
    }
}
