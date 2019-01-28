package com.gearcode.brush.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by jason on 2018/4/21.
 */
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static final int ADJUST_TIMES = 20;

    public static void mouseMove(int x, int y) throws AWTException {
        Robot robot = new Robot();
        for (int i = 0; i < ADJUST_TIMES; i++) {
            robot.mouseMove(x, y);
            if(MouseInfo.getPointerInfo().getLocation().getX() == x && MouseInfo.getPointerInfo().getLocation().getY() == y) {
                break;
            }
        }

    }

    public static void mouseClick(int event, int x, int y) throws AWTException {
        mouseMove(x, y);
        Robot robot = new Robot();
        robot.mousePress(event);
        robot.mouseRelease(event);
    }

    public static void keyPress(int code) throws AWTException {
        Robot robot = new Robot();
        boolean support = (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_Z)
                || code == KeyEvent.VK_SPACE
                || code == KeyEvent.VK_ENTER
                || code == KeyEvent.VK_TAB
                || code == KeyEvent.VK_BACK_SPACE;
        if(support) {
            robot.keyPress(code);
            robot.keyRelease(code);
        }

        // NUM_LOCK
        if(code == KeyEvent.VK_NUM_LOCK) {
            robot.keyPress(code);
            robot.keyRelease(code);
        }

    }

    public static BufferedImage captureScreen(int x, int y, int width, int height) throws AWTException {
        Rectangle rect = new Rectangle(x, y, width, height);
        return new Robot().createScreenCapture(rect);
    }

    public static void blockingNumLock() throws Exception {
        while(true) {
            keyPress(KeyEvent.VK_NUM_LOCK);
            keyPress(KeyEvent.VK_NUM_LOCK);
            logger.info("NUM_LOCK");
            Thread.sleep(10000);
        }
    }

    public static void main(String[] args) {

        try {
            mouseClick(InputEvent.BUTTON3_MASK, 200, 200);
        } catch (AWTException e) {
            e.printStackTrace();
        }

    }
}
