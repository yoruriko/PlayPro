package com.ricogao.playpro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.R;
import com.ricogao.playpro.fragment.MainFragment;
import com.ricogao.playpro.fragment.SettingFragment;
import com.ricogao.playpro.model.Record;

import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ricogao on 2017/3/15.
 */

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private Fragment mainFragment, settingFragment;
    @BindColor(R.color.font_grey)
    int fontGrey;
    @BindColor(R.color.font_dark_grey)
    int fontDark;
    @BindView(R.id.btn_main)
    ImageButton btnMain;
    @BindView(R.id.btn_setting)
    ImageButton btnSetting;

    @OnClick(R.id.btn_start)
    protected void onStartClick() {
        Intent it = new Intent(MainActivity.this, RecordActivity.class);
        startActivity(it);
    }

    @OnClick(R.id.btn_main)
    void onMainClick() {
        btnMain.setColorFilter(fontGrey);
        btnSetting.setColorFilter(fontDark);
        switchFragment(mainFragment);
    }

    @OnClick(R.id.btn_setting)
    void onSettingClick() {
        if (settingFragment == null) {
            settingFragment = new SettingFragment();
        }
        btnMain.setColorFilter(fontDark);
        btnSetting.setColorFilter(fontGrey);
        switchFragment(settingFragment);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        ButterKnife.bind(this);

        mainFragment = new MainFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_content, mainFragment).commit();
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();
    }


}
