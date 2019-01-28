package com.gearcode.brush.client.test;

import java.awt.*;

/**
 * 环境：JDK8，windows10，ThinkPad-L480笔记本电脑
 * 问题：java的Robot.mouseMove方法，移动的鼠标指针并不准确，多次移动至某一个相同坐标后，才正确
 */
public class TestRobotMoveMouse {
    public static void main(String[] args) throws AWTException, InterruptedException {
        Dimension clientSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println(clientSize.getWidth() + ", " + clientSize.getHeight());

        Point point = new Point(1913, 1071);

        Robot robot = new Robot();
        for (int i = 0; i < 100; i++) {
            robot.mouseMove(point.x, point.y);
            System.out.println(i + "\t" + MouseInfo.getPointerInfo().getLocation().getX() + ", " + MouseInfo.getPointerInfo().getLocation().getY());

            if(point.equals(MouseInfo.getPointerInfo().getLocation())) {
                System.out.println("校准次数: " + (i+1));
                break;
            }

            Thread.sleep(1);
        }

        Point point1 = new Point(111, 321);
        for (int i = 0; i < 100; i++) {
            robot.mouseMove(point1.x, point1.y);
            System.out.println(i + "\t" + MouseInfo.getPointerInfo().getLocation().getX() + ", " + MouseInfo.getPointerInfo().getLocation().getY());

            if(point1.equals(MouseInfo.getPointerInfo().getLocation())) {
                System.out.println("校准次数: " + (i+1));
                break;
            }

            Thread.sleep(1);
        }
    }
}
