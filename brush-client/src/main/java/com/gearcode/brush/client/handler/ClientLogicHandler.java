package com.gearcode.brush.client.handler;

import com.alibaba.fastjson.JSON;
import com.gearcode.brush.client.util.ClientConfig;
import com.gearcode.brush.client.util.ClientConfigUtil;
import com.gearcode.brush.client.util.Constants;
import com.gearcode.brush.client.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Created by jason on 2018/5/7.
 */
public class ClientLogicHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger logger = LoggerFactory.getLogger(ClientLogicHandler.class);
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("HEARTBEAT", CharsetUtil.UTF_8));

    /**
     * 图像质量压缩
     */
    final static double SCREEN_QUALITY = 0.6;
    final static String SCREEN_FORMAT = "png";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        try {
            logger.info("Receive server message: {}", msg);

            String[] split = msg.split(",");
            String command = split[0];

            if (command.equals("HEARTBEAT")) {
                logger.info("HEARTBEAT from Server");
            }

            // 发送屏幕
            if (command.equals("FETCH_SCREEN")) {
                // 输出缓冲区, 共输出两幅图像, 数据帧 Int32 Int32 Img1bytes Img2bytes
                ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
                ByteArrayOutputStream focusStream = new ByteArrayOutputStream();
                Dimension clientSize = Toolkit.getDefaultToolkit().getScreenSize();

                // 计算
                int consoleWidth = Integer.parseInt(split[1]);
                int consoleHeight = Integer.parseInt(split[2]);
                int focusX = Integer.parseInt(split[3]);
                int focusY = Integer.parseInt(split[4]);
                int offsetX = focusX - consoleWidth / 2, offsetY = focusY - consoleHeight / 2;
                int maxOffsetX = clientSize.width - consoleWidth;
                int maxOffsetY = clientSize.height - consoleHeight;
                offsetX = offsetX < 0 ? 0 : (offsetX > maxOffsetX ? maxOffsetX : offsetX);
                offsetY = offsetY < 0 ? 0 : (offsetY > maxOffsetY ? maxOffsetY : offsetY);

                // FULL
                Rectangle thumbnailRect = new Rectangle(clientSize);
                BufferedImage thumbnailCapture = new Robot().createScreenCapture(thumbnailRect);
                Thumbnails.of(thumbnailCapture).size(consoleWidth, consoleHeight).outputQuality(SCREEN_QUALITY).outputFormat(SCREEN_FORMAT).toOutputStream(thumbnailStream);

                // FOCUS
                Rectangle focusRect = new Rectangle(offsetX, offsetY, consoleWidth, consoleHeight);
                BufferedImage focusCapture = new Robot().createScreenCapture(focusRect);
                Thumbnails.of(focusCapture).scale(1).outputQuality(SCREEN_QUALITY).outputFormat(SCREEN_FORMAT).toOutputStream(focusStream);

                // 写入到最终的数据流
                logger.info("Client size: {}, Focus size: {}", thumbnailStream.size(), focusStream.size());
                ByteBuf resultBuffer = ctx.alloc().buffer(1 + 4 + thumbnailStream.size() + 4 + focusStream.size())
                        .writeByte(Constants.ClientMessageType.SCREEN.value())
                        .writeInt(thumbnailStream.size())
                        .writeBytes(thumbnailStream.toByteArray())
                        .writeInt(focusStream.size())
                        .writeBytes(focusStream.toByteArray());

                // 写入socket
                logger.info("Send to server, size: {}", resultBuffer.readableBytes());
                ctx.writeAndFlush(resultBuffer);
            }

            // 点击
            if (command.equals("CLICK")) {
                // 获取参数
                Dimension clientSize = Toolkit.getDefaultToolkit().getScreenSize();
                int button = Integer.parseInt(split[1]);
                int consoleWidth = Integer.parseInt(split[2]);
                int consoleHeight = Integer.parseInt(split[3]);
                int focusX = Integer.parseInt(split[4]);
                int focusY = Integer.parseInt(split[5]);
                int clickConsoleX = Integer.parseInt(split[6]);
                int clickConsoleY = Integer.parseInt(split[7]);

                // 计算offset
                int offsetX = focusX - consoleWidth / 2, offsetY = focusY - consoleHeight / 2;
                int maxOffsetX = clientSize.width - consoleWidth;
                int maxOffsetY = clientSize.height - consoleHeight;
                offsetX = offsetX < 0 ? 0 : (offsetX > maxOffsetX ? maxOffsetX : offsetX);
                offsetY = offsetY < 0 ? 0 : (offsetY > maxOffsetY ? maxOffsetY : offsetY);

                // 点击坐标
                int clickX = clickConsoleX + offsetX;
                int clickY = clickConsoleY + offsetY;

                int mask = 0;
                switch (button) {
                    case 1:
                        mask = InputEvent.BUTTON1_MASK;
                        break;
                    case 2:
                        mask = InputEvent.BUTTON2_MASK;
                        break;
                    case 3:
                        mask = InputEvent.BUTTON3_MASK;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown button mask: " + button);
                }
                Utils.mouseClick(mask, clickX, clickY);
                logger.info("Click: {}, {}, {}", mask, clickX, clickY);

            }

            // 输入
            if (command.equals("INPUT")) {
                for (int i = 1; i < split.length; i++) {
                    int ascii = Integer.parseInt(split[i]);
                    Utils.keyPress(ascii);
                    logger.info("Input: {}", (char) ascii);
                }
            }

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getLocalizedMessage(), cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // Idle
        if(evt instanceof IdleStateEvent) {
            logger.info("Sending HEARTBEAT");
            ctx.writeAndFlush(ctx.alloc().buffer(1).writeByte(Constants.ClientMessageType.HEATBEAT.value())).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    logger.error("Send HEARTBEAT error, close this channel.");
                    future.channel().close();
                }
            });
            return;
        }

        // Config
        if(evt instanceof ClientConfig) {
            ClientConfig config = (ClientConfig) evt;
            sendConfigToServer(ctx, config);
        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ClientConfig config = ClientConfigUtil.getCurrentClientConfig();
        sendConfigToServer(ctx, config);
    }

    /**
     * 发送配置信息到Server端
     * @param ctx
     * @param config
     */
    private void sendConfigToServer(ChannelHandlerContext ctx, ClientConfig config) {
        String configJSON = JSON.toJSONString(config);

        ByteBuf buffer = ctx.alloc().buffer(1 + configJSON.length());
        ctx.writeAndFlush(buffer.writeByte(Constants.ClientMessageType.CONFIG.value()).writeBytes(configJSON.getBytes(CharsetUtil.UTF_8)));
        logger.info("Send message success!");
    }
}
