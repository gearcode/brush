package com.gearcode.brush.client.util;

import java.awt.*;

/**
 *
 * @author jason
 * @date 2018/5/8
 */
public class ClientConfigUtil {

    public static ClientConfig getCurrentClientConfig() {
        ClientConfig config = new ClientConfig();

        // 设置Client名称
        config.setName(System.getenv("USERNAME") + "(" + System.getenv("COMPUTERNAME") + ")");

        // 获取屏幕尺寸
        Dimension clientSize = Toolkit.getDefaultToolkit().getScreenSize();
        config.setWidth(clientSize.width);
        config.setHeight(clientSize.height);

        // 获取密码
        config.setPassword(PrefConfig.retrieve(Constants.PREF_KEY_PASSWORD));
        return config;
    }
}
