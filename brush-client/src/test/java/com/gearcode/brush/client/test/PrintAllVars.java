package com.gearcode.brush.client.test;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by liteng3 on 2018/5/9.
 */
public class PrintAllVars {
    public static void main(String[] args) {

        Properties properties = System.getProperties();
        Enumeration<?> names = properties.propertyNames();
        while (names.hasMoreElements()) {
            String e = (String) names.nextElement();
            System.out.println(e + " = " + properties.getProperty(e));
        }
    }
}
