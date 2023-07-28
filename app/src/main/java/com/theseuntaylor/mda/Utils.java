package com.theseuntaylor.mda;

import com.google.android.gms.location.ActivityTransition;

public class Utils {

    public ActivityTransition getActivityTransition(int activityType, int activityTransition) {
        return new ActivityTransition.Builder()
                .setActivityType(activityType)
                .setActivityTransition(activityTransition)
                .build();
    }

}
