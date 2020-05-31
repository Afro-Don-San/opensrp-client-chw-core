package org.smartregister.chw.core.contract;

import com.adosa.opensrp.chw.fp.contract.BaseFpProfileContract;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;

import org.json.JSONObject;
import org.smartregister.repository.AllSharedPreferences;

public interface CorePathfinderFamilyPlanningMemberProfileContract {

    interface View extends BaseFpProfileContract.View {
        void startFormActivity(JSONObject formJson, PathfinderFpMemberObject fpMemberObject);
    }

    interface Presenter extends BaseFpProfileContract.Presenter {
        void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString) throws Exception;

        void startFamilyPlanningReferral();
    }

    interface Interactor extends BaseFpProfileContract.Interactor {
        void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception;
    }
}