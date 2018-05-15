package com.gearcode.brush.client.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * Created by liteng3 on 2018/5/9.
 */
public class PrintAllVars {
    public static void main(String[] args) throws UnknownHostException {

        Properties properties = System.getProperties();
        Enumeration<?> names = properties.propertyNames();
        while (names.hasMoreElements()) {
            String e = (String) names.nextElement();
            System.out.println(e + " = " + properties.getProperty(e));
        }

        System.out.println("============================================");

        Map<String, String> map = System.getenv();
        map.entrySet().forEach(e -> {
            System.out.println(e.getKey() + " = " + e.getValue());
        });

        System.out.println("============================================");

        String username = System.getProperty("user.name");

        System.out.println("username = " + username);
        System.out.println("Hostname of local machine: " + InetAddress.getLocalHost().getHostName());
    }
}
