package com.sajh1s2.espblufi.saj;

import org.apache.http.util.ByteArrayBuffer;

/* loaded from: classes3.dex */
public class BluFiUtils {
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String parseNormalReceiveCustomData(String str, String str2) {
        return str2.replace(str, "").replace("OK", "").replace("\n", "").replaceAll("\\s*", "").trim();
    }

    public static String parseNormalJsonData(String str, String str2) {
        return str2.replace(str, "").replace("OK", "").replaceAll("\r|\n", "").trim();
    }

    public static String bytesToHex(byte[] bArr) {
        char[] cArr = new char[bArr.length * 2];
        int i = 0;
        for (byte b : bArr) {
            int i2 = i + 1;
            char[] cArr2 = HEX_CHAR;
            cArr[i] = cArr2[(b >>> 4) & 15];
            i = i2 + 1;
            cArr[i2] = cArr2[b & 15];
        }
        return new String(cArr);
    }

    public static String checkTwo(int i) {
        try {
            String tenTo16Normal = Integer.toHexString(i);
            if (tenTo16Normal.length() == 1) {
                return "0" + tenTo16Normal;
            }
            return tenTo16Normal;
        } catch (Exception e) {
            AppLog.e(e.toString());
            return "01";
        }
    }

    public static String exchangeHasCrcModBusData(int i, String str) {
        try {
            String changeData = changeData(checkTwo(i) + str.substring(2, str.length() - 4));
            return BlufiConstants.MIDBUS_START + changeData;
        } catch (Exception e) {
            AppLog.e(e.toString());
            return null;
        }
    }

    public static String exchangeNoCrcModBusData(int i, String str) {
        try {
            String changeData = changeData(checkTwo(i) + str.substring(2));
            return BlufiConstants.MIDBUS_START + changeData;
        } catch (Exception e) {
            AppLog.e(e.toString());
            return null;
        }
    }

    private static String changeData(String str) {
        if (str.length() % 2 != 0) {
            AppLog.e("data:" + str + ",send data error");
            return null;
        }
        ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(str.length() / 2);
        int i = 0;
        while (i < str.length()) {
            if (str.charAt(i) != ' ') {
                int i2 = i + 2;
                byteArrayBuffer.append(hexStrToByteArray(str.substring(i, i2)));
                i = i2;
            } else {
                i++;
            }
        }
        return str + CRC16Utils.CRC16_Check(byteArrayBuffer.toByteArray(), byteArrayBuffer.length()).toUpperCase();
    }

    private static byte hexStrToByteArray(String str) {
        byte[] bytes = str.getBytes();
        return (byte) (Byte.decode("0x" + new String(new byte[]{bytes[1]})).byteValue() | ((byte) (Byte.decode("0x" + new String(new byte[]{bytes[0]})).byteValue() << 4)));
    }
}
