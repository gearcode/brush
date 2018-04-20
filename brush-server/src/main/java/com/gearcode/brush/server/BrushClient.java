package com.gearcode.brush.server;

import io.netty.channel.socket.SocketChannel;

/**
 * Created by liteng3 on 2018/4/13.
 */
public class BrushClient {

    private String id;
    private String ip;
    private String name;
    private String pass = "";

    private transient SocketChannel socketChannel;
    private volatile BrushConsole console;

    public BrushConsole getConsole() {
        return console;
    }

    public BrushClient setConsole(BrushConsole console) {
        this.console = console;
        return this;
    }

    public String getPass() {
        return pass;
    }

    public BrushClient setPass(String pass) {
        this.pass = pass;
        return this;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public BrushClient setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        return this;
    }

    public String getName() {
        return name;
    }

    public BrushClient setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public BrushClient setId(String id) {
        this.id = id;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public BrushClient setIp(String ip) {
        this.ip = ip;
        return this;
    }
}
