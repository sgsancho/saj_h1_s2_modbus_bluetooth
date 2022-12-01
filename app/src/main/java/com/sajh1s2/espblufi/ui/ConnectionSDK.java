package com.sajh1s2.espblufi.ui;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

import com.sajh1s2.espblufi.saj.AppLog;

public class ConnectionSDK {
    public static int H;
    public static int W;
    private static Application appContext;
    private static ConnectionSDK connectionSDK;
    private ExecutorService backgroundExecutor;
    private Handler backgroundHandler;

    public static ConnectionSDK getInstance() {
        synchronized (ConnectionSDK.class) {
            if (connectionSDK == null) {
                connectionSDK = new ConnectionSDK();
            }
        }
        return connectionSDK;
    }

    public static Application getAppContext() {
        return appContext;
    }

    public void init(Application application) {
        appContext = application;
        AppLog.d("ConnectionSDK", "==>>onCreate:  " + (System.currentTimeMillis() / 1000));
        new ScreenAdaptation(application, 720.0f, 1280).register();
        getScreen(application);
        this.backgroundHandler = new Handler(Looper.getMainLooper());
        this.backgroundExecutor = Executors.newCachedThreadPool(new ThreadFactory() { // from class: com.saj.localconnection.common.base.ConnectionSDK.1
            private AtomicInteger atomicInteger = new AtomicInteger();

            @Override // java.util.concurrent.ThreadFactory
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "Background executor service #" + this.atomicInteger.getAndIncrement());
                thread.setPriority(1);
                thread.setDaemon(true);
                return thread;
            }
        });
        //ToastUtils.init(application);
        //CrashHandler.getInstance().init(application, this);
    }

    public Handler getHandler() {
        return this.backgroundHandler;
    }

    public void runInBackground(Runnable runnable) {
        this.backgroundExecutor.submit(runnable);
    }

    public void getScreen(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        H = displayMetrics.heightPixels;
        W = displayMetrics.widthPixels;
    }

    public void initUserData(String str, String str2) {
        AppLog.d("baseUrl:" + str);
        AppLog.d("appProjectName:" + str2);
        /*
        User user = new User();
        user.setBaseUrl(str);
        user.setAppProjectName(str2);
        AuthManager.getInstance().setUser(user);
        */
    }
}
