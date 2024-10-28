package com.iot.BE.utils;

import java.util.ArrayList;
import java.util.List;

public class Constant {

    // shared class
    public static final String BROKER = "tcp://192.168.0.111:1885";
    public static final String CLIENT_ID = "java_client";
    public static  List<Integer> sharedList = new ArrayList<>();

    public final static String LED_CONTROL = "LED_CONTROL";
    public final static String FAN_CONTROL = "FAN_CONTROL";
    public final static String AC_CONTROL = "AC_CONTROL";

    public final static String DATA_SENSOR = "SENSOR/DATA";
    public final static String LED_RESPONSE = "LED_RESPONSE";
    public final static String FAN_RESPONSE = "FAN_RESPONSE";
    public final static String AC_RESPONSE = "AC_RESPONSE";
    public final static String ASC = "ASC";
    public final static String DESC = "DESC";
}
