package com.ricogao.playpro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.ricogao.playpro.Adapter.SessionItemAdapter;
import com.ricogao.playpro.R;
import com.ricogao.playpro.model.Event;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ricogao on 2017/3/18.
 */

public class SessionListActivity extends AppCompatActivity implements SessionItemAdapter.SessionItemListener {

    private final static String TAG = SessionListActivity.class.getSimpleName();
    private SessionItemAdapter adapter;


    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_session_layout);
        ButterKnife.bind(this);
        initAdapter();
    }

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Event> list = new ArrayList<Event>();

        for (int i = 0; i < 10; i++) {
            list.add(new Event());
        }

        adapter = new SessionItemAdapter(this, list);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSessionItemClick(int position) {
        Toast.makeText(this, position + " is clicked", Toast.LENGTH_SHORT).show();
        Intent it = new Intent(SessionListActivity.this, SessionDetailActivity.class);
        startActivity(it);
    }
}
