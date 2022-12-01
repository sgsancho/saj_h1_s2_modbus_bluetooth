package com.sajh1s2.espblufi.saj;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import androidx.exifinterface.media.ExifInterface;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.http.util.ByteArrayBuffer;

/* loaded from: classes3.dex */
public class LocalUtils {
    public static final byte MAX_VALUE = Byte.MAX_VALUE;
    public static final byte MIN_VALUE = Byte.MIN_VALUE;
    public static final int SIZE_BITS = 8;
    public static final int SIZE_BYTES = 1;

    public static String[] RS485_DATA_ATE = {"115200", "57600", "38400", "19200", "9600", "4800", "2400", "1200"};
    //public static String[] slaveNum = {"1", "2", "3", "4", AmapLoc.RESULT_TYPE_SELF_LAT_LON, AmapLoc.RESULT_TYPE_NO_LONGER_USED, "7", "8", "9", AgooConstants.ACK_REMOVE_PACKAGE, AgooConstants.ACK_BODY_NULL, AgooConstants.ACK_PACK_NULL, AgooConstants.ACK_FLAG_NULL, "14", AgooConstants.ACK_PACK_ERROR, "16", "17", "18", "19", "20", AgooConstants.REPORT_MESSAGE_NULL, AgooConstants.REPORT_ENCRYPT_FAIL, AgooConstants.REPORT_DUPLICATE_FAIL, "24", "25", "26", "27", "28", "29", "30", "31", BlufiConstants.MIDBUS_START};
    public static String[] RS485_DATA_ATE_AC_STORE = {"9600", "4800", "2400", "1200"};
    public static String[] pFData = {"0.8", "0.81", "0.82", "0.83", "0.84", "0.85", "0.86", "0.87", "0.88", "0.89", "0.9", "0.91", "0.92", "0.93", "0.94", "0.95", "0.96", "0.97", "0.98", "0.99", "1", "-0.99", "-0.98", "-0.97", "-0.96", "-0.95", "-0.94", "-0.93", "-0.92", "-0.91", "-0.9", "-0.89", "-0.88", "-0.87", "-0.86", "-0.85", "-0.84", "-0.83", "-0.82", "-0.81", "-0.8"};
    public static String[] acPFData = {"0.8", "0.81", "0.82", "0.83", "0.84", "0.85", "0.86", "0.87", "0.88", "0.89", "0.9", "0.91", "0.92", "0.93", "0.94", "0.95", "0.96", "0.97", "0.98", "0.99", "1"};

    public static byte hexChar2Byte(char c) {
        int i;
        if (c < '0' || c > '9') {
            char c2 = 'a';
            if (c < 'a' || c > 'f') {
                c2 = 'A';
                if (c < 'A' || c > 'F') {
                    return (byte) -1;
                }
            }
            i = (c - c2) + 10;
        } else {
            i = c - '0';
        }
        return (byte) i;
    }

    public static String setChargeNum(int i) {
        switch (i) {
            case 0:
                return "00000000";
            case 1:
                return "00000001";
            case 2:
                return "00000011";
            case 3:
                return "00000111";
            case 4:
                return "00001111";
            case 5:
                return "00011111";
            case 6:
                return "00111111";
            case 7:
                return "01111111";
            default:
                return "11111111";
        }
    }

    public static byte[] sendData(String str) {
        int i = 0;
        if (str.trim().length() % 2 != 0) {
            AppLog.e("data:" + str + ",send data error");
            return new byte[0];
        }
        ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(str.length() / 2);
        int i2 = 0;
        while (i2 < str.length()) {
            if (str.charAt(i2) != ' ') {
                int i3 = i2 + 2;
                byteArrayBuffer.append(hexStrToByteArray(str.substring(i2, i3)));
                i2 = i3;
            } else {
                i2++;
            }
        }
        String str2 = str + CRC16Utils.CRC16_Check(byteArrayBuffer.toByteArray(), byteArrayBuffer.length()).toUpperCase();
        byteArrayBuffer.clear();
        while (i < str2.length()) {
            if (str2.charAt(i) != ' ') {
                int i4 = i + 2;
                byteArrayBuffer.append(hexStrToByteArray(str2.substring(i, i4)));
                i = i4;
            } else {
                i++;
            }
        }
        return byteArrayBuffer.toByteArray();
    }

    private static byte hexStrToByteArray(String str) {
        byte[] bytes = str.getBytes();
        return (byte) (Byte.decode("0x" + new String(new byte[]{bytes[1]})).byteValue() | ((byte) (Byte.decode("0x" + new String(new byte[]{bytes[0]})).byteValue() << 4)));
    }

    public static String unit16TO10_int(String str) {
        try {
            return "FFFF".equals(str.toUpperCase()) ? "N/A" : String.valueOf(Integer.parseInt(str, 16));
        } catch (Exception e) {
            AppLog.e(e.toString());
            return "N/A";
        }
    }

    public static int unit16TO10_int0(String str) {
        try {
            if ("FFFF".equals(str.toUpperCase())) {
                return 0;
            }
            return Integer.parseInt(str, 16);
        } catch (Exception e) {
            AppLog.e(e.toString());
            return 0;
        }
    }

