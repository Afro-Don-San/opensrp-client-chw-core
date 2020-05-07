package org.smartregister.chw.core.job;

import android.content.Intent;

import androidx.annotation.NonNull;

import org.smartregister.AllConstants;
import org.smartregister.chw.core.sync.intent.SyncClientEventsPerTaskIntentService;
import org.smartregister.job.BaseJob;

/**
 * Created by cozej4 on 2020-02-08.
 *
 * @author cozej4 https://github.com/cozej4
 */
public class CloseExpiredReferralsServiceJob extends BaseJob {

    public static final String TAG = "SyncTaskWithClientEventsServiceJob";

    private Class<? extends SyncClientEventsPerTaskIntentService> serviceClass;

    public CloseExpiredReferralsServiceJob(Class<? extends SyncClientEventsPerTaskIntentService> serviceClass) {
        this.serviceClass = serviceClass;
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), serviceClass);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
