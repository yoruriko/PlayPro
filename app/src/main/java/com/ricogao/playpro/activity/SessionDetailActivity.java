package com.ricogao.playpro.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.R;
import com.ricogao.playpro.fragment.ChartFragment;
import com.ricogao.playpro.fragment.DetailFragment;
import com.ricogao.playpro.fragment.EventFragment;
import com.ricogao.playpro.fragment.FieldFragment;
import com.ricogao.playpro.fragment.TrackFragment;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Event_Table;
import com.ricogao.playpro.model.Record;


import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ricogao on 2017/3/19.
 */

public class SessionDetailActivity extends AppCompatActivity {

    public final static String TAG = SessionDetailActivity.class.getSimpleName();
    private long eventId;
    private EventFragment trackFragment, fieldFragment, chartFragment, detailFragment;
    private Subscription readDataSub;

    private Event currentEvent;

    @BindView(R.id.bar_track)
    View barTrack;
    @BindView(R.id.bar_field)
    View barField;
    @BindView(R.id.bar_chart)
    View barChart;
    @BindView(R.id.bar_detail)
    View barDetail;

    @OnClick(R.id.btn_track)
    void onTrackClick() {
        switchFragment(trackFragment);

        barTrack.setVisibility(View.VISIBLE);
        barField.setVisibility(View.INVISIBLE);
        barChart.setVisibility(View.INVISIBLE);
        barDetail.setVisibility(View.INVISIBLE);
    }


    @OnClick(R.id.btn_field)
    void onFieldClick() {
        if (fieldFragment == null) {
            fieldFragment = new FieldFragment();
            fieldFragment.setEvent(currentEvent);
        }
        switchFragment(fieldFragment);
        Intent it = new Intent(this, FieldListActivity.class);
        it.putExtra("eventId", eventId);
        startActivity(it);

        barTrack.setVisibility(View.INVISIBLE);
        barField.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.INVISIBLE);
        barDetail.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.btn_chart)
    void onChartClick() {
        if (chartFragment == null) {
            chartFragment = new ChartFragment();
            chartFragment.setEvent(currentEvent);
        }

        switchFragment(chartFragment);

        barTrack.setVisibility(View.INVISIBLE);
        barField.setVisibility(View.INVISIBLE);
        barChart.setVisibility(View.VISIBLE);
        barDetail.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.btn_details)
    void onDetailClick() {
        if (detailFragment == null) {
            detailFragment = new DetailFragment();
            detailFragment.setEvent(currentEvent);
        }
        switchFragment(detailFragment);

        barTrack.setVisibility(View.INVISIBLE);
        barField.setVisibility(View.INVISIBLE);
        barChart.setVisibility(View.INVISIBLE);
        barDetail.setVisibility(View.VISIBLE);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail_layout);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Session");
        }

        trackFragment = new TrackFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, trackFragment).commit();
        eventId = getIntent().getLongExtra("eventId", 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        readData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle("Are you sure to delete this session?")
                        .setPositiveButton("delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteEvent();
                                SessionDetailActivity.this.finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteEvent() {
        currentEvent.delete();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (readDataSub != null && readDataSub.isUnsubscribed()) {
            readDataSub.unsubscribe();
        }
    }

    private Event readEvent(long eventId) {
        return new Select()
                .from(Event.class)
                .where(Condition.column(Event_Table.id.getNameAlias()).eq(eventId))
                .querySingle();
    }

    private void switchFragment(EventFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void readData() {

        readDataSub = Observable
                .just(readEvent(eventId))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        currentEvent = event;
                        event.loadAssociatedField();
                        event.loadAssociatedRecords();
                        event.loadAssociatedAnalysis();
                        trackFragment.setEvent(currentEvent);
                    }
                });

    }


}
