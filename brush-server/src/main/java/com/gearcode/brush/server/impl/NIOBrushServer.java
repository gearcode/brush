package com.gearcode.brush.server.impl;

import com.gearcode.brush.server.BrushClient;
import com.gearcode.brush.server.BrushServer;
import com.gearcode.brush.server.ServerConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by liteng3 on 2018/3/20.
 */
public class NIOBrushServer implements BrushServer {

    private static volatile NIOBrushServer instance;

    private volatile boolean stop = false;
    private ServerConfig config;
    private ConcurrentLinkedQueue<SocketChannel> clientList = new ConcurrentLinkedQueue<>();

    private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("server-pool-%d").build();
    private ExecutorService serverPool = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(32), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    private NIOBrushServer(ServerConfig config) {
        this.config = config;
    }

    public static NIOBrushServer getInstance(ServerConfig config) throws IOException {
        if(instance == null) {
            synchronized (NIOBrushServer.class) {
                if(instance == null) {
                    instance = new NIOBrushServer(config);
                }
            }
        }

        instance.start();

        return instance;
    }

    @Override
    public void start() {

        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();

            // Server build
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(config.getPort()), config.getBacklog());
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server start up, port: " + config.getPort());

            serverPool.execute(()-> {
                try {
                    while(!stop) {
                        // blocking util event, or timeout
                        int selected = selector.select(config.getSelectTimeout());
                        System.out.println("Selected: " + selected);

                        Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
                        while(keysIterator.hasNext()) {
                            SelectionKey key = keysIterator.next();
                            // 删除此key, 以免在循环中重复处理
                            keysIterator.remove();

                            try {

                                if(!key.isValid()) {
                                    continue;
                                }

                                // new client
                                if(key.isAcceptable()) {
                                    accept(key, selector);
                                }

                                // receive client message
                                if(key.isReadable()) {
                                    SocketChannel channel = (SocketChannel) key.channel();
                                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);

                                    // 从channel读取字节到buffer中
                                    int readBytes = channel.read(readBuffer);
                                    System.out.println("read bytes: " + readBytes);
                                    if(readBytes > 0) {
                                        readBuffer.flip();
                                        // ???
                                        byte[] bytes = new byte[readBuffer.remaining()];
                                        readBuffer.get(bytes);
                                        String s = new String(bytes, "UTF-8");
                                        System.out.println("read: " + s);

                                    }
                                    // 客户端主动断开连接
                                    else if (readBytes < 0) {
                                        key.cancel();
                                        serverChannel.close();
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                if(null != key) {
                                    key.cancel();
                                    SelectableChannel channel = key.channel();
                                    if(null != channel) {
                                        System.out.println("客户端关闭了连接: " + channel);
                                        key.channel().close();
                                        // 删除客户端
                                        clientList.remove(channel);
                                    }
                                }
                            }

                        }
                    }
                    // close all
                    selector.close();
                    serverChannel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public List<BrushClient> clients() {
        return null;
    }

    @Override
    public BrushClient findClient(String shortId) {
        return null;
    }

    /**
     * stop the server
     */
    public void stop() throws InterruptedException {
        stop = true;
        serverPool.shutdown();
        serverPool.awaitTermination(config.getShutdownTimeout(), TimeUnit.MILLISECONDS);
    }

    public Queue<SocketChannel> getClients() {
        return clientList;
    }

//    public void broadcast(byte[] bytes) {
//        clientList.forEach(socketChannel -> {
//
//        });
//    }

    /**
     * Accept new client and register selector
     * @param key
     * @param selector
     */
    private void accept(SelectionKey key, Selector selector) {
        try {
            System.out.println("key: " + key.toString());

            SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
            if(socketChannel == null) {
                return;
            }
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);

            clientList.add(socketChannel);

            System.out.println("New client: " + socketChannel.getRemoteAddress());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {

    }

}
