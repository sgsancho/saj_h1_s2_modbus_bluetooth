package com.sajh1s2.espblufi.saj;

import java.util.UUID;

/* loaded from: classes3.dex */
public interface BlufiParameter {
    public static final int DIRECTION_INPUT = 1;
    public static final int DIRECTION_OUTPUT = 0;
    public static final byte NEG_SET_SEC_ALL_DATA = 1;
    public static final byte NEG_SET_SEC_TOTAL_LEN = 0;
    public static final int OP_MODE_NULL = 0;
    public static final int OP_MODE_SOFTAP = 2;
    public static final int OP_MODE_STA = 1;
    public static final int OP_MODE_STASOFTAP = 3;
    public static final int SOFTAP_SECURITY_OPEN = 0;
    public static final int SOFTAP_SECURITY_WEP = 1;
    public static final int SOFTAP_SECURITY_WPA = 2;
    public static final int SOFTAP_SECURITY_WPA2 = 3;
    public static final int SOFTAP_SECURITY_WPA_WPA2 = 4;
    public static final UUID UUID_SERVICE = UUID.fromString("0000ffff-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_WRITE_CHARACTERISTIC = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_NOTIFICATION_CHARACTERISTIC = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");

    /* loaded from: classes3.dex */
    public static final class Type {

        /* loaded from: classes3.dex */
        public static final class Ctrl {
            public static final int PACKAGE_VALUE = 0;
            public static final int SUBTYPE_ACK = 0;
            public static final int SUBTYPE_CLOSE_CONNECTION = 8;
            public static final int SUBTYPE_CONNECT_WIFI = 3;
            public static final int SUBTYPE_DEAUTHENTICATE = 6;
            public static final int SUBTYPE_DISCONNECT_WIFI = 4;
            public static final int SUBTYPE_GET_VERSION = 7;
            public static final int SUBTYPE_GET_WIFI_LIST = 9;
            public static final int SUBTYPE_GET_WIFI_STATUS = 5;
            public static final int SUBTYPE_SET_OP_MODE = 2;
            public static final int SUBTYPE_SET_SEC_MODE = 1;
        }

        /* loaded from: classes3.dex */
        public static final class Data {
            public static final int PACKAGE_VALUE = 1;
            public static final int SUBTYPE_CA_CERTIFICATION = 10;
            public static final int SUBTYPE_CLIENT_CERTIFICATION = 11;
            public static final int SUBTYPE_CLIENT_PRIVATE_KEY = 13;
            public static final int SUBTYPE_CUSTOM_DATA = 19;
            public static final int SUBTYPE_ERROR = 18;
            public static final int SUBTYPE_NEG = 0;
            public static final int SUBTYPE_SERVER_CERTIFICATION = 12;
            public static final int SUBTYPE_SERVER_PRIVATE_KEY = 14;
            public static final int SUBTYPE_SOFTAP_AUTH_MODE = 7;
            public static final int SUBTYPE_SOFTAP_CHANNEL = 8;
            public static final int SUBTYPE_SOFTAP_MAX_CONNECTION_COUNT = 6;
            public static final int SUBTYPE_SOFTAP_WIFI_PASSWORD = 5;
            public static final int SUBTYPE_SOFTAP_WIFI_SSID = 4;
            public static final int SUBTYPE_STA_WIFI_BSSID = 1;
            public static final int SUBTYPE_STA_WIFI_PASSWORD = 3;
            public static final int SUBTYPE_STA_WIFI_SSID = 2;
            public static final int SUBTYPE_UPGRADE = 20;
            public static final int SUBTYPE_USERNAME = 9;
            public static final int SUBTYPE_VERSION = 16;
            public static final int SUBTYPE_WIFI_CONNECTION_STATE = 15;
            public static final int SUBTYPE_WIFI_LIST = 17;
        }
    }
}
