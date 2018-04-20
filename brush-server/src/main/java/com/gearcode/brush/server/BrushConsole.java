package com.gearcode.brush.server;

import io.netty.util.NetUtil;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by liteng3 on 2018/4/20.
 */
public class BrushConsole {

    String id;
    String ip;
    Integer width;
    Integer height;
    Integer focus;
    transient WebSocketSession session;

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(obj instanceof BrushConsole) {
            BrushConsole o = (BrushConsole) obj;
            if(o.getSession() == null) {
                return false;
            }
            return o.getSession().getId().equals(id);
        } else {
            return false;
        }
    }

    public BrushConsole(WebSocketSession session, Integer width, Integer height) {
        this.id = session.getId();
        this.session = session;
        this.width = width;
        this.height = height;
        this.ip = NetUtil.toAddressString(session.getRemoteAddress().getAddress());
    }

    public String getIp() {
        return ip;
    }

    public BrushConsole setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public BrushConsole setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public BrushConsole setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getFocus() {
        return focus;
    }

    public BrushConsole setFocus(Integer focus) {
        this.focus = focus;
        return this;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public BrushConsole setSession(WebSocketSession session) {
        this.session = session;
        return this;
    }
}
