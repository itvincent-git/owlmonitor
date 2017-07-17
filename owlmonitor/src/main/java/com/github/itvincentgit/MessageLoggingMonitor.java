package com.github.itvincentgit;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Printer;

/**
 * MainThread线程卡顿监控
 * @author zhongyongsheng
 */
public class MessageLoggingMonitor implements Printer {
    private static final String TAG = "MessageLoggingMonitor";
    public static final String START_PREFIX = ">";
    public static final String END_PREFIX = "<";
    public static int sStackTraceFrom = 1;
    public static int sStackTraceLength = 7;
    protected long mStartTime;
    protected long mEndTime;
    protected long mFrameIntervalMillis;
    protected final OwlMonitorCallback mCallback;
    protected final Handler mHandler;
    protected final Runnable mCheckRunnable = new Runnable() {
        @Override
        public void run() {
            String stack = StackTraceString.getTraceString(sStackTraceFrom,
                    sStackTraceLength,
                    Looper.getMainLooper().getThread());
            if (mCallback != null) {
                onTrace(stack);
            }
        }
    };

    protected MessageLoggingMonitor(long frameIntervalNanos, OwlMonitorCallback callback, Handler handler) {
        mFrameIntervalMillis = frameIntervalNanos / 1000000;
        mCallback = callback;
        mHandler = handler;
        setMessageLogging(this);
    }

    protected static void setMessageLogging(MessageLoggingMonitor monitor) {
        Looper.getMainLooper().setMessageLogging(monitor);
    }

    @Override
    public void println(final String x) {
        if (x.startsWith(START_PREFIX)){
            mStartTime = System.currentTimeMillis();
            startCheck();
        }else if (x.startsWith(END_PREFIX)){
            mEndTime = System.currentTimeMillis();
            endCheck();
        }
        final long timeUsed = mEndTime - mStartTime;
        if (timeUsed > mFrameIntervalMillis) {
            if (mCallback != null) {
                onBlock(timeUsed);
            }
        }
    }

    private void onBlock(final long timeUsed) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mCallback.onBlockMainThread(timeUsed);
                } catch (Exception e) {
                    Log.e(TAG, "onBlockMainThread error", e);
                }
            }
        };
        if (mHandler != null)
            Message.obtain(mHandler, runnable).sendToTarget();
        else
            runnable.run();
    }

    private void onTrace(final String stack) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mCallback.onBlockTrace(stack);
                } catch (Exception e) {
                    Log.e(TAG, "onBlockTrace error", e);
                }
            }
        };
        if (mHandler != null)
            Message.obtain(mHandler, runnable).sendToTarget();
        else
            runnable.run();
    }

    private void startCheck() {
        mHandler.removeCallbacks(mCheckRunnable);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, mCheckRunnable), mFrameIntervalMillis);
    }

    private void endCheck() {
        mHandler.removeCallbacks(mCheckRunnable);
    }

    protected void pause() {
        setMessageLogging(null);
        mHandler.removeCallbacks(mCheckRunnable);
    }

    protected void resume() {
        setMessageLogging(this);
    }
}
