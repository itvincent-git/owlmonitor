package com.github.itvincentgit;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Choreographer;
import android.view.Display;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;

/**
 * OwlMonitor（猫头鹰监测）
 * <li>检测ui frame的刷新卡顿</li>
 * <li>检测主线程Looper的超时操作</li>
 * <li>简洁的stacktrace信息</li>
 * <li>自定义卡顿信息的处理</li>
 * <li>支持应用在后台时停止监测，减少耗电，后台时调用pause()，前台时调用resume()</li>
 *
 * @author zhongyongsheng
 */
@TargetApi(JELLY_BEAN)
public class OwlMonitor{
    private static final String TAG = "OwlMonitor";
    private static final int NANO_SECOND = 1000000000;
    private static final String THREAD_NAME = "OwlMonitor";
    private static int sSkippedFrameWarningLimit = 30;

    private long mFrameIntervalNanos = (long)(NANO_SECOND / 60f);
    private long mLastFrameTime = Long.MAX_VALUE;
    private OwlMonitorCallback mCallback;
    private Handler mHandler;
    private MessageLoggingMonitor mMessageLoggingMonitor;
    private OwlFrameCallback mOwlFrameCallback;

    private static final class UIFrameMonitorHolder {
        private static final OwlMonitor sInstance = new OwlMonitor();
    }

    public static synchronized OwlMonitor instance(){
        return UIFrameMonitorHolder.sInstance;
    }

    private OwlMonitor() {
    }

    /**
     * 开始监测
     * @param callback 回调
     */
    public void start(OwlMonitorCallback callback) {
        start(callback, null);
    }

    /**
     * 开始监测
     * @param callback 回调
     * @param config 配置信息
     */
    public void start(OwlMonitorCallback callback, OwlConfig config){
        try {
            if (Build.VERSION.SDK_INT >= JELLY_BEAN) {
                mCallback = callback;

                if (config != null) {
                    if (config.handler != null) {
                        mHandler = config.handler;
                    }
                }
                if (mHandler == null) {
                    HandlerThread handlerThread = new HandlerThread(THREAD_NAME);
                    handlerThread.start();
                    mHandler = new Handler(handlerThread.getLooper());
                }

                mOwlFrameCallback = new OwlFrameCallback();
                mFrameIntervalNanos = (long)(NANO_SECOND / getRefreshRate());
                initSystemProperties();
                postFrameCallback();
                mMessageLoggingMonitor = new MessageLoggingMonitor(mFrameIntervalNanos * sSkippedFrameWarningLimit,
                        mCallback, mHandler);

            }
        }catch (Throwable t){
            Log.e(TAG, "Init error.", t);
        }
    }

    /**
     * 暂停监控
     */
    public void pause() {
        if (Build.VERSION.SDK_INT >= JELLY_BEAN) {
            try {
                Choreographer choreographer = Choreographer.getInstance();
                choreographer.removeFrameCallback(mOwlFrameCallback);
                mMessageLoggingMonitor.pause();
            } catch (Throwable t) {
                Log.e(TAG, "Pause error.", t);
            }
        }
    }

    /**
     * 恢复监控
     */
    public void resume() {
        if (Build.VERSION.SDK_INT >= JELLY_BEAN) {
            try {
                mLastFrameTime = Long.MAX_VALUE;
                postFrameCallback();
                mMessageLoggingMonitor.resume();
            } catch (Throwable t) {
                Log.e(TAG, "Resume error.", t);
            }
        }
    }

    /**
     * 取系统参数，多少帧为卡顿
     */
    private void initSystemProperties(){
        try {
            Class c = Class.forName("android.os.SystemProperties");
            Method m = c.getMethod("getInt", new Class[]{String.class, int.class});
            sSkippedFrameWarningLimit = (Integer) m.invoke(null, "debug.choreographer.skipwarning", 30);
        } catch (Exception e) {
            Log.e(TAG, "Init SystemProperties error.", e);
            sSkippedFrameWarningLimit = 30;
        }
    }

    /**
     * 取屏幕刷新频率
     * @return
     */
    private float getRefreshRate() {
        try {
            Class c = Class.forName("android.hardware.display.DisplayManagerGlobal");
            Method getInstance = c.getMethod("getInstance");
            Object instance = getInstance.invoke(null);

            Method getDisplayInfo = c.getMethod("getDisplayInfo", int.class);
            Object displayInfo = getDisplayInfo.invoke(instance, Display.DEFAULT_DISPLAY);

            Class displayInfoClass = Class.forName("android.view.DisplayInfo");
            Field refreshRate = displayInfoClass.getField("refreshRate");
            float refreshRateValue = refreshRate.getFloat(displayInfo);
            return refreshRateValue;
        } catch (Exception e) {
            Log.w(TAG, "Get refresh rate error, used default value instead.");
            return 60f;
        }
    }

    private void postFrameCallback(){
        Choreographer choreographer = Choreographer.getInstance();
        choreographer.postFrameCallback(mOwlFrameCallback);
    }

    private void onSkippedUIFrame(final long diff) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final long skippedFrames = diff / mFrameIntervalNanos;
                    final long diffMs = diff / 1000000;
                    mCallback.onSkippedUIFrames(skippedFrames, diffMs);
                } catch (Exception e) {
                    Log.e(TAG, "onSkippedUIFrame error", e);
                }
            }
        };
        if (mHandler != null) {
            Message.obtain(mHandler, runnable).sendToTarget();
        } else {
            runnable.run();
        }
    }

    class OwlFrameCallback implements Choreographer.FrameCallback {

        /**
         * UI渲染新一帧会回调一次
         * @param frameTimeNanos
         */
        @Override
        public void doFrame(long frameTimeNanos) {

            final long diff = frameTimeNanos - mLastFrameTime;
            mLastFrameTime = frameTimeNanos;

            if (diff > mFrameIntervalNanos * sSkippedFrameWarningLimit) {//2帧之间超过刷新率上限

                if (mCallback != null) {
                    onSkippedUIFrame(diff);
                }
            }

            postFrameCallback();
        }
    }
}
