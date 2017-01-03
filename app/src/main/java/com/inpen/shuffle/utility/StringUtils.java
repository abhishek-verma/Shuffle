package com.inpen.shuffle.utility;

/**
 * Created by Abhishek on 1/1/2017.
 */

public class StringUtils {

    public static String getFormettedDurationString(long duration) {
        int mins = (int) (duration / 60);
        int secs = (int) (duration % 60);

        return String.format("%d:%d", mins, secs);
    }
}
