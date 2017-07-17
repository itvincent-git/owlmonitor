package com.github.itvincentgit;

/**
 * 线程里的调用栈，转为格式化的字符串
 * @author zhongyongsheng
 */

public class StackTraceString {

    private static final String STACK_EMPTY = "Stack empty";

    /**
     * 返回目标线程里的调用栈
     * @param from 从第几行堆栈开始输出
     * @param length 输出几行堆栈
     * @param thread 要显示的线程
     * @return
     */
    public static String getTraceString(int from, int length, Thread thread) {
        StackTraceElement[] stack = thread.getStackTrace();
        if (stack != null && stack.length > 0) {
            int i = from;
            StringBuilder sb = new StringBuilder(128);
            while (i <= from + length && i < stack.length) {
                StackTraceElement element = stack[i];
                sb.append("[").append(element.getMethodName())
                        .append(":").append(element.getLineNumber())
                        .append(",").append(element.getClassName())
                        .append("]");
                i++;
            }
            return sb.toString();
        }
        return STACK_EMPTY;
    }

    /**
     * 返回目标线程里的调用栈
     * @param from 从第几行堆栈开始输出
     * @param length 输出几行堆栈
     * @return
     */
    public static String getTraceString(int from, int length) {
        return getTraceString(from, length, Thread.currentThread());
    }

}
