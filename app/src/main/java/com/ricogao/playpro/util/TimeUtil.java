package com.ricogao.playpro.util;

/**
 * Created by ricogao on 2017/4/3.
 */

public class TimeUtil {

    public static String formatDuration(long dT) {
        long hours = (dT % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (dT % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (dT % (1000 * 60)) / 1000;

        String hourString = (hours < 10) ? "0" + hours : hours + "";
        String minutesString = (minutes < 10) ? "0" + minutes : minutes + "";
        String secondsString = (seconds < 10) ? "0" + seconds : seconds + "";
        return hourString + ":" + minutesString + ":" + secondsString;
    }

    public static String formatHour(long dT) {
        long hours = (dT % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        return hours + "";
    }
}
