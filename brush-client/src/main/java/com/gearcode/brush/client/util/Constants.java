package com.gearcode.brush.client.util;

/**
 * Created by jason on 2018/4/17.
 */
public class Constants {

    public static final String PREF_KEY_PASSWORD = "password";

    public enum ClientMessageType {
        CONFIG(1), HEATBEAT(2), SCREEN(3);

        int value;

        ClientMessageType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static ClientMessageType valueOf(int value) {
            switch (value) {
                case 1: return CONFIG;
                case 2: return HEATBEAT;
                case 3: return SCREEN;
                default: throw new IllegalArgumentException("Unknown message type: " + value);
            }
        }
    }
}