    public static String unit16TO10_long(String str) {
        try {
            return "FFFFFFFF".equals(str.toUpperCase()) ? "N/A" : String.valueOf(Long.parseLong(str, 16));
        } catch (Exception e) {
            AppLog.e(e.toString());
            return "N/A";
        }
    }

    public static String unit16TO10Add0(String str) {
        try {
            String unit16TO10_int = unit16TO10_int(str);
            if (unit16TO10_int.length() == 1) {
                return "0" + unit16TO10_int;
            }
            return unit16TO10_int;
        } catch (Exception e) {
            AppLog.e(e.toString());
            return "N/A";
        }
    }

    public static String tenTo16Add0AddRatio(String str, int i) {
        try {
            if ("N/A".equals(str)) {
                return "ffff";
            }
            if (i == 1) {
                str = String.valueOf((int) (Double.parseDouble(str) * 10.0d));
            } else if (i == 2) {
                str = String.valueOf((int) (Double.parseDouble(str) * 100.0d));
            } else if (i == 3) {
                str = String.valueOf((int) (Double.parseDouble(str) * 1000.0d));
            }
            if (i == 20) {
                str = String.valueOf((int) (Double.parseDouble(str) / 20.0d));
            }
            String hexString = Integer.toHexString((int) Double.parseDouble(str));
            int length = hexString.length();
            if (length == 1) {
                return "000" + hexString;
            } else if (length == 2) {
                return "00" + hexString;
            } else if (length != 3) {
                return hexString;
            } else {
                return "0" + hexString;
            }
        } catch (Exception e) {
            AppLog.e("tenTo16Add0AddRatio:" + e.toString());
            return "";
        }
    }

    public static String tenTo16AddFourSize(String str) {
        String hexString = Integer.toHexString(Integer.parseInt(str));
        int length = hexString.length();
        if (length == 1) {
            return "000" + hexString;
        } else if (length == 2) {
            return "00" + hexString;
        } else if (length != 3) {
            return hexString;
        } else {
            return "0" + hexString;
        }
    }

    public static String sTo2Size(String str) {
        if (str.length() == 1) {
            return "0" + str;
        }
        return str;
    }

    public static String tenTo16Two(String str) {
        try {
            String hexString = Integer.toHexString(Integer.parseInt(str));
            if (hexString.length() == 1) {
                return "0" + hexString;
            }
            return hexString;
        } catch (Exception e) {
            AppLog.e(e.toString());
            return "00";
        }
    }

