package com.ricogao.playpro.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ricogao.playpro.R;

import butterknife.ButterKnife;

/**
 * Created by ricogao on 2017/4/5.
 */

public class DetailFragment extends EventFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_fragment_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }
}
