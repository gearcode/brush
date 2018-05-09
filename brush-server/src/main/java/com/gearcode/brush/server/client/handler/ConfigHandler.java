package com.gearcode.brush.server.client.handler;

import com.alibaba.fastjson.JSON;
import com.gearcode.brush.server.client.BrushServer;
import com.gearcode.brush.server.client.bean.BrushClient;
import com.gearcode.brush.server.client.bean.ClientConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jason
 * @date 2018/5/8
 */
public class ConfigHandler implements ClientMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClientMessageHandler.class);

    @Override
    public void handle(BrushServer server, ChannelHandlerContext ctx, ByteBuf byteBuf) {

        String configJSON = byteBuf.toString(CharsetUtil.UTF_8);
        logger.info("Config from client: {}", configJSON);

        ClientConfig config = JSON.parseObject(configJSON, ClientConfig.class);

        BrushClient client = server.findClient(ctx.channel().id().asShortText());
        if(client != null) {
            client.setClientConfig(config);

            if(null != config.getPassword()) {
                client.setStandby(true);
            }
        }
    }
}