    public static String convertStringToHex(String str) {
        char[] charArray = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : charArray) {
            sb.append(Integer.toHexString(c));
        }
        return sb.toString();
    }

    public static String convertHexToString(String str) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < str.length() - 1) {
            int i2 = i + 2;
            sb.append((char) Integer.parseInt(str.substring(i, i2), 16));
            i = i2;
        }
        return sb.toString();
    }

    /* JADX WARN: Code restructure failed: missing block: B:30:0x0053, code lost:
        if (r1 == 1) goto L28;
     */
    /* JADX WARN: Code restructure failed: missing block: B:31:0x0055, code lost:
        if (r1 == 2) goto L26;
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x0057, code lost:
        if (r1 == 3) goto L24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x0059, code lost:
        if (r1 == 4) goto L22;
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:?, code lost:
        return "N/A";
     */
    /* JADX WARN: Code restructure failed: missing block: B:46:?, code lost:
        return r8.getResources().getString(com.saj.connection.R.string.local_pv1_pv2_pv3_parallel);
     */
    /* JADX WARN: Code restructure failed: missing block: B:47:?, code lost:
        return r8.getResources().getString(com.saj.connection.R.string.local_pv2_and_pv3_parallel);
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:?, code lost:
        return r8.getResources().getString(com.saj.connection.R.string.local_pv1_and_pv2_parallel);
     */
    /* JADX WARN: Code restructure failed: missing block: B:49:?, code lost:
        return r8.getResources().getString(com.saj.connection.R.string.local_pv1_and_pv3_parallel);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static java.lang.String matchPVInputMode(android.content.Context r8, java.lang.String r9) {
        /*
            java.lang.String r0 = "N/A"
            r1 = -1
            int r2 = r9.hashCode()     // Catch: java.lang.Exception -> L93
            r3 = 48
            r4 = 4
            r5 = 3
            r6 = 2
            r7 = 1
            if (r2 == r3) goto L48
            r3 = 52
            if (r2 == r3) goto L3e
            r3 = 53
            if (r2 == r3) goto L34
            r3 = 55
            if (r2 == r3) goto L2a
            r3 = 56
            if (r2 == r3) goto L20
            goto L51
        L20:
            java.lang.String r2 = "8"
            boolean r9 = r9.equals(r2)     // Catch: java.lang.Exception -> L93
            if (r9 == 0) goto L51
            r1 = 4
            goto L51
        L2a:
            java.lang.String r2 = "7"
            boolean r9 = r9.equals(r2)     // Catch: java.lang.Exception -> L93
            if (r9 == 0) goto L51
            r1 = 3
            goto L51
        L34:
            java.lang.String r2 = "5"
            boolean r9 = r9.equals(r2)     // Catch: java.lang.Exception -> L93
            if (r9 == 0) goto L51
            r1 = 2
            goto L51
        L3e:
            java.lang.String r2 = "4"
            boolean r9 = r9.equals(r2)     // Catch: java.lang.Exception -> L93
            if (r9 == 0) goto L51
            r1 = 1
            goto L51
        L48:
            java.lang.String r2 = "0"
            boolean r9 = r9.equals(r2)     // Catch: java.lang.Exception -> L93
            if (r9 == 0) goto L51
            r1 = 0
        L51:
            if (r1 == 0) goto L88
            if (r1 == r7) goto L7d
            if (r1 == r6) goto L72
            if (r1 == r5) goto L67
            if (r1 == r4) goto L5c
            goto L92
        L5c:
            android.content.res.Resources r8 = r8.getResources()     // Catch: java.lang.Exception -> L93
            int r9 = com.saj.connection.R.string.local_pv1_pv2_pv3_parallel     // Catch: java.lang.Exception -> L93
            java.lang.String r0 = r8.getString(r9)     // Catch: java.lang.Exception -> L93
            goto L92
        L67:
            android.content.res.Resources r8 = r8.getResources()     // Catch: java.lang.Exception -> L93
            int r9 = com.saj.connection.R.string.local_pv2_and_pv3_parallel     // Catch: java.lang.Exception -> L93
            java.lang.String r0 = r8.getString(r9)     // Catch: java.lang.Exception -> L93
            goto L92
        L72:
            android.content.res.Resources r8 = r8.getResources()     // Catch: java.lang.Exception -> L93
            int r9 = com.saj.connection.R.string.local_pv1_and_pv2_parallel     // Catch: java.lang.Exception -> L93
            java.lang.String r0 = r8.getString(r9)     // Catch: java.lang.Exception -> L93
            goto L92
        L7d:
            android.content.res.Resources r8 = r8.getResources()     // Catch: java.lang.Exception -> L93
            int r9 = com.saj.connection.R.string.local_pv1_and_pv3_parallel     // Catch: java.lang.Exception -> L93
            java.lang.String r0 = r8.getString(r9)     // Catch: java.lang.Exception -> L93
            goto L92
        L88:
            android.content.res.Resources r8 = r8.getResources()     // Catch: java.lang.Exception -> L93
            int r9 = com.saj.connection.R.string.local_pv1_and_pv2_independent_mode     // Catch: java.lang.Exception -> L93
            java.lang.String r0 = r8.getString(r9)     // Catch: java.lang.Exception -> L93
        L92:
            return r0
        L93:
            r8 = move-exception
            java.lang.String r8 = r8.toString()
            com.saj.connection.utils.AppLog.e(r8)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.saj.connection.utils.LocalUtils.matchPVInputMode(android.content.Context, java.lang.String):java.lang.String");
    }

    /* JADX WARN: Code restructure failed: missing block: B:15:0x0026, code lost:
        if (r1 == 1) goto L13;
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:?, code lost:
        return "N/A";
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:?, code lost:
        return r5.getResources().getString(com.saj.connection.R.string.local_pv_parallel_mode);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static java.lang.String matchR6PVInputMode(android.content.Context r5, java.lang.String r6) {
        /*
            java.lang.String r0 = "N/A"
            r1 = -1
            int r2 = r6.hashCode()     // Catch: java.lang.Exception -> L3f
            r3 = 48
            r4 = 1
            if (r2 == r3) goto L1b
            r3 = 56
            if (r2 == r3) goto L11
            goto L24
        L11:
            java.lang.String r2 = "8"
            boolean r6 = r6.equals(r2)     // Catch: java.lang.Exception -> L3f
            if (r6 == 0) goto L24
            r1 = 1
            goto L24
        L1b:
            java.lang.String r2 = "0"
            boolean r6 = r6.equals(r2)     // Catch: java.lang.Exception -> L3f
            if (r6 == 0) goto L24
            r1 = 0
        L24:
            if (r1 == 0) goto L34
            if (r1 == r4) goto L29
            goto L3e
        L29:
            android.content.res.Resources r5 = r5.getResources()     // Catch: java.lang.Exception -> L3f
            int r6 = com.saj.connection.R.string.local_pv_parallel_mode     // Catch: java.lang.Exception -> L3f
            java.lang.String r0 = r5.getString(r6)     // Catch: java.lang.Exception -> L3f
            goto L3e
        L34:
            android.content.res.Resources r5 = r5.getResources()     // Catch: java.lang.Exception -> L3f
            int r6 = com.saj.connection.R.string.local_pv_independent_mode     // Catch: java.lang.Exception -> L3f
            java.lang.String r0 = r5.getString(r6)     // Catch: java.lang.Exception -> L3f
        L3e:
            return r0
        L3f:
            r5 = move-exception
            java.lang.String r5 = r5.toString()
            com.saj.connection.utils.AppLog.e(r5)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.saj.connection.utils.LocalUtils.matchR6PVInputMode(android.content.Context, java.lang.String):java.lang.String");
    }

    public static String getPowerRegulation(String str, String str2) {
        String str3;
        try {
            String tenTo16Add0AddRatio = tenTo16Add0AddRatio(str, 1);
            if (Double.parseDouble(str2) >= 0.0d) {
                str3 = tenTo16Add0AddRatio("3", 0) + tenTo16Add0AddRatio(str2, 3);
            } else {
                str3 = tenTo16Add0AddRatio("2", 0) + tenTo16Add0AddRatio(String.valueOf(BigDecimal.valueOf(Double.parseDouble(str2)).abs()), 3);
            }
            return tenTo16Add0AddRatio + str3;
        } catch (Exception unused) {
            return "";
        }
    }

    public static String set1PointData(String str) {
        String str2 = "N/A";
        try {
            if (!"65535".equals(str) && !"FFFF".equals(str.toUpperCase()) && !"N/A".equals(str)) {
                if (str.length() > 1) {
                    str2 = str.substring(0, str.length() - 1) + "." + str.substring(str.length() - 1);
                } else if (Integer.parseInt(str) == 0) {
                    str2 = "0";
                } else {
                    str2 = "0." + str;
                }
            }
        } catch (Exception unused) {
        }
        return str2;
    }

    /* JADX WARN: Code restructure failed: missing block: B:23:0x0088, code lost:
        return "-" + r0;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static java.lang.String set1PointDataS(java.lang.String r7) {
        /*
            java.lang.String r0 = "N/A"
            java.lang.String r1 = "-"
            boolean r2 = r7.startsWith(r1)     // Catch: java.lang.Exception -> L89
            r3 = 0
            r4 = 1
            if (r2 == 0) goto L14
            java.lang.String r2 = ""
            java.lang.String r7 = r7.replace(r1, r2)     // Catch: java.lang.Exception -> L89
            r2 = 1
            goto L15
        L14:
            r2 = 0
        L15:
            java.lang.String r5 = "65535"
            boolean r5 = r5.equals(r7)     // Catch: java.lang.Exception -> L89
            if (r5 != 0) goto L77
            java.lang.String r5 = "FFFF"
            java.lang.String r6 = r7.toUpperCase()     // Catch: java.lang.Exception -> L89
            boolean r5 = r5.equals(r6)     // Catch: java.lang.Exception -> L89
            if (r5 != 0) goto L77
            boolean r5 = r0.equals(r7)     // Catch: java.lang.Exception -> L89
            if (r5 == 0) goto L30
            goto L77
        L30:
            int r5 = r7.length()     // Catch: java.lang.Exception -> L89
            if (r5 <= r4) goto L5d
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L89
            r5.<init>()     // Catch: java.lang.Exception -> L89
            int r6 = r7.length()     // Catch: java.lang.Exception -> L89
            int r6 = r6 - r4
            java.lang.String r3 = r7.substring(r3, r6)     // Catch: java.lang.Exception -> L89
            r5.append(r3)     // Catch: java.lang.Exception -> L89
            java.lang.String r3 = "."
            r5.append(r3)     // Catch: java.lang.Exception -> L89
            int r3 = r7.length()     // Catch: java.lang.Exception -> L89
            int r3 = r3 - r4
            java.lang.String r7 = r7.substring(r3)     // Catch: java.lang.Exception -> L89
            r5.append(r7)     // Catch: java.lang.Exception -> L89
            java.lang.String r0 = r5.toString()     // Catch: java.lang.Exception -> L89
            goto L77
        L5d:
            int r3 = java.lang.Integer.parseInt(r7)     // Catch: java.lang.Exception -> L89
            if (r3 != 0) goto L66
            java.lang.String r0 = "0"
            goto L77
        L66:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L89
            r3.<init>()     // Catch: java.lang.Exception -> L89
            java.lang.String r4 = "0."
            r3.append(r4)     // Catch: java.lang.Exception -> L89
            r3.append(r7)     // Catch: java.lang.Exception -> L89
            java.lang.String r0 = r3.toString()     // Catch: java.lang.Exception -> L89
        L77:
            if (r2 == 0) goto L89
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r1)
            r7.append(r0)
            java.lang.String r7 = r7.toString()
            return r7
        L89:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.saj.connection.utils.LocalUtils.set1PointDataS(java.lang.String):java.lang.String");
    }

    public static String set2PointData(String str) {
        String str2 = "N/A";
        try {
            if (!"65535".equals(str) && !"FFFF".equals(str.toUpperCase()) && !"N/A".equals(str)) {
                if (str.length() > 2) {
                    str2 = str.substring(0, str.length() - 2) + "." + str.substring(str.length() - 2);
                } else if (str.length() == 2) {
                    str2 = "0." + str;
                } else if (Integer.parseInt(str) == 0) {
                    str2 = "0";
                } else {
                    str2 = "0.0" + str;
                }
            }
        } catch (Exception unused) {
        }
        return str2;
    }

    /* JADX WARN: Code restructure failed: missing block: B:26:0x00a0, code lost:
        return "-" + r0;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static java.lang.String set2PointDataS(java.lang.String r7) {
        /*
            java.lang.String r0 = "N/A"
            java.lang.String r1 = "-"
            boolean r2 = r7.startsWith(r1)     // Catch: java.lang.Exception -> La1
            r3 = 0
            if (r2 == 0) goto L13
            r2 = 1
            java.lang.String r4 = ""
            java.lang.String r7 = r7.replace(r1, r4)     // Catch: java.lang.Exception -> La1
            goto L14
        L13:
            r2 = 0
        L14:
            java.lang.String r4 = "65535"
            boolean r4 = r4.equals(r7)     // Catch: java.lang.Exception -> La1
            if (r4 != 0) goto L8f
            java.lang.String r4 = "FFFF"
            java.lang.String r5 = r7.toUpperCase()     // Catch: java.lang.Exception -> La1
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> La1
            if (r4 != 0) goto L8f
            boolean r4 = r0.equals(r7)     // Catch: java.lang.Exception -> La1
            if (r4 == 0) goto L2f
            goto L8f
        L2f:
            int r4 = r7.length()     // Catch: java.lang.Exception -> La1
            r5 = 2
            if (r4 <= r5) goto L5d
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> La1
            r4.<init>()     // Catch: java.lang.Exception -> La1
            int r6 = r7.length()     // Catch: java.lang.Exception -> La1
            int r6 = r6 - r5
            java.lang.String r3 = r7.substring(r3, r6)     // Catch: java.lang.Exception -> La1
            r4.append(r3)     // Catch: java.lang.Exception -> La1
            java.lang.String r3 = "."
            r4.append(r3)     // Catch: java.lang.Exception -> La1
            int r3 = r7.length()     // Catch: java.lang.Exception -> La1
            int r3 = r3 - r5
            java.lang.String r7 = r7.substring(r3)     // Catch: java.lang.Exception -> La1
            r4.append(r7)     // Catch: java.lang.Exception -> La1
            java.lang.String r0 = r4.toString()     // Catch: java.lang.Exception -> La1
            goto L8f
        L5d:
            int r3 = r7.length()     // Catch: java.lang.Exception -> La1
            if (r3 != r5) goto L75
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> La1
            r3.<init>()     // Catch: java.lang.Exception -> La1
            java.lang.String r4 = "0."
            r3.append(r4)     // Catch: java.lang.Exception -> La1
            r3.append(r7)     // Catch: java.lang.Exception -> La1
            java.lang.String r0 = r3.toString()     // Catch: java.lang.Exception -> La1
            goto L8f
        L75:
            int r3 = java.lang.Integer.parseInt(r7)     // Catch: java.lang.Exception -> La1
            if (r3 != 0) goto L7e
            java.lang.String r0 = "0"
            goto L8f
        L7e:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> La1
            r3.<init>()     // Catch: java.lang.Exception -> La1
            java.lang.String r4 = "0.0"
            r3.append(r4)     // Catch: java.lang.Exception -> La1
            r3.append(r7)     // Catch: java.lang.Exception -> La1
            java.lang.String r0 = r3.toString()     // Catch: java.lang.Exception -> La1
        L8f:
            if (r2 == 0) goto La1
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r1)
            r7.append(r0)
            java.lang.String r7 = r7.toString()
            return r7
        La1:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.saj.connection.utils.LocalUtils.set2PointDataS(java.lang.String):java.lang.String");
    }

    public static String set3PointData(String str) {
        String str2 = "N/A";
        try {
            if (!"65535".equals(str) && !"FFFF".equals(str.toUpperCase()) && !"N/A".equals(str)) {
                if (str.length() > 3) {
                    str2 = str.substring(0, str.length() - 3) + "." + str.substring(str.length() - 3, str.length());
                } else if (str.length() == 3) {
                    str2 = "0." + str;
                } else if (str.length() == 2) {
                    str2 = "0.0" + str;
                } else if (Integer.parseInt(str) == 0) {
                    str2 = "0";
                } else {
                    str2 = "0.00" + str;
                }
            }
        } catch (Exception unused) {
        }
        return str2;
    }

    public static String toBinary8String(String str) {
        int parseInt = Integer.parseInt(str, 16);
        char[] cArr = {'0', '1'};
        char[] cArr2 = new char[8];
        int i = 8;
        do {
            i--;
            cArr2[i] = cArr[parseInt & 1];
            parseInt >>>= 1;
        } while (i > 0);
        return new String(cArr2, i, 8);
    }

    public static String toBinary16String(String str) {
        int parseInt = Integer.parseInt(str, 16);
        char[] cArr = {'0', '1'};
        char[] cArr2 = new char[16];
        int i = 16;
        do {
            i--;
            cArr2[i] = cArr[parseInt & 1];
            parseInt >>>= 1;
        } while (i > 0);
        return new String(cArr2, i, 16);
    }

    public static String twoToTen(String str) throws Exception {
        return Integer.valueOf(str, 2).toString();
    }

    public static String tenTo16(int i) throws Exception {
        String hexString = Integer.toHexString(i);
        int length = hexString.length();
        if (length == 1) {
            return "000" + hexString;
        } else if (length == 2) {
            return "00" + hexString;
        } else if (length != 3) {
            return hexString;
        } else {
            return "0" + hexString;
        }
    }

    public static String tenTo16Normal(int i) throws Exception {
        return Integer.toHexString(i);
    }

    public static String tenTo16(String str) throws Exception {
        String hexString = Integer.toHexString(Integer.parseInt(str));
        int length = hexString.length();
        if (length == 1) {
            return "000" + hexString;
        } else if (length == 2) {
            return "00" + hexString;
        } else if (length != 3) {
            return hexString;
        } else {
            return "0" + hexString;
        }
    }

    public static String tenTo16change2(String str) throws Exception {
        String hexString = Integer.toHexString(Integer.parseInt(str));
        if (hexString.length() == 1) {
            return "0" + hexString;
        }
        return hexString;
    }

    public static String tenChange2Ten(String str) throws Exception {
        if (str.length() == 1) {
            return "0" + str;
        }
        return str;
    }



    public static boolean isErrorCode(String str) {
        return isErrorCode(str, true);
    }

    public static boolean isErrorCode(String str, boolean z) {
        try {
            if (!"83".equals(str.substring(2, 4)) && !"90".equals(str.substring(2, 4))) {
                return false;
            }
            if (!"00".equals(str.substring(4, 6))) {
                AppLog.e("ifShowTips:" + z + ",data:" + str);
                if (z) {
                    //errorResponse(str.substring(4, 6));
                }
            }
            return true;
        } catch (Exception e) {
            AppLog.e(e.toString());
            return true;
        }
    }

    public static int checkDataLenth(byte[] bArr) {
        try {
            String encodeHexStr = HexUtil.encodeHexStr(bArr);
            if (encodeHexStr.length() > 6) {
                if (encodeHexStr.startsWith("0103") || encodeHexStr.startsWith("0110")) {
                    return Integer.parseInt(encodeHexStr.substring(4, 6), 16) * 2;
                }
                return 0;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String checkErrData(String str) {
        String unit16TO10_int = unit16TO10_int(str);
        return ("N/A".equals(unit16TO10_int) || "65535".equals(unit16TO10_int)) ? "0" : Integer.parseInt(unit16TO10_int) > 10 ? "MessageService.MSG_DB_COMPLETE" : String.valueOf(Integer.parseInt(unit16TO10_int) * 10);
    }

    public static String getPvUnit(String str) {
        try {
            if (!"N/A".equals(str) && !"0".equals(str) && !"0.0".equals(str) && !"0.00".equals(str)) {
                return String.format("%sA", str);
            }
        } catch (Exception unused) {
        }
        return "N/A";
    }

    public static String getParseModBusDataIntWithUnit(String str, int i, int i2, String str2) {
        String str3 = "N/A";
        try {
            String substring = str.substring(i, i2);
            AppLog.d("tempModBusData=" + substring);
            if (!"FFFF".equals(substring.toUpperCase()) && !"0000".equals(substring.toUpperCase())) {
                String unit16TO10_int = unit16TO10_int(substring);
                if (ExifInterface.GPS_MEASUREMENT_INTERRUPTED.equals(str2)) {
                    if (unit16TO10_int.length() > 1) {
                        str3 = unit16TO10_int.substring(0, unit16TO10_int.length() - 1) + "." + unit16TO10_int.substring(unit16TO10_int.length() - 1) + str2;
                    } else {
                        str3 = "0." + unit16TO10_int + str2;
                    }
                } else if (unit16TO10_int.length() > 2) {
                    str3 = unit16TO10_int.substring(0, unit16TO10_int.length() - 2) + "." + unit16TO10_int.substring(unit16TO10_int.length() - 2) + str2;
                } else if (unit16TO10_int.length() == 2) {
                    str3 = "0." + unit16TO10_int + str2;
                } else if (unit16TO10_int.length() != 1) {
                    str3 = unit16TO10_int;
                } else if ("0".equals(unit16TO10_int)) {
                    str3 = "0." + unit16TO10_int + str2;
                } else {
                    str3 = "0.0" + unit16TO10_int + str2;
                }
            }
        } catch (Exception unused) {
        }
        return str3;
    }

    public static String int32To10(String str) {
        try {
            long parseLong = Long.parseLong(str, 16);
            char[] charArray = Long.toBinaryString(parseLong).toCharArray();
            String str2 = "";
            if (charArray.length < 32) {
                String str3 = "";
                for (int i = 0; i < 32 - charArray.length; i++) {
                    str3 = str3 + "0";
                }
                charArray = (str3 + String.valueOf(charArray)).toCharArray();
            }
            if ("1".equals(String.valueOf(charArray[0]))) {
                for (char c : charArray) {
                    str2 = "1".equals(String.valueOf(c)) ? str2 + "0" : str2 + "1";
                }
                return "-" + (Long.parseLong(str2, 2) + 1);
            }
            return String.valueOf(parseLong);
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    public static String int16To10(String str) {
        try {
            int parseInt = Integer.parseInt(str, 16);
            char[] charArray = Integer.toBinaryString(parseInt).toCharArray();
            String str2 = "";
            if (charArray.length < 16) {
                String str3 = "";
                for (int i = 0; i < 16 - charArray.length; i++) {
                    str3 = str3 + "0";
                }
                charArray = (str3 + String.valueOf(charArray)).toCharArray();
            }
            if ("1".equals(String.valueOf(charArray[0]))) {
                for (char c : charArray) {
                    str2 = "1".equals(String.valueOf(c)) ? str2 + "0" : str2 + "1";
                }
                return "-" + (Integer.parseInt(str2, 2) + 1);
            }
            return String.valueOf(parseInt);
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    public static byte[] getSendData(String str) {
        int i = 0;
        if (str.length() % 2 != 0) {
            AppLog.d("send data error");
            return new byte[0];
        }
        ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(str.length() / 2);
        int i2 = 0;
        while (i2 < str.length()) {
            if (str.charAt(i2) != ' ') {
                int i3 = i2 + 2;
                byteArrayBuffer.append(hexStrToByteArray(str.substring(i2, i3)));
                i2 = i3;
            } else {
                i2++;
            }
        }
        String str2 = str + CRC16_Check(byteArrayBuffer.toByteArray(), byteArrayBuffer.length()).toUpperCase();
        AppLog.d("发送完整的指令：" + str2);
        byteArrayBuffer.clear();
        while (i < str2.length()) {
            if (str2.charAt(i) != ' ') {
                int i4 = i + 2;
                byteArrayBuffer.append(hexStrToByteArray(str2.substring(i, i4)));
                i = i4;
            } else {
                i++;
            }
        }
        return byteArrayBuffer.toByteArray();
    }

    public static String byteArray2HexStringWithSpace(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bArr) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() < 2) {
                hexString = "0" + hexString;
            }
            sb.append(hexString);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String byteArray2HexString(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bArr) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() < 2) {
                hexString = "0" + hexString;
            }
            sb.append(hexString);
        }
        return sb.toString();
    }

    public static String byteArray2tens(byte[] bArr) {
        if (bArr != null && bArr.length != 0) {
            try {
                StringBuilder sb = new StringBuilder();
                for (byte b : bArr) {
                    StringBuilder sb2 = new StringBuilder();
                    char c = (char) b;
                    sb2.append(c);
                    sb2.append("");
                    String sb3 = sb2.toString();
                    if (c != 0) {
                        sb.append(sb3);
                    }
                }
                return sb.toString();
            } catch (Exception e) {
                AppLog.e(e.toString());
            }
        }
        return "";
    }

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

    public static boolean isCorrectData(String str, int i) {
        if (!TextUtils.isEmpty(str)) {
            String substring = str.substring(0, (i + 1 + 4) * 2);
            String substring2 = substring.substring(substring.length() - 4);
            String substring3 = substring.substring(0, substring.length() - 4);
            byte[] hexString2ByteArray = hexString2ByteArray(substring3);
            AppLog.d("==>>temp:" + substring3);
            String lowerCase = CRC16_Check(hexString2ByteArray, hexString2ByteArray.length).toLowerCase();
            String substring4 = substring.substring(0, 4);
            if (!substring4.contains("8") && !substring4.contains("9")) {
                return lowerCase.equals(substring2);
            }
        }
        return false;
    }

    public static int bcdByteArray2Int(byte b, byte b2) {
        int i = b & MIN_VALUE;
        int i2 = b;
        if (i == 128) {
            i2 = b + 256;
        }
        int i3 = (((byte) (i2 / 16)) * 1000) + (((byte) (i2 % 16)) * 100) + 0;
        int i4 = b2 & MIN_VALUE;
        int i5 = b2;
        if (i4 == 128) {
            i5 = b2 + 256;
        }
        return i3 + (((byte) (i5 / 16)) * 10) + ((byte) (i5 % 16));
    }

    public static byte[] hexString2ByteArray(String str) {
        if (str == null || str.equals("") || str.length() % 2 != 0) {
            return null;
        }
        byte[] bArr = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            int i2 = i * 2;
            char charAt = str.charAt(i2);
            char charAt2 = str.charAt(i2 + 1);
            byte hexChar2Byte = hexChar2Byte(charAt);
            byte hexChar2Byte2 = hexChar2Byte(charAt2);
            if (hexChar2Byte < 0 || hexChar2Byte2 < 0) {
                return null;
            }
            bArr[i] = (byte) ((hexChar2Byte << 4) + hexChar2Byte2);
        }
        return bArr;
    }

    public static String hex16ToString(String str) {
        int length = str.length() / 2;
        byte[] bArr = new byte[length];
        for (int i = 0; i < length; i++) {
            int i2 = i * 2;
            try {
                bArr[i] = (byte) (Integer.parseInt(str.substring(i2, i2 + 2), 16) & 255);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            return new String(bArr, StandardCharsets.UTF_8);
        } catch (Exception e2) {
            e2.printStackTrace();
            return str;
        }
    }

    public static String stringTo16Hex(String str) {
        byte[] bytes;
        try {
            String str2 = "";
            for (byte b : str.getBytes()) {
                Integer.valueOf(b);
                String hexString = Integer.toHexString(b);
                if (hexString.length() > 2) {
                    hexString = hexString.substring(hexString.length() - 2);
                }
                str2 = str2 + hexString;
            }
            return str2;
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }

    public static byte[] string2ASCIIByteArray(String str) {
        try {
            return str.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            AppLog.e("字符串转换为ASCII码Byte数组错误");
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isIP(String str) {
        if (str.length() < 7 || str.length() > 15 || "".equals(str)) {
            return false;
        }
        boolean find = Pattern.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}").matcher(str).find();
        if (find) {
            String[] split = str.split("\\.");
            if (split.length == 4) {
                try {
                    for (String str2 : split) {
                        if (Integer.parseInt(str2) < 0 || Integer.parseInt(str2) > 255) {
                            return false;
                        }
                    }
                    return true;
                } catch (Exception unused) {
                }
            }
            return false;
        }
        return find;
    }

    public static String two16ToBinary4String(String str) {
        if (str.length() != 2) {
            return "";
        }
        return toBinary4String(str.substring(0, 1)) + toBinary4String(str.substring(1));
    }

    public static String toBinary4String(String str) {
        int parseInt = Integer.parseInt(str, 16);
        char[] cArr = {'0', '1'};
        char[] cArr2 = new char[4];
        int i = 4;
        do {
            i--;
            cArr2[i] = cArr[parseInt & 1];
            parseInt >>>= 1;
        } while (i > 0);
        return new String(cArr2, i, 4);
    }

    public static String getUnsignedByte(String str) {
        try {
            int parseInt = Integer.parseInt(str);
            if (parseInt > 8000) {
                int i = (parseInt - 65535) + 1;
                return "" + i;
            }
            return "" + parseInt;
        } catch (Exception e) {
            AppLog.d("Unsigned Exception" + e.toString());
            return str;
        }
    }

    public static boolean isLoraMode(String str) {
        try {
            return str.startsWith("317");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean is324Mode(String str) {
        try {
            return str.startsWith("324");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean is324ModeHex(String str) {
        try {
            return str.startsWith("333234");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String IPtoAsciiStr(String str) {
        String[] split = str.split("\\.");
        String str2 = "";
        if (split.length == 4) {
            for (int i = 0; i < 4; i++) {
                try {
                    String hexString = Integer.toHexString(Integer.parseInt(split[i]));
                    if (hexString.length() < 2) {
                        hexString = "0" + hexString;
                    }
                    str2 = str2 + hexString;
                } catch (Exception e) {
                    AppLog.e(e.toString());
                }
            }
        }
        return str2;
    }

    public static boolean isValuableUdpMsg(String str, String str2) {
        try {
            if (str.startsWith("0110")) {
                return true;
            }
            return Integer.parseInt(str.substring(str.length() + (-2)), 16) * 2 == Integer.parseInt(str2.substring(4, 6), 16);
        } catch (Exception unused) {
            return false;
        }
    }

    public static boolean isIllegalParamWithRange(Context context, String str, String str2, String str3, String str4) {
        if (TextUtils.isEmpty(str) || str.equals("N/A")) {
            //ToastUtils.showShort(str5);
        } else if (TextUtils.isEmpty(str3) || TextUtils.isEmpty(str4)) {
            return false;
        } else {
            try {
                float parseFloat = Float.parseFloat(str);
                float parseFloat2 = Float.parseFloat(str3);
                float parseFloat3 = Float.parseFloat(str4);
                if (parseFloat >= parseFloat2 && parseFloat <= parseFloat3) {
                    return false;
                }
                //ToastUtils.showShort(str6);
            } catch (Exception e) {
                AppLog.e(e.toString());
            }
        }
        return true;
    }

    public static void getValue(EditText editText, String str) {
        if (TextUtils.isEmpty(str)) {
            editText.setText("");
        } else {
            editText.setText(str);
        }
    }

    public String getValue(String str) {
        return TextUtils.isEmpty(str) ? "" : str;
    }

    public static void getValueRange(TextView textView, String str, String str2) {
        String str3;
        if (TextUtils.isEmpty(str) && TextUtils.isEmpty(str2)) {
            str3 = "N/A";
        } else {
            str3 = "[" + str + "~" + str2 + "]";
        }
        textView.setText(str3);
    }

    public static List<String> getVWattVVarLists() {
        ArrayList arrayList = new ArrayList();
        arrayList.add("AS4777_AS_4777");
        arrayList.add("AS4777_Ausgrid");
        arrayList.add("AS4777_EndeavourEnergy");
        arrayList.add("AS4777_EssentialEnergy");
        arrayList.add("AS4777_EvoEnergy");
        arrayList.add("AS4777_AusNET");
        arrayList.add("AS4777_CitiPower");
        arrayList.add("AS4777_PowerCor");
        arrayList.add("AS4777_Jemena");
        arrayList.add("AS4777_UnitedEnergy");
        arrayList.add("AS4777_ErgonEnergy");
        arrayList.add("AS4777_Energex");
        arrayList.add("AS4777_SAPN");
        arrayList.add("AS4777_WesternPower");
        arrayList.add("AS4777_AusA");
        arrayList.add("AS4777_AusB");
        arrayList.add("AS4777_AusC");
        arrayList.add("AS4777_NZL");
        return arrayList;
    }

}
