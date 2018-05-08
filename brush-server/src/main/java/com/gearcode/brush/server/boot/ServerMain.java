package com.gearcode.brush.server.boot;

import com.gearcode.brush.server.client.BrushServer;
import com.gearcode.brush.server.client.NettyBrushServer;

import java.io.IOException;

/**
 *
 * @author jason
 * @date 2018/3/16
 */
public class ServerMain {

    public static void main(String[] args) throws IOException {
        BrushServer brushServer = new NettyBrushServer();
        brushServer.start();
    }
}
