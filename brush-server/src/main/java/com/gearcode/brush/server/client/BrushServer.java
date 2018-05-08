package com.gearcode.brush.server.client;

import com.gearcode.brush.server.client.bean.BrushClient;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jason on 2018/4/8.
 */
public interface BrushServer {

    /**
     * 启动server
     */
    void start();

    List<BrushClient> clients();

    BrushClient findClient(String shortId);

    ConcurrentMap<String, BrushClient> getClientMap();
}
