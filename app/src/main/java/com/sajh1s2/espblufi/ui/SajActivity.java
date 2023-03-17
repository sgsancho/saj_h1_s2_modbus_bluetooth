package com.sajh1s2.espblufi.ui;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sajh1s2.espblufi.R;
import com.sajh1s2.espblufi.app.BaseActivity;
import com.sajh1s2.espblufi.app.BlufiLog;
import com.sajh1s2.espblufi.constants.BlufiConstants;
import com.sajh1s2.espblufi.databinding.SajActivityBinding;
import com.sajh1s2.espblufi.databinding.SajContentBinding;
import com.sajh1s2.espblufi.databinding.SajMessageItemBinding;
import com.sajh1s2.espblufi.saj.BluFiUtils;
import com.sajh1s2.espblufi.saj.HexUtil;
import com.sajh1s2.espblufi.saj.LocalUtils;
import com.sajh1s2.espblufi.tcpserver.MessageDebugEvent;
import com.sajh1s2.espblufi.tcpserver.ServerService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import blufi.espressif.BlufiCallback;
import blufi.espressif.BlufiClient;
import blufi.espressif.params.BlufiConfigureParams;
import blufi.espressif.params.BlufiParameter;
import blufi.espressif.response.BlufiScanResult;
import blufi.espressif.response.BlufiStatusResponse;
import blufi.espressif.response.BlufiVersionResponse;

import static com.sajh1s2.espblufi.saj.BluFiUtils.bytesToHex;
import static java.lang.Math.abs;


@SuppressLint("MissingPermission")
public class SajActivity extends BaseActivity {
    private static final int REQUEST_CONFIGURE = 0x20;

    private final BlufiLog mLog = new BlufiLog(getClass());

    private BluetoothDevice mDevice;
    private static BlufiClient mBlufiClient;
    private volatile boolean mConnected;

    private List<Message> mMsgList;
    private MsgAdapter mMsgAdapter;

    private SajContentBinding mContent;

    public static Socket socket;
    public static String modbus_message;
    public static byte[] modbus_frame_response;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SajActivityBinding mBinding = SajActivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        setHomeAsUpEnable(true);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        mDevice = getIntent().getParcelableExtra(BlufiConstants.KEY_BLE_DEVICE);
        assert mDevice != null;
        String deviceName = mDevice.getName() == null ? getString(R.string.string_unknown) : mDevice.getName();
        //setTitle(deviceName + "\n" + ip + ":8080");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(ip + ":8080");
        getSupportActionBar().setSubtitle(deviceName);

        mContent = mBinding.content;

        mMsgList = new LinkedList<>();
        mMsgAdapter = new MsgAdapter();
        mContent.recyclerView.setAdapter(mMsgAdapter);

        startService(new Intent(getBaseContext(), ServerService.class));

        connect();

