package com.gearcode.brush.client.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by liteng3 on 2018/5/17.
 */
public class TestSingleInstance {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        InetAddress localHostAddress = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});

        System.out.println(localHost);
        System.out.println(localHostAddress);
    }
}
