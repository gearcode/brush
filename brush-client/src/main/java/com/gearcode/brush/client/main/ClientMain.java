package com.gearcode.brush.client.main;

import com.gearcode.brush.client.handler.ClientChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;


/**
 * Created by jason on 2018/3/16.
 */
public class ClientMain {
    private static final Logger logger = LoggerFactory.getLogger(ClientMain.class);

    static final String IP = "www.gearcode.com";
    static final int PORT = 10230;

    static final long RECONNECT_DELAY = 3000L;


    public static void main(String[] args) throws InterruptedException, IOException {

        final TrayIcon trayIcon;

        if (SystemTray.isSupported()) {

            SystemTray tray = SystemTray.getSystemTray();

            URL icon = ClientMain.class.getClassLoader().getResource("images/gear.gif");
            Image image = ImageIO.read(icon);
            System.out.println(image);
//
//            MouseListener mouseListener = new MouseListener() {
//                @Override
//                public void mouseClicked(MouseEvent e) {
//                    System.out.println("Tray Icon - Mouse clicked!");
//                }
//                @Override
//                public void mouseEntered(MouseEvent e) {
//                    System.out.println("Tray Icon - Mouse entered!");
//                }
//                @Override
//                public void mouseExited(MouseEvent e) {
//                    System.out.println("Tray Icon - Mouse exited!");
//                }
//                @Override
//                public void mousePressed(MouseEvent e) {
//                    System.out.println("Tray Icon - Mouse pressed!");
//                }
//                @Override
//                public void mouseReleased(MouseEvent e) {
//                    System.out.println("Tray Icon - Mouse released!");
//                }
//            };

            ActionListener exitListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
            };

            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Exit");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);

            trayIcon = new TrayIcon(image, "Tray Demo", popup);
            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    trayIcon.displayMessage("Action Event",
                            "An Action Event Has Been Performed!",
                            TrayIcon.MessageType.INFO);
                }
            };

            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
//            trayIcon.addMouseListener(mouseListener);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

        } else {

            //  System Tray is not supported

        }


        while(true) {
            logger.info("Start connect to server...");
            connectToServer();
            logger.info("Connection closed, reconnect 3 seconds later...");
            Thread.sleep(RECONNECT_DELAY);
        }
    }

    private static void connectToServer() {

        // netty nio线程池hhh
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(IP, PORT)
                    .handler(new ClientChannelInitializer());
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