     //   Thread.setDefaultUncaughtExceptionHandler(new MainActivity.MyExceptionHandler(this, SajActivity.class));

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageDebugEvent(MessageDebugEvent messageDebug) {
        //Toast.makeText(getApplicationContext(), messageDebug.getMessageDebug(), Toast.LENGTH_LONG).show();
        //updateMessage(messageDebug.getMessageDebug(), false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBlufiClient != null) {
            mBlufiClient.close();
            mBlufiClient = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONFIGURE) {
            if (!mConnected) {
                return;
            }
            if (resultCode == RESULT_OK) {
                BlufiConfigureParams params =
                        (BlufiConfigureParams) data.getSerializableExtra(BlufiConstants.KEY_CONFIGURE_PARAM);
                configure(params);
            }

            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateMessage(String message, boolean isNotificaiton) {
        runOnUiThread(() -> {
            Message msg = new Message();
            msg.text = message;
            msg.isNotification = isNotificaiton;
            mMsgList.add(msg);
            mMsgAdapter.notifyItemInserted(mMsgList.size() - 1);
            mContent.recyclerView.scrollToPosition(mMsgList.size() - 1);
        });
    }

    /**
     * Try to connect device
     */
    private void connect() {

        if (mBlufiClient != null) {
            mBlufiClient.close();
            mBlufiClient = null;
        }

        mBlufiClient = new BlufiClient(getApplicationContext(), mDevice);
        mBlufiClient.setGattCallback(new GattCallback());
        mBlufiClient.setBlufiCallback(new BlufiCallbackMain());
        mBlufiClient.setGattWriteTimeout(BlufiConstants.GATT_WRITE_TIMEOUT);
        mBlufiClient.connect();
    }

    /**
     * Request device disconnect the connection.
     */
    private void disconnectGatt() {

        if (mBlufiClient != null) {
            mBlufiClient.requestCloseConnection();
        }
    }



    /**
     * Go to configure options
     */
    private void configureOptions() {
        Intent intent = new Intent(SajActivity.this, ConfigureOptionsActivity.class);
        startActivityForResult(intent, REQUEST_CONFIGURE);
    }

    /**
     * Request to configure station or softap
     *
     * @param params configure params
     */
    private void configure(BlufiConfigureParams params) {

        mBlufiClient.configure(params);
    }

    /**
     * Request to get device current status
     */
    private void requestDeviceStatus() {

        mBlufiClient.requestDeviceStatus();
    }

    /**
     * Request to get device blufi version
     */
    private void requestDeviceVersion() {

        mBlufiClient.requestDeviceVersion();
    }

    /**
     * Request to get AP list that the device scanned
     */
    private void requestDeviceWifiScan() {

        mBlufiClient.requestDeviceWifiScan();
    }








    private void onGattConnected() {
        mConnected = true;
        runOnUiThread(() -> {

        });
    }

    private void onGattServiceCharacteristicDiscovered() {
        runOnUiThread(() -> {
        });
    }

    private void onGattDisconnected() {
        mConnected = false;
        runOnUiThread(() -> {
            connect();
        });
    }

    /**
     * mBlufiClient call onCharacteristicWrite and onCharacteristicChanged is required
     */
    private class GattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String devAddr = gatt.getDevice().getAddress();
            mLog.d(String.format(Locale.ENGLISH, "onConnectionStateChange addr=%s, status=%d, newState=%d",
                    devAddr, status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        onGattConnected();
                        updateMessage(String.format("Connected %s", devAddr), false);

                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        gatt.close();
                        onGattDisconnected();

                        updateMessage(String.format("Disconnected %s", devAddr), false);
                        break;
                }
            } else {
                gatt.close();
                onGattDisconnected();

                updateMessage(String.format(Locale.ENGLISH, "Disconnect %s, status=%d", devAddr, status),
                        false);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            mLog.d(String.format(Locale.ENGLISH, "onMtuChanged status=%d, mtu=%d", status, mtu));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateMessage(String.format(Locale.ENGLISH, "Set mtu complete, mtu=%d ", mtu), false);
            } else {
                mBlufiClient.setPostPackageLengthLimit(20);
                updateMessage(String.format(Locale.ENGLISH, "Set mtu failed, mtu=%d, status=%d", mtu, status), false);
            }

            onGattServiceCharacteristicDiscovered();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            mLog.d(String.format(Locale.ENGLISH, "onServicesDiscovered status=%d", status));
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
                updateMessage(String.format(Locale.ENGLISH, "Discover services error status %d", status), false);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            mLog.d(String.format(Locale.ENGLISH, "onDescriptorWrite status=%d", status));
            if (descriptor.getUuid().equals(BlufiParameter.UUID_NOTIFICATION_DESCRIPTOR) &&
                    descriptor.getCharacteristic().getUuid().equals(BlufiParameter.UUID_NOTIFICATION_CHARACTERISTIC)) {
                String msg = String.format(Locale.ENGLISH, "Set notification enable %s", (status == BluetoothGatt.GATT_SUCCESS ? " complete" : " failed"));
                updateMessage(msg, false);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
                updateMessage(String.format(Locale.ENGLISH, "WriteChar error status %d", status), false);
            }
        }
    }

    private class BlufiCallbackMain extends BlufiCallback {
        @Override
        public void onGattPrepared(
                BlufiClient client,
                BluetoothGatt gatt,
                BluetoothGattService service,
                BluetoothGattCharacteristic writeChar,
                BluetoothGattCharacteristic notifyChar
        ) {
            if (service == null) {
                mLog.w("Discover service failed");
                gatt.disconnect();
                updateMessage("Discover service failed", false);
                return;
            }
            if (writeChar == null) {
                mLog.w("Get write characteristic failed");
                gatt.disconnect();
                updateMessage("Get write characteristic failed", false);
                return;
            }
            if (notifyChar == null) {
                mLog.w("Get notification characteristic failed");
                gatt.disconnect();
                updateMessage("Get notification characteristic failed", false);
                return;
            }

            updateMessage("Discover service and characteristics success", false);

            int mtu = BlufiConstants.DEFAULT_MTU_LENGTH;
            mLog.d("Request MTU " + mtu);
            boolean requestMtu = gatt.requestMtu(mtu);
            if (!requestMtu) {
                mLog.w("Request mtu failed");
                updateMessage(String.format(Locale.ENGLISH, "Request mtu %d failed", mtu), false);
                onGattServiceCharacteristicDiscovered();
            }
        }

        @Override
        public void onNegotiateSecurityResult(BlufiClient client, int status) {
            if (status == STATUS_SUCCESS) {
                updateMessage("Negotiate security complete", false);
            } else {
                updateMessage("Negotiate security failed， code=" + status, false);
            }
        }

        @Override
        public void onPostConfigureParams(BlufiClient client, int status) {
            if (status == STATUS_SUCCESS) {
                updateMessage("Post configure params complete", false);
            } else {
                updateMessage("Post configure params failed, code=" + status, false);
            }
        }

        @Override
        public void onDeviceStatusResponse(BlufiClient client, int status, BlufiStatusResponse response) {
            if (status == STATUS_SUCCESS) {
                updateMessage(String.format("Receive device status response:\n%s", response.generateValidInfo()),
                        true);
            } else {
                updateMessage("Device status response error, code=" + status, false);
                Toast.makeText(getApplicationContext(), "Device status response error, code=" + status + ". Reconectando...", Toast.LENGTH_LONG).show();
                finish();
            }

        }

        @Override
        public void onDeviceScanResult(BlufiClient client, int status, List<BlufiScanResult> results) {
            if (status == STATUS_SUCCESS) {
                StringBuilder msg = new StringBuilder();
                msg.append("Receive device scan result:\n");
                for (BlufiScanResult scanResult : results) {
                    msg.append(scanResult.toString()).append("\n");
                }
                updateMessage(msg.toString(), true);
            } else {
                updateMessage("Device scan result error, code=" + status, false);
            }
        }

        @Override
        public void onDeviceVersionResponse(BlufiClient client, int status, BlufiVersionResponse response) {
            if (status == STATUS_SUCCESS) {
                updateMessage(String.format("Receive device version: %s", response.getVersionString()),
                        true);

            } else {
                updateMessage("Device version error, code=" + status, false);
            }

        }

        @Override
        public void onPostCustomDataResult(BlufiClient client, int status, byte[] data) {
            String dataStr = gettStringTrama(data);
            String format = "Post data %s %s";
            if (status == STATUS_SUCCESS) {
                updateMessage(String.format(format, dataStr, "complete"), false);
            } else {
                updateMessage("Device status response error, code=" + status, false);
                Toast.makeText(getApplicationContext(), "Post data failed " + dataStr + ". Reconectando...", Toast.LENGTH_LONG).show();
                finish();
                updateMessage(String.format(format, dataStr, "failed"), false);
            }
        }

        private String gettStringTrama(byte[] resultBuff) {
            String trama = "";
            for (int i=0; i<resultBuff.length; i++)
            {
                if (i==0) {
                    trama += resultBuff[i];
                }
                else {
                    trama += ", " + resultBuff[i];
                }
            }
            return trama;
        }

        @Override
        public void onReceiveCustomData(BlufiClient client, int status, byte[] data) {
            debug("5. pilone: onReceiveCustomData");
            if (status == STATUS_SUCCESS) {

                try {
                    String hexStr = bytesToHex(data);
                    int longitud = abs(data[3]);
                    byte[] slice = Arrays.copyOfRange(data, 3, longitud+4);


                    updateMessage(String.format("%s\nReceive custom data for %s:\n%s", new Date(), modbus_message, gettStringTrama(slice)), true);

                    DataOutputStream dataoutputStream = null;
                    String data_binario = "";
                    /*
                    dataoutputStream = new DataOutputStream(socket.getOutputStream());
                    byte[] response = {72, 49, 83, 50, 54, 48, 50, 74, 50, 49, 53, 49, 69, 48, 57, 51, 48, 53, 0, 0};
                    dataoutputStream.write(response); //sending response for client socket request
                    dataoutputStream.close();
                    System.out.println("pilone: En teoría acabo de responder en el socket222222");
                    */
                    if(data[0]==50 && data[1]==1 && data[2]==3) {
                        byte[] slice_copy = Arrays.copyOfRange(data, 3, longitud+4);
                        dataoutputStream = new DataOutputStream(socket.getOutputStream());

                        byte[] combined = new byte[modbus_frame_response.length + slice_copy.length];

                        for (int i = 0; i < combined.length; ++i)
                        {
                            combined[i] = i < modbus_frame_response.length ? modbus_frame_response[i] : slice_copy[i - modbus_frame_response.length];
                        }

                        dataoutputStream.write(combined); //sending response for client socket request
                        //dataoutputStream.flush();
                        //dataoutputStream.close();
                        debug("6. pilone: onReceiveCustomData, response -> " + gettStringTrama(combined));

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch(IllegalArgumentException i){
                    debug("pilone: PETÓ" + i);
                }

            } else {
                updateMessage("Receive custom data error, code=" + status, false);
            }
        }

        @Override
        public void onError(BlufiClient client, int errCode) {
            updateMessage(String.format(Locale.ENGLISH, "Receive error code %d", errCode), false);
            if (errCode == CODE_GATT_WRITE_TIMEOUT) {
                updateMessage("Gatt write timeout", false);
                client.close();
                onGattDisconnected();
            }
        }
    }

    /**
     * Try to post custom data
     */

    public static void postSajData(String modbus_message) {
        debug("4. pilone: postSajData");
        byte[] data = HexUtil.hexStringToBytes(BluFiUtils.exchangeHasCrcModBusData(1, HexUtil.formatHexString(LocalUtils.sendData(modbus_message))));
        mBlufiClient.postCustomData(data);
    }

    public static void debug(String message) {
        EventBus.getDefault().postSticky(new MessageDebugEvent(message));
        System.out.println(message);
    }



/*
    private class BlufiButtonListener implements View.OnClickListener, View.OnLongClickListener {
        private Toast mToast;

        @Override
        public void onClick(View v) {
            if (v == mContent.blufiSaj) {
                if(!mConnected) {
                    connect();
                }
                else {
                    postSajData("");
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mToast != null) {
                mToast.cancel();
            }

            int msgRes = 0;
            if (v == mContent.blufiSaj) {
                msgRes = R.string.blufi_function_custom_saj;
            }

            mToast = Toast.makeText(SajActivity.this, msgRes, Toast.LENGTH_SHORT);
            mToast.show();

            return true;
        }
    }*/

    private static class MsgHolder extends RecyclerView.ViewHolder {
        TextView text1;

        MsgHolder(SajMessageItemBinding binding) {
            super(binding.getRoot());

            text1 = binding.text1;
        }
    }

    private class MsgAdapter extends RecyclerView.Adapter<MsgHolder> {

        @NonNull
        @Override
        public MsgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            SajMessageItemBinding binding = SajMessageItemBinding.inflate(
                    getLayoutInflater(),
                    parent,
                    false
            );
            return new MsgHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MsgHolder holder, int position) {
            Message msg = mMsgList.get(position);
            holder.text1.setText(msg.text);
            holder.text1.setTextColor(msg.isNotification ? Color.RED : Color.BLACK);
        }

        @Override
        public int getItemCount() {
            return mMsgList.size();
        }
    }

    private static class Message {
        String text;
        boolean isNotification;
    }
}
