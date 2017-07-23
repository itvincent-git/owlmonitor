# OwlMonitor 猫头鹰监控
OwlMonitor is an analysis tool for detecting Android UI block.

1. Detect ui frame refresh block.
2. Detects the timeout operation of the main thread Looper.
3. The main thread block outputs the simple stacktrace information.
4. Customize the processing of block output information, such as log output, reporting background and so on.
5. Support in the background to stop monitoring, reduce power consumption, the background call pause (), the front desk call resume ().



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