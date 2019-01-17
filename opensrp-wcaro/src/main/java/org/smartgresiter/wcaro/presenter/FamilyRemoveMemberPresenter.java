package org.smartgresiter.wcaro.presenter;

import org.json.JSONObject;
import org.smartgresiter.wcaro.contract.FamilyRemoveMemberContract;
import org.smartgresiter.wcaro.interactor.FamilyRemoveMemberInteractor;
import org.smartgresiter.wcaro.util.Constants;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.view.LocationPickerView;

import java.lang.ref.WeakReference;
import java.util.Map;

public class FamilyRemoveMemberPresenter extends FamilyProfileMemberPresenter implements FamilyRemoveMemberContract.Presenter {

    FamilyRemoveMemberContract.Model model;
    protected WeakReference<FamilyRemoveMemberContract.View> viewReference;
    FamilyRemoveMemberContract.Interactor interactor;

    private String familyHead;
    private String primaryCaregiver;

    public FamilyRemoveMemberPresenter(FamilyRemoveMemberContract.View view, FamilyRemoveMemberContract.Model model, String viewConfigurationIdentifier, String familyBaseEntityId, String familyHead, String primaryCaregiver) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId, familyHead, primaryCaregiver);

        this.model = model;
        this.viewReference = new WeakReference<>(view);
        this.interactor = new FamilyRemoveMemberInteractor();
        this.familyHead = familyHead;
        this.primaryCaregiver = primaryCaregiver;
    }


    @Override
    public void removeMember(CommonPersonObjectClient client) {

        String memberID = client.getColumnmaps().get(DBConstants.KEY.BASE_ENTITY_ID);
        if (memberID.equalsIgnoreCase(familyHead) ||
                memberID.equalsIgnoreCase(primaryCaregiver)) {

            interactor.processFamilyMember(familyBaseEntityId, client, this);

        } else {

            JSONObject form = model.prepareJsonForm(client);
            if (form != null) {

                LocationPickerView lpv = new LocationPickerView(viewReference.get().getContext());
                lpv.init();
                String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());

                interactor.removeMember(familyBaseEntityId, memberID, lastLocationId);
                viewReference.get().startJsonActivity(form);
            }
        }
    }

    @Override
    public void processMember(Map<String, String> familyDetails, CommonPersonObjectClient client) {
        String memberID = client.getColumnmaps().get(DBConstants.KEY.BASE_ENTITY_ID);
        String currentFamilyHead = familyDetails.get(Constants.RELATIONSHIP.FAMILY_HEAD);
        String currentCareGiver = familyDetails.get(Constants.RELATIONSHIP.PRIMARY_CAREGIVER);

        if(memberID != null){
            if (memberID.equalsIgnoreCase(currentFamilyHead)) {

                if(viewReference.get() != null){
                    viewReference.get().displayChangeFamilyHeadDialog(client);
                }

            }else if(memberID.equalsIgnoreCase(currentCareGiver)){

                if(viewReference.get() != null){
                    viewReference.get().displayChangeCareGiverDialog(client);
                }

            }
        }
    }

    @Override
    public void removeEveryone() {

        LocationPickerView lpv = new LocationPickerView(viewReference.get().getContext());
        lpv.init();
        String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());

        interactor.removeFamily(familyBaseEntityId, lastLocationId, this);

    }

    @Override
    public void onFamilyRemoved(Boolean success) {
        if(success){
            // close
            if(viewReference.get() != null){
                viewReference.get().goToPrevious();
            }
        }
    }

}