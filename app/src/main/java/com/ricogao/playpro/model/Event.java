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

    List<Record> locations;

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    long timestamp;

    @Column
    long duration;

    @Column
    double Distance;


    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setDistance(double distance) {
        Distance = distance;
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
        return Distance;
    }

    public void setLocations(List<Record> locations) {
        this.locations = locations;
    }

    public List<Record> getLocations() {
        if (locations == null) {
            return loadAssociatedLocations();
        }
        return locations;
    }

    @Override
    public void save() {
        if (this.locations != null) {
            for (Record r : locations) {
                r.associateEvent(this);
            }
        }
        super.save();
    }

    public List<Record> loadAssociatedLocations() {
        this.locations = new Select()
                .from(Record.class)
                .where(Condition.column(Record_Table.eventId.getNameAlias()).eq(id))
                .orderBy(Record_Table.timestamp.getNameAlias(), true)
                .queryList();
        return locations;
    }
}
