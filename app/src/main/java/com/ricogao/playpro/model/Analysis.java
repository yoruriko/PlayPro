package com.ricogao.playpro.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.ricogao.playpro.util.MyDatabase;

/**
 * Created by ricogao on 2017/4/11.
 */

@Table(database = MyDatabase.class)
public class Analysis extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    int centreCount;

    @Column
    int wingCount;

    @Column
    int fontCount;

    @Column
    int midFieldCount;

    @Column
    int backCount;

    @Column
    int standCount;

    @Column
    int walkCount;

    @Column
    int runCount;

    @Column
    float stepPace;

    @Column
    float topSpeedScore;

    @Column
    float avgSpeedScore;

    @Column
    float staminaScore;

    @Column
    float activeScore;

    @Column
    float positionScore;

    @Column
    float workRateScore;


    public long getId() {
        return id;
    }

    public int getCentreCount() {
        return centreCount;
    }

    public void setCentreCount(int centreCount) {
        this.centreCount = centreCount;
    }

    public int getWingCount() {
        return wingCount;
    }

    public void setWingCount(int wingCount) {
        this.wingCount = wingCount;
    }

    public int getFontCount() {
        return fontCount;
    }

    public void setFontCount(int fontCount) {
        this.fontCount = fontCount;
    }

    public int getMidFieldCount() {
        return midFieldCount;
    }

    public void setMidFieldCount(int midFieldCount) {
        this.midFieldCount = midFieldCount;
    }

    public int getBackCount() {
        return backCount;
    }

    public void setBackCount(int backCount) {
        this.backCount = backCount;
    }

    public int getStandCount() {
        return standCount;
    }

    public void setStandCount(int standCount) {
        this.standCount = standCount;
    }

    public int getWalkCount() {
        return walkCount;
    }

    public void setWalkCount(int walkCount) {
        this.walkCount = walkCount;
    }

    public int getRunCount() {
        return runCount;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public float getStepPace() {
        return stepPace;
    }

    public void setStepPace(float stepPace) {
        this.stepPace = stepPace;
    }

    public float getTopSpeedScore() {
        return topSpeedScore;
    }

    public void setTopSpeedScore(float topSpeedScore) {
        this.topSpeedScore = topSpeedScore;
    }

    public float getAvgSpeedScore() {
        return avgSpeedScore;
    }

    public void setAvgSpeedScore(float avgSpeedScore) {
        this.avgSpeedScore = avgSpeedScore;
    }

    public float getStaminaScore() {
        return staminaScore;
    }

    public void setStaminaScore(float staminaScore) {
        this.staminaScore = staminaScore;
    }

    public float getActiveScore() {
        return activeScore;
    }

    public void setActiveScore(float activeScore) {
        this.activeScore = activeScore;
    }

    public float getPositionScore() {
        return positionScore;
    }

    public void setPositionScore(float positionScore) {
        this.positionScore = positionScore;
    }

    public float getWorkRateScore() {
        return workRateScore;
    }

    public void setWorkRateScore(float workRateScore) {
        this.workRateScore = workRateScore;
    }


    public float getScore() {
        float score = (avgSpeedScore + topSpeedScore + staminaScore + workRateScore + positionScore + activeScore) / 60f * 100f;
        return score;
    }
}
