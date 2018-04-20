package com.gearcode.brush.server.codec;

import com.gearcode.brush.server.BrushClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;


/**
 * 初始化ServerSocket
 * @author jason
 * @date 2018/4/10
 */
@ChannelHandler.Sharable
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(ServerChannelInitializer.class);

    ConcurrentMap<String, BrushClient> clientMap;

    /**
     * 数据帧最大8M
     */
    final static int PAYLOAD = 8 * 1024 * 1024;

    public ServerChannelInitializer(ConcurrentMap clientMap) {
        this.clientMap = clientMap;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        logger.info("Connected from {}", NetUtil.toAddressString(ch.remoteAddress().getAddress()));

        // 初始化ServerSocket的Pipeline
        ch.pipeline()
        .addLast(new MessageToByteEncoder<ByteBuf>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
                logger.info("Sending message: {}", msg.readableBytes());
                out.capacity(out.readableBytes() + 6);
                out.writeBytes(new byte[]{0, 0});
                out.writeInt(msg.readableBytes());
                out.writeBytes(msg);
            }
        })
        .addLast(new StringEncoder(CharsetUtil.UTF_8))
        .addLast(new LengthFieldBasedFrameDecoder(PAYLOAD, 2, 4, 0, 6))
        .addLast(new ServerLogicHandler(clientMap));

        // 加入客户端列表
        BrushClient client = new BrushClient();
        client.setIp(NetUtil.toAddressString(ch.remoteAddress().getAddress()));
        client.setId(ch.id().asShortText());
        client.setName(client.getIp() + "(" + client.getId() + ")");
        client.setSocketChannel(ch);
        clientMap.put(ch.id().asShortText(), client);

    }
}
