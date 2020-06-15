package org.smartregister.chw.core.presenter;

import org.json.JSONObject;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.presenter.BaseAncHomeVisitPresenter;
import org.smartregister.chw.anc.util.JsonFormUtils;

import timber.log.Timber;

/**
 * Created by cozej4 on 6/3/20.
 *
 * @author cozej4 https://github.com/cozej4
 */
public class CorePathfinderFpFollowupVisitPresenter extends BaseAncHomeVisitPresenter {
    public CorePathfinderFpFollowupVisitPresenter(MemberObject memberObject, BaseAncHomeVisitContract.View view, BaseAncHomeVisitContract.Interactor interactor) {
        super(memberObject, view, interactor);
    }

    @Override
    public void startForm(String formName, String memberID, String currentLocationId) {
        try {
            if (view.get() != null) {
                JSONObject jsonObject = new JSONObject(formName);
                JsonFormUtils.getRegistrationForm(jsonObject, memberID, currentLocationId);
                view.get().startFormActivity(jsonObject);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void submitVisit() {
        super.submitVisit();
    }
}
