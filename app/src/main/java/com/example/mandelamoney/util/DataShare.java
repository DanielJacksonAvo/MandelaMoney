package com.example.mandelamoney.util;

public class DataShare {
    private static Object obj;
    public static void send(Object obj) {
        DataShare.obj = obj;
    }

    public static Object receive() {
        Object temp = obj;
        clear();
        return temp;
    }

    public static void clear() {
        obj = null;
    }

}
