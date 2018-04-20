package com.gearcode.brush.server;

/**
 * Created by liteng3 on 2018/3/20.
 */
public class ServerConfig {

    private Integer port;
    private Integer backlog = 1024;
    private Long selectTimeout = 1000L;
    private Long shutdownTimeout = 3000L;

    public Integer getPort() {
        return port;
    }

    public ServerConfig setPort(Integer port) {
        this.port = port;
        return this;
    }

    public Integer getBacklog() {
        return backlog;
    }

    public ServerConfig setBacklog(Integer backlog) {
        this.backlog = backlog;
        return this;
    }

    public Long getSelectTimeout() {
        return selectTimeout;
    }

    public ServerConfig setSelectTimeout(Long selectTimeout) {
        this.selectTimeout = selectTimeout;
        return this;
    }

    public Long getShutdownTimeout() {
        return shutdownTimeout;
    }

    public ServerConfig setShutdownTimeout(Long shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
        return this;
    }
}
