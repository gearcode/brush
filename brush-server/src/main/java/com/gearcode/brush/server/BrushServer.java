package com.gearcode.brush.server;

import java.util.List;

/**
 * Created by liteng3 on 2018/4/8.
 */
public interface BrushServer {

    /**
     * 启动server
     */
    void start();

    List<BrushClient> clients();

    BrushClient findClient(String shortId);
}
