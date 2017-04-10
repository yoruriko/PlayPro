package com.ricogao.playpro.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.R;
import com.ricogao.playpro.fragment.EditFieldFragment;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Event_Table;
import com.ricogao.playpro.model.Field;
import com.ricogao.playpro.model.Field_Table;

import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by ricogao on 2017/4/8.
 */

public class EditFieldActivity extends AppCompatActivity {

    private final static String TAG = EditFieldActivity.class.getSimpleName();

    private long eventId;
    private long fieldId;

    private Subscription readDataSub;
    private Event currentEvent;
    private Field currentField;
    private EditFieldFragment editFieldFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_field);

        eventId = getIntent().getLongExtra("eventId", -1);
        fieldId = getIntent().getLongExtra("fieldId", -1);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Field");
        }

        ButterKnife.bind(this);
        editFieldFragment = new EditFieldFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, editFieldFragment).commit();
        readData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                onSaveClick();
                setResult(FieldListActivity.REQUEST_SELECTED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSaveClick() {
        editFieldFragment.saveField();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
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

    private void readData() {
        readDataSub = Observable
                .just(readEvent(eventId))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        currentEvent = event;
                        event.loadAssociatedRecords();
                        event.loadAssociatedField();

                        if (fieldId != -1) {
                            currentField = loadField(fieldId);
                        }

                    }
                })
                .subscribe(new Subscriber<Event>() {
                    @Override
                    public void onCompleted() {
                        editFieldFragment.setData(currentEvent, currentField);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "Load record with error:" + e.getMessage());
                    }

                    @Override
                    public void onNext(Event event) {
                        Log.i(TAG, event.getRecords().size() + " records is found.");
                        Log.i(TAG, "With field ID: " + event.getFieldId());
                    }
                });
    }

    private Field loadField(long fieldId) {
        return new Select()
                .from(Field.class)
                .where(Condition.column(Field_Table.id.getNameAlias()).eq(fieldId))
                .querySingle();
    }
}
