package com.a601.multichannel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.util.Arrays;
import java.util.concurrent.ThreadFactory;

/**
 * Created by ylx on 2017/7/20.
 */

public class USBClient {

    private static final int PARA_HEAD_LENGTH = 4;
    private static final int PARA_DATA_LENGTH = 502;
    private static final int PARA_CHECK_LENGTH = 2;
    private static final int RESERVED_SYSTEM_LENGTH = 10;
    private static final int RESERVED_CHANNEL_LENGTH = 14;
    private static final byte[] PARA_HEAD_VALUE = {(byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4};

    private static final int READ_HEAD_LENGTH = 10;
    private static final int READ_INDENTIFY_LENGTH = 1;
    private static final int READ_RESPONSE_LENGTH = 1;
    private static final int READ_SEQUENCE_LENGTH = 2;
    private static final int READ_PAKAGELENGTH_LENGTH = 4;
    private static final int READ_DATA_CURRENT_CHL_NO_LENGTH = 1;
    private static final int READ_DATA_CODER1_COUNT_LENGTH = 4;
    private static final int READ_DATA_CODER2_COUNT_LENGTH = 4;
    private static final int READ_DATA_GATE1_PEAK_POS_LENGTH = 4;
    private static final int READ_DATA_GATE1_PEAK_AMP_LENGTH = 1;
    private static final int READ_DATA_GATE2_PEAK_POS_LENGTH = 4;
    private static final int READ_DATA_GATE2_PEAK_AMP_LENGTH = 1;
    private static final int READ_DATA_GATE3_PEAK_POS_LENGTH = 4;
    private static final int READ_DATA_GATE3_PEAK_AMP_LENGTH = 1;
    private static final int READ_DATA_GATE4_PEAK_POS_LENGTH = 4;
    private static final int READ_DATA_GATE4_PEAK_AMP_LENGTH = 1;
    private static final int READ_CHECK_LENGTH = 4;
    private static final byte[] READ_INDENTIFY_RESPONSE = {0x01};
    private static final byte[] READ_INDENTIFY_DATA = {0x02};
    private static final byte[] READ_HEAD_VALUE = {(byte) 0x51, (byte) 0x52,
            (byte) 0x53, (byte) 0x54, (byte) 0x55, (byte) 0x56,
            (byte) 0x57, (byte) 0x58, (byte) 0x59, (byte) 0x5A};


    private static final int READ_MAX_LENGTH = 4 * 4096;//USB一次接受的最长字节数


    //USBClient 单例
    private static USBClient singleton;
    //系统参数、各通道参数单例
    private SystemPara systemPara;
    private ChannelPara[] channelParas = new ChannelPara[ChannelPara.CHANNEL_TOTAL_NUM];
    //各通道的数据单例
    private ChannelData[] channelDatas = new ChannelData[ChannelPara.CHANNEL_TOTAL_NUM];


    private static D2xxManager ftD2xx = null;
    private FT_Device ftDevice = null;

    private boolean isOpened = false;//设备是否打开的标志
    private boolean readThreadRun = false;//读线程运行标志
    private ReadDataThread readDataThread;
//    private boolean writeRun = false;//写线程运行标志
//    private WriteParametersThread writeParametersThreadhread;


    public USBClient(Context context) {
        systemPara = SystemPara.getSystemPara(context);
        for (int i = ChannelPara.CHANNEL1; i <= ChannelPara.CHANNEL8; i++) {
            channelParas[i - 1] = ChannelPara.getChannelPara(context, i);
            channelDatas[i - 1] = ChannelData.getChannelData(i);
        }

        try {
            ftD2xx = D2xxManager.getInstance(context);
        } catch (D2xxManager.D2xxException e) {
            e.printStackTrace();
        }

    }

    public static USBClient getInstance(Context context) {
        if (singleton == null) {
            singleton = new USBClient(context);
        }
        return singleton;
    }

    public boolean openDevice(Context context) {
        int devCount = 0;
        String TAG = "USB OPEN DEVICE:";

        devCount = ftD2xx.createDeviceInfoList(context);
        Log.i(TAG, "检测到设备" + devCount);
        if (devCount > 0) {
            D2xxManager.FtDeviceInfoListNode deviceInfoListNode = ftD2xx.getDeviceInfoListDetail(0);
            //打开USB设备
            ftDevice = ftD2xx.openByIndex(context, 0);
            //配置USB工作在同步FIFO模式
            ftDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_SYNC_FIFO);

            if (ftDevice == null) {
                Log.e(TAG, "USB 设备打开失败");
                isOpened = false;
                return false;
            } else {
                Log.i(TAG, "USB 设备打开成功");
                isOpened = true;
                return true;
            }
        } else {
            Log.e(TAG, "没有设备可以打开");
            isOpened = false;
            return false;
        }
    }

