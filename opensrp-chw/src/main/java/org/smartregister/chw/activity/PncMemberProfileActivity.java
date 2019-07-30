package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.presenter.BaseAncMemberProfilePresenter;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.pnc.activity.BasePncMemberProfileActivity;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.JsonFormUtils;

import timber.log.Timber;

public class PncMemberProfileActivity extends BasePncMemberProfileActivity {

    public static void startMe(Activity activity, MemberObject memberObject, String familyHeadName, String familyHeadPhoneNumber) {
        Intent intent = new Intent(activity, PncMemberProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.FAMILY_HEAD_NAME, familyHeadName);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.FAMILY_HEAD_PHONE, familyHeadPhoneNumber);
        activity.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_pnc_member_registration:
                startFormForEdit(R.string.edit_member_form_title,
                        org.smartregister.chw.util.Constants.JSON_FORM.getFamilyMemberRegister());
                return true;
            case R.id.action_pnc_registration:
                startFormForEdit(R.string.edit_anc_registration_form_title,
                        org.smartregister.chw.util.Constants.JSON_FORM.getAncRegistration());
                return true;
            case R.id.action__pnc_remove_member:
                CommonRepository commonRepository = org.smartregister.chw.util.Utils.context().commonrepository(org.smartregister.chw.util.Utils.metadata().familyMemberRegister.tableName);

                final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(MEMBER_OBJECT.getBaseEntityId());
                final CommonPersonObjectClient client =
                        new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
                client.setColumnmaps(commonPersonObject.getColumnmaps());

                IndividualProfileRemoveActivity.startIndividualProfileActivity(PncMemberProfileActivity.this, client, MEMBER_OBJECT.getFamilyBaseEntityId(), MEMBER_OBJECT.getFamilyHead(), MEMBER_OBJECT.getPrimaryCareGiver());
                return true;
            case R.id.action_pregnancy_out_come:
                AncRegisterActivity.startAncRegistrationActivity(PncMemberProfileActivity.this, MEMBER_OBJECT.getBaseEntityId(), null,
                        org.smartregister.chw.util.Constants.JSON_FORM.getPregnancyOutcome(), AncLibrary.getInstance().getUniqueIdRepository().getNextUniqueId().getOpenmrsId(), MEMBER_OBJECT.getFamilyBaseEntityId());
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //TODO
    @Override
    protected void setupViews() {
        super.setupViews();
        textViewAncVisitNot.setOnClickListener(null);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseAncMemberProfilePresenter(this, new PncMemberProfileInteractor(this), MEMBER_OBJECT);
    }

    @Override
    public void openMedicalHistory() {
        PncMedicalHistoryActivity.startMe(this, MEMBER_OBJECT);
    }

    @Override
    public void openUpcomingService() {
        PncUpcomingServicesActivity.startMe(this, MEMBER_OBJECT);
    }


    public void startFormForEdit(Integer title_resource, String formName) {

        try {
            JSONObject form = org.smartregister.chw.util.JsonFormUtils.getAncPncForm(title_resource, formName, MEMBER_OBJECT, this);

            startActivityForResult(org.smartregister.chw.util.JsonFormUtils.getAncPncStartFormIntent(form, this), JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
