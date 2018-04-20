package com.gearcode.brush.server.boot;

import com.gearcode.brush.server.BrushServer;
import com.gearcode.brush.server.impl.NettyBrushServer;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by liteng3 on 2018/4/13.
 */
public class NettyBrushServerSpringHolder implements InitializingBean {


    BrushServer server = new NettyBrushServer();

    public BrushServer getServer() {
        return server;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread thread = new Thread(() -> {
            server.start();
        });
        thread.setDaemon(true);
        thread.setName("NettyBrushServer-thread");
        thread.start();
    }
}
