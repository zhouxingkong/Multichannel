package com.a601.multichannel;

import android.content.Context;
import android.content.SharedPreferences;

import static com.a601.multichannel.TypeConvertUtil.convertIntToBytes;

/**
 * Created by ylx on 2017/7/19.
 */

public class ChannelPara {

    //常量定义
    public static final int CHANNEL1 = 1;
    public static final int CHANNEL2 = 2;
    public static final int CHANNEL3 = 3;
    public static final int CHANNEL4 = 4;
    public static final int CHANNEL5 = 5;
    public static final int CHANNEL6 = 6;
    public static final int CHANNEL7 = 7;
    public static final int CHANNEL8 = 8;
    public static final int CHANNEL_TOTAL_NUM = 8;
    //各个配置参数的Bytes长度
    public static int TRIG_PULSE_WIDE_LENGTH = 1;
    public static int SAMPLE_DELAYS_LENGTH = 4;
    public static int SAMPLE_DEPTHS_LENGTH = 4;
    public static int GAIN_BAND_SELECTS_LENGTH = 1;
    public static int DAC_DATAS_LENGTH = 2;
    public static int DEMODU_SELECTS_LENGTH = 1;
    public static int FILTER_BANDS_LENGTH = 1;
    public static int GATE1_DET_POSS_LENGTH = 4;
    public static int GATE1_DET_WIDTHS_LENGTH = 4;
    public static int GATE2_DET_POSS_LENGTH = 4;
    public static int GATE2_DET_WIDTHS_LENGTH = 4;
    public static int GATE3_DET_POSS_LENGTH = 4;
    public static int GATE3_DET_WIDTHS_LENGTH = 4;
    public static int GATE4_DET_POSS_LENGTH = 4;
    public static int GATE4_DET_WIDTHS_LENGTH = 4;

    //八个通道的单例
    private static ChannelPara[] channelParas = new ChannelPara[CHANNEL_TOTAL_NUM];

    //各通道的工作参数int形式
    private int TRIG_PULSE_WIDE;
    private int SAMPLE_DELAYS;
    private int SAMPLE_DEPTHS;
    private int GAIN_BAND_SELECTS;
    private int DAC_DATAS;
    private int DEMODU_SELECTS;
    private int FILTER_BANDS;
    private int GATE1_DET_POSS;
    private int GATE1_DET_WIDTHS;
    private int GATE2_DET_POSS;
    private int GATE2_DET_WIDTHS;
    private int GATE3_DET_POSS;
    private int GATE3_DET_WIDTHS;
    private int GATE4_DET_POSS;
    private int GATE4_DET_WIDTHS;


    //工作参数的bytes形式 便于USB发送
    private byte[] TRIG_PULSE_WIDE_BYTES;
    private byte[] SAMPLE_DELAYS_BYTES;
    private byte[] SAMPLE_DEPTHS_BYTES;
    private byte[] GAIN_BAND_SELECTS_BYTES;
    private byte[] DAC_DATAS_BYTES;
    private byte[] DEMODU_SELECTS_BYTES;
    private byte[] FILTER_BANDS_BYTES;
    private byte[] GATE1_DET_POSS_BYTES;
    private byte[] GATE1_DET_WIDTHS_BYTES;
    private byte[] GATE2_DET_POSS_BYTES;
    private byte[] GATE2_DET_WIDTHS_BYTES;
    private byte[] GATE3_DET_POSS_BYTES;
    private byte[] GATE3_DET_WIDTHS_BYTES;
    private byte[] GATE4_DET_POSS_BYTES;
    private byte[] GATE4_DET_WIDTHS_BYTES;

    //通道号
    private int mchannel;

