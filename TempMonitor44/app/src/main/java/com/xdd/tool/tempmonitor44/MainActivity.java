package com.xdd.tool.tempmonitor44;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Pair;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MainActivity extends AppCompatActivity {


    private BluetoothAdapter adapter;
    private Set<BluetoothDevice> devices;
    private Set<Handler> handlers = new HashSet<Handler>();
    private Set<SmsSender> smsSenders = new HashSet<SmsSender>();
    private ReadWriteLock smsLocker = new ReentrantReadWriteLock();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public class SmsSender {
        private SmsManager smsManager = null;
        private String phoneNumber = null;
        private String deviceDescription = null;
        private String deviceName = null;
        private boolean bManualSendFlag = false;
        private boolean bAutoSendFlag = false;
        private ReadWriteLock smsLocker = null;
        private String currentSms = null;
        private String previousTemp = null;
        private Date previousDate = null;


        public SmsSender(SmsManager smsManager, String phoneNumber, String deviceDescription, String deviceName,
                         ReadWriteLock smsLocker, boolean bManualSendFlag, boolean bAutoSendFlag) {
            this.smsManager = smsManager;
            this.phoneNumber = phoneNumber;
            this.deviceDescription = deviceDescription;
            this.deviceName = deviceName;
            this.bManualSendFlag = bManualSendFlag;
            this.bAutoSendFlag = bAutoSendFlag;
            this.smsLocker = smsLocker;
        }

        public void setManualSendFlag(boolean bManualSendFlag) {
            this.bManualSendFlag = bManualSendFlag;
        }

        public boolean isManualSendFlagEnabled() {
            return this.bManualSendFlag;
        }

        public void setAutoSendFlag(boolean bAutoSendFlag) {
            this.bAutoSendFlag = bAutoSendFlag;
        }

        public boolean isAutoSendFlagEnabled() {
            return this.bAutoSendFlag;
        }

        public SmsManager getSmsManager() {
            return this.smsManager;
        }

        public String getPhoneNumber() {
            return this.phoneNumber;
        }

        public String getDeviceDescription() {
            return this.deviceDescription;
        }

        public String getDeviceName() {
            return this.deviceName;
        }

        public void setPreviousTemp(String previousTemp) {
            this.previousTemp = previousTemp;
        }

        public String getPreviousTemp() {
            return this.previousTemp;
        }

        public void setCurrentSms(String currentSms) {
            this.currentSms = currentSms;
        }

        public String getCurrentSms() {
            return this.currentSms;
        }

        public Date getPreviousDate() {
            return this.previousDate;
        }

        public void setPreviousDate(Date date) {
            this.previousDate = date;
        }

        public ReadWriteLock getSmsLocker() {
            return smsLocker;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public void scanBlueTooth(View view) {
        //得到BluetoothAdapter对象
        TextView textView = (TextView) findViewById(R.id.blueToothDevices);
        textView.setText("检测蓝牙设备。");
        adapter = BluetoothAdapter.getDefaultAdapter();

        //判断BluetoothAdapter对象是否为空，如果为空，则表明本机没有蓝牙设备
        if (adapter != null) {
            //System.out.println("本机拥有蓝牙设备");
            textView.setText("本机拥有蓝牙设备。");
            //调用isEnabled()方法判断当前蓝牙设备是否可用
            if (!adapter.isEnabled()) {
                //如果蓝牙设备不可用的话,创建一个intent对象,该对象用于启动一个Activity,提示用户启动蓝牙适配器
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            }
            //得到所有已经配对的蓝牙适配器对象
            //Set<BluetoothDevice> devices = adapter.getBondedDevices();
            devices = adapter.getBondedDevices();
            if (devices.size() > 0) {
                textView.setText("");
                int n = 0;
                for (BluetoothDevice device : devices) {
                    //得到BluetoothDevice对象,也就是说得到配对的蓝牙适配器
                    //setText
                    n++;
                    textView.append("设备号：");
                    textView.append(Integer.toString(n));
//                    textView.append("设备名：");
//                    textView.append(device.getName());
//                    textView.append("地址：");
//                    textView.append(device.getAddress());
                    textView.append(TempUtil.getDeviceName(device.getName(), device.getAddress()));
                }
            } else {
                //System.out.println("没有发现配对蓝牙设备");
                textView.setText("没有发现配对蓝牙设备。");
            }
        } else {
            //System.out.println("本机没有蓝牙设备");
            textView.setText("本机没有蓝牙设备。");
        }
    }

    public void connectBlueTooth(View view) {
        TextView textView = (TextView) findViewById(R.id.connectBlueToothStatus);
        EditText editText = (EditText) findViewById(R.id.editExit);
        String[] deviceNoArray = null;
        if (editText != null && editText.getText() != null
                && editText.getText().length() > 0) {
            if (editText.getText().toString().contains(",")) {
                deviceNoArray = editText.getText().toString().split(",");
            } else if (!editText.getText().toString().equals("")) {
                deviceNoArray = new String[1];
                deviceNoArray[0] = editText.getText().toString();
            }
        }

        Set<Pair<Integer, BluetoothDevice>> connectingDevices = new HashSet<Pair<Integer, BluetoothDevice>>();
        if (devices.size() > 0) {
            Integer n = 0;
            for (BluetoothDevice device : devices) {
                n++;
                if (deviceNoArray == null || deviceNoArray.length < 1
                        || isContainedDeviceNo(deviceNoArray, n)) {
                    connectingDevices.add(new Pair<Integer, BluetoothDevice>(n, device));
                }
            }
        }
        if (connectingDevices.isEmpty()) {
            textView.setText("没有可连接蓝牙设备。");
            return;
        }
        textView.setText("连接蓝牙设备数量：" + connectingDevices.size());
        int topMargin = 20;
        for (Pair<Integer, BluetoothDevice> connectingDevice : connectingDevices) {
            BluetoothDevice device = connectingDevice.second;
            //String[] strings = device.getAddress().split(":");
            String deviceDescription = "设备号：" + connectingDevice.first;
            deviceDescription += TempUtil.getDeviceNameDescription(device.getName(), device.getAddress());
            //textView.append(deviceDescription);
            deviceDescription += "：";
            String deviceName = TempUtil.getDeviceName(device.getName(), device.getAddress());
            connectAndReceive(device, topMargin, deviceDescription, deviceName);
            //
            try {
                Thread.sleep(1000);// 延迟
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
            topMargin += 100;
        }
        Handler handler = new Handler();
        handlers.add(handler);
        Button manualButton = (Button) findViewById(R.id.button6);
        SendSmsThread sendSmsThread = new SendSmsThread(smsSenders, smsLocker, handler, manualButton);
        sendSmsThread.start();
    }

    private boolean isContainedDeviceNo(String[] deviceNoArray, Integer n) {
        if (deviceNoArray == null || deviceNoArray.length < 1) {
            return false;
        }
        for (String deviceNo : deviceNoArray) {
            if (deviceNo.trim().equals(String.valueOf(n))) {
                return true;
            }
        }
        return false;
    }

    private void connectAndReceive(BluetoothDevice connectingDevice, int topMargin, String deviceDescription, String deviceName) {
        //UUID MY_UUID = getMyUuid();
        //Standard //SerialPortService ID
        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        BluetoothDevice myDevice = connectingDevice;
        BluetoothSocket mySocket = null;

        TextView tempText = (TextView) findViewById(R.id.connectBlueToothStatus);
//        tempText.append("。。。开始创建连接");
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 10) {
            try {
                mySocket = myDevice
                        .createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        } else {
            try {
                mySocket = myDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        if (mySocket == null) {
            tempText.append("。。。创建连接套接字失败");
            return;
        }
        try {
            mySocket.connect();
        } catch (IOException e) {
            tempText.append("。。。连接设备失败" + e.getMessage());
            try {
                mySocket.close();
            } catch (IOException ee) {
                //ee.printStackTrace();
            }
            return;
        }
//        tempText.append("。。。连接设备成功");
        //开始创建输入流
        InputStream myInputStream = null;
        try {
            myInputStream = mySocket.getInputStream();
        } catch (IOException e) {
            tempText.append("。。。创建输入流失败");
            return;
        }
//        tempText.append("。。。准备读取数据");
        TextView currentView = new TextView(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = topMargin;
        lp.addRule(RelativeLayout.BELOW, R.id.textView);
        currentView.setLayoutParams(lp);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutView);
        layout.addView(currentView);
        EditText editTextPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
        String phoneNumber = editTextPhoneNumber.getText().toString();
        Handler handler = new Handler();
        handlers.add(handler);
        SmsSender smsSender = new SmsSender(SmsManager.getDefault(), phoneNumber, deviceDescription, deviceName, smsLocker, false, false);
        smsSenders.add(smsSender);
        ReadInputStreamThread readThread = new ReadInputStreamThread(myInputStream, handler, currentView, smsSender);
        readThread.start();
//        tempText.append("。。。开始读取数据");
    }

    class ReadInputStreamThread extends Thread {
        private InputStream myInputStream = null;
        private Handler myHandler = null;
        private TextView myTextView = null;
        private SmsSender mySmsSender = null;
        private boolean stopWorker = false;
        final byte delimiter = 10; //This is the ASCII code for a newline character

        public ReadInputStreamThread(InputStream inputStream, Handler handler, TextView textView, SmsSender smsSender) {
            this.myInputStream = inputStream;
            this.myHandler = handler;
            this.myTextView = textView;
            this.mySmsSender = smsSender;
        }

        @Override
        public void run() {
            int readBufferPosition = 0;
            byte[] buffer = new byte[1024];// 缓冲数据流

            while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                myHandler.post(new Runnable() {
                    public void run() {
                        myTextView.setText(mySmsSender.getDeviceDescription());
                        myTextView.append("读取数据中。。。。。。");
                    }
                });
                // 延迟再读取
                try {
                    Thread.sleep(1000);// 延迟
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }

                try {
                    int bytesAvailable = myInputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        myInputStream.read(packetBytes);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;
                                myHandler.post(new SmsRunner(data, mySmsSender, myTextView));
                            } else {
                                buffer[readBufferPosition++] = b;
                            }
                        }
                    } else {
                        myHandler.post(new Runnable() {
                            public void run() {
                                myTextView.setText(mySmsSender.getDeviceDescription());
                                myTextView.append("读取蓝牙不可用。");
                            }
                        });
                    }
                } catch (IOException ex) {
                    myHandler.post(new Runnable() {
                        public void run() {
                            myTextView.setText(mySmsSender.getDeviceDescription());
                            myTextView.append("读取数据失败。");
                        }
                    });
                }
                // 延迟再读取
                try {
                    Thread.sleep(1000);// 延迟
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    public class SmsRunner implements Runnable {
        private TextView myTextView = null;
        private SmsSender mySmsSender = null;
        private String mySms = null;
        private String mySmsText = null;
        private String myTemp = null;

        public SmsRunner(String data, SmsSender smsSender, TextView textView) {
            this.mySmsSender = smsSender;
            this.myTextView = textView;
            this.mySms = generateSms(data);
            this.mySmsText = generateSmsText(data);
        }

        @Override
        public void run() {
            sendSmsBasedOnTempChange();
        }

        private void sendSmsBasedOnTempChange() {
            if (mySms == null || mySmsText == null || myTemp == null) {
                return;
            }
            myTextView.setText(mySmsText);
            mySmsSender.setCurrentSms(mySms);
            if (mySmsSender.isAutoSendFlagEnabled()) {
                boolean needToSend = false;
                //send, if temp is changed more than 1 degree
                if (mySmsSender.getPreviousTemp() == null
                        || !TempUtil.robustEqual(myTemp, mySmsSender.getPreviousTemp())) {
                    needToSend = true;
                }
                if (needToSend) {
                    mySmsSender.setPreviousTemp(myTemp);
//                    myTextView.append("need to send");
//                    myTextView.append(mySmsSender.getPreviousTemp());
                    mySmsSender.getSmsManager().
                            sendTextMessage(mySmsSender.getPhoneNumber(), null, mySms, null, null);
                }
            }
        }

        private String generateSms(String data) {
            if (data == null || !data.contains(";")) {
                return null;
            }
            String datas[] = data.split(";");
            if (datas[0] == null || !datas[0].contains(":")) {
                return null;
            }
            String temp[] = datas[0].split(":");
            this.myTemp = temp[1];
            // "." is escaped character
            String intTemp[] = temp[1].split("\\.");
            if (datas[1] == null || !datas[1].contains(":")) {
                return null;
            }
            String humi[] = datas[1].split(":");
            // "." is escaped character
            String intHumi[] = humi[1].split("\\.");

            String sms = "温度:" + intTemp[0] + ";" + "湿度:" + intHumi[0];
            return mySmsSender.getDeviceName() + ";" + sms;
        }

        private String generateSmsText(String data) {
            if (data == null || !data.contains(";")) {
                return null;
            }
            String datas[] = data.split(";");
            if (datas[0] == null || !datas[0].contains(":")) {
                return null;
            }
            String temp[] = datas[0].split(":");
            this.myTemp = temp[1];
            if (datas[1] == null || !datas[1].contains(":")) {
                return null;
            }
            String humi[] = datas[1].split(":");
            String sms = "温度：" + temp[1] + "；" + "湿度：" + humi[1];
            return mySmsSender.getDeviceDescription() + sms;
        }
    }

    public class SendSmsThread extends Thread {
        private boolean stopRunning = false;
        private Set<SmsSender> smsSenders = null;
        private ReadWriteLock smsLocker = null;
        private Button myManualButton = null;
        private Handler myHandler = null;

        SendSmsThread(Set<SmsSender> smsSenders, ReadWriteLock smsLocker, Handler handler, Button manualButton) {
            this.smsSenders = smsSenders;
            this.smsLocker = smsLocker;
            this.myManualButton = manualButton;
            this.myHandler = handler;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted() && !stopRunning) {
                try {
                    Thread.sleep(10000);// 延迟 10 seconds
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
                smsLocker.writeLock().lock();
                sendSmsManualAndOnTime();
                smsLocker.writeLock().unlock();
            }
        }

        private void sendSmsManualAndOnTime() {

            if (smsSenders == null || smsSenders.isEmpty()) {
                return;
            }
            SmsSender firstSmsSender = null;
            String tmpSms = "";
            for (SmsSender smsSender : smsSenders) {
                if (firstSmsSender == null) {
                    firstSmsSender = smsSender;
                }
                if (smsSender.getCurrentSms() != null && !smsSender.getCurrentSms().isEmpty()) {
                    tmpSms += smsSender.getCurrentSms();
                    tmpSms += ";";
                }
            }
            final SmsSender smsSender = firstSmsSender;
            final String sms = tmpSms;
            final Boolean manualStatus = smsSender.isManualSendFlagEnabled();

            boolean needToSend = false;
            //send, if passed 1 hour
            if (firstSmsSender.isAutoSendFlagEnabled()) {
                Date date = new Date();
                if (TempUtil.moreThanOneHourDelta(firstSmsSender.getPreviousDate(), date)) {
                    needToSend = true;
                    for (SmsSender tmpSmsSender : smsSenders) {
                        tmpSmsSender.setPreviousDate(date);
                    }
                }
            }

            if (firstSmsSender.isManualSendFlagEnabled()) {
                needToSend = true;
                for (SmsSender tmpSmsSender : smsSenders) {
                    tmpSmsSender.setManualSendFlag(false);
                }
            }

            if (needToSend && sms != null && !sms.isEmpty() && !sms.equals("")
                    && smsSender.getSmsManager() != null) {
                myHandler.post(new Runnable() {
                    public void run() {
                        //sms has 70 length limitation
                        if (sms != null && sms.length() > 70) {
                            ArrayList<String> msgs = smsSender.getSmsManager().divideMessage(sms);
                            for (String msg : msgs) {
                                if (msg != null) {
                                    smsSender.getSmsManager().
                                            sendTextMessage(smsSender.getPhoneNumber(), null, msg, null, null);
                                }
                            }
                        } else if (sms != null && sms.length() > 0) {
                            smsSender.getSmsManager().
                                    sendTextMessage(smsSender.getPhoneNumber(), null, sms, null, null);
                        } else {
                            //ignore here
                        }
                    }
                });
            }

            if (manualStatus) {
                myHandler.post(new Runnable() {
                    public void run() {
                        //myManualButton.setText(sms);
                        myManualButton.setText("手动发送短信（已关闭）");
                    }
                });
            }
        }
    }

    public void setAutoSms(View view) {
        smsLocker.writeLock().lock();
        if (!smsSenders.isEmpty()) {
            Button button = (Button) findViewById(R.id.button7);
            Boolean autoStatus = null;
            for (SmsSender smsSender : smsSenders) {
                if (autoStatus == null) {
                    autoStatus = smsSender.isAutoSendFlagEnabled();
                }
                if (autoStatus) {
                    smsSender.setAutoSendFlag(false);
                } else {
                    smsSender.setAutoSendFlag(true);
                }
            }
            if (autoStatus) {
                button.setText("自动发送短信（已关闭）");
            } else {
                button.setText("自动发送短信（已开启）");
            }
        }
        smsLocker.writeLock().unlock();
    }

    public void setManualSms(View view) {
        smsLocker.writeLock().lock();
        if (!smsSenders.isEmpty()) {
            for (SmsSender smsSender : smsSenders) {
                smsSender.setManualSendFlag(true);
            }
            Button button = (Button) findViewById(R.id.button6);
            button.setText("手动发送短信（已开启）");
        }
        smsLocker.writeLock().unlock();
    }
}
