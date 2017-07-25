package com.a601.multichannel;

/**
 * Created by ylx on 2017/7/25.
 */

public class TypeConvertUtil {
    public static byte[] convertIntToBytes(int integer, int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[length - 1 - i] = (byte) ((integer >> (8 * i)) & 0xFF);
        }
        return data;
    }


    public static int addIntAndBytes(int a, byte[] b) {
        int sum = a;
        for (int i = 0; i < b.length; i++) {
            sum += (b[i] & 0xff);
        }
        return sum;
    }


    public static int convertBytesToInt(byte[] b) {
        int a = 0;
        int c = 1;
        for (int i = 0; i < b.length; i++) {
            a += (b[b.length-1-i] & 0xff) * c;
            c = c * 256;
        }
        return a;
    }

}
