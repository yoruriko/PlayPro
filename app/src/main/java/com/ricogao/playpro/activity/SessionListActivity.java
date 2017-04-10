package com.ricogao.playpro.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.Adapter.SessionItemAdapter;
import com.ricogao.playpro.R;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Event_Table;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by ricogao on 2017/3/18.
 */

public class SessionListActivity extends AppCompatActivity implements SessionItemAdapter.SessionItemListener {

    private final static String TAG = SessionListActivity.class.getSimpleName();
    private List<Event> mEvents;
    private SessionItemAdapter adapter;
    private CompositeSubscription subscriptions;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_session_layout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sessions");
        }
        ButterKnife.bind(this);
        subscriptions = new CompositeSubscription();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscriptions != null && subscriptions.isUnsubscribed()) {
            subscriptions.unsubscribe();
        }
    }

    private void loadData() {
        mEvents = new ArrayList<>();
        Subscription loadDataSub = Observable
                .just(getAllLocalEvents())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        mEvents = events;
                        showData(mEvents);
                    }
                });

        subscriptions.add(loadDataSub);
    }

    private List<Event> getAllLocalEvents() {
        return new Select()
                .from(Event.class)
                .orderBy(Event_Table.timestamp.getNameAlias(), false)
                .queryList();
    }

    private void showData(List<Event> events) {
        if (adapter == null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new SessionItemAdapter(this, events);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setEvents(events);
            adapter.notifyDataSetChanged();
        }

    }

    private void removeEvent(int position) {
        Event event = mEvents.get(position);
        event.delete();
        mEvents.remove(position);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSessionItemClick(long eventId) {
        Intent it = new Intent(SessionListActivity.this, SessionDetailActivity.class);
        it.putExtra("eventId", eventId);
        startActivity(it);
    }

    @Override
    public void onSessionLongClick(int position) {
        final int pos = position;
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(mEvents.get(pos).getTimestamp());
        new AlertDialog.Builder(this)
                .setTitle("Are you sure to delete this Event on: " + date + " ?")
                .setPositiveButton("delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeEvent(pos);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}
