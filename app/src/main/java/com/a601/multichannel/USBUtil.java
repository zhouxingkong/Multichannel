package com.a601.multichannel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;


/**
 * Created by xingkong on 2017/3/4.
 */

public class USBUtil {

    private static D2xxManager ftD2xx = null;
    private FT_Device ftDevice = null;
    private D2xxManager.DriverParameters d2xxDrvParameter;
    private readDevice_thread readThread;
    private writeDevice_thread writeThread;

    //接收参数
    public int iavailable = 0;
    byte[] readData;
    byte[] writeData;
    public static final int readLength = 100;

    int sendByteCount = 0;

    //发送参数

    public USBUtil(){
        readData = new byte[readLength];
        writeData = new byte[1];
        writeData[0]=1;
    }

    public void getInstance(Context context){
        try {
            ftD2xx = D2xxManager.getInstance(context);		//获取manager实例
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }
    }

    public void openDevice(Context context){

        int devCount = 0;

        devCount = ftD2xx.createDeviceInfoList(context);
        //Log.i("Misc Function Test ", "Device number = " + Integer.toString(devCount));
        System.out.println("检测到设备"+devCount);
        if (devCount > 0) {
            D2xxManager.FtDeviceInfoListNode deviceList = ftD2xx.getDeviceInfoListDetail(0);
            //下面是四种方法打开USB设备，使用一种即可
            ftDevice = ftD2xx.openByIndex(context, 0, d2xxDrvParameter);
            ftDevice = ftD2xx.openBySerialNumber(context, deviceList.serialNumber); //通过序列号打开
            ftDevice = ftD2xx.openByLocation(context, deviceList.location); //通过位置打开
            ftDevice = ftD2xx.openByDescription(context, deviceList.description);   //通过描述打开
        }
        else {
            System.out.println("没有设备可以打开");
        }
    }

    public void readDevice(){
        if(ftDevice == null) {
            System.out.print("没连接设备，所以不能读");
            return;
        }

        //readDeviceThread_start = true;
        readThread = new readDevice_thread(handler);	//开启新线程保存文件
        readThread.start();

    }
    public void writeDevice(){
        if(ftDevice == null) {
            System.out.println("设备没有打开，不能写入");
            return;
        }

        //if( uart_configured == false || DevCount <= 0) { return; }
        //TimeText.setText("Testing...");
        //uartInterface.StartWriteFileThread();
        writeThread = new writeDevice_thread(handler);		//开启新线程发送文件
        writeThread.start();
    }
    //读USB数据的线程
    private class readDevice_thread  extends Thread {
        Handler mHandler;

        readDevice_thread(Handler h) {
            mHandler = h;
            this.setPriority(Thread.MAX_PRIORITY);
        }
        @Override
        public void run() {
            sendByteCount = 0;
            //ftDevice.setLatencyTimer((byte) 16);
            // ftDev.setReadTimeout(1000);
            //ftDevice.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
            while (true) {
                iavailable = ftDevice.getQueueStatus();
                if (iavailable > 0) {
                    if (iavailable > 100) iavailable = 100;
                    //System.out.println("接收缓存数据个数为" + iavailable);
                    ftDevice.read(readData, iavailable);
                    System.out.println("接收到" + (readData[iavailable - 1] & 0xFF));

                    sendByteCount += iavailable;
                } else {
                    System.out.println("没啥数据可以接收");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

            }
        }
    }
    //写USB数据的线程
    private class writeDevice_thread  extends Thread {
        Handler mHandler;

        writeDevice_thread(Handler h){
            mHandler = h;
            this.setPriority(Thread.MAX_PRIORITY);
        }
        @Override
        public void run() {

            //ftDevice.setLatencyTimer((byte)16);     //延迟定时器
            //ftDevice.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

            while(true) {
                //ftDevice.write(writeData, readcount);		//将数据发送到device
                ftDevice.write(writeData, 1);
                //sendByteCount += readcount;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}

                writeData[0]++;

            }

        }
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
