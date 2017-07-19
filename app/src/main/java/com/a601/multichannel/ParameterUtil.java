package com.a601.multichannel;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

/**
 * Created by ylx on 2017/7/19.
 * 读取SharedPreference--config.xml中的Bool_Init,判断程序是否加载过默认参数
 * 若没有：则将默认参数保存在SharedPreferences里
 * SharedPreferences文件名：
 *      system-----系统参数
 *      channel1----通道1工作参数
 *      channel2----通道2工作参数
 *      channel3----通道3工作参数
 *      channel4----通道4工作参数
 * 在应用程序中，若对工作参数有修改应保存在SharedPreferences中
 *
 */

public class ParameterUtil {

    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;

    private static boolean isInit(Context context) {
        pref = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        if (pref.getBoolean("Bool_Init", false)) {
            return true;
        } else {
            return false;
        }
    }

    public static void initParameter(Context context) {
        Resources res = context.getResources();
        if (isInit(context) == true) {
            return ;
        }

        editor = context.getSharedPreferences("system", Context.MODE_PRIVATE).edit();
        editor.putInt("REPEAT_PERIOD", res.getInteger(R.integer.REPEAT_PERIOD));
        editor.putInt("HIGH_VOLTAGE", res.getInteger(R.integer.HIGH_VOLTAGE));
        editor.putInt("CHANNEL_FLAG", res.getInteger(R.integer.CHANNEL_FLAG));
        editor.putInt("WORK_MODE", res.getInteger(R.integer.WORK_MODE));
        editor.putInt("SCAN_ACCURACY", res.getInteger(R.integer.SCAN_ACCURACY));
        editor.putInt("ENCODER_HANDLE", res.getInteger(R.integer.ENCODER_HANDLE));
        editor.putInt("STATUSLED", res.getInteger(R.integer.STATUSLED));
        editor.apply();

        for (int i =1; i<=4 ; i++) {
            editor = context.getSharedPreferences("channel"+i, Context.MODE_PRIVATE).edit();
            editor.putInt("TRIG_PULSE_WIDE", res.getInteger(R.integer.TRIG_PULSE_WIDE));
            editor.putInt("SAMPLE_DELAY", res.getInteger(R.integer.SAMPLE_DELAY));
            editor.putInt("SAMPLE_DEPTH", res.getInteger(R.integer.SAMPLE_DEPTH));
            editor.putInt("GAIN_BAND_SELECT", res.getInteger(R.integer.GAIN_BAND_SELECT));
            editor.putInt("DAC_DATA", res.getInteger(R.integer.DAC_DATA));
            editor.putInt("DEMODU_SELECT", res.getInteger(R.integer.DEMODU_SELECT));
            editor.putInt("FILTER_BAND", res.getInteger(R.integer.FILTER_BAND));
            editor.putInt("GATE1_DET_POS", res.getInteger(R.integer.GATE1_DET_POS));
            editor.putInt("GATE1_DET_WIDTH", res.getInteger(R.integer.GATE1_DET_WIDTH));
            editor.putInt("GATE2_DET_POS", res.getInteger(R.integer.GATE2_DET_POS));
            editor.putInt("GATE2_DET_WIDTH", res.getInteger(R.integer.GATE2_DET_WIDTH));
            editor.putInt("GATE3_DET_POS", res.getInteger(R.integer.GATE3_DET_POS));
            editor.putInt("GATE3_DET_WIDTH", res.getInteger(R.integer.GATE3_DET_WIDTH));
            editor.putInt("GATE4_DET_POS", res.getInteger(R.integer.GATE4_DET_POS));
            editor.putInt("GATE4_DET_WIDTH", res.getInteger(R.integer.GATE4_DET_WIDTH));
            editor.apply();
        }

        editor = context.getSharedPreferences("config", Context.MODE_PRIVATE).edit();
        editor.putBoolean("Bool_Init", true);
        editor.apply();

    }


}
