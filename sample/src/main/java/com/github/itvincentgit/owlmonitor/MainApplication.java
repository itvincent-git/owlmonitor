package com.github.itvincentgit.owlmonitor;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.github.itvincentgit.OwlMonitor;
import com.github.itvincentgit.OwlMonitorCallback;

/**
 * @author zhongyongsheng
 */

public class MainApplication extends Application {

    private static final String TAG = "MainApplication";
    private ActivityLifecycleCallbacks mCallback = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            OwlMonitor.instance().resume();
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            OwlMonitor.instance().pause();
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        OwlMonitor.instance().start(new OwlMonitorCallback() {
            @Override
            public void onSkippedUIFrames(long skippedFrames, long skippedDurationMs) {
                Log.i(TAG, String.format("onSkippedUIFrames frame:%d duration:%d ms", skippedFrames, skippedDurationMs));
            }

            @Override
            public void onBlockMainThread(long blockDurationMs) {
                Log.i(TAG, String.format("onBlockMainThread block:%d ms", blockDurationMs));
            }

            @Override
            public void onBlockTrace(String stack) {
                Log.i(TAG, String.format("onBlockTrace stack:%s", stack));

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(mCallback);
        }
    }
}
