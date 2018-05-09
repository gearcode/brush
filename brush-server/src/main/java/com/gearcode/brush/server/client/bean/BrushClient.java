package com.gearcode.brush.server.client.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gearcode.brush.server.websocket.BrushConsole;
import io.netty.channel.socket.SocketChannel;

/**
 *
 * @author jason
 * @date 2018/4/13
 */
public class BrushClient {

    /**
     * 客户端ID, 使用SocketChannel的ID, 例如: channel.id().asShortText()
     */
    private String id;
    private String ip;
    private String name;

    /**
     * 客户端是否已就绪
     */
    private Boolean standby = false;

    private ClientConfig clientConfig;

    /**
     * 客户端的socketChannel
     */
    @JsonIgnore
    private transient SocketChannel socketChannel;

    /**
     * 对应的console端
     */
    @JsonIgnore
    private volatile BrushConsole console;

    public Boolean isStandby() {
        return standby;
    }

    public BrushClient setStandby(Boolean standby) {
        this.standby = standby;
        return this;
    }


    public BrushConsole getConsole() {
        return console;
    }

    public BrushClient setConsole(BrushConsole console) {
        this.console = console;
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

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public BrushClient setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }
}
