package com.gearcode.brush.client.test;

import com.gearcode.brush.client.util.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Created by jason on 2018/4/26.
 */
public class TestRunWinsta0 {

    public static void main(String[] args) throws AWTException, IOException, InterruptedException {

        while(true) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            BufferedImage bufferedImage = Utils.captureScreen(0, 0, screenSize.width, screenSize.height);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", output);
            // 写入文件
            Path path = Paths.get("C:\\brush\\" + System.currentTimeMillis() + ".jpg");
            Files.write(path, output.toByteArray(), APPEND, CREATE);

            Thread.sleep(2000);
        }
    }

    private static void log(String s) throws IOException {
        Path path = Paths.get("C:\\brush\\log.txt");
        Files.write(path, s.getBytes(), APPEND, CREATE);
    }
}
