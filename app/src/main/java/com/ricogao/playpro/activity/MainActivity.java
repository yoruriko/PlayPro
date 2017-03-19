package com.ricogao.playpro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ricogao.playpro.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ricogao on 2017/3/15.
 */

public class MainActivity extends AppCompatActivity {

    @OnClick(R.id.btn_start)
    protected void onStartClick() {
        Intent it = new Intent(MainActivity.this, RecordActivity.class);
        startActivity(it);
    }

    @OnClick(R.id.btn_sessions)
    protected void onSessionClick() {
        Intent it = new Intent(MainActivity.this, SessionListActivity.class);
        startActivity(it);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_layout);
        ButterKnife.bind(this);
    }
}
