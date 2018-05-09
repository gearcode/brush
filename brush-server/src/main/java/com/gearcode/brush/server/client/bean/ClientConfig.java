package com.gearcode.brush.server.client.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 *
 * @author jason
 * @date 2018/5/8
 */
public class ClientConfig implements Serializable {
    @JsonIgnore
    private transient String password;
    private Integer width;
    private Integer height;

    public ClientConfig() {
    }

    public String getPassword() {
        return password;
    }

    public ClientConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public ClientConfig setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public ClientConfig setHeight(Integer height) {
        this.height = height;
        return this;
    }
}
