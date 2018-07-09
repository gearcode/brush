package com.gearcode.brush.client.test;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by liteng3 on 2018/7/4.
 */
public class TestBlockScreenSaver {
    public static void main(String[] args) throws AWTException, InterruptedException {

        Robot robot = new Robot();

        while(true) {
            System.out.println("press");
            robot.keyPress(KeyEvent.VK_F24);
            Thread.sleep(1000);
            robot.keyRelease(KeyEvent.VK_F24);
            System.out.println("release");
            Thread.sleep(60000);
        }

    }
}
