package com.ricogao.playpro.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ricogao.playpro.R;
import com.ricogao.playpro.util.CircleTransform;
import com.ricogao.playpro.util.SharedPreferencesUtil;
import com.ricogao.playpro.util.TimeUtil;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ricogao on 2017/4/5.
 */

public class DetailFragment extends EventFragment {

    private SharedPreferencesUtil spUtil;
    @BindView(R.id.img_profile)
    ImageView imgProfile;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.tv_distance)
    TextView tvDistance;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_speed_average)
    TextView tvAvgSpeed;
    @BindView(R.id.tv_speed_max)
    TextView tvMaxSpeed;
    @BindView(R.id.tv_calorie)
    TextView tvCalorie;
    @BindView(R.id.tv_pace)
    TextView tvPace;
    @BindView(R.id.tv_active_play)
    TextView tvActivePlay;
    @BindView(R.id.tv_score)
    TextView tvScore;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_fragment_layout, container, false);
        ButterKnife.bind(this, view);
        spUtil = new SharedPreferencesUtil(this.getContext());
        initView();
        return view;
    }

    private void initView() {
        showProfileImage();
        tvName.setText(spUtil.getUsername());
        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getTimestamp()));

        tvDistance.setText(String.format("%.2f", event.getDistance() * 0.001f) + " KM");
        tvTime.setText(TimeUtil.formatDuration(event.getDuration()));
        float avgSpeed = event.getDistance() / (event.getDuration() * 0.001f) * 3.6f;

        tvAvgSpeed.setText(String.format("%.2f", avgSpeed));
        tvMaxSpeed.setText(String.format("%.2f", event.getMaxSpeed() * 3.6f));

        tvCalorie.setText(String.format("%.1f", event.getCalories()));
        tvPace.setText(String.format("%.1f", event.getAnalysis().getStepPace()));

        tvActivePlay.setText(String.format("%.1f", event.getAnalysis().getActiveScore() * 10f) + "%");
        tvScore.setText(String.format("%.1f", event.getAnalysis().getScore()));
    }

    private void showProfileImage() {

        Picasso.with(this.getContext())
                .load(spUtil.getProfileImageUri())
                .placeholder(R.drawable.blank_profile)
                .centerCrop()
                .fit()
                .transform(new CircleTransform())
                .into(imgProfile);

    }


}
