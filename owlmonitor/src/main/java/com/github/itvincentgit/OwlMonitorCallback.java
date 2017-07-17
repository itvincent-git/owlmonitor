package com.github.itvincentgit;

/**
 * OwlMonitor（猫头鹰监测）回调，回调都是在handler的非UI线程里执行
 * @author zhongyongsheng
 */

public interface OwlMonitorCallback {

    /**
     * UI阻塞时长
     * @param skippedFrames
     * @param skippedDurationMs
     */
    void onSkippedUIFrames(long skippedFrames, long skippedDurationMs);

    /**
     * MainLooper被阻塞的时长
     * @param blockDurationMs
     */
    void onBlockMainThread(long blockDurationMs);

    /**
     *  MainLooper被阻塞时的堆栈信息
     * @param stack
     */
    void onBlockTrace(String stack);
}

