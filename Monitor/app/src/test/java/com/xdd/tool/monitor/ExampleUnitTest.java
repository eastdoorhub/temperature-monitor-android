package com.xdd.tool.monitor;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_robustEqual() throws Exception {
        String curTmp = "23.00 ";
        String preTmp = "22.00 ";
        if (TempUtil.robustEqual(curTmp, preTmp)) {
            System.out.println("It's equal.");
        } else {
            System.out.println("It's not equal.");
        }
        long preTime = new java.util.Date().getTime();

        String[] intTemp = curTmp.split("\\.");
        System.out.println("Int value:" + intTemp[1]);
        assertEquals(1, 1);
    }
    @Test
    public void test_getDeviceName() throws Exception {
        String name = "DH-05";
        String address = "20:15:12:28:78:16";
        assertEquals(TempUtil.getDeviceNameDescription(name, address), "设备名：DH-05-78-16");
    }

    @Test
    public void test_Boolean_boolean() throws Exception {
        Boolean test = null;
        boolean bTest = true;
        test = bTest;
        if (test == null) {
            System.out.println("Can't assign boolean to Boolean.");
        }else{
            System.out.println("Can assign boolean to Boolean.");
        }

        if (test) {
            System.out.println("Can assign true to True.");
        }else{
            System.out.println("Can't assign boolean to Boolean.");
        }
    }
}