package com.example.mandelamoney;

public class DataShare {
    private static Object obj;
    public static void send(Object obj) {
        DataShare.obj = obj;
    }

    public static Object receive() {
        Object temp = obj;
        obj = null;
        return temp;
    }

    public static void clear() {
        obj = null;
    }

}
