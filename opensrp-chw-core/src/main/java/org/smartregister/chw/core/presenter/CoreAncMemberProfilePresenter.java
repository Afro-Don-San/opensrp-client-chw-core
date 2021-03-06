package org.smartregister.chw.core.presenter;

import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.presenter.BaseAncMemberProfilePresenter;
import org.smartregister.chw.core.contract.AncMemberProfileContract;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.FormUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class CoreAncMemberProfilePresenter extends BaseAncMemberProfilePresenter implements
        FamilyProfileContract.InteractorCallBack, AncMemberProfileContract.Presenter, AncMemberProfileContract.InteractorCallBack {
    private String entityId;
    private WeakReference<AncMemberProfileContract.View> view;
    private AncMemberProfileContract.Interactor interactor;
    private FormUtils formUtils;

    public CoreAncMemberProfilePresenter(AncMemberProfileContract.View view, AncMemberProfileContract.Interactor interactor, MemberObject memberObject) {
        super(view, interactor, memberObject);
        setEntityId(memberObject.getBaseEntityId());
        this.view = new WeakReference<>(view);
        this.interactor = interactor;
    }

    @Override
    public void startFormForEdit(CommonPersonObjectClient commonPersonObject) {
//        TODO Implement
    }

    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient client) {
//        TODO Implement
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId) {
//        TODO Implement
        Timber.d("onUniqueIdFetched unimplemented");
    }

    @Override
    public void onNoUniqueId() {
//        TODO Implement
        Timber.d("onNoUniqueId unimplemented");
    }

    @Override
    public void onRegistrationSaved(boolean b, boolean b1, FamilyEventClient familyEventClient) {
//     TODO Implement
        Timber.d("onRegistrationSaved unimplemented");
    }

    @Override
    public void setClientTasks(Set<Task> taskList) {
        if (getView() != null) {
            getView().setClientTasks(taskList);
        }
    }

    @Override
    public AncMemberProfileContract.View getView() {
        if (view != null) {
            return view.get();
        } else {
            return null;
        }
    }

    @Override
    public void fetchTasks() {
        interactor.getClientTasks(CoreConstants.REFERRAL_PLAN_ID, getEntityId(), this);
    }

    public String getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Override
    public void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString) throws Exception {
        interactor.createReferralEvent(allSharedPreferences, jsonString, getEntityId());
    }

    @Override
    public void startAncReferralForm() {
        try {
            getView().startFormActivity(getFormUtils().getFormJson(CoreConstants.JSON_FORM.getAncReferralForm()));
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startAncDangerSignsOutcomeForm(MemberObject memberObject) {
        try {
            JSONObject formJsonObject = getFormUtils().getFormJson(CoreConstants.JSON_FORM.getAncDangerSignsOutcomeForm());
            Map<String, String> valueMap = new HashMap<>();
            String lmp = memberObject.getLastMenstrualPeriod();
            DateTime lmpDateTime = new DateTime(new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(lmp));
            String edd = DateTimeFormat.forPattern("dd-MM-yyyy").print(lmpDateTime.plusDays(280));
            valueMap.put("lmp", lmp);
            valueMap.put("gest_age", String.valueOf(memberObject.getGestationAge()));
            valueMap.put("edd", edd);

            CoreJsonFormUtils.populateJsonForm(formJsonObject, valueMap);
            getView().startFormActivity(formJsonObject);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void createAncDangerSignsOutcomeEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception {
        interactor.createAncDangerSignsOutcomeEvent(allSharedPreferences, jsonString, entityID);
    }

    private FormUtils getFormUtils() {

        if (formUtils == null) {
            try {
                formUtils = FormUtils.getInstance(Utils.context().applicationContext());
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return formUtils;
    }
}


