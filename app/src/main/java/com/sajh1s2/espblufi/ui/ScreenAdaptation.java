package com.sajh1s2.espblufi.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.WindowManager;

public class ScreenAdaptation {
    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() { // from class: com.saj.localconnection.utils.ScreenAdaptation.1
        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityDestroyed(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityPaused(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityResumed(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityStarted(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityStopped(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityCreated(Activity activity, Bundle bundle) {
            ScreenAdaptation.resetDensity(activity, ScreenAdaptation.this.mWidth, ScreenAdaptation.this.mHeight);
        }
    };
    private Application mApplication;
    private float mHeight;
    private float mWidth;

    public ScreenAdaptation(Application application, float f, int i) {
        this.mApplication = application;
        this.mWidth = f;
        this.mHeight = i;
    }

    public void register() {
        resetDensity(this.mApplication, this.mWidth, this.mHeight);
        this.mApplication.registerActivityLifecycleCallbacks(this.activityLifecycleCallbacks);
    }

    public void unregister() {
        this.mApplication.getResources().getDisplayMetrics().setToDefaults();
        this.mApplication.unregisterActivityLifecycleCallbacks(this.activityLifecycleCallbacks);
    }

    /* JADX INFO: Access modifiers changed from: private */
    @SuppressLint("WrongConstant")
    public static void resetDensity(Context context, float f, float f2) {
        Point point = new Point();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getSize(point);
        context.getResources().getDisplayMetrics().density = (point.x / f) * 2.0f;
        context.getResources().getDisplayMetrics().density = (point.y / f2) * 2.0f;
        context.getResources().getDisplayMetrics().scaledDensity = (point.x / f) * 2.0f;
        context.getResources().getDisplayMetrics().scaledDensity = (point.y / f2) * 2.0f;
    }
}
