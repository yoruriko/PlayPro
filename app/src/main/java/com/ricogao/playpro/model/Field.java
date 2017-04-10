package com.ricogao.playpro.model;

import com.google.android.gms.maps.model.LatLng;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.ricogao.playpro.util.MyDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ricogao on 2017/4/8.
 */
@Table(database = MyDatabase.class)
public class Field extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;

    @Column
    double topLeftLat;

    @Column
    double topLeftLng;
    @Column
    double topRightLat;
    @Column
    double topRightLng;
    @Column
    double botLeftLat;
    @Column
    double botLeftLng;
    @Column
    double botRightLat;
    @Column
    double botRightLng;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBound(List<LatLng> bounds) {
        topLeftLat = bounds.get(0).latitude;
        topLeftLng = bounds.get(0).longitude;
        topRightLat = bounds.get(1).latitude;
        topRightLng = bounds.get(1).longitude;
        botRightLat = bounds.get(2).latitude;
        botRightLng = bounds.get(2).longitude;
        botLeftLat = bounds.get(3).latitude;
        botLeftLng = bounds.get(3).longitude;
    }

    public List<LatLng> getBound() {
        List<LatLng> bounds = new ArrayList<>();
        bounds.add(new LatLng(topLeftLat, topLeftLng));
        bounds.add(new LatLng(topRightLat, topRightLng));
        bounds.add(new LatLng(botRightLat, botRightLng));
        bounds.add(new LatLng(botLeftLat, botLeftLng));
        return bounds;
    }

    public long getId() {
        return id;
    }
}