    public void closeDevice() {
        if (ftDevice != null) {
            synchronized (ftDevice) {
                if (true == ftDevice.isOpen()) {
                    ftDevice.close();
                    isOpened = false;
                }
            }
        }
        Log.i("USB Close DEVICE", "CLosE");

    }

    public boolean isOpened() {
        return isOpened;
    }

    //给下位机配置参数
    public boolean writeParameters() {
        String TAG = "USB WRITE Parameters:";

        if (isOpened == false) {
            Log.e(TAG, "USB设备没有打开，不能写入");
            return false;
        }
        Log.i(TAG, "开始写入");
        new WriteParametersThread().start();
        return true;
    }

    public void readDataStart() {
            readDataThread = new ReadDataThread();
            readThreadRun = true;
            readDataThread.run();
    }

    public void readDataStop() {
        readThreadRun = false;
    }

    private class WriteParametersThread extends Thread {
        String TAG = "USB WRITTING THREAD:";

        @Override
        public synchronized void run() {
            Log.i(TAG, "进入写线程");
            int checksum_int = 0; //校验和
            //发送包头
            ftDevice.write(PARA_HEAD_VALUE, PARA_HEAD_LENGTH);
            //发送系统参数
            ftDevice.write(systemPara.getREPEAT_PERIOD_BYTES(), SystemPara.REPEAT_PERIOD_LENGTH);
            ftDevice.write(systemPara.getHIGH_VOLTAGE_BYTES(), SystemPara.HIGH_VOLTAGE_LENGTH);
            ftDevice.write(systemPara.getCHANNEL_FLAG_BYTES(), SystemPara.CHANNEL_FLAG_LENGTH);
            ftDevice.write(systemPara.getWORK_MODE_BYTES(), SystemPara.WORK_MODE_LENGTH);
            ftDevice.write(systemPara.getSCAN_ACCURACY_BYTES(), SystemPara.SCAN_ACCURACY_LENGTH);
            ftDevice.write(systemPara.getENCODER_HANDLE_BYTES(), SystemPara.ENCODER_HANDLE_LENGTH);
            ftDevice.write(systemPara.getSTATUSLED_BYTES(), SystemPara.STATUSLED_LENGTH);
            ftDevice.write(new byte[RESERVED_SYSTEM_LENGTH], RESERVED_SYSTEM_LENGTH);
            //求校验和
            checksum_int = addIntAndBytes(checksum_int, systemPara.getREPEAT_PERIOD_BYTES());
            checksum_int = addIntAndBytes(checksum_int, systemPara.getHIGH_VOLTAGE_BYTES());
            checksum_int = addIntAndBytes(checksum_int, systemPara.getCHANNEL_FLAG_BYTES());
            checksum_int = addIntAndBytes(checksum_int, systemPara.getWORK_MODE_BYTES());
            checksum_int = addIntAndBytes(checksum_int, systemPara.getSCAN_ACCURACY_BYTES());
            checksum_int = addIntAndBytes(checksum_int, systemPara.getENCODER_HANDLE_BYTES());
            checksum_int = addIntAndBytes(checksum_int, systemPara.getSTATUSLED_BYTES());
            //发送通道参数
            for (int i = ChannelPara.CHANNEL1 - 1; i < ChannelPara.CHANNEL8; i++) {
                ftDevice.write(channelParas[i].getTRIG_PULSE_WIDE_BYTES(), ChannelPara.TRIG_PULSE_WIDE_LENGTH);
                ftDevice.write(channelParas[i].getSAMPLE_DELAYS_BYTES(), ChannelPara.SAMPLE_DELAYS_LENGTH);
                ftDevice.write(channelParas[i].getSAMPLE_DEPTHS_BYTES(), ChannelPara.SAMPLE_DEPTHS_LENGTH);
                ftDevice.write(channelParas[i].getGAIN_BAND_SELECTS_BYTES(), ChannelPara.GAIN_BAND_SELECTS_LENGTH);
                ftDevice.write(channelParas[i].getDAC_DATAS_BYTES(), ChannelPara.DAC_DATAS_LENGTH);
                ftDevice.write(channelParas[i].getDEMODU_SELECTS_BYTES(), ChannelPara.DEMODU_SELECTS_LENGTH);
                ftDevice.write(channelParas[i].getFILTER_BANDS_BYTES(), ChannelPara.FILTER_BANDS_LENGTH);
                ftDevice.write(channelParas[i].getGATE1_DET_POSS_BYTES(), ChannelPara.GATE1_DET_POSS_LENGTH);
                ftDevice.write(channelParas[i].getGATE1_DET_WIDTHS_BYTES(), ChannelPara.GATE1_DET_WIDTHS_LENGTH);
                ftDevice.write(channelParas[i].getGATE2_DET_POSS_BYTES(), ChannelPara.GATE2_DET_POSS_LENGTH);
                ftDevice.write(channelParas[i].getGATE2_DET_WIDTHS_BYTES(), ChannelPara.GATE2_DET_WIDTHS_LENGTH);
                ftDevice.write(channelParas[i].getGATE3_DET_POSS_BYTES(), ChannelPara.GATE3_DET_POSS_LENGTH);
                ftDevice.write(channelParas[i].getGATE3_DET_WIDTHS_BYTES(), ChannelPara.GATE3_DET_WIDTHS_LENGTH);
                ftDevice.write(channelParas[i].getGATE4_DET_POSS_BYTES(), ChannelPara.GATE4_DET_POSS_LENGTH);
                ftDevice.write(channelParas[i].getGATE4_DET_WIDTHS_BYTES(), ChannelPara.GATE4_DET_WIDTHS_LENGTH);
                ftDevice.write(new byte[RESERVED_CHANNEL_LENGTH], RESERVED_CHANNEL_LENGTH);
                //求校验和
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getTRIG_PULSE_WIDE_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getSAMPLE_DELAYS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getSAMPLE_DEPTHS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getGAIN_BAND_SELECTS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getDAC_DATAS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getDEMODU_SELECTS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getFILTER_BANDS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getGATE1_DET_POSS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getGATE1_DET_WIDTHS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getGATE2_DET_POSS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getGATE2_DET_WIDTHS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getGATE3_DET_POSS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getGATE3_DET_WIDTHS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getGATE4_DET_POSS_BYTES());
                checksum_int = addIntAndBytes(checksum_int, channelParas[i].getGATE4_DET_WIDTHS_BYTES());
            }
            //发送校验和
            byte[] checkSum = new byte[PARA_CHECK_LENGTH];
            for (int i = 0; i < PARA_CHECK_LENGTH; i++) {
                checkSum[i] = (byte) ((checksum_int >> (i * 8)) & 0xff);
            }
            ftDevice.write(checkSum, PARA_CHECK_LENGTH);
            Log.i(TAG, "完成写USB设备");
        }
    }

    private int addIntAndBytes(int a, byte[] b) {
        for (int i = 0; i < b.length; i++) {
            a = a + b[i];
        }
        return a;
    }

    private class ReadDataThread extends Thread {
        String TAG = "USB READ THREAD:";
        byte[] head = new byte[READ_HEAD_LENGTH];
        byte[] identifier = new byte[READ_INDENTIFY_LENGTH];
        byte[] response = new byte[READ_RESPONSE_LENGTH];
        byte[] sequence = new byte[READ_SEQUENCE_LENGTH];
        byte[] pakageLength = new byte[READ_PAKAGELENGTH_LENGTH];
        byte[] currentChannelNumber = new byte[READ_DATA_CURRENT_CHL_NO_LENGTH];
        byte[] AScanData;
        byte[] coder1Count = new byte[READ_DATA_CODER1_COUNT_LENGTH];
        byte[] coder2Count = new byte[READ_DATA_CODER2_COUNT_LENGTH];
        byte[] gate1PeakPos = new byte[READ_DATA_GATE1_PEAK_POS_LENGTH];
        byte[] gate1PeakAmp = new byte[READ_DATA_GATE1_PEAK_AMP_LENGTH];
        byte[] gate2PeakPos = new byte[READ_DATA_GATE2_PEAK_POS_LENGTH];
        byte[] gate2PeakAmp = new byte[READ_DATA_GATE2_PEAK_AMP_LENGTH];
        byte[] gate3PeakPos = new byte[READ_DATA_GATE3_PEAK_POS_LENGTH];
        byte[] gate3PeakAmp = new byte[READ_DATA_GATE3_PEAK_AMP_LENGTH];
        byte[] gate4PeakPos = new byte[READ_DATA_GATE4_PEAK_POS_LENGTH];
        byte[] gate4PeakAmp = new byte[READ_DATA_GATE4_PEAK_AMP_LENGTH];
        byte[] checkSum = new byte[READ_CHECK_LENGTH];

        int sleepms = 1000;//使读线程睡眠，让出CPU给其他线程的时间

        int sampleDepthInt = 0;

        ReadDataThread() {
            this.setPriority(Thread.MAX_PRIORITY);

        }

        @Override
        public synchronized void run() {

            while (readThreadRun) {
                // TODO: 2017/7/21 加入校验和
                Log.i(TAG, "进入读线程");

                while (ftDevice.getQueueStatus() < READ_HEAD_LENGTH) {
                    sleepReadThread();
                }

                ftDevice.read(head, READ_HEAD_LENGTH);
                Log.i(TAG, "读到包头");

                if (false == Arrays.equals(head, READ_HEAD_VALUE)) continue;
                while (ftDevice.getQueueStatus() < READ_INDENTIFY_LENGTH) sleepReadThread();
                ftDevice.read(identifier, READ_INDENTIFY_LENGTH);
                if (Arrays.equals(identifier, READ_INDENTIFY_RESPONSE)) {
                    readResponse();
                } else if (Arrays.equals(identifier, READ_INDENTIFY_DATA)) {
                    readChannelData();
                } else {
                    continue;
                }
                Log.i(TAG, "完成一次读数据");

            }

        }

        private void readResponse() {
            while (ftDevice.getQueueStatus() < READ_RESPONSE_LENGTH) sleepReadThread();
            ftDevice.read(response, READ_RESPONSE_LENGTH);
            if (Arrays.equals(response, new byte[]{0x01})) {
                Log.i(TAG, "配置成功");
                return;
            } else if (Arrays.equals(response, new byte[]{0x02})) {
                writeParameters();//校验失败 重新发送
            } else {
                return;
            }
        }

        private void readChannelData() {
            int myCheckSum = 0;
            while (ftDevice.getQueueStatus() < READ_SEQUENCE_LENGTH) {
                sleepReadThread();
            }

            ftDevice.read(sequence, READ_SEQUENCE_LENGTH);
            while (ftDevice.getQueueStatus() < READ_PAKAGELENGTH_LENGTH) sleepReadThread();
            ftDevice.read(pakageLength, READ_PAKAGELENGTH_LENGTH);

            while (ftDevice.getQueueStatus() < READ_DATA_CURRENT_CHL_NO_LENGTH) sleepReadThread();
            ftDevice.read(currentChannelNumber, READ_DATA_CURRENT_CHL_NO_LENGTH);

            //读取A扫数据
            sampleDepthInt = convertBytesToInt(pakageLength) - 29;
            AScanData = new byte[sampleDepthInt];
            int a = sampleDepthInt / READ_MAX_LENGTH;
            int b = sampleDepthInt % READ_MAX_LENGTH;
            byte[] c = new byte[READ_MAX_LENGTH];
            for (int i = 0; i < a; i++) {
                while (ftDevice.getQueueStatus() < READ_MAX_LENGTH) sleepReadThread();
                ftDevice.read(c, READ_MAX_LENGTH);
                System.arraycopy(c, 0, AScanData, READ_MAX_LENGTH * i, READ_MAX_LENGTH);
            }
            while (ftDevice.getQueueStatus() < b) ;
            ftDevice.read(c, b);
            System.arraycopy(c, 0, AScanData, READ_MAX_LENGTH * a, b);

            while (ftDevice.getQueueStatus() < READ_DATA_CODER1_COUNT_LENGTH) sleepReadThread();
            ftDevice.read(coder1Count, READ_DATA_CODER1_COUNT_LENGTH);
            while (ftDevice.getQueueStatus() < READ_DATA_CODER2_COUNT_LENGTH) sleepReadThread();
            ftDevice.read(coder2Count, READ_DATA_CODER2_COUNT_LENGTH);

            while (ftDevice.getQueueStatus() < READ_DATA_GATE1_PEAK_POS_LENGTH) sleepReadThread();
            ftDevice.read(gate1PeakPos, READ_DATA_GATE1_PEAK_POS_LENGTH);
            while (ftDevice.getQueueStatus() < READ_DATA_GATE1_PEAK_AMP_LENGTH) sleepReadThread();
            ftDevice.read(gate1PeakAmp, READ_DATA_GATE1_PEAK_AMP_LENGTH);
            while (ftDevice.getQueueStatus() < READ_DATA_GATE2_PEAK_POS_LENGTH) sleepReadThread();
            ftDevice.read(gate2PeakPos, READ_DATA_GATE2_PEAK_POS_LENGTH);
            while (ftDevice.getQueueStatus() < READ_DATA_GATE2_PEAK_AMP_LENGTH) sleepReadThread();
            ftDevice.read(gate2PeakAmp, READ_DATA_GATE2_PEAK_AMP_LENGTH);
            while (ftDevice.getQueueStatus() < READ_DATA_GATE3_PEAK_POS_LENGTH) sleepReadThread();
            ftDevice.read(gate3PeakPos, READ_DATA_GATE3_PEAK_POS_LENGTH);
            while (ftDevice.getQueueStatus() < READ_DATA_GATE3_PEAK_AMP_LENGTH) sleepReadThread();
            ftDevice.read(gate3PeakAmp, READ_DATA_GATE3_PEAK_AMP_LENGTH);
            while (ftDevice.getQueueStatus() < READ_DATA_GATE4_PEAK_POS_LENGTH) sleepReadThread();
            ftDevice.read(gate4PeakPos, READ_DATA_GATE4_PEAK_POS_LENGTH);
            while (ftDevice.getQueueStatus() < READ_DATA_GATE4_PEAK_AMP_LENGTH) sleepReadThread();
            ftDevice.read(gate4PeakAmp, READ_DATA_GATE4_PEAK_AMP_LENGTH);

            while (ftDevice.getQueueStatus() < READ_CHECK_LENGTH) sleepReadThread();
            ftDevice.read(checkSum, READ_CHECK_LENGTH);

            myCheckSum += convertBytesToInt(currentChannelNumber);
            myCheckSum += convertBytesToInt(AScanData);
            myCheckSum += convertBytesToInt(coder1Count);
            myCheckSum += convertBytesToInt(coder2Count);
            myCheckSum += convertBytesToInt(gate1PeakPos);
            myCheckSum += convertBytesToInt(gate1PeakAmp);
            myCheckSum += convertBytesToInt(gate2PeakPos);
            myCheckSum += convertBytesToInt(gate2PeakAmp);
            myCheckSum += convertBytesToInt(gate3PeakPos);
            myCheckSum += convertBytesToInt(gate3PeakAmp);
            myCheckSum += convertBytesToInt(gate4PeakPos);
            myCheckSum += convertBytesToInt(gate4PeakAmp);

            if (Arrays.equals(checkSum, convertIntToBytes(myCheckSum, READ_CHECK_LENGTH))) {
                updateChannelData();
            } else {
                return;
            } 
        }

        private void updateChannelData() {
            // TODO: 2017/7/24 验证这一点对不对
            int currentChannel = convertBytesToInt(currentChannelNumber) - 1;//当前收到的数据通道
            channelDatas[currentChannel].setCURRENT_CHL_NO(currentChannel + 1);
            channelDatas[currentChannel].setSAMPLE_DEPTH(sampleDepthInt);
            channelDatas[currentChannel].setASCAN_DATA(AScanData);
            channelDatas[currentChannel].setCODER1_COUNT(convertBytesToInt(coder1Count));
            channelDatas[currentChannel].setCODER2_COUNT(convertBytesToInt(coder2Count));
            channelDatas[currentChannel].setGATE1_PEAK_POS(convertBytesToInt(gate1PeakPos));
            channelDatas[currentChannel].setGATE1_PEAK_AMP(convertBytesToInt(gate1PeakAmp));
            channelDatas[currentChannel].setGATE2_PEAK_POS(convertBytesToInt(gate2PeakPos));
            channelDatas[currentChannel].setGATE2_PEAK_AMP(convertBytesToInt(gate2PeakAmp));
            channelDatas[currentChannel].setGATE3_PEAK_POS(convertBytesToInt(gate3PeakPos));
            channelDatas[currentChannel].setGATE3_PEAK_AMP(convertBytesToInt(gate3PeakAmp));
            channelDatas[currentChannel].setGATE4_PEAK_POS(convertBytesToInt(gate4PeakPos));
            channelDatas[currentChannel].setGATE4_PEAK_AMP(convertBytesToInt(gate4PeakAmp));
        }

        private int convertBytesToInt(byte[] b) {
            int a = 0;
            int c = 1;
            for (int i = 0; i < b.length; i++) {
                for (int j = 0; j < i; j++) {
                    c *= 256;
                }
                a += b[i] * c;
            }
            return a;
        }

        private void sleepReadThread() {
            try {
                Thread.sleep(sleepms);
                Log.i(TAG, "没什么可读的" + ftDevice.getQueueStatus());
            } catch (InterruptedException e) {
                Log.e(TAG, "Error in Sleep");
                e.printStackTrace();
            }

        }
    }


    private byte[] convertIntToBytes(int integer, int length) {
        byte[] data = new byte[length];
        for (int i =0 ;i<length;i++) {
            data[i] = (byte) ((integer >> (8 * i)) & 0xFF);
        }
        return data;
    }

    final Handler handler =  new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {

            }
            else if(msg.what == 1) {

            }
            else if(msg.what == 2) {

            }
        }
    };
}
