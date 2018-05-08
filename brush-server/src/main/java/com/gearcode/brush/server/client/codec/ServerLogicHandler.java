package com.gearcode.brush.server.client.codec;

import com.gearcode.brush.server.client.BrushServer;
import com.gearcode.brush.server.client.handler.ClientMessageHandler;
import com.gearcode.brush.server.client.handler.ConfigHandler;
import com.gearcode.brush.server.client.handler.HeartbeatHandler;
import com.gearcode.brush.server.client.handler.ScreenHandler;
import com.gearcode.brush.server.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jason
 * @date 2018/4/17
 */
@ChannelHandler.Sharable
public class ServerLogicHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(ServerLogicHandler.class);
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("HEARTBEAT", CharsetUtil.UTF_8));

    BrushServer server;

    /**
    业务线程池
     */
    static ThreadPoolExecutor businessPool = new ThreadPoolExecutor(1, 20,
     60, TimeUnit.SECONDS,
     new LinkedBlockingQueue<>(100), new DefaultThreadFactory("SERVER-BIZ", true), new ThreadPoolExecutor.AbortPolicy());

    static Map<Constants.ClientMessageType, Class<? extends ClientMessageHandler>> messageHandlers = new HashMap();
    static {
        messageHandlers.put(Constants.ClientMessageType.CONFIG, ConfigHandler.class);
        messageHandlers.put(Constants.ClientMessageType.HEATBEAT, HeartbeatHandler.class);
        messageHandlers.put(Constants.ClientMessageType.SCREEN, ScreenHandler.class);
    }

    static class ServerTask implements Runnable {

        BrushServer server;
        ClientMessageHandler handler;
        ChannelHandlerContext ctx;
        ByteBuf msg;

        public ServerTask(BrushServer server, ClientMessageHandler handler, ChannelHandlerContext ctx, ByteBuf msg) {
            this.server = server;
            this.handler = handler;
            this.ctx = ctx;
            this.msg = msg;
        }

        @Override
        public void run() {
            try {
                handler.handle(server, ctx, msg);
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    public ServerLogicHandler(BrushServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int readableBytes = msg.readableBytes();
        logger.info("Receive message: {}", readableBytes);

        // 消息体为空, 不进行任何处理
        if(readableBytes < 1) {
            logger.error("Message body size < 1");
            return;
        }

        /*
        获取对应的消息处理器
         */
        byte type = msg.readByte();
        Class<ClientMessageHandler> handlerClass = (Class<ClientMessageHandler>) messageHandlers.get(Constants.ClientMessageType.valueOf(type));
        if(handlerClass == null ) {
            throw new NullPointerException("ClientMessageHandler is null, message type: " + type);
        }
        ClientMessageHandler handler = handlerClass.newInstance();

        // 提交ServerTask
        businessPool.submit(new ServerTask(server, handler, ctx, msg.readBytes(msg.readableBytes())));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof IOException) {
            // 客户端入站异常, 关闭连接
            logger.error(cause.getLocalizedMessage());
            server.getClientMap().remove(ctx.channel().id().asShortText());
            ctx.close();
        } else {
            logger.error(cause.getLocalizedMessage(), cause);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // Idle
        if(evt instanceof IdleStateEvent) {
            logger.info("Sending HEARTBEAT");
            ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        logger.error("Send HEARTBEAT error, close this channel.");
                        server.getClientMap().remove(future.channel().id().asShortText());
                        future.channel().close();
                    }
                }
            });
            return;
        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                ctx.writeAndFlush("FETCH_SCREEN").sync();
//                logger.info("Send message success!");
    }

}