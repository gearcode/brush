package com.gearcode.brush.client.util;

/**
 *
 * @author jason
 * @date 2018/5/8
 */
public class ClientConfigUtil {
    public static ClientConfig getClientConfig() {
        ClientConfig config = new ClientConfig();
        config.setPassword(PrefConfig.retrieve(Constants.PREF_KEY_PASSWORD));
        return config;
    }
}
