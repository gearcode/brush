package com.gearcode.brush.client.main;

import com.gearcode.brush.client.handler.ClientChannelInitializer;
import com.gearcode.brush.client.util.ClientConfigUtil;
import com.gearcode.brush.client.util.Constants;
import com.gearcode.brush.client.util.PrefConfig;
import com.gearcode.brush.client.util.ScreenLockSettingUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
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

    static Channel channel;

    static volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException, IOException {

        // 初始化托盘图标
        initTray();

        // 设置密码
        String password = PrefConfig.retrieve(Constants.PREF_KEY_PASSWORD);
        if(password == null) {
            showPasswordDialog();
        }

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook");
            running = false;
            channel.eventLoop().shutdownGracefully();
        }));

        while(true) {
            logger.info("Start connect to server...");
            connectToServer();
            if(running) {
                logger.info("Connection closed, reconnect 3 seconds later...");
                Thread.sleep(RECONNECT_DELAY);
            } else {
                break;
            }
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
            channel = f.channel();
            channel.closeFuture().sync();

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            logger.info("Shutdown netty gracefully");
            group.shutdownGracefully();
        }
    }

    private static void initTray() throws IOException {

        if (SystemTray.isSupported()) {
            final TrayIcon trayIcon;
            SystemTray tray = SystemTray.getSystemTray();
            URL icon = ClientMain.class.getClassLoader().getResource("images/gear.gif");
            Image image = ImageIO.read(icon);

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


            MenuItem setPassMenuItem = new MenuItem("Set password");
            setPassMenuItem.addActionListener(e -> {
                logger.info("Set password action");
                showPasswordDialog();
            });

            CheckboxMenuItem disableScreenLockMenuItem = new CheckboxMenuItem("Disable ScreenLock");

            disableScreenLockMenuItem.addItemListener(e -> {
                logger.info("Disable ScreenLock, {}, {}", e.getStateChange(), e.getStateChange());
                switch (e.getStateChange()) {
                    case ItemEvent.SELECTED:
                        ScreenLockSettingUtil.disable();
                        break;
                    case ItemEvent.DESELECTED:
                        ScreenLockSettingUtil.enable();
                        break;
                    default:
                        break;
                }

            });

            MenuItem exitMenuItem = new MenuItem("Exit");
            ActionListener exitListener = e -> {
                logger.info("Tray exiting...");
                System.exit(0);
            };
            exitMenuItem.addActionListener(exitListener);

            PopupMenu popup = new PopupMenu();
            popup.add(setPassMenuItem);
            popup.add(disableScreenLockMenuItem);
            popup.add(exitMenuItem);

            trayIcon = new TrayIcon(image, "BrushClient", popup);
            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    trayIcon.displayMessage("BrushClient",
                            "WeChat: crazyjason",
                            TrayIcon.MessageType.INFO);
                }
            };

            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
//            trayIcon.addMouseListener(mouseListener);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                logger.error("TrayIcon could not be added.");
            }

            /*
            显示Message
             */
            trayIcon.displayMessage("BrushClient",
                    "WeChat: crazyjason",
                    TrayIcon.MessageType.INFO);

        } else {
            //  System Tray is not supported
        }

    }

    private static void showPasswordDialog() {
        JFrame frame = new JFrame("Set password");
        frame.setLayout(new FlowLayout());

        Container container = frame.getContentPane();

        container.add(new JLabel("Password:"));

        JPasswordField passwordField = new JPasswordField(16);
        passwordField.addActionListener(e -> {
            confirmPassword(passwordField, frame);
        });
        container.add(passwordField);

        ActionListener confirmPassword = e -> {
            confirmPassword(passwordField, frame);
        };

        JButton confirm = new JButton("Confirm");
        confirm.addActionListener(confirmPassword);
        container.add(confirm);

        frame.setVisible(true);
        frame.pack();

        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private static void confirmPassword(JPasswordField passwordField, JFrame frame) {
        char[] passwordCharArr = passwordField.getPassword();
        String password = new String(passwordCharArr);
        logger.info("Confirm password: {}", password);

        PrefConfig.save(Constants.PREF_KEY_PASSWORD, password);
        frame.dispose();

        channel.pipeline().fireUserEventTriggered(ClientConfigUtil.getCurrentClientConfig());
    }

}
