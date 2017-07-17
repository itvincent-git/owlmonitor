# OwlMonitor猫头鹰监控
OwlMonitor猫头鹰监控，是一个检测Android UI卡顿的分析工具。
1. 检测ui frame的刷新卡顿
2. 检测主线程Looper的超时操作
3. 主线程卡顿输出简洁的stacktrace信息
4. 自定义卡顿信息的处理，例如日志输出、上报后台等
5. 支持在后台时停止监测，减少耗电，后台时调用pause()，前台时调用resume()

## Sample
```
public class MainApplication extends Application {
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
```

## Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

## Step 2. Add the dependency
```
dependencies {
    compile 'com.github.itvincent-git:owlmonitor:1.0.0'
}
```