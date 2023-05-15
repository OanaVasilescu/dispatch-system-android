package com.example.dispatchsystem;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class ServerJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case ShowNotificationJob.TAG:
                return new ShowNotificationJob();
            default:
                return null;
        }
    }
}
