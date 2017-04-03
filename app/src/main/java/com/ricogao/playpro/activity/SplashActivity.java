package com.ricogao.playpro.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.ricogao.playpro.R;

import java.lang.ref.WeakReference;

public class SplashActivity extends AppCompatActivity {

    private static final int FORWARD_TO_MAIN = 12;
    private static final int DELAY_TIME = 3000;

    private final SplashHandler myHandler = new SplashHandler(this);


    /**
     * Avoid Memory leak in splash Activity
     */
    private static class SplashHandler extends Handler {
        WeakReference<SplashActivity> activityWeakReference;

        private SplashHandler(SplashActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SplashActivity activity = activityWeakReference.get();
            if (msg.what == FORWARD_TO_MAIN && activity != null) {
                activity.toMainActivity(activity);
                activity.finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_layout);

        //show splash screen for 3000ms
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myHandler.sendEmptyMessage(FORWARD_TO_MAIN);
            }
        }, DELAY_TIME);
    }


    public void toMainActivity(Activity activity) {
        Intent it = new Intent(activity, MainActivity.class);
        activity.startActivity(it);
    }


}
