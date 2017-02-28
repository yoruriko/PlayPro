package com.ricogao.playpro.model;

import android.location.Location;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.ricogao.playpro.util.MyDatabase;

/**
 * Created by ricogao on 2017/2/28.
 */
@Table(database = MyDatabase.class)
public class Record extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    long timestamp;

    @Column
    double latitude;

    @Column
    double longitude;

    @Column
    long eventId;


    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getEventId() {
        return eventId;
    }

    public Location getLocation() {
        Location location = new Location("PlayPro");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setTime(timestamp);
        return location;
    }

    public void associateEvent(Event event) {
        this.eventId = event.getId();
        this.save();
    }
}
