package com.a601.multichannel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.util.Arrays;
import java.util.concurrent.ThreadFactory;

import static com.a601.multichannel.TypeConvertUtil.addIntAndBytes;
import static com.a601.multichannel.TypeConvertUtil.convertBytesToInt;
import static com.a601.multichannel.TypeConvertUtil.convertIntToBytes;

/**
 * Created by ylx on 2017/7/20.
 */

public class USBClient {



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
    private WriteParametersThread writeParametersThreadhread;


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
//        Log.i(TAG, "检测到设备" + devCount);
        if (devCount > 0) {
            D2xxManager.FtDeviceInfoListNode deviceInfoListNode = ftD2xx.getDeviceInfoListDetail(0);
            //打开USB设备
            ftDevice = ftD2xx.openByIndex(context, 0);
            //配置USB工作在同步FIFO模式
            ftDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_SYNC_FIFO);

            if (ftDevice == null) {
//                Log.e(TAG, "USB 设备打开失败");
                isOpened = false;
                return false;
            } else {
//                Log.i(TAG, "USB 设备打开成功");
                isOpened = true;
                return true;
            }
        } else {
//            Log.e(TAG, "没有设备可以打开");
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
//        Log.i("USB Close DEVICE", "CLosE");

    }

    public boolean isOpened() {
        return isOpened;
    }

    //给下位机配置参数
    public boolean writeParameters() {
        String TAG = "USB WRITE Parameters:";

        if (isOpened == false) {
//            Log.e(TAG, "USB设备没有打开，不能写入");
            return false;
        }
//        Log.i(TAG, "开始写入");

        if (writeParametersThreadhread != null) {
            while (writeParametersThreadhread.isAlive()) {
            }
            writeParametersThreadhread = new WriteParametersThread();
            writeParametersThreadhread.start();
            return true;
        }
        writeParametersThreadhread = new WriteParametersThread();
        writeParametersThreadhread.start();
        return true;
    }

    public void readDataStart() {
        String TAG = "USB READ Data";

        if (isOpened == false) {
//            Log.e(TAG, "USB设备没有打开，不能读数据");
            return;
        }
        Log.i(TAG, "开始读数据");
        if (readDataThread != null) {
            if (readDataThread.isAlive()) {
                return;
            }
            readThreadRun = true;
            readDataThread = new ReadDataThread();
            readDataThread.start();
            return;
        }
        readDataThread = new ReadDataThread();
        readThreadRun = true;
        readDataThread.start();
    }

    public void readDataStop() {
        readThreadRun = false;
    }

    private class WriteParametersThread extends Thread {
        String TAG = "USB WRITTING THREAD:";
        private static final int PARA_HEAD_LENGTH = 4;
        private static final int PARA_DATA_LENGTH = 502;
        private static final int PARA_CHECK_LENGTH = 2;
        private static final int RESERVED_SYSTEM_LENGTH = 10;
        private static final int RESERVED_CHANNEL_LENGTH = 14;
        private final byte[] PARA_HEAD_VALUE = {(byte) 0xF0, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3};
        byte[] sendData = new byte[PARA_HEAD_LENGTH + PARA_DATA_LENGTH + PARA_CHECK_LENGTH];

        @Override
        public synchronized void run() {
//            Log.i(TAG, "进入写线程");
            int checksum_int = 0; //校验和
            int currentPos = 0;
            //发送包头
            System.arraycopy(PARA_HEAD_VALUE, 0, sendData, currentPos, PARA_HEAD_LENGTH);
            currentPos += PARA_HEAD_LENGTH;
            //发送系统参数
            System.arraycopy(systemPara.getREPEAT_PERIOD_BYTES(), 0, sendData, currentPos, SystemPara.REPEAT_PERIOD_LENGTH);
            currentPos += SystemPara.REPEAT_PERIOD_LENGTH;
            System.arraycopy(systemPara.getHIGH_VOLTAGE_BYTES(), 0, sendData, currentPos, SystemPara.HIGH_VOLTAGE_LENGTH);
            currentPos += SystemPara.HIGH_VOLTAGE_LENGTH;
            System.arraycopy(systemPara.getCHANNEL_FLAG_BYTES(), 0, sendData, currentPos, SystemPara.CHANNEL_FLAG_LENGTH);
            currentPos += SystemPara.CHANNEL_FLAG_LENGTH;
            System.arraycopy(systemPara.getWORK_MODE_BYTES(), 0, sendData, currentPos, SystemPara.WORK_MODE_LENGTH);
            currentPos += SystemPara.WORK_MODE_LENGTH;
            System.arraycopy(systemPara.getSCAN_ACCURACY_BYTES(), 0, sendData, currentPos, SystemPara.SCAN_ACCURACY_LENGTH);
            currentPos += SystemPara.SCAN_ACCURACY_LENGTH;
            System.arraycopy(systemPara.getENCODER_HANDLE_BYTES(), 0, sendData, currentPos, SystemPara.ENCODER_HANDLE_LENGTH);
            currentPos += SystemPara.ENCODER_HANDLE_LENGTH;
            System.arraycopy(systemPara.getSTATUSLED_BYTES(), 0, sendData, currentPos, SystemPara.STATUSLED_LENGTH);
            currentPos += SystemPara.STATUSLED_LENGTH;
            System.arraycopy(new byte[RESERVED_SYSTEM_LENGTH], 0, sendData, currentPos, RESERVED_SYSTEM_LENGTH);
            currentPos += RESERVED_SYSTEM_LENGTH;
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
                System.arraycopy(channelParas[i].getTRIG_PULSE_WIDE_BYTES(), 0, sendData, currentPos, ChannelPara.TRIG_PULSE_WIDE_LENGTH);
                currentPos += ChannelPara.TRIG_PULSE_WIDE_LENGTH;
                System.arraycopy(channelParas[i].getSAMPLE_DELAYS_BYTES(), 0, sendData, currentPos, ChannelPara.SAMPLE_DELAYS_LENGTH);
                currentPos += ChannelPara.SAMPLE_DELAYS_LENGTH;
                System.arraycopy(channelParas[i].getSAMPLE_DEPTHS_BYTES(), 0, sendData, currentPos, ChannelPara.SAMPLE_DEPTHS_LENGTH);
                currentPos += ChannelPara.SAMPLE_DEPTHS_LENGTH;
                System.arraycopy(channelParas[i].getGAIN_BAND_SELECTS_BYTES(), 0, sendData, currentPos, ChannelPara.GAIN_BAND_SELECTS_LENGTH);
                currentPos += ChannelPara.GAIN_BAND_SELECTS_LENGTH;
                System.arraycopy(channelParas[i].getDAC_DATAS_BYTES(), 0, sendData, currentPos, ChannelPara.DAC_DATAS_LENGTH);
                currentPos += ChannelPara.DAC_DATAS_LENGTH;
                System.arraycopy(channelParas[i].getDEMODU_SELECTS_BYTES(), 0, sendData, currentPos, ChannelPara.DEMODU_SELECTS_LENGTH);
                currentPos += ChannelPara.DEMODU_SELECTS_LENGTH;
                System.arraycopy(channelParas[i].getFILTER_BANDS_BYTES(), 0, sendData, currentPos, ChannelPara.FILTER_BANDS_LENGTH);
                currentPos += ChannelPara.FILTER_BANDS_LENGTH;
                System.arraycopy(channelParas[i].getGATE1_DET_POSS_BYTES(), 0, sendData, currentPos, ChannelPara.GATE1_DET_POSS_LENGTH);
                currentPos += ChannelPara.GATE1_DET_POSS_LENGTH;
                System.arraycopy(channelParas[i].getGATE1_DET_WIDTHS_BYTES(), 0, sendData, currentPos, ChannelPara.GATE1_DET_WIDTHS_LENGTH);
                currentPos += ChannelPara.GATE1_DET_WIDTHS_LENGTH;
                System.arraycopy(channelParas[i].getGATE2_DET_POSS_BYTES(), 0, sendData, currentPos, ChannelPara.GATE2_DET_POSS_LENGTH);
                currentPos += ChannelPara.GATE2_DET_POSS_LENGTH;
                System.arraycopy(channelParas[i].getGATE2_DET_WIDTHS_BYTES(), 0, sendData, currentPos, ChannelPara.GATE2_DET_WIDTHS_LENGTH);
                currentPos += ChannelPara.GATE2_DET_WIDTHS_LENGTH;
                System.arraycopy(channelParas[i].getGATE3_DET_POSS_BYTES(), 0, sendData, currentPos, ChannelPara.GATE3_DET_POSS_LENGTH);
                currentPos += ChannelPara.GATE3_DET_POSS_LENGTH;
                System.arraycopy(channelParas[i].getGATE3_DET_WIDTHS_BYTES(), 0, sendData, currentPos, ChannelPara.GATE3_DET_WIDTHS_LENGTH);
                currentPos += ChannelPara.GATE3_DET_WIDTHS_LENGTH;
                System.arraycopy(channelParas[i].getGATE4_DET_POSS_BYTES(), 0, sendData, currentPos, ChannelPara.GATE4_DET_POSS_LENGTH);
                currentPos += ChannelPara.GATE4_DET_POSS_LENGTH;
                System.arraycopy(channelParas[i].getGATE4_DET_WIDTHS_BYTES(), 0, sendData, currentPos, ChannelPara.GATE4_DET_WIDTHS_LENGTH);
                currentPos += ChannelPara.GATE4_DET_WIDTHS_LENGTH;
                System.arraycopy(new byte[RESERVED_CHANNEL_LENGTH], 0, sendData, currentPos, RESERVED_CHANNEL_LENGTH);
                currentPos += RESERVED_CHANNEL_LENGTH;
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
            byte[] checkSum = convertIntToBytes(checksum_int, PARA_CHECK_LENGTH);
            System.arraycopy(checkSum, 0, sendData, currentPos, PARA_CHECK_LENGTH);
            currentPos += PARA_CHECK_LENGTH;

            ftDevice.write(sendData, PARA_HEAD_LENGTH + PARA_DATA_LENGTH + PARA_CHECK_LENGTH);
//            Log.i(TAG, "完成写USB设备" + checksum_int + "length:" + currentPos);
        }
    }


    private class ReadDataThread extends Thread {
        String TAG = "USB READ THREAD:";
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
        private final byte[] READ_INDENTIFY_RESPONSE = {0x01};
        private final byte[] READ_INDENTIFY_DATA = {0x02};
        private final byte[] READ_HEAD_VALUE = {(byte) 0x51, (byte) 0x52,
                (byte) 0x53, (byte) 0x54, (byte) 0x55, (byte) 0x56,
                (byte) 0x57, (byte) 0x58, (byte) 0x59, (byte) 0x5A};


        private static final int READ_MAX_LENGTH = 4096 * 4;//USB一次接受的最长字节数
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

        int sleepms = 0;//使读线程睡眠，让出CPU给其他线程的时间

        int sampleDepthInt = 0;
        int a, b;
        byte[] c = new byte[READ_MAX_LENGTH];


        ReadDataThread() {
            this.setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public synchronized void run() {

            while (readThreadRun) {
//                Log.i(TAG, "进入读线程");

                while (ftDevice.getQueueStatus() < READ_HEAD_LENGTH) {
                }

                ftDevice.read(head, READ_HEAD_LENGTH);

                if (false == Arrays.equals(head, READ_HEAD_VALUE)) {
                    continue;
                }
//                Log.i(TAG, "读到包头");
                while (ftDevice.getQueueStatus() < READ_INDENTIFY_LENGTH) ;
                ftDevice.read(identifier, READ_INDENTIFY_LENGTH);
                if (Arrays.equals(identifier, READ_INDENTIFY_RESPONSE)) {
                    readResponse();
                } else if (Arrays.equals(identifier, READ_INDENTIFY_DATA)) {
                    readChannelData();
                } else {
                    continue;
                }
//                Log.i(TAG, "完成一次读数据");
//                sleepReadThread();
            }
            readThreadRun = false;
        }

        private void readResponse() {
            while (ftDevice.getQueueStatus() < READ_RESPONSE_LENGTH) ;
            ftDevice.read(response, READ_RESPONSE_LENGTH);
            if (Arrays.equals(response, new byte[]{0x01})) {
//                Log.i(TAG, "配置成功");
                return;
            } else if (Arrays.equals(response, new byte[]{0x02})) {
                writeParameters();//校验失败 重新发送
            } else {
                return;
            }
        }

        private void readChannelData() {
//            Log.e(TAG, "readChannelDATA");
            int myCheckSum = 0;
            while (ftDevice.getQueueStatus() < READ_SEQUENCE_LENGTH + READ_PAKAGELENGTH_LENGTH + READ_DATA_CURRENT_CHL_NO_LENGTH) {
            }
            ftDevice.read(sequence, READ_SEQUENCE_LENGTH);
            ftDevice.read(pakageLength, READ_PAKAGELENGTH_LENGTH);
            ftDevice.read(currentChannelNumber, READ_DATA_CURRENT_CHL_NO_LENGTH);

            Log.e(TAG, "channel_no:" + convertBytesToInt(currentChannelNumber) + "seq:" + convertBytesToInt(sequence) + "sample_depth" + channelParas[convertBytesToInt(currentChannelNumber)].getSAMPLE_DEPTHS());
//            Log.e(TAG, "channel_no:" + convertBytesToInt(currentChannelNumber) + "sampleDepth" + sampleDepthInt + (pakageLength[0] & 0xff) + (pakageLength[1] & 0xff) + (pakageLength[2] & 0xff) + (pakageLength[3] & 0xff));
            //读取A扫数据
            sampleDepthInt = convertBytesToInt(pakageLength) - 29;
            AScanData = new byte[sampleDepthInt];
            a = sampleDepthInt / READ_MAX_LENGTH;
            b = sampleDepthInt % READ_MAX_LENGTH;
            for (int i = 0; i < a; i++) {
                while (ftDevice.getQueueStatus() < READ_MAX_LENGTH) ;
                ftDevice.read(c, READ_MAX_LENGTH);
                System.arraycopy(c, 0, AScanData, READ_MAX_LENGTH * i, READ_MAX_LENGTH);
            }
            while (ftDevice.getQueueStatus() < b) ;
            ftDevice.read(c, b);
            System.arraycopy(c, 0, AScanData, READ_MAX_LENGTH * a, b);

            while (ftDevice.getQueueStatus() < 29 + 4) {
            }
            ftDevice.read(coder1Count, READ_DATA_CODER1_COUNT_LENGTH);
            ftDevice.read(coder2Count, READ_DATA_CODER2_COUNT_LENGTH);
            ftDevice.read(gate1PeakPos, READ_DATA_GATE1_PEAK_POS_LENGTH);
            ftDevice.read(gate1PeakAmp, READ_DATA_GATE1_PEAK_AMP_LENGTH);
            ftDevice.read(gate2PeakAmp, READ_DATA_GATE2_PEAK_AMP_LENGTH);
            ftDevice.read(gate2PeakPos, READ_DATA_GATE2_PEAK_POS_LENGTH);
            ftDevice.read(gate3PeakPos, READ_DATA_GATE3_PEAK_POS_LENGTH);
            ftDevice.read(gate3PeakAmp, READ_DATA_GATE3_PEAK_AMP_LENGTH);
            ftDevice.read(gate4PeakPos, READ_DATA_GATE4_PEAK_POS_LENGTH);
            ftDevice.read(gate4PeakAmp, READ_DATA_GATE4_PEAK_AMP_LENGTH);
            ftDevice.read(checkSum, READ_CHECK_LENGTH);

            myCheckSum = addIntAndBytes(myCheckSum, currentChannelNumber);
            myCheckSum = addIntAndBytes(myCheckSum, AScanData);
            myCheckSum = addIntAndBytes(myCheckSum, coder1Count);
            myCheckSum = addIntAndBytes(myCheckSum, coder2Count);
            myCheckSum = addIntAndBytes(myCheckSum, gate1PeakPos);
            myCheckSum = addIntAndBytes(myCheckSum, gate1PeakAmp);
            myCheckSum = addIntAndBytes(myCheckSum, gate2PeakPos);
            myCheckSum = addIntAndBytes(myCheckSum, gate2PeakAmp);
            myCheckSum = addIntAndBytes(myCheckSum, gate3PeakPos);
            myCheckSum = addIntAndBytes(myCheckSum, gate3PeakAmp);
            myCheckSum = addIntAndBytes(myCheckSum, gate4PeakPos);
            myCheckSum = addIntAndBytes(myCheckSum, gate4PeakAmp);

            if (Arrays.equals(checkSum, convertIntToBytes(myCheckSum, READ_CHECK_LENGTH))) {
                updateChannelData();
            } else {
                AScanData = null;
                return;
            }
        }

        private void updateChannelData() {
            int currentChannel = convertBytesToInt(currentChannelNumber);//当前收到的数据通道从零开始
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
            AScanData = null;
        }


        private void sleepReadThread() {
            try {
                Thread.sleep(sleepms);
//                Log.i(TAG, "Sleeping...." + ftDevice.getQueueStatus());
            } catch (InterruptedException e) {
//                Log.e(TAG, "Error in Sleep");
                e.printStackTrace();
            }

        }
    }


}