    private Context mContext;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public ChannelPara(Context context, int channel) {
        preferences = context.getSharedPreferences("channel" + channel, Context.MODE_PRIVATE);
        editor = preferences.edit();
        TRIG_PULSE_WIDE = preferences.getInt("TRIG_PULSE_WIDE", 0);
        TRIG_PULSE_WIDE_BYTES = convertIntToBytes(TRIG_PULSE_WIDE, TRIG_PULSE_WIDE_LENGTH);
        SAMPLE_DELAYS = preferences.getInt("SAMPLE_DELAY", 0);
        SAMPLE_DELAYS_BYTES = convertIntToBytes(SAMPLE_DELAYS, SAMPLE_DELAYS_LENGTH);
        SAMPLE_DEPTHS = preferences.getInt("SAMPLE_DEPTH", 0);
        SAMPLE_DEPTHS_BYTES = convertIntToBytes(SAMPLE_DEPTHS, SAMPLE_DEPTHS_LENGTH);
        GAIN_BAND_SELECTS = preferences.getInt("GAIN_BAND_SELECT", 0);
        GAIN_BAND_SELECTS_BYTES = convertIntToBytes(GAIN_BAND_SELECTS, GAIN_BAND_SELECTS_LENGTH);
        DAC_DATAS = preferences.getInt("DAC_DATA", 0);
        DAC_DATAS_BYTES = convertIntToBytes(DAC_DATAS, DAC_DATAS_LENGTH);
        DEMODU_SELECTS = preferences.getInt("DEMODU_SELECT", 0);
        DEMODU_SELECTS_BYTES = convertIntToBytes(DEMODU_SELECTS, DEMODU_SELECTS_LENGTH);
        FILTER_BANDS = preferences.getInt("FILTER_BAND", 0);
        FILTER_BANDS_BYTES = convertIntToBytes(FILTER_BANDS, FILTER_BANDS_LENGTH);
        GATE1_DET_POSS = preferences.getInt("GATE1_DET_POS", 0);
        GATE1_DET_POSS_BYTES = convertIntToBytes(GATE1_DET_POSS, GATE1_DET_POSS_LENGTH);
        GATE1_DET_WIDTHS = preferences.getInt("GATE1_DET_WIDTH", 0);
        GATE1_DET_WIDTHS_BYTES = convertIntToBytes(GATE1_DET_WIDTHS, GATE1_DET_WIDTHS_LENGTH);
        GATE2_DET_POSS = preferences.getInt("GATE2_DET_POS", 0);
        GATE2_DET_POSS_BYTES = convertIntToBytes(GATE2_DET_POSS, GATE2_DET_POSS_LENGTH);
        GATE2_DET_WIDTHS = preferences.getInt("GATE2_DET_WIDTH", 0);
        GATE2_DET_WIDTHS_BYTES = convertIntToBytes(GATE2_DET_WIDTHS, GATE2_DET_WIDTHS_LENGTH);
        GATE3_DET_POSS = preferences.getInt("GATE3_DET_POS", 0);
        GATE3_DET_POSS_BYTES = convertIntToBytes(GATE3_DET_POSS, GATE3_DET_POSS_LENGTH);
        GATE3_DET_WIDTHS = preferences.getInt("GATE3_DET_WIDTH", 0);
        GATE3_DET_WIDTHS_BYTES = convertIntToBytes(GATE3_DET_WIDTHS, GATE3_DET_WIDTHS_LENGTH);
        GATE4_DET_POSS = preferences.getInt("GATE4_DET_POS", 0);
        GATE4_DET_POSS_BYTES = convertIntToBytes(GATE4_DET_POSS, GATE4_DET_POSS_LENGTH);
        GATE4_DET_WIDTHS = preferences.getInt("GATE4_DET_WIDTH", 0);
        GATE4_DET_WIDTHS_BYTES = convertIntToBytes(GATE4_DET_WIDTHS, GATE4_DET_WIDTHS_LENGTH);
        mchannel = channel;
        mContext = context;

    }

    public static ChannelPara getChannelPara(Context context, int channel) {
        //为了防止参数越界
        int num = (channel < 1 || channel > CHANNEL_TOTAL_NUM) ? CHANNEL1 : channel;
        if (channelParas[num-1] == null) {
            channelParas[num-1] = new ChannelPara(context, num);
        }
        return channelParas[num-1];
    }

    public int getTRIG_PULSE_WIDE() {
        return TRIG_PULSE_WIDE;
    }

