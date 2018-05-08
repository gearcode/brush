package com.gearcode.brush.server.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jason
 * @date 2018/5/7
 */
@ChannelHandler.Sharable
public class LengthFieldBasedFrameEncoder extends MessageToByteEncoder<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(LengthFieldBasedFrameEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        logger.info("Encode outbound message, length: {}", msg.readableBytes());
        // 2 MAGIC / 4 LENGTH
        out.capacity(out.readableBytes() + 6);
        // MAGIC CODE
        out.writeBytes(new byte[]{0, 0});
        // LENGTH
        out.writeInt(msg.readableBytes());
        // DATA
        out.writeBytes(msg);
    }
}
