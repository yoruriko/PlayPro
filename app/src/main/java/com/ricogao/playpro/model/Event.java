package com.ricogao.playpro.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.ricogao.playpro.util.MyDatabase;

import java.util.List;

/**
 * Created by ricogao on 2017/2/28.
 */
@Table(database = MyDatabase.class)
public class Event extends BaseModel {

    List<Record> records;

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    long timestamp;

    @Column
    long duration;

    @Column
    double distance;

    @Column
    float maxSpeed;

    @Column
    float calories;


    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setDistance(double distance) {
        distance = distance;
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setRecords(List<Record> locations) {
        this.records = locations;
    }

    public List<Record> getRecords() {
        return records;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    @Override
    public void save() {
        super.save();

        if (this.records != null) {
            for (Record r : records) {
                r.associateEvent(this);
            }
        }

    }

    public List<Record> loadAssociatedRecords() {
        this.records = new Select()
                .from(Record.class)
                .where(Condition.column(Record_Table.eventId.getNameAlias()).eq(id))
                .orderBy(Record_Table.timestamp.getNameAlias(), true)
                .queryList();
        return records;
    }
}
