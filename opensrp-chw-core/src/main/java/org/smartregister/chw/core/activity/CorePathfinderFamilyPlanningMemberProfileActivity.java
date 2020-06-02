package org.smartregister.chw.core.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adosa.opensrp.chw.fp.activity.BasePathfinderFpProfileActivity;
import com.adosa.opensrp.chw.fp.dao.PathfinderFpDao;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Rules;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.contract.CorePathfinderFamilyPlanningMemberProfileContract;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.custom_views.CorePathfinderFamilyPlanningFloatingMenu;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.dao.ChildDao;
import org.smartregister.chw.core.dao.PNCDao;
import org.smartregister.chw.core.domain.MemberType;
import org.smartregister.chw.core.interactor.CorePathfinderFamilyPlanningProfileInteractor;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.presenter.CorePathfinderFamilyPlanningMemberProfilePresenter;
import org.smartregister.chw.core.rule.PathfinderFpAlertRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.FpUtil;
import org.smartregister.chw.core.utils.PathfinderFamilyPlanningUtil;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants.EventType.FP_FOLLOW_UP_VISIT;
import static org.smartregister.chw.core.utils.Utils.updateToolbarTitle;

public abstract class CorePathfinderFamilyPlanningMemberProfileActivity extends BasePathfinderFpProfileActivity
        implements FamilyProfileExtendedContract.PresenterCallBack, CorePathfinderFamilyPlanningMemberProfileContract.View {


    protected RecyclerView notificationAndReferralRecyclerView;
    protected RelativeLayout notificationAndReferralLayout;

    protected static CommonPersonObjectClient getClientDetailsByBaseEntityID(@NonNull String baseEntityId) {
        CommonRepository commonRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);

        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        return client;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeNotificationReferralRecyclerView();
        updateToolbarTitle(this, R.id.toolbar_title, pathfinderFpMemberObject.getFamilyName());
    }

    protected void initializeNotificationReferralRecyclerView() {
        notificationAndReferralLayout = findViewById(R.id.notification_and_referral_row);
        notificationAndReferralRecyclerView = findViewById(R.id.notification_and_referral_recycler_view);
        notificationAndReferralRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    protected abstract void removeMember();

    protected abstract void startFamilyPlanningRegistrationActivity();

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        fpProfilePresenter = new CorePathfinderFamilyPlanningMemberProfilePresenter(this, new CorePathfinderFamilyPlanningProfileInteractor(this), pathfinderFpMemberObject);
    }

    @Override
    public void initializeCallFAB() {
        PathfinderFpMemberObject memberObject = PathfinderFpDao.getMember(pathfinderFpMemberObject.getBaseEntityId());
        fpFloatingMenu = new CorePathfinderFamilyPlanningFloatingMenu(this, memberObject);

        OnClickFloatingMenu onClickFloatingMenu = viewId -> {
            if (viewId == R.id.family_planning_fab) {
                checkPhoneNumberProvided();
                ((CorePathfinderFamilyPlanningFloatingMenu) fpFloatingMenu).animateFAB();
            } else if (viewId == R.id.call_layout) {
                ((CorePathfinderFamilyPlanningFloatingMenu) fpFloatingMenu).launchCallWidget();
                ((CorePathfinderFamilyPlanningFloatingMenu) fpFloatingMenu).animateFAB();
            } else if (viewId == R.id.refer_to_facility_layout) {
                ((CorePathfinderFamilyPlanningMemberProfilePresenter) fpProfilePresenter).startFamilyPlanningReferral();
            } else {
                Timber.d("Unknown fab action");
            }

        };

        ((CorePathfinderFamilyPlanningFloatingMenu) fpFloatingMenu).setFloatingMenuOnClickListener(onClickFloatingMenu);
        fpFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.END);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(fpFloatingMenu, linearLayoutParams);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.record_fp_followup_visit) {
            openFollowUpVisitForm(false);
        }
    }

    private void checkPhoneNumberProvided() {
        boolean phoneNumberAvailable = (StringUtils.isNotBlank(pathfinderFpMemberObject.getPhoneNumber())
                || StringUtils.isNotBlank(pathfinderFpMemberObject.getFamilyHeadPhoneNumber()));

        ((CorePathfinderFamilyPlanningFloatingMenu) fpFloatingMenu).redraw(phoneNumberAvailable);
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void verifyHasPhone() {
        // Implement
    }

    @Override
    public void notifyHasPhone(boolean b) {
        // Implement
    }

    @Override
    public void setupViews() {
        super.setupViews();
        new CorePathfinderFamilyPlanningMemberProfileActivity.UpdateFollowUpVisitButtonTask(pathfinderFpMemberObject).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == org.smartregister.chw.core.R.id.action_registration) {
            startFormForEdit(org.smartregister.chw.core.R.string.registration_info,
                    CoreConstants.JSON_FORM.FAMILY_MEMBER_REGISTER);
            return true;
        } else if (itemId == org.smartregister.chw.core.R.id.action_remove_member) {
            removeMember();
            return true;
        } else if (itemId == org.smartregister.chw.core.R.id.action_fp_change) {
            startFamilyPlanningRegistrationActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.family_planning_member_profile_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CoreConstants.ProfileActivityResults.CHANGE_COMPLETED:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(this, CorePathfinderFamilyPlanningRegisterActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    startActivity(intent);
                    finish();
                }
                break;
            case JsonFormUtils.REQUEST_CODE_GET_JSON:
                if (resultCode == RESULT_OK) {
                    try {
                        String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                        JSONObject form = new JSONObject(jsonString);
                        if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.FAMILY_PLANNING_REFERRAL)) {
                            ((CorePathfinderFamilyPlanningMemberProfilePresenter) fpProfilePresenter).createReferralEvent(Utils.getAllSharedPreferences(), jsonString);
                            showToast(this.getString(R.string.referral_submitted));
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                break;
            case Constants.REQUEST_CODE_HOME_VISIT:
                refreshViewOnHomeVisitResult();
                break;
            default:
                break;
        }
    }

    protected Observable<MemberType> getMemberType() {
        return Observable.create(e -> {
            MemberObject memberObject = PNCDao.getMember(pathfinderFpMemberObject.getBaseEntityId());
            String type = null;

            if (AncDao.isANCMember(memberObject.getBaseEntityId())) {
                type = CoreConstants.TABLE_NAME.ANC_MEMBER;
            } else if (PNCDao.isPNCMember(memberObject.getBaseEntityId())) {
                type = CoreConstants.TABLE_NAME.PNC_MEMBER;
            } else if (ChildDao.isChild(memberObject.getBaseEntityId())) {
                type = CoreConstants.TABLE_NAME.CHILD;
            }

            MemberType memberType = new MemberType(memberObject, type);
            e.onNext(memberType);
            e.onComplete();
        });
    }

    protected void executeOnLoaded(CorePathfinderFamilyPlanningMemberProfileActivity.OnMemberTypeLoadedListener listener) {
        final Disposable[] disposable = new Disposable[1];
        getMemberType().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MemberType>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable[0] = d;
                    }

                    @Override
                    public void onNext(MemberType memberType) {
                        listener.onMemberTypeLoaded(memberType);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {
                        disposable[0].dispose();
                        disposable[0] = null;
                    }
                });
    }

    private void refreshViewOnHomeVisitResult() {
        Observable<Visit> observable = Observable.create(visitObservableEmitter -> {
            Visit lastVisit = PathfinderFpDao.getLatestVisit(pathfinderFpMemberObject.getBaseEntityId(), FP_FOLLOW_UP_VISIT);
            visitObservableEmitter.onNext(lastVisit);
            visitObservableEmitter.onComplete();
        });

        final Disposable[] disposable = new Disposable[1];
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Visit>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable[0] = d;
                    }

                    @Override
                    public void onNext(Visit visit) {
                        updateLastVisitRow(visit.getDate());
                        onMemberDetailsReloaded(pathfinderFpMemberObject);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {
                        disposable[0].dispose();
                        disposable[0] = null;
                    }
                });
    }

    public void onMemberDetailsReloaded(PathfinderFpMemberObject pathfinderFpMemberObject) {
        super.onMemberDetailsReloaded(pathfinderFpMemberObject);
    }

    public void startFormForEdit(Integer titleResource, String formName) {

        JSONObject form = null;
        CommonPersonObjectClient client = org.smartregister.chw.core.utils.Utils.clientForEdit(pathfinderFpMemberObject.getBaseEntityId());

        if (formName.equals(CoreConstants.JSON_FORM.getFamilyMemberRegister())) {
            form = CoreJsonFormUtils.getAutoPopulatedJsonEditMemberFormString(
                    (titleResource != null) ? getResources().getString(titleResource) : null,
                    CoreConstants.JSON_FORM.getFamilyMemberRegister(),
                    this, client,
                    Utils.metadata().familyMemberRegister.updateEventType, pathfinderFpMemberObject.getLastName(), false);
        } else if (formName.equals(CoreConstants.JSON_FORM.getAncRegistration())) {
            form = CoreJsonFormUtils.getAutoJsonEditAncFormString(
                    pathfinderFpMemberObject.getBaseEntityId(), this, formName, PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_REGISTRATION, getResources().getString(titleResource));
        }

        try {
            assert form != null;
            startFormActivity(form, pathfinderFpMemberObject);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startFormActivity(JSONObject formJson, PathfinderFpMemberObject pathfinderFpMemberObject) {
        Intent intent = org.smartregister.chw.core.utils.Utils.formActivityIntent(this, formJson.toString());
        intent.putExtra(PathfinderFamilyPlanningConstants.FamilyPlanningMemberObject.MEMBER_OBJECT, pathfinderFpMemberObject);
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    private void updateFollowUpVisitButton(String buttonStatus) {
        switch (buttonStatus) {
            case CoreConstants.VISIT_STATE.DUE:
                setFollowUpButtonDue();
                break;
            case CoreConstants.VISIT_STATE.OVERDUE:
                setFollowUpButtonOverdue();
                break;
            default:
                break;
        }
    }

    public void updateFollowUpVisitStatusRow(Visit lastVisit) {
        setupFollowupVisitEditViews(VisitUtils.isVisitWithin24Hours(lastVisit));
    }

    public interface OnMemberTypeLoadedListener {
        void onMemberTypeLoaded(MemberType memberType);
    }

    private class UpdateFollowUpVisitButtonTask extends AsyncTask<Void, Void, Void> {
        private PathfinderFpMemberObject pathfinderFpMemberObject;
        private PathfinderFpAlertRule fpAlertRule;
        private Visit lastVisit;

        public UpdateFollowUpVisitButtonTask(PathfinderFpMemberObject pathfinderFpMemberObject) {
            this.pathfinderFpMemberObject = pathfinderFpMemberObject;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId(), FP_FOLLOW_UP_VISIT, pathfinderFpMemberObject.getFpMethod());

            Timber.e("Coze == "+new Gson().toJson(pathfinderFpMemberObject));
            if (!pathfinderFpMemberObject.getFpStartDate().equals("")) {
                Date lastVisitDate;
                if (lastVisit == null) {
                    lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId(), PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_REGISTRATION, pathfinderFpMemberObject.getFpMethod());
                }
                lastVisitDate = lastVisit.getDate();

                Rules rule = PathfinderFamilyPlanningUtil.getFpRules(pathfinderFpMemberObject.getFpMethod());
                Integer pillCycles = PathfinderFpDao.getLastPillCycle(pathfinderFpMemberObject.getBaseEntityId(), pathfinderFpMemberObject.getFpMethod());
                fpAlertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpMemberObject.getFpStartDate()), pillCycles, pathfinderFpMemberObject.getFpMethod());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (fpAlertRule != null && (fpAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.OVERDUE) ||
                    fpAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.DUE))
            ) {
                updateFollowUpVisitButton(fpAlertRule.getButtonStatus());
            }
            Timber.i("Initiation method = %s", pathfinderFpMemberObject.getFpInitiationStage());
            if (fpAlertRule == null && pathfinderFpMemberObject.getFpMethod().equals("0")) {
                if (pathfinderFpMemberObject.getFpInitiationStage().equals("")) {
                    showIntroductionToFpButton();
                } else if (pathfinderFpMemberObject.getFpInitiationStage().equals(PathfinderFamilyPlanningConstants.EventType.INTRODUCTION_TO_FAMILY_PLANNING)) {
                    showFpPregnancyScreeningButton();
                } else if (pathfinderFpMemberObject.getFpInitiationStage().equals(PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_PREGNANCY_SCREENING) && pathfinderFpMemberObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.NOT_LIKELY_PREGNANT)) {
                    showChooseFpMethodButton();
                } else if (pathfinderFpMemberObject.getFpInitiationStage().equals(PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_PREGNANCY_SCREENING) && pathfinderFpMemberObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.PREGNANT)) {
                    showIssueANCReferralButton();
                }
            } else if (pathfinderFpMemberObject.getFpInitiationStage().equalsIgnoreCase(PathfinderFamilyPlanningConstants.EventType.CHOOSING_FAMILY_PLANNING_METHOD)) {
                showGiveFpMethodButton();
            } else {
                updateFollowUpVisitStatusRow(lastVisit);
            }
        }
    }
}

