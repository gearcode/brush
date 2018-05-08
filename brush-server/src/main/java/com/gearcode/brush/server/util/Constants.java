package com.gearcode.brush.server.util;

/**
 * Created by jason on 2018/4/17.
 */
public class Constants {
    public static final String WS_HS_KEY_CLIENT = "client";
    public static final String WS_HS_KEY_TOKEN = "token";
    public static final String WS_HS_KEY_WIDTH = "width";
    public static final String WS_HS_KEY_HEIGHT = "height";

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
