package com.gearcode.brush.server.codec;

import com.gearcode.brush.server.BrushClient;
import com.gearcode.brush.server.BrushConsole;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by liteng3 on 2018/4/17.
 */
@ChannelHandler.Sharable
public class ServerLogicHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(ServerLogicHandler.class);

    ConcurrentMap<String, BrushClient> channelMap;

    public ServerLogicHandler(ConcurrentMap<String, BrushClient> channelMap) {
        this.channelMap = channelMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int readableBytes = msg.readableBytes();
        logger.info("Receive message: {}", readableBytes);

        // 读取bytes
        byte[] bytes = new byte[readableBytes];
        msg.readBytes(bytes);

        // 收到client的数据, 直接发送给console
        SocketChannel channel = (SocketChannel) ctx.channel();
        BrushClient brushClient = channelMap.get(channel.id().asShortText());
        BrushConsole console = brushClient.getConsole();
        if(console != null) {
            try {
                console.getSession().sendMessage(new BinaryMessage(bytes));
                logger.info("Send message from client[{}] to console[{}], message: IMG[{}]",
                        NetUtil.toAddressString(console.getSession().getRemoteAddress().getAddress()),
                        NetUtil.toAddressString(channel.remoteAddress().getAddress()),
                        bytes.length);
            } catch (IOException e) {
                logger.error("Send message to console error!", e);
                //TODO 删除此Console
            }
        }

//        // 写入文件
//        String ip = NetUtil.toAddressString(((SocketChannel) ctx.channel()).remoteAddress().getAddress());
//        Path path = Paths.get(ip + " - " + System.currentTimeMillis() + ".jpg");
//        byte[] bytes = new byte[readableBytes];
//        msg.readBytes(bytes);
//        Files.write(path, bytes, APPEND, CREATE);
//        logger.info("Save to: {}", path.toAbsolutePath());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof IOException) {
            // 客户端入站异常, 关闭连接
            logger.error(cause.getLocalizedMessage());
            channelMap.remove(ctx.channel().id().asShortText());
            ctx.close();
        } else {
            logger.error(cause.getLocalizedMessage(), cause);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                ctx.writeAndFlush("FETCH_SCREEN").sync();
//                logger.info("Send message success!");
    }
}
