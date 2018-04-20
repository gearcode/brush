package com.gearcode.brush.server.impl;

import com.gearcode.brush.server.BrushClient;
import com.gearcode.brush.server.BrushServer;
import com.gearcode.brush.server.codec.ServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 基于Netty实现的BrushServer
 * @author jason
 * @date 2018/4/8
 */
public class NettyBrushServer implements BrushServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyBrushServer.class);

    private static final int Port = 10230;
    private static final int ParentGroupThreads = 2;
    private static final int ChildGroupThreads = 4;
    private static final int ConnectionTimeoutMillis = 5000;

    private ConcurrentMap<String, BrushClient> clientMap;

    public NettyBrushServer() {
        clientMap = new ConcurrentHashMap<>();
    }

    @Override
    public void start() {

        // EventLoopGroup
        NioEventLoopGroup parentGroup = new NioEventLoopGroup(ParentGroupThreads, new DefaultThreadFactory("parentGroup", true));
        NioEventLoopGroup childGroup = new NioEventLoopGroup(ChildGroupThreads, new DefaultThreadFactory("childGroup", true));
        // ServerBootstrap
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(Port)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ConnectionTimeoutMillis)
                .childHandler(new ServerChannelInitializer(clientMap));

        try {
            // 阻塞直到绑定操作完成
            ChannelFuture future = serverBootstrap.bind().sync();
            logger.info("Server startup! PORT: {}", Port);
            // 阻塞线程直到服务器停止
            future.channel().closeFuture().sync();
            logger.info("Server shutdown!");

        } catch (InterruptedException e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                clientMap.clear();
                parentGroup.shutdownGracefully().sync();
                childGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }

    }

    @Override
    public List<BrushClient> clients() {
        List<BrushClient> list = clientMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
        return list;
    }

    @Override
    public BrushClient findClient(String shortId) {
        clientMap.get(shortId);
        return clientMap.get(shortId);
    }
}
