package com.ricogao.playpro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.Adapter.FieldItemAdapter;
import com.ricogao.playpro.R;
import com.ricogao.playpro.model.Field;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by ricogao on 2017/4/9.
 */

public class FieldListActivity extends AppCompatActivity implements FieldItemAdapter.FieldItemListener {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    public final static int REQUEST_SELECT_FIELD = 119;
    public final static int REQUEST_SELECTED = 123;

    private Subscription loadDataSub;

    private List<Field> mFields;

    private FieldItemAdapter adapter;

    private long eventId;
    private boolean isFromRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_session_layout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Select Field");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ButterKnife.bind(this);

        eventId = getIntent().getLongExtra("eventId", 0);
        isFromRecord = getIntent().getBooleanExtra("isFromRecord", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_add:
                startField(-1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadDataSub != null && loadDataSub.isUnsubscribed()) {
            loadDataSub.unsubscribe();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        loadDataSub = Observable.just(getAllFields())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Field>>() {
                    @Override
                    public void call(List<Field> fields) {
                        mFields = fields;
                        showData(mFields);
                    }
                });
    }

    private List<Field> getAllFields() {
        return new Select().from(Field.class).queryList();
    }

    private void showData(List<Field> fields) {
        if (adapter == null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new FieldItemAdapter(this, fields);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setFields(fields);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFieldItemClick(long fieldId) {
        startField(fieldId);
    }

    private void startField(long fieldId) {
        Intent it = new Intent(this, EditFieldActivity.class);
        it.putExtra("eventId", eventId);
        it.putExtra("fieldId", fieldId);
        startActivityForResult(it, REQUEST_SELECT_FIELD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_FIELD && resultCode == REQUEST_SELECTED) {
            if (isFromRecord) {
                Intent it = new Intent(FieldListActivity.this, SessionDetailActivity.class);
                it.putExtra("eventId", eventId);
                startActivity(it);
            }
            this.finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
