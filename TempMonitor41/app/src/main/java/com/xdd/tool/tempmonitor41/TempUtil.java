package com.xdd.tool.tempmonitor41;

import java.util.Date;
import java.util.TreeMap;

/**
 * Created by exiawag on 2/27/2016.
 */
final public class TempUtil {

    private TempUtil() {
    }

    public static boolean robustEqual(String curTmp, String preTmp) {
        double d1 = Double.parseDouble(curTmp);
        double d2 = Double.parseDouble(preTmp);
        if (Math.abs(d1 - d2) > 2) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean robustIntEqual(String curTmp, String preTmp) {
        int d1 = Integer.parseInt(curTmp);
        int d2 = Integer.parseInt(preTmp);
        if (Math.abs(d1 - d2) > 2) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean moreThanOneHourDelta(Date oldDate, Date newDate){
        if (oldDate != null && newDate != null
                && (newDate.getTime() - oldDate.getTime()) > 3600 * 1000){
            return true;
        }else if(oldDate == null && newDate !=null){
            return true;
        }else{
            return false;
        }
    }


    public static String getDeviceNameDescription(String name, String address){
        String[] strings = address.split(":");
        String  deviceDescription = "设备:" + name
                + "-" + strings[strings.length - 2] + "-" + strings[strings.length - 1];
        return deviceDescription;
    }

    public static String getDeviceName(String name, String address){
        String[] strings = address.split(":");
        String  deviceName = name
                + "-" + strings[strings.length - 2] + "-" + strings[strings.length - 1];
        return deviceName;
    }

}
