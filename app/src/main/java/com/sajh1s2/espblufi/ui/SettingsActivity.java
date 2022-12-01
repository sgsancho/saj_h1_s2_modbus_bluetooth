package com.sajh1s2.espblufi.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.sajh1s2.espblufi.R;
import com.sajh1s2.espblufi.app.BaseActivity;
import com.sajh1s2.espblufi.app.BlufiApp;
import com.sajh1s2.espblufi.constants.BlufiConstants;
import com.sajh1s2.espblufi.constants.SettingsConstants;
import com.sajh1s2.espblufi.task.BlufiAppReleaseTask;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import blufi.espressif.BlufiClient;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setHomeAsUpEnable(true);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
        private static final String KEY_MTU_LENGTH = SettingsConstants.PREF_SETTINGS_KEY_MTU_LENGTH;
        private static final String KEY_BLE_PREFIX = SettingsConstants.PREF_SETTINGS_KEY_BLE_PREFIX;

        private BlufiApp mApp;

        private EditTextPreference mMtuPref;
        private EditTextPreference mBlePrefixPref;

        private Preference mVersionCheckPref;
        private Preference mTelegram;
        private Preference mPDF;
        private volatile BlufiAppReleaseTask.ReleaseInfo mAppLatestRelease;

        private long mAppVersionCode;
        private String mAppVersionName;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.blufi_settings, rootKey);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mApp = BlufiApp.getInstance();

            getVersionInfo();
            findPreference(R.string.settings_version_key).setSummary(mAppVersionName);
            findPreference(R.string.settings_blufi_version_key).setSummary(BlufiClient.VERSION);

            mMtuPref = findPreference(R.string.settings_mtu_length_key);
            int mtuLen = (int) mApp.settingsGet(KEY_MTU_LENGTH, BlufiConstants.DEFAULT_MTU_LENGTH);
            mMtuPref.setOnPreferenceChangeListener(this);
            if (mtuLen >= BlufiConstants.MIN_MTU_LENGTH && mtuLen <= BlufiConstants.MAX_MTU_LENGTH) {
                mMtuPref.setSummary(String.valueOf(mtuLen));
            }
            PreferenceCategory blufiCategory = findPreference(R.string.settings_category_blufi_key);
            blufiCategory.removePreference(mMtuPref);

            mBlePrefixPref = findPreference(R.string.settings_ble_prefix_key);
            mBlePrefixPref.setOnPreferenceChangeListener(this);
            String blePrefix = (String) mApp.settingsGet(KEY_BLE_PREFIX, BlufiConstants.BLUFI_PREFIX);
            mBlePrefixPref.setSummary(blePrefix);

            mTelegram = findPreference(R.string.settings_telegram_key);
            mPDF = findPreference(R.string.settings_pdf_key);

        }

        public <T extends Preference> T findPreference(@StringRes int res) {
            return findPreference(getString(res));
        }

        private void getVersionInfo() {
            try {
                PackageInfo pi = requireActivity().getPackageManager()
                        .getPackageInfo(requireActivity().getPackageName(), 0);
                mAppVersionName = pi.versionName;
                mAppVersionCode = pi.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                mAppVersionName = getString(R.string.string_unknown);
                mAppVersionCode = -1;
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference == mVersionCheckPref) {
                if (mAppLatestRelease == null) {
                    mVersionCheckPref.setEnabled(false);
                    checkAppLatestRelease();
                } else {
                    downloadLatestRelease();
                }
                return true;
            }
            else if(preference == mTelegram) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                builder.setTitle("Aviso");
                builder.setMessage("Esta aplicación se ha desarrollado para facilitar el acceso a los datos a tiempo real por modbus del Inversor SAJ H1 S2 sin necesidad de retirar el Dongle del puerto RS232. \n" +
                        "Este trabajo ha sido gracias a la colaboración del grupo de Telegram https://t.me/saj_nooficialoriginal \n" +
                        "No es una aplicación oficial y podría dejar de funcionar en cualquier momento. \n" +
                        "Úsala bajo tu propia responsabilidad. Desconocemos si afecta en algún sentido al rendimiento y fiabilidad de su instalación fotovoltaica.\n" +
                        "\n" +
                        "Para más información entra al grupo de Telegram https://t.me/saj_nooficialoriginal");
                builder.setPositiveButton("Aceptar", null);
                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
            else if(preference == mPDF)
            {
                File pdfFile = new File("res/raw/instrucciones_saj_h1s2_modbus.pdf");
                Uri path = Uri.fromFile(pdfFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
            return super.onPreferenceTreeClick(preference);
        }

        /*private void CopyReadAssets()
        {
            AssetManager assetManager = getAssets();

            InputStream in = null;
            OutputStream out = null;
            File file = new File(getFilesDir(), "git.pdf");
            try
            {
                in = assetManager.open("git.pdf");
                out = openFileOutput(file.getName(), Context.MODE_WORLD_READABLE);

                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e)
            {
                Log.e("tag", e.getMessage());
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
                    Uri.parse("file://" + getFilesDir() + "/git.pdf"),
                    "application/pdf");

            startActivity(intent);
        }

        private void copyFile(InputStream in, OutputStream out) throws IOException
        {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, read);
            }
        }*/

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mMtuPref) {
                String lenStr = newValue.toString();
                int mtuLen = BlufiConstants.DEFAULT_MTU_LENGTH;
                if (!TextUtils.isEmpty(lenStr)) {
                    int newLen = Integer.parseInt(lenStr);
                    mtuLen = Math.min(BlufiConstants.MAX_MTU_LENGTH, Math.max(BlufiConstants.MIN_MTU_LENGTH, newLen));
                }
                mMtuPref.setSummary(String.valueOf(mtuLen));
                mApp.settingsPut(KEY_MTU_LENGTH, mtuLen);
                return true;
            } else if (preference == mBlePrefixPref) {
                String prefix = newValue.toString();
                mBlePrefixPref.setSummary(prefix);
                mApp.settingsPut(KEY_BLE_PREFIX, prefix);
                return true;
            }

            return false;
        }

        private void checkAppLatestRelease() {
            Observable.just(new BlufiAppReleaseTask())
                    .subscribeOn(Schedulers.io())
                    .map(task -> new AtomicReference<>(task.requestLatestRelease()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(reference -> {
                        mVersionCheckPref.setEnabled(true);

                        mAppLatestRelease = null;
                        BlufiAppReleaseTask.ReleaseInfo latestRelease = reference.get();
                        if (latestRelease == null) {
                            mVersionCheckPref.setSummary(R.string.settings_upgrade_check_failed);
                            return;
                        } else if (latestRelease.getVersionCode() < 0) {
                            mVersionCheckPref.setSummary(R.string.settings_upgrade_check_not_found);
                            return;
                        }

                        long latestVersion = latestRelease.getVersionCode();
                        if (latestVersion > mAppVersionCode) {
                            mVersionCheckPref.setSummary(R.string.settings_upgrade_check_disciver_new);
                            mAppLatestRelease = latestRelease;

                            new AlertDialog.Builder(requireActivity())
                                    .setTitle(R.string.settings_upgrade_dialog_title)
                                    .setMessage(R.string.settings_upgrade_dialog_message)
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setPositiveButton(R.string.settings_upgrade_dialog_upgrade,
                                            (dialog, which) -> downloadLatestRelease())
                                    .show();
                        } else {
                            mVersionCheckPref.setSummary(R.string.settings_upgrade_check_current_latest);
                        }
                    })
                    .subscribe();

        }

        private void downloadLatestRelease() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(mAppLatestRelease.getDownloadUrl());
            intent.setData(uri);
            startActivity(intent);
        }
    }  // Fragment end
} // Activity end
