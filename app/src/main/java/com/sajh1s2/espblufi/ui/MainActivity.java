package com.sajh1s2.espblufi.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sajh1s2.espblufi.R;
import com.sajh1s2.espblufi.app.BlufiApp;
import com.sajh1s2.espblufi.app.BlufiLog;
import com.sajh1s2.espblufi.constants.BlufiConstants;
import com.sajh1s2.espblufi.constants.SettingsConstants;
import com.sajh1s2.espblufi.databinding.MainActivityBinding;
import com.sajh1s2.espblufi.databinding.MainBleItemBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {
    private static final long TIMEOUT_SCAN = 4000L;

    private static final int REQUEST_PERMISSION = 0x01;
    private static final int REQUEST_BLUFI = 0x10;

    private static final int MENU_SETTINGS = 0x01;

    private final BlufiLog mLog = new BlufiLog(getClass());

    private MainActivityBinding mBinding;

    private List<ScanResult> mBleList;
    private BleAdapter mBleAdapter;

    private Map<String, ScanResult> mDeviceMap;
    private ScanCallback mScanCallback;
    private String mBlufiFilter;
    private volatile long mScanStartTime;

    private ExecutorService mThreadPool;
    private Future<Boolean> mUpdateFuture;

    private TextView sin_resultados;

    public android.app.AlertDialog dialog;

    private FirebaseAnalytics mFirebaseAnalytics;

    private String TAG = "pilone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mBlufiFilter = (String) BlufiApp.getInstance().settingsGet(SettingsConstants.PREF_SETTINGS_KEY_BLE_PREFIX,
                BlufiConstants.BLUFI_PREFIX);

        mThreadPool = Executors.newSingleThreadExecutor();


        mBinding.refreshLayout.setColorSchemeResources(R.color.colorAccent);
        mBinding.refreshLayout.setOnRefreshListener(this::scan);

        mBleList = new LinkedList<>();
        mBleAdapter = new BleAdapter();
        mBinding.recyclerView.setAdapter(mBleAdapter);

        mDeviceMap = new HashMap<>();
        mScanCallback = new ScanCallback();

        List<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionList.add(Manifest.permission.BLUETOOTH_SCAN);
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        ActivityCompat.requestPermissions(
                this,
                permissionList.toArray(new String[0]),
                REQUEST_PERMISSION
        );

        int SDK_INT = android.os.Build.VERSION.SDK_INT;

        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Where you get exception write that code inside this.
        }

        sin_resultados = (TextView)findViewById(R.id.sin_resultados);

        checkFilterScan();
    }


    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);

        if (isFirstRun){
            // Place your dialog code here to display the dialog


            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Aviso");
            builder.setMessage("Esta aplicación se ha desarrollado para facilitar el acceso a los datos a tiempo real por modbus del Inversor SAJ H1 S2 sin necesidad de retirar el Dongle del puerto RS232. \n" +
                    "Este trabajo ha sido gracias a la colaboración del grupo de Telegram https://t.me/saj_nooficialoriginal \n" +
                    "No es una aplicación oficial y podría dejar de funcionar en cualquier momento. \n" +
                    "Úsala bajo tu propia responsabilidad. Desconocemos si afecta en algún sentido al rendimiento y fiabilidad de su instalación fotovoltaica.\n" +
                    "\n" +
                    "Para más información entra al grupo de Telegram https://t.me/saj_nooficialoriginal");
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                            .edit()
                            .putBoolean("isFirstRun", false)
                            .apply();
                }
            });
            android.app.AlertDialog dialog2 = builder.create();
            dialog2.show();
        }
    }


    @Override
    protected void onResume() {
        mBlufiFilter = (String) BlufiApp.getInstance().settingsGet(SettingsConstants.PREF_SETTINGS_KEY_BLE_PREFIX,
                BlufiConstants.BLUFI_PREFIX);
        if(dialog!=null) {
            if (!dialog.isShowing()) {
                checkFilterScan();
            }
        }
        else {
            checkFilterScan();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopScan();
        mThreadPool.shutdownNow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int size = permissions.length;
        for (int i = 0; i < size; ++i) {
            String permission = permissions[i];
            int grant = grantResults[i];

            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    mBinding.refreshLayout.setRefreshing(true);
                }
            }
        }
    }
    private void checkFilterScan() {
        if (mBlufiFilter==null || mBlufiFilter.equals(""))
        {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Configuración inicial");
                builder.setMessage("Para conectar al bluetooth, debes configurar el nombre del dispositivo. Ejemplo: BlueLink:12345");

                builder.setPositiveButton("Ajustes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                        MainActivity.this.startActivity(intent);
                    }
                });
                builder.setCancelable(false);
                dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            checkFirstRun();
        }
        else {
            scan();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BLUFI) {
            mBinding.refreshLayout.setRefreshing(true);
            return;
        }
        //checkFilterScan();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_SETTINGS, 0, R.string.main_menu_settings);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == MENU_SETTINGS) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scan() {

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
            if (!adapter.isEnabled() || scanner == null) {
                Toast.makeText(this, R.string.main_bt_disable_msg, Toast.LENGTH_SHORT).show();
                mBinding.refreshLayout.setRefreshing(false);
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check location enable
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                boolean locationEnable = locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager);
                if (!locationEnable) {
                    Toast.makeText(this, R.string.main_location_disable_msg, Toast.LENGTH_SHORT).show();
                    mBinding.refreshLayout.setRefreshing(false);
                    return;
                }
            }

            sin_resultados.setVisibility(View.VISIBLE);
            sin_resultados.setText("Buscando el dongle: " + mBlufiFilter + " ...");

            mDeviceMap.clear();
            mBleList.clear();
            mBleAdapter.notifyDataSetChanged();
            mBlufiFilter = (String) BlufiApp.getInstance().settingsGet(SettingsConstants.PREF_SETTINGS_KEY_BLE_PREFIX,
                    BlufiConstants.BLUFI_PREFIX);
            mScanStartTime = SystemClock.elapsedRealtime();

            mLog.d("Start scan ble");

            scanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),
                    mScanCallback);
            mUpdateFuture = mThreadPool.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }

                    long scanCost = SystemClock.elapsedRealtime() - mScanStartTime;
                    if (scanCost > TIMEOUT_SCAN) {
                        break;
                    }

                    onIntervalScanUpdate(false);
                }


                BluetoothLeScanner inScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
                if (inScanner != null) {
                    inScanner.stopScan(mScanCallback);
                }
                onIntervalScanUpdate(true);
                mLog.d("Scan ble thread is interrupted");

                if (mDeviceMap.size() == 0) {
                    sin_resultados.setText("No se ha encontrado ningún dispostivo Bluetooth con identificador "+ mBlufiFilter+" \n\nRevisa el nombre en \"Settings\" y asegurate de estar al alcance del Bluetooth");
                }
                else
                {
                    sin_resultados.setVisibility(View.INVISIBLE);
                }

                return true;
            });
    }

    private void stopScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner != null) {
            scanner.stopScan(mScanCallback);
        }
        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true);
        }
        mLog.d("Stop scan ble");
    }

    private void onIntervalScanUpdate(boolean over) {
        List<ScanResult> devices = new ArrayList<>(mDeviceMap.values());
        Collections.sort(devices, (dev1, dev2) -> {
            Integer rssi1 = dev1.getRssi();
            Integer rssi2 = dev2.getRssi();
            return rssi2.compareTo(rssi1);
        });
        runOnUiThread(() -> {
            mBleList.clear();
            mBleList.addAll(devices);
            mBleAdapter.notifyDataSetChanged();

            if (over) {
                mBinding.refreshLayout.setRefreshing(false);
            }
        });
    }

    private void gotoDeviceSAJ(BluetoothDevice device) {
        //Intent intent = new Intent(com.espressif.espblufi.ui.MainActivity.this, BlufiActivity.class);
        Intent intent = new Intent(MainActivity.this, SajActivity.class);
        intent.putExtra(BlufiConstants.KEY_BLE_DEVICE, device);
        startActivityForResult(intent, REQUEST_BLUFI);

        mDeviceMap.clear();
        mBleList.clear();
        mBleAdapter.notifyDataSetChanged();
    }


    private class BleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ScanResult scanResult;
        MainBleItemBinding binding;

        BleHolder(MainBleItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
            binding.itemContent.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            stopScan();
            gotoDeviceSAJ(scanResult.getDevice());
        }

    }

    private class ScanCallback extends android.bluetooth.le.ScanCallback {

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            sin_resultados.setVisibility(View.GONE);
            for (ScanResult result : results) {
                onLeScan(result);
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onLeScan(result);
        }

        private void onLeScan(ScanResult scanResult) {
            String name = scanResult.getDevice().getName();
            if (!TextUtils.isEmpty(mBlufiFilter)) {
                if (name == null || !name.startsWith(mBlufiFilter)) {
                    return;
                }
                if (scanResult.getDevice().getName().equals(mBlufiFilter)) {
                    stopScan();
                    gotoDeviceSAJ(scanResult.getDevice());
                }
            }

            mDeviceMap.put(scanResult.getDevice().getAddress(), scanResult);


        }
    }

    private class BleAdapter extends RecyclerView.Adapter<BleHolder> {

        @NonNull
        @Override
        public BleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MainBleItemBinding binding = MainBleItemBinding.inflate(getLayoutInflater(), parent, false);
            return new BleHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull BleHolder holder, int position) {
            ScanResult scanResult = mBleList.get(position);
            holder.scanResult = scanResult;

            BluetoothDevice device = scanResult.getDevice();
            String name = device.getName() == null ? getString(R.string.string_unknown) : device.getName();
            holder.binding.text1.setText(name);

            SpannableStringBuilder info = new SpannableStringBuilder();
            info.append("Mac:").append(device.getAddress())
                    .append(" RSSI:").append(String.valueOf(scanResult.getRssi()));
            info.setSpan(new ForegroundColorSpan(0xFF9E9E9E), 0, 21, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            info.setSpan(new ForegroundColorSpan(0xFF8D6E63), 21, info.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.binding.text2.setText(info);
        }

        @Override
        public int getItemCount() {
            return mBleList.size();
        }
    }
}
