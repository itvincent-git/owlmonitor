package com.github.itvincentgit.owlmonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.itvincentgit.OwlMonitor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.sleep).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        findViewById(R.id.resume).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sleep:
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                break;
            case R.id.pause:
                OwlMonitor.instance().pause();
                break;
            case R.id.resume:
                OwlMonitor.instance().resume();
                break;
            default:
        }
    }
}
