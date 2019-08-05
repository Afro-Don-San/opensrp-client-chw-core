package com.opensrp.chw.hf.fragement;

import android.os.Bundle;

import com.opensrp.chw.core.activity.CoreFamilyRegisterActivity;
import com.opensrp.chw.core.fragment.CoreFamilyProfileChangeDialog;
import com.opensrp.chw.core.fragment.CoreFamilyRemoveMemberFragment;
import com.opensrp.chw.core.utils.CoreConstants;
import com.opensrp.chw.hf.activity.FamilyRegisterActivity;
import com.opensrp.chw.hf.model.FamilyRemoveMemberModel;
import com.opensrp.chw.hf.presenter.FamilyRemoveMemberPresenter;
import com.opensrp.chw.hf.provider.HfFamilyRemoveMemberProvider;

import java.util.HashMap;
import java.util.Set;

import timber.log.Timber;

public class FamilyRemoveMemberFragment extends CoreFamilyRemoveMemberFragment {

    public static final String DIALOG_TAG = FamilyRemoveMemberFragment.class.getSimpleName();

    public static CoreFamilyRemoveMemberFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        FamilyRemoveMemberFragment fragment = new FamilyRemoveMemberFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        Timber.v(DIALOG_TAG, "setAdvancedSearchFormData");
    }

    @Override
    protected void setPresenter(String familyHead, String primaryCareGiver) {
        this.presenter = new FamilyRemoveMemberPresenter(this, new FamilyRemoveMemberModel(), null, familyBaseEntityId, familyHead, primaryCareGiver);
    }

    @Override
    protected CoreFamilyProfileChangeDialog getChangeFamilyCareGiverDialog() {
        return FamilyProfileChangeDialog.newInstance(getContext(), familyBaseEntityId,
                CoreConstants.PROFILE_CHANGE_ACTION.PRIMARY_CARE_GIVER);
    }

    @Override
    protected CoreFamilyProfileChangeDialog getChangeFamilyHeadDialog() {
        return FamilyProfileChangeDialog.newInstance(getContext(), familyBaseEntityId,
                CoreConstants.PROFILE_CHANGE_ACTION.HEAD_OF_FAMILY);
    }

    @Override
    protected void setRemoveMemberProvider(Set visibleColumns, String familyHead, String primaryCaregiver, String familyBaseEntityId) {
        this.removeMemberProvider = new HfFamilyRemoveMemberProvider(familyBaseEntityId, this.getActivity(),
                this.commonRepository(), visibleColumns, new RemoveMemberListener(), new FooterListener(), familyHead, primaryCaregiver);
    }

    @Override
    protected Class<? extends CoreFamilyRegisterActivity> getFamilyRegisterActivityClass() {
        return FamilyRegisterActivity.class;
    }

    @Override
    protected String getRemoveFamilyMemberDialogTag() {
        return FamilyRemoveMemberFragment.DIALOG_TAG;
    }

}
