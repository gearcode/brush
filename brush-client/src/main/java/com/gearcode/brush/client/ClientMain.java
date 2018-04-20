package com.gearcode.brush.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;


/**
 * Created by liteng3 on 2018/3/16.
 */
public class ClientMain {
    private static final Logger logger = LoggerFactory.getLogger(ClientMain.class);

    static final String IP = "127.0.0.1";
    static final int PORT = 10230;

    /**
     * 数据帧最大8M
     */
    final static int PAYLOAD = 8 * 1024 * 1024;

    /**
     * 图像质量压缩
     */
    final static double SCREEN_QUALITY = 0.8;
    final static String SCREEN_FORMAT = "png";

    public static void main(String[] args) throws InterruptedException {

        // TODO 业务线程池

        while(true) {
            logger.info("Start connect to server...");
            connectToServer();
            logger.info("Connection closed, reconnect 3 seconds later...");
            Thread.sleep(3000L);
        }
    }

    private static void connectToServer() {

        // netty nio线程池
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(IP, PORT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(PAYLOAD, 2, 4, 0, 6))
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                    .addLast(new MessageToByteEncoder<byte[]>() {
                                        @Override
                                        protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception {
                                            logger.info("Sending message: {}", msg.length);
                                            out.capacity(out.readableBytes() + 6);
                                            out.writeBytes(new byte[]{0, 0});
                                            out.writeInt(msg.length);
                                            out.writeBytes(msg);
                                        }
                                    })
                                    .addLast(new SimpleChannelInboundHandler<String>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                            try {
                                                logger.info("Receive server message: {}", msg);

                                                String[] split = msg.split(",");
                                                String command = split[0];

                                                // 发送屏幕
                                                if (command.equals("FETCH_SCREEN")) {
                                                    int consoleWidth = Integer.parseInt(split[1]);
                                                    int consoleHeight = Integer.parseInt(split[2]);

                                                    ByteArrayOutputStream captureCompressionStream = new ByteArrayOutputStream();
                                                    Dimension clientSize = Toolkit.getDefaultToolkit().getScreenSize();

                                                    // focus
                                                    if (split.length == 5) {
                                                        int focusX = Integer.parseInt(split[3]);
                                                        int focusY = Integer.parseInt(split[4]);
                                                        int offsetX = focusX - consoleWidth / 2, offsetY = focusY - consoleHeight / 2;
                                                        int maxOffsetX = clientSize.width - consoleWidth;
                                                        int maxOffsetY = clientSize.height - consoleHeight;
                                                        offsetX = offsetX < 0 ? 0 : (offsetX > maxOffsetX ? maxOffsetX : offsetX);
                                                        offsetY = offsetY < 0 ? 0 : (offsetY > maxOffsetY ? maxOffsetY : offsetY);
                                                        Rectangle screenRect = new Rectangle(offsetX, offsetY, consoleWidth, consoleHeight);
                                                        logger.info("Focus: {}", screenRect);
                                                        BufferedImage capture = new Robot().createScreenCapture(screenRect);
                                                        Thumbnails.of(capture).scale(1).outputQuality(SCREEN_QUALITY).outputFormat(SCREEN_FORMAT).toOutputStream(captureCompressionStream);
                                                    } else {
                                                        Rectangle screenRect = new Rectangle(clientSize);
                                                        BufferedImage capture = new Robot().createScreenCapture(screenRect);
                                                        // 压缩
                                                        Thumbnails.of(capture).size(consoleWidth, consoleHeight).outputQuality(SCREEN_QUALITY).outputFormat(SCREEN_FORMAT).toOutputStream(captureCompressionStream);
                                                    }

                                                    // 写入socket
                                                    ctx.writeAndFlush(captureCompressionStream.toByteArray());
                                                    logger.info("Write capture, size: {}", captureCompressionStream.size());
                                                }

                                                // 远端命令
                                                if (command.equals("CLICK")) {
                                                }

                                            } catch (Exception e) {
                                                logger.error(e.getLocalizedMessage(), e);
                                            }
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                            logger.error("ERROR: {}", cause.getLocalizedMessage());
//                                            cause.printStackTrace();
                                        }
                                    });
                        }
                    });
            ChannelFuture f = b.connect();
            f.addListener((ChannelFutureListener) future -> {
                if(future.isSuccess()) {
                    logger.info("Connect server success!");
                } else {
                    logger.error("Failed to connect to server: [{}:{}], error: {}", IP, PORT, future.cause().getLocalizedMessage());
                }
            });

            // 阻塞至channel关闭
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
