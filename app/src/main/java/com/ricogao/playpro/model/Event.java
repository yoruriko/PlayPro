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

    private List<Record> records;
    private Field field;
    private Analysis analysis;

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    long fieldId = -1;

    @Column
    long analysisId = -1;

    @Column
    long timestamp;

    @Column
    long duration;

    @Column
    float distance;

    @Column
    float maxSpeed;

    @Column
    float calories;

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setDistance(float distance) {
        this.distance = distance;
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

    public float getDistance() {
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

    public void setField(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(long analysisId) {
        this.analysisId = analysisId;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    @Override
    public void save() {
        super.save();

        if (this.records != null) {
            for (Record r : records) {
                r.associateEvent(this);
            }
        }

        if (this.field != null) {
            field.save();
        }

        if (this.analysis != null) {
            analysis.save();
        }

    }

    @Override
    public void delete() {
        if (records == null) {
            loadAssociatedRecords();
        }

        if (analysis == null) {
            loadAssociatedAnalysis();
        }

        for (Record r : records) {
            r.delete();
        }

        analysis.delete();

        super.delete();
    }

    public List<Record> loadAssociatedRecords() {
        this.records = new Select()
                .from(Record.class)
                .where(Condition.column(Record_Table.eventId.getNameAlias()).eq(id))
                .orderBy(Record_Table.timestamp.getNameAlias(), true)
                .queryList();
        return records;
    }

    public Field loadAssociatedField() {
        if (fieldId == -1) {
            return null;
        }

        this.field = new Select()
                .from(Field.class)
                .where(Condition.column(Field_Table.id.getNameAlias()).eq(fieldId))
                .querySingle();

        return field;
    }

    public Analysis loadAssociatedAnalysis() {
        if (analysisId == -1) {
            return null;
        }

        this.analysis = new Select()
                .from(Analysis.class)
                .where(Condition.column(Analysis_Table.id.getNameAlias()).eq(analysisId))
                .querySingle();

        return analysis;
    }
}
