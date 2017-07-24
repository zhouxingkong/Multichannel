package com.a601.multichannel;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ylx on 2017/7/19.
 */

public class SystemPara {

    //各个参数的Bytes长度
    public static int REPEAT_PERIOD_LENGTH = 4;
    public static int HIGH_VOLTAGE_LENGTH = 1;
    public static int CHANNEL_FLAG_LENGTH = 2;
    public static int WORK_MODE_LENGTH = 1;
    public static int SCAN_ACCURACY_LENGTH = 2;
    public static int ENCODER_HANDLE_LENGTH = 1;
    public static int STATUSLED_LENGTH = 1;

    //单例对象
    private static SystemPara singleton;
    //系统传参数成员变量
    private int REPEAT_PERIOD;
    private int HIGH_VOLTAGE;
    private int CHANNEL_FLAG;
    private int WORK_MODE;
    private int SCAN_ACCURACY;
    private int ENCODER_HANDLE;
    private int STATUSLED;
    //系统参数的bytes形式 便于USB发送
    private byte[] REPEAT_PERIOD_BYTES;
    private byte[] HIGH_VOLTAGE_BYTES;
    private byte[] CHANNEL_FLAG_BYTES;
    private byte[] WORK_MODE_BYTES;
    private byte[] SCAN_ACCURACY_BYTES;
    private byte[] ENCODER_HANDLE_BYTES;
    private byte[] STATUSLED_BYTES;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private Context mContext;

    public SystemPara(Context context) {
        preferences = context.getSharedPreferences("system", Context.MODE_PRIVATE);
        editor = preferences.edit();
        REPEAT_PERIOD = preferences.getInt("REPEAT_PERIOD", 0);
        REPEAT_PERIOD_BYTES = convertIntToBytes(REPEAT_PERIOD, REPEAT_PERIOD_LENGTH);
        HIGH_VOLTAGE = preferences.getInt("HIGH_VOLTAGE", 0 );
        HIGH_VOLTAGE_BYTES = convertIntToBytes(HIGH_VOLTAGE, HIGH_VOLTAGE_LENGTH);
        CHANNEL_FLAG = preferences.getInt("CHANNEL_FLAG", 0 );
        CHANNEL_FLAG_BYTES = convertIntToBytes(CHANNEL_FLAG, CHANNEL_FLAG_LENGTH);
        WORK_MODE = preferences.getInt("WORK_MODE", 0 );
        WORK_MODE_BYTES = convertIntToBytes(WORK_MODE, WORK_MODE_LENGTH);
        SCAN_ACCURACY = preferences.getInt("SCAN_ACCURACY", 0 );
        SCAN_ACCURACY_BYTES = convertIntToBytes(SCAN_ACCURACY, SCAN_ACCURACY_LENGTH);
        ENCODER_HANDLE = preferences.getInt("ENCODER_HANDLE", 0 );
        ENCODER_HANDLE_BYTES = convertIntToBytes(ENCODER_HANDLE, ENCODER_HANDLE_LENGTH);
        STATUSLED = preferences.getInt("STATUSLED", 0 );
        STATUSLED_BYTES = convertIntToBytes(STATUSLED, STATUSLED_LENGTH);

        mContext = context;

    }

    public static SystemPara getSystemPara(Context context) {
        if (singleton == null) {
            singleton = new SystemPara(context);
        }
        return singleton;
    }

    public int getREPEAT_PERIOD() {
        return REPEAT_PERIOD;
    }

    public void setREPEAT_PERIOD(int REPEAT_PERIOD) {
        this.REPEAT_PERIOD = REPEAT_PERIOD;
        REPEAT_PERIOD_BYTES = convertIntToBytes(REPEAT_PERIOD ,REPEAT_PERIOD_LENGTH);
        editor.putInt("REPEAT_PERIOD", REPEAT_PERIOD);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getHIGH_VOLTAGE() {
        return HIGH_VOLTAGE;
    }

    public void setHIGH_VOLTAGE(int HIGH_VOLTAGE) {
        this.HIGH_VOLTAGE = HIGH_VOLTAGE;
        HIGH_VOLTAGE_BYTES = convertIntToBytes(HIGH_VOLTAGE , HIGH_VOLTAGE_LENGTH);
        editor.putInt("HIGH_VOLTAGE", HIGH_VOLTAGE);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getCHANNEL_FLAG() {
        return CHANNEL_FLAG;
    }

    // TODO: 2017/7/24 下发的通道按位发 
    public void setCHANNEL_FLAG(int CHANNEL_FLAG) {
        this.CHANNEL_FLAG = CHANNEL_FLAG;
        CHANNEL_FLAG_BYTES = convertIntToBytes(CHANNEL_FLAG , CHANNEL_FLAG_LENGTH);
        editor.putInt("CHANNEL_FLAG", CHANNEL_FLAG);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getWORK_MODE() {
        return WORK_MODE;
    }

    public void setWORK_MODE(int WORK_MODE) {
        this.WORK_MODE = WORK_MODE;
        WORK_MODE_BYTES = convertIntToBytes(WORK_MODE , WORK_MODE_LENGTH);
        editor.putInt("WORK_MODE", WORK_MODE);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getSCAN_ACCURACY() {
        return SCAN_ACCURACY;
    }

    public void setSCAN_ACCURACY(int SCAN_ACCURACY) {
        this.SCAN_ACCURACY = SCAN_ACCURACY;
        SCAN_ACCURACY_BYTES = convertIntToBytes(SCAN_ACCURACY , SCAN_ACCURACY_LENGTH);
        editor.putInt("SCAN_ACCURACY", SCAN_ACCURACY);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getENCODER_HANDLE() {
        return ENCODER_HANDLE;
    }

    public void setENCODER_HANDLE(int ENCODER_HANDLE) {
        this.ENCODER_HANDLE = ENCODER_HANDLE;
        ENCODER_HANDLE_BYTES = convertIntToBytes(ENCODER_HANDLE , ENCODER_HANDLE_LENGTH);
        editor.putInt("ENCODER_HANDLE", ENCODER_HANDLE);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getSTATUSLED() {
        return STATUSLED;
    }

    public void setSTATUSLED(int STATUSLED) {
        this.STATUSLED = STATUSLED;
        STATUSLED_BYTES = convertIntToBytes(STATUSLED , STATUSLED_LENGTH);
        editor.putInt("STATUSLED", STATUSLED);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    private byte[] convertIntToBytes(int integer, int length) {
        byte[] data = new byte[length];
        for (int i =0 ;i<length;i++) {
            data[i] = (byte) ((integer >> (8 * i)) & 0xFF);
        }
        return data;
    }

    public byte[] getREPEAT_PERIOD_BYTES() {
        return REPEAT_PERIOD_BYTES;
    }

    public byte[] getHIGH_VOLTAGE_BYTES() {
        return HIGH_VOLTAGE_BYTES;
    }

    public byte[] getCHANNEL_FLAG_BYTES() {
        return CHANNEL_FLAG_BYTES;
    }

    public byte[] getWORK_MODE_BYTES() {
        return WORK_MODE_BYTES;
    }

    public byte[] getSCAN_ACCURACY_BYTES() {
        return SCAN_ACCURACY_BYTES;
    }

    public byte[] getENCODER_HANDLE_BYTES() {
        return ENCODER_HANDLE_BYTES;
    }

    public byte[] getSTATUSLED_BYTES() {
        return STATUSLED_BYTES;
    }
}
