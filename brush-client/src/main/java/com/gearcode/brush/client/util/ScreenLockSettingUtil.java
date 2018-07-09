package com.gearcode.brush.client.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.*;

/**
 * Created by liteng3 on 2018/7/9.
 */
public class ScreenLockSettingUtil {
    private static final Logger logger = LoggerFactory.getLogger(ScreenLockSettingUtil.class);

    private static int PRESS_SLEEP_MILL = 60000;

    private static final ThreadFactory dslThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("disablescreenlock-pool-%d").build();

    private static final ExecutorService executor = new ThreadPoolExecutor(1, 1,0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), dslThreadFactory);

    static class DisableThread extends Thread {
        private static volatile boolean running = false;
        public void enable() {
            running = true;
        }
        public void disable() {
            running = false;
        }
        @Override
        public void run() {
            /*
            每分钟按一次F24, 以此阻止系统自动锁屏
             */
            try {
                Robot robot = new Robot();
                while (true) {
                    if(running) {
                        robot.keyPress(KeyEvent.VK_F24);
                        Thread.sleep(100);
                        robot.keyRelease(KeyEvent.VK_F24);
                        logger.info("Press F24");

                    }
                    Thread.sleep(PRESS_SLEEP_MILL);
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private static final DisableThread task = new DisableThread();
    static {
        executor.submit(task);
    }

    public static void disable() {
        task.enable();
    }

    public static void enable() {
        task.disable();
    }

}
