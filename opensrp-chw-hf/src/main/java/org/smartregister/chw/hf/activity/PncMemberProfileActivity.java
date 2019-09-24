package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CorePncMemberProfileActivity;
import org.smartregister.chw.core.activity.CorePncRegisterActivity;
import org.smartregister.chw.core.interactor.CorePncMemberProfileInteractor;
import org.smartregister.chw.hf.interactor.PncMemberProfileInteractor;

public class PncMemberProfileActivity extends CorePncMemberProfileActivity {
    public static void startMe(Activity activity, MemberObject memberObject, String familyHeadName, String familyHeadPhoneNumber) {
        Intent intent = new Intent(activity, PncMemberProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.FAMILY_HEAD_NAME, familyHeadName);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.FAMILY_HEAD_PHONE, familyHeadPhoneNumber);
        activity.startActivity(intent);
    }

    @Override
    protected Class<? extends CoreFamilyProfileActivity> getFamilyProfileActivityClass() {
        return FamilyProfileActivity.class;
    }

    @Override
    protected CorePncMemberProfileInteractor getPncMemberProfileInteractor() {
        return new PncMemberProfileInteractor();
    }

    @Override
    protected void removePncMember() {
        //TODO implement functionality to remove PNC member
    }

    @Override
    protected Class<? extends CorePncRegisterActivity> getPncRegisterActivityClass() {
        return PncRegisterActivity.class;
    }
    @Override
    public void openMedicalHistory() {
        PncMedicalHistoryActivity.startMe(this, MEMBER_OBJECT);
    }
}
