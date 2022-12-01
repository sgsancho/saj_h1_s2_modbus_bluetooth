package com.sajh1s2.espblufi.saj;

public class CRC16Utils {
    public static String CRC16_Check(byte[] bArr, int i) {
        int i2 = 65535;
        for (int i3 = 0; i3 < i; i3++) {
            int i4 = bArr[i3];
            if (i4 < 0) {
                i4 += 256;
            }
            i2 ^= i4 & 255;
            for (int i5 = 0; i5 < 8; i5++) {
                i2 = (i2 & 1) == 1 ? (i2 >> 1) ^ 40961 : i2 >> 1;
            }
        }
        String hexString = Integer.toHexString(i2 & 65535);
        if (hexString.length() == 3) {
            hexString = "0" + hexString;
        } else if (hexString.length() == 2) {
            hexString = "00" + hexString;
        } else if (hexString.length() == 1) {
            hexString = "000" + hexString;
        }
        String substring = hexString.substring(0, 2);
        return hexString.substring(2, 4) + substring;
    }
}
