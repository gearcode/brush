package com.gearcode.brush.client.util;

import com.gearcode.brush.client.handler.ClientLogicHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author jason
 * @date 2018/5/8
 */
public class ClientConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ClientConfigUtil.class);

    public static ClientConfig getCurrentClientConfig() {
        ClientConfig config = new ClientConfig();

        // 设置Client名称
        String hostName = "Unknown";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        config.setName(System.getProperty("user.name") + "(" + hostName + ")");

        // 获取屏幕尺寸
        Dimension clientSize = Toolkit.getDefaultToolkit().getScreenSize();
        config.setWidth(clientSize.width);
        config.setHeight(clientSize.height);

        // 获取密码
        config.setPassword(PrefConfig.retrieve(Constants.PREF_KEY_PASSWORD));
        return config;
    }
}
