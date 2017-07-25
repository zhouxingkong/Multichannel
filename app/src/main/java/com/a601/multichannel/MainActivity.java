package com.a601.multichannel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {
    USBClient mUsbClient;
    Switch USBSwitch;
    Switch redSwitch;
    Switch yellowSwitch;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParameterUtil.initParameter(this);
        mUsbClient = USBClient.getInstance(this);
        USBSwitch = (Switch) findViewById(R.id.switch_usb);
        redSwitch = (Switch) findViewById(R.id.switch_red);
        yellowSwitch = (Switch) findViewById(R.id.switch_yellow);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsbClient.readDataStart();

            }
        });

        USBSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mUsbClient.openDevice(MainActivity.this);
                    mUsbClient.readDataStart();
//                    USBSwitch.setEnabled(false);
                } else {
                    mUsbClient.readDataStop();
//                    mUsbClient.readDataStart();
                    mUsbClient.closeDevice();
                }
            }
        });

        redSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    int a =  SystemPara.getSystemPara(MainActivity.this).getSTATUSLED() | 0x02;
                    SystemPara.getSystemPara(MainActivity.this).setSTATUSLED(a);
                    ChannelPara.getChannelPara(MainActivity.this, 1).setSAMPLE_DEPTHS(100);
                } else {
                    int a =  SystemPara.getSystemPara(MainActivity.this).getSTATUSLED() & ~0x02;
                    SystemPara.getSystemPara(MainActivity.this).setSTATUSLED(a);
                }
            }
        });

        yellowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    int a =  SystemPara.getSystemPara(MainActivity.this).getSTATUSLED() | 0x01;
                    SystemPara.getSystemPara(MainActivity.this).setSTATUSLED(a);
                    SystemPara.getSystemPara(MainActivity.this).setCHANNEL_FLAG(0xff);
                } else {
                    int a =  SystemPara.getSystemPara(MainActivity.this).getSTATUSLED() & ~0x01;
                    SystemPara.getSystemPara(MainActivity.this).setSTATUSLED(a);
                }
            }
        });

    }
}
