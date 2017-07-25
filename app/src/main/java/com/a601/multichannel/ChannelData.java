package com.a601.multichannel;

import static com.a601.multichannel.ChannelPara.CHANNEL1;
import static com.a601.multichannel.ChannelPara.CHANNEL_TOTAL_NUM;

/**
 * Created by ylx on 2017/7/21.
 */

public class ChannelData {

    private int SAMPLE_DEPTH;
    private int CURRENT_CHL_NO;
    private byte[] ASCAN_DATA;
    private int CODER1_COUNT;
    private int CODER2_COUNT;
    private int GATE1_PEAK_POS;
    private int GATE1_PEAK_AMP;
    private int GATE2_PEAK_POS;
    private int GATE2_PEAK_AMP;
    private int GATE3_PEAK_POS;
    private int GATE3_PEAK_AMP;
    private int GATE4_PEAK_POS;
    private int GATE4_PEAK_AMP;

    //八个通道的数据单例
    private static ChannelData[] channelDatas = new ChannelData[CHANNEL_TOTAL_NUM];


    public static ChannelData getChannelData(int channel) {
        //防止越界
        int num = (channel < 1 || channel > CHANNEL_TOTAL_NUM) ? CHANNEL1 : channel;
        if (channelDatas[num - 1] == null) {
            channelDatas[num - 1] = new ChannelData();
        }
        return channelDatas[num - 1];
    }

    public int getSAMPLE_DEPTH() {
        return SAMPLE_DEPTH;
    }

    public void setSAMPLE_DEPTH(int SAMPLE_DEPTH) {
        this.SAMPLE_DEPTH = SAMPLE_DEPTH;
    }

    public int getCURRENT_CHL_NO() {
        return CURRENT_CHL_NO;
    }

    public void setCURRENT_CHL_NO(int CURRENT_CHL_NO) {
        this.CURRENT_CHL_NO = CURRENT_CHL_NO;
    }

    public byte[] getASCAN_DATA() {
        return ASCAN_DATA;
    }

    public void setASCAN_DATA(byte[] ASCAN_DATA) {
        this.ASCAN_DATA = ASCAN_DATA;
    }

    public int getCODER1_COUNT() {
        return CODER1_COUNT;
    }

    public void setCODER1_COUNT(int CODER1_COUNT) {
        this.CODER1_COUNT = CODER1_COUNT;
    }

    public int getCODER2_COUNT() {
        return CODER2_COUNT;
    }

    public void setCODER2_COUNT(int CODER2_COUNT) {
        this.CODER2_COUNT = CODER2_COUNT;
    }

    public int getGATE1_PEAK_POS() {
        return GATE1_PEAK_POS;
    }

    public void setGATE1_PEAK_POS(int GATE1_PEAK_POS) {
        this.GATE1_PEAK_POS = GATE1_PEAK_POS;
    }

    public int getGATE1_PEAK_AMP() {
        return GATE1_PEAK_AMP;
    }

    public void setGATE1_PEAK_AMP(int GATE1_PEAK_AMP) {
        this.GATE1_PEAK_AMP = GATE1_PEAK_AMP;
    }

    public int getGATE2_PEAK_POS() {
        return GATE2_PEAK_POS;
    }

    public void setGATE2_PEAK_POS(int GATE2_PEAK_POS) {
        this.GATE2_PEAK_POS = GATE2_PEAK_POS;
    }

    public int getGATE2_PEAK_AMP() {
        return GATE2_PEAK_AMP;
    }

    public void setGATE2_PEAK_AMP(int GATE2_PEAK_AMP) {
        this.GATE2_PEAK_AMP = GATE2_PEAK_AMP;
    }

    public int getGATE3_PEAK_POS() {
        return GATE3_PEAK_POS;
    }

    public void setGATE3_PEAK_POS(int GATE3_PEAK_POS) {
        this.GATE3_PEAK_POS = GATE3_PEAK_POS;
    }

    public int getGATE3_PEAK_AMP() {
        return GATE3_PEAK_AMP;
    }

    public void setGATE3_PEAK_AMP(int GATE3_PEAK_AMP) {
        this.GATE3_PEAK_AMP = GATE3_PEAK_AMP;
    }

    public int getGATE4_PEAK_POS() {
        return GATE4_PEAK_POS;
    }

    public void setGATE4_PEAK_POS(int GATE4_PEAK_POS) {
        this.GATE4_PEAK_POS = GATE4_PEAK_POS;
    }

    public int getGATE4_PEAK_AMP() {
        return GATE4_PEAK_AMP;
    }

    public void setGATE4_PEAK_AMP(int GATE4_PEAK_AMP) {
        this.GATE4_PEAK_AMP = GATE4_PEAK_AMP;
    }


}