    public void setTRIG_PULSE_WIDE(int TRIG_PULSE_WIDE) {
        this.TRIG_PULSE_WIDE = TRIG_PULSE_WIDE;
        TRIG_PULSE_WIDE_BYTES = convertIntToBytes(TRIG_PULSE_WIDE,TRIG_PULSE_WIDE_LENGTH);
        editor.putInt("TRIG_PULSE_WIDE" , TRIG_PULSE_WIDE);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getSAMPLE_DELAYS() {
        return SAMPLE_DELAYS;
    }

    public void setSAMPLE_DELAYS(int SAMPLE_DELAYS) {
        this.SAMPLE_DELAYS = SAMPLE_DELAYS;
        SAMPLE_DELAYS_BYTES = convertIntToBytes(SAMPLE_DELAYS,SAMPLE_DELAYS_LENGTH);
        editor.putInt("SAMPLE_DELAY" , SAMPLE_DELAYS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getSAMPLE_DEPTHS() {
        return SAMPLE_DEPTHS;
    }

    public void setSAMPLE_DEPTHS(int SAMPLE_DEPTHS) {
        this.SAMPLE_DEPTHS = SAMPLE_DEPTHS;
        SAMPLE_DEPTHS_BYTES = convertIntToBytes(SAMPLE_DEPTHS,SAMPLE_DEPTHS_LENGTH);
        editor.putInt("SAMPLE_DEPTH" , SAMPLE_DEPTHS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getGAIN_BAND_SELECTS() {
        return GAIN_BAND_SELECTS;
    }

    public void setGAIN_BAND_SELECTS(int GAIN_BAND_SELECTS) {
        this.GAIN_BAND_SELECTS = GAIN_BAND_SELECTS;
        GAIN_BAND_SELECTS_BYTES = convertIntToBytes(GAIN_BAND_SELECTS,GAIN_BAND_SELECTS_LENGTH);
        editor.putInt("GAIN_BAND_SELECT" , GAIN_BAND_SELECTS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getDAC_DATAS() {
        return DAC_DATAS;
    }

    public void setDAC_DATAS(int DAC_DATAS) {
        this.DAC_DATAS = DAC_DATAS;
        DAC_DATAS_BYTES = convertIntToBytes(DAC_DATAS,DAC_DATAS_LENGTH);
        editor.putInt("DAC_DATA" , DAC_DATAS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getDEMODU_SELECTS() {
        return DEMODU_SELECTS;
    }

    public void setDEMODU_SELECTS(int DEMODU_SELECTS) {
        this.DEMODU_SELECTS = DEMODU_SELECTS;
        DEMODU_SELECTS_BYTES = convertIntToBytes(DEMODU_SELECTS,DEMODU_SELECTS_LENGTH);
        editor.putInt("DEMODU_SELECT" , DEMODU_SELECTS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getFILTER_BANDS() {
        return FILTER_BANDS;
    }

    public void setFILTER_BANDS(int FILTER_BANDS) {
        this.FILTER_BANDS = FILTER_BANDS;
        FILTER_BANDS_BYTES = convertIntToBytes(FILTER_BANDS,FILTER_BANDS_LENGTH);
        editor.putInt("FILTER_BAND" , FILTER_BANDS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getGATE1_DET_POSS() {
        return GATE1_DET_POSS;
    }

    public void setGATE1_DET_POSS(int GATE1_DET_POSS) {
        this.GATE1_DET_POSS = GATE1_DET_POSS;
        GATE1_DET_POSS_BYTES = convertIntToBytes(GATE1_DET_POSS,GATE1_DET_POSS_LENGTH);
        editor.putInt("GATE1_DET_POS" , GATE1_DET_POSS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getGATE1_DET_WIDTHS() {
        return GATE1_DET_WIDTHS;
    }

    public void setGATE1_DET_WIDTHS(int GATE1_DET_WIDTHS) {
        this.GATE1_DET_WIDTHS = GATE1_DET_WIDTHS;
        GATE1_DET_WIDTHS_BYTES = convertIntToBytes(GATE1_DET_WIDTHS,GATE1_DET_WIDTHS_LENGTH);
        editor.putInt("GATE1_DET_WIDTH" , GATE1_DET_WIDTHS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getGATE2_DET_POSS() {
        return GATE2_DET_POSS;
    }

    public void setGATE2_DET_POSS(int GATE2_DET_POSS) {
        this.GATE2_DET_POSS = GATE2_DET_POSS;
        GATE2_DET_POSS_BYTES = convertIntToBytes(GATE2_DET_POSS,GATE2_DET_POSS_LENGTH);
        editor.putInt("GATE2_DET_POS" , GATE2_DET_POSS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getGATE2_DET_WIDTHS() {
        return GATE2_DET_WIDTHS;
    }

    public void setGATE2_DET_WIDTHS(int GATE2_DET_WIDTHS) {
        this.GATE2_DET_WIDTHS = GATE2_DET_WIDTHS;
        GATE2_DET_WIDTHS_BYTES = convertIntToBytes(GATE2_DET_WIDTHS,GATE2_DET_WIDTHS_LENGTH);
        editor.putInt("GATE2_DET_WIDTH" , GATE2_DET_WIDTHS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getGATE3_DET_POSS() {
        return GATE3_DET_POSS;
    }

    public void setGATE3_DET_POSS(int GATE3_DET_POSS) {
        this.GATE3_DET_POSS = GATE3_DET_POSS;
        GATE3_DET_POSS_BYTES = convertIntToBytes(GATE3_DET_POSS,GATE3_DET_POSS_LENGTH);
        editor.putInt("GATE3_DET_POS" , GATE3_DET_POSS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getGATE3_DET_WIDTHS() {
        return GATE3_DET_WIDTHS;
    }

    public void setGATE3_DET_WIDTHS(int GATE3_DET_WIDTHS) {
        this.GATE3_DET_WIDTHS = GATE3_DET_WIDTHS;
        GATE3_DET_WIDTHS_BYTES = convertIntToBytes(GATE3_DET_WIDTHS,GATE3_DET_WIDTHS_LENGTH);
        editor.putInt("GATE3_DET_WIDTH" , GATE3_DET_WIDTHS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getGATE4_DET_POSS() {
        return GATE4_DET_POSS;
    }

    public void setGATE4_DET_POSS(int GATE4_DET_POSS) {
        this.GATE4_DET_POSS = GATE4_DET_POSS;
        GATE4_DET_POSS_BYTES = convertIntToBytes(GATE4_DET_POSS,GATE4_DET_POSS_LENGTH);
        editor.putInt("GATE4_DET_POS" , GATE4_DET_POSS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }

    public int getGATE4_DET_WIDTHS() {
        return GATE4_DET_WIDTHS;
    }

    public void setGATE4_DET_WIDTHS(int GATE4_DET_WIDTHS) {
        this.GATE4_DET_WIDTHS = GATE4_DET_WIDTHS;
        GATE4_DET_WIDTHS_BYTES = convertIntToBytes(GATE4_DET_WIDTHS,GATE4_DET_WIDTHS_LENGTH);
        editor.putInt("GATE4_DET_WIDTH" , GATE4_DET_WIDTHS);
        editor.apply();
        USBClient.getInstance(mContext).writeParameters();
    }


    public byte[] getTRIG_PULSE_WIDE_BYTES() {
        return TRIG_PULSE_WIDE_BYTES;
    }

    public byte[] getSAMPLE_DELAYS_BYTES() {
        return SAMPLE_DELAYS_BYTES;
    }

    public byte[] getSAMPLE_DEPTHS_BYTES() {
        return SAMPLE_DEPTHS_BYTES;
    }

    public byte[] getGAIN_BAND_SELECTS_BYTES() {
        return GAIN_BAND_SELECTS_BYTES;
    }

    public byte[] getDAC_DATAS_BYTES() {
        return DAC_DATAS_BYTES;
    }

    public byte[] getDEMODU_SELECTS_BYTES() {
        return DEMODU_SELECTS_BYTES;
    }

    public byte[] getFILTER_BANDS_BYTES() {
        return FILTER_BANDS_BYTES;
    }

    public byte[] getGATE1_DET_POSS_BYTES() {
        return GATE1_DET_POSS_BYTES;
    }

    public byte[] getGATE1_DET_WIDTHS_BYTES() {
        return GATE1_DET_WIDTHS_BYTES;
    }

    public byte[] getGATE2_DET_POSS_BYTES() {
        return GATE2_DET_POSS_BYTES;
    }

    public byte[] getGATE2_DET_WIDTHS_BYTES() {
        return GATE2_DET_WIDTHS_BYTES;
    }

    public byte[] getGATE3_DET_POSS_BYTES() {
        return GATE3_DET_POSS_BYTES;
    }

    public byte[] getGATE3_DET_WIDTHS_BYTES() {
        return GATE3_DET_WIDTHS_BYTES;
    }

    public byte[] getGATE4_DET_POSS_BYTES() {
        return GATE4_DET_POSS_BYTES;
    }

    public byte[] getGATE4_DET_WIDTHS_BYTES() {
        return GATE4_DET_WIDTHS_BYTES;
    }
}
