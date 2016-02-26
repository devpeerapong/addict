package com.lactozily.addict;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
public class AddictJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case AddictServiceChecker.TAG:
                return new AddictServiceChecker();
            default:
                return null;
        }
    }
}
