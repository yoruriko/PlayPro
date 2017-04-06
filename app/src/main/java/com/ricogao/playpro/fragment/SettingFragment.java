package com.ricogao.playpro.fragment;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ricogao.playpro.R;
import com.ricogao.playpro.util.CircleTransform;
import com.ricogao.playpro.util.SharedPreferencesUtil;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ricogao on 2017/4/5.
 */

public class SettingFragment extends Fragment {

    private final static String TAG = SettingFragment.class.getSimpleName();
    private SharedPreferencesUtil spUtil;
    private final static int REQUEST_IMAGE_GET = 123;

    @BindView(R.id.tv_user_name)
    TextView tvUserName;

    @BindView(R.id.img_profile)
    ImageView imgProfile;

    @OnClick(R.id.btn_user_name)
    void onUserNameClick() {

        final EditText editText = new EditText(this.getContext());
        new AlertDialog.Builder(this.getContext()).setTitle("Enter user name")
                .setView(editText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String username = editText.getText().toString();
                        spUtil.saveUserName(username);
                        tvUserName.setText(username);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @OnClick(R.id.btn_profile_img)
    void onProfileImageClick() {
        selectImage();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_fragment_layout, container, false);
        ButterKnife.bind(this, view);
        spUtil = new SharedPreferencesUtil(this.getContext());
        return view;
    }


    private void initView() {
        tvUserName.setText(spUtil.getUsername());
    }

    private void showProfileImage(Uri uri) {
        Picasso.with(this.getContext())
                .load(uri)
                .placeholder(R.drawable.blank_profile)
                .centerCrop()
                .fit()
                .transform(new CircleTransform())
                .into(imgProfile);

    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
        showProfileImage(spUtil.getProfileImageUri());
    }

    private void selectImage() {
        Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        it.setType("image/*");
        startActivityForResult(it, REQUEST_IMAGE_GET);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_GET) {
            if (data != null) {
                Uri uri = data.getData();
                this.getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                spUtil.saveProfileImageUri(uri);
                showProfileImage(uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }
}
