package org.smartregister.chw.hf.fragement;

import android.view.View;

import org.smartregister.chw.core.fragment.BaseReferralRegisterFragment;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.activity.ReferralTaskViewActivity;
import org.smartregister.chw.hf.presenter.ReferralFragmentPresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.family.util.DBConstants;

public class ReferralRegisterFragment extends BaseReferralRegisterFragment {
    private ReferralFragmentPresenter referralFragmentPresenter;

    @Override
    protected void initializePresenter() {
        referralFragmentPresenter = new ReferralFragmentPresenter(this);
        presenter = referralFragmentPresenter;

    }

    @Override
    protected void onViewClicked(View view) {
        CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();
        referralFragmentPresenter.setBaseEntityId(Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, true));
        referralFragmentPresenter.fetchClient();
        ReferralTaskViewActivity.startReferralTaskViewActivity(getActivity(), getCommonPersonObjectClient(), getTask(Utils.getValue(client.getColumnmaps(), "_id", true)), true);
    }

    private Task getTask(String taskId) {
        Task task;
        task = HealthFacilityApplication.getInstance().getTaskRepository().getTaskByIdentifier(taskId);
        return task;
    }
}