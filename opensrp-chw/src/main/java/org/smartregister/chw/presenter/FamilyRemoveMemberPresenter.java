package org.smartregister.chw.presenter;

import com.opensrp.chw.core.contract.FamilyRemoveMemberContract;
import com.opensrp.chw.core.presenter.CoreFamilyRemoveMemberPresenter;

import org.smartregister.chw.interactor.FamilyRemoveMemberInteractor;

public class FamilyRemoveMemberPresenter extends CoreFamilyRemoveMemberPresenter {
    public FamilyRemoveMemberPresenter(FamilyRemoveMemberContract.View view, FamilyRemoveMemberContract.Model model, String viewConfigurationIdentifier, String familyBaseEntityId, String familyHead, String primaryCaregiver) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId, familyHead, primaryCaregiver);
        setInteractor(FamilyRemoveMemberInteractor.getInstance());
    }
}