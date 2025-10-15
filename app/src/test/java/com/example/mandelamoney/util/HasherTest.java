package com.example.mandelamoney.util;

import junit.framework.TestCase;

public class HasherTest extends TestCase {

    public void testGetHash() {
        String password = "CharliePassword";
        String hash = Hasher.getHash(password);
        System.out.println("Input String: " + password);
        System.out.println("Actual Hash: " + hash);
    }
}