package com.ricogao.playpro.util;

import com.ricogao.playpro.model.Analysis;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Record;

/**
 * Created by ricogao on 2017/4/11.
 */

public class AnalysisUtil {

    public static Analysis generateAnalysisFromEvent(Event event, Position pos, int centre, int wing, int font, int midfield, int back) {
        Analysis analysis = (event.getAnalysis() == null) ? new Analysis() : event.getAnalysis();
        analysis.setCentreCount(centre);
        analysis.setWingCount(wing);
        analysis.setFontCount(font);
        analysis.setMidFieldCount(midfield);
        analysis.setBackCount(back);

        int stand = 0;
        int walk = 0;
        int run = 0;
        int totalStep = 0;

        //topSpeed score = Max speed/speed limit * Factor
        float topSpeedScore = event.getMaxSpeed() / 12.52f * 10f;
        if (topSpeedScore > 10f) {
            topSpeedScore = 10f;
        } else if (topSpeedScore < 0f) {
            topSpeedScore = 0f;
        }
        analysis.setTopSpeedScore(topSpeedScore);

        float avgSpeed = event.getDistance() / (event.getDuration() * 0.001f);
        //AvgSpeed Score = Avg speed/(1/3*speed limit) * Factor
        float avgSpeedScore = avgSpeed / 4.17f * 10f;

        analysis.setAvgSpeedScore(avgSpeedScore);

        //stamina score = duration / 45 minutes * Factor
        float staminaScore = event.getDuration() / (45f * 60f * 1000f) * 10f;
        if (staminaScore > 10f) {
            staminaScore = 10f;
        } else if (staminaScore < 0f) {
            staminaScore = 0f;
        }

        analysis.setStaminaScore(staminaScore);

        for (Record record : event.getRecords()) {
            if (record.getState() == Record.STATE_STAND) {
                stand++;
            } else if (record.getState() == Record.STATE_WALK) {
                walk++;
            } else if (record.getState() == Record.STATE_RUN) {
                run++;
            }

            totalStep += record.getSteps();
        }

        //step pace measure in step/min
        float stepPace = totalStep / (event.getDuration() * 0.001f) * 60f;

        analysis.setStepPace(stepPace);

        //active score = (Active records)/total records * Factor
        float activeScore = (walk + run) / (float) event.getRecords().size() * 10f;

        analysis.setActiveScore(activeScore);

        float positionScore = getPositionScore(pos, centre, wing, font, midfield, back);
        analysis.setPositionScore(positionScore);

        //total distance cover in the session, uses 6km as a target 45 min game
        float workRateScore = (event.getDistance() * 0.001f) / 6f * 10f;

        if (workRateScore > 10f) {
            workRateScore = 10f;
        } else if (workRateScore < 0f) {
            workRateScore = 0f;
        }
        analysis.setWorkRateScore(workRateScore);

        analysis.setStandCount(stand);
        analysis.setWalkCount(walk);
        analysis.setRunCount(run);

        return analysis;
    }

    //score based on distribution of the records on the field sections
    private static float getPositionScore(Position pos, int centre, int wing, int font, int midfield, int back) {
        float total = wing + centre;
        float score1;
        float score2;
        float score3;

        switch (pos) {
            case CENTRE_FORWARD:
                score1 = centre / total * 5f;
                score2 = font / total * 3f;
                score3 = midfield / total * 2f;
                return score1 + score2 + score3;
            case CENTRE_MIDFIELD:
                score1 = centre / total * 5f;
                score2 = midfield / total * 3f;
                score3 = (font + back) / total * 2f;
                return score1 + score2 + score3;
            case WING_MIDFIELD:
                score1 = wing / total * 5f;
                score2 = midfield / total * 3f;
                score3 = (font + back) / total * 2f;
                return score1 + score2 + score3;
            case CENTRE_BACK:
                score1 = centre / total * 5f;
                score2 = back / total * 3f;
                score3 = midfield / total * 2f;
                return score1 + score2 + score3;
            case WING_BACK:
                score1 = wing / total * 5f;
                score2 = back / total * 3f;
                score3 = midfield / total * 2f;
                return score1 + score2 + score3;
            default:
                return 0f;
        }

    }

}
