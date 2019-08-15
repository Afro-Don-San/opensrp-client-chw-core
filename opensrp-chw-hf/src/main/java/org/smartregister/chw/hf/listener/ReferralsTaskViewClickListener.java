package org.smartregister.chw.hf.listener;

import android.view.View;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.ReferralTaskViewActivity;

public class ReferralsTaskViewClickListener implements View.OnClickListener {
    private ReferralTaskViewActivity referralTaskViewActivity;

    public ReferralTaskViewActivity getReferralTaskViewActivity() {
        return referralTaskViewActivity;
    }

    public void setReferralTaskViewActivity(ReferralTaskViewActivity referralTaskViewActivity) {
        this.referralTaskViewActivity = referralTaskViewActivity;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.view_profile) {
            getReferralTaskViewActivity().finish();
        } else if (view.getId() == R.id.mark_ask_done) {
            getReferralTaskViewActivity().closeTask();
        }
    }
}
