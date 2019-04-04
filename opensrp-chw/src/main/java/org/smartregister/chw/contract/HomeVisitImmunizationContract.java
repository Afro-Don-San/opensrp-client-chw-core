package org.smartregister.chw.contract;

import android.app.Activity;
import android.content.Context;

import org.smartregister.chw.interactor.HomeVisitImmunizationInteractor;
import org.smartregister.chw.util.HomeVisitVaccineGroupDetails;
import org.smartregister.chw.util.ImmunizationState;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineWrapper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface HomeVisitImmunizationContract {
    interface View extends InteractorCallBack {

        void setActivity(Activity activity);

        void setChildClient(CommonPersonObjectClient childClient);

        void refreshPresenter(List<Alert> alerts, List<Vaccine> vaccines, List<Map<String, Object>> sch);

        Presenter initializePresenter();

        Presenter getPresenter();

        Context getContext();

        void updateImmunizationState();
    }

    interface Presenter {

        void createAllVaccineGroups(List<Alert> alerts, List<Vaccine> vaccines, List<Map<String, Object>> sch);

        void getVaccinesNotGivenLastVisit();

        void calculateCurrentActiveGroup();

        HomeVisitImmunizationContract.View getView();

        void onDestroy(boolean isChangingConfiguration);

        boolean isPartiallyComplete();

        boolean isComplete();

        Interactor getHomeVisitImmunizationInteractor();
        void setView(WeakReference<View> view);

        ArrayList<VaccineRepo.Vaccine> getVaccinesDueFromLastVisit();
        ArrayList<HomeVisitVaccineGroupDetails> getAllgroups();

        ArrayList<VaccineWrapper> getNotGivenVaccines();
        HomeVisitVaccineGroupDetails getCurrentActiveGroup();

        boolean groupIsDue();

        ArrayList<VaccineWrapper> createVaccineWrappers(HomeVisitVaccineGroupDetails duevaccines);

        CommonPersonObjectClient getchildClient();

        void setChildClient(CommonPersonObjectClient childClient);

        void updateNotGivenVaccine(VaccineWrapper name);

        ArrayList<VaccineWrapper> getVaccinesGivenThisVisit();

        void assigntoGivenVaccines(ArrayList<VaccineWrapper> tagsToUpdate);

        void updateImmunizationState(InteractorCallBack callBack);

        ArrayList<VaccineRepo.Vaccine> getVaccinesDueFromLastVisitStillDueState();

        boolean isSingleVaccineGroupPartialComplete();

        boolean isSingleVaccineGroupComplete();

        void setGroupVaccineText(List<Map<String, Object>> sch);

        void setSingleVaccineText(ArrayList<VaccineRepo.Vaccine> vaccinesDueFromLastVisit, List<Map<String, Object>> sch);

        String getGroupImmunizationSecondaryText();

        void setGroupImmunizationSecondaryText(String groupImmunizationSecondaryText);

        String getSingleImmunizationSecondaryText();

        void setSingleImmunizationSecondaryText(String singleImmunizationSecondaryText);
    }

    interface Interactor {

        void onDestroy(boolean isChangingConfiguration);

        HomeVisitVaccineGroupDetails getCurrentActiveHomeVisitVaccineGroupDetail(ArrayList<HomeVisitVaccineGroupDetails> allGroups);

        HomeVisitVaccineGroupDetails getLastActiveHomeVisitVaccineGroupDetail(ArrayList<HomeVisitVaccineGroupDetails> allGroups);

        boolean isPartiallyComplete(HomeVisitVaccineGroupDetails toprocess);

        boolean isComplete(HomeVisitVaccineGroupDetails toprocess);

        boolean groupIsDue(HomeVisitVaccineGroupDetails toprocess);

        boolean hasVaccinesNotGivenSinceLastVisit(ArrayList<HomeVisitVaccineGroupDetails> allGroup);

        int getIndexOfCurrentGroup(ArrayList<HomeVisitVaccineGroupDetails> allGroup);

        ArrayList<VaccineRepo.Vaccine> getNotGivenVaccinesLastVisitList(ArrayList<HomeVisitVaccineGroupDetails> allGroup);

        ArrayList<VaccineRepo.Vaccine> getNotGivenVaccinesNotInNotGivenThisVisit(HomeVisitVaccineGroupDetails allGroup);

        ArrayList<HomeVisitVaccineGroupDetails> determineAllHomeVisitVaccineGroupDetails(List<Alert> alerts, List<Vaccine> vaccines, ArrayList<VaccineWrapper> notGivenVaccines, List<Map<String, Object>> sch);

        void updateImmunizationState(CommonPersonObjectClient childClient, ArrayList<VaccineWrapper> notGivenVaccines, InteractorCallBack callBack);
    }

    interface InteractorCallBack {
        void immunizationState(List<Alert> alerts, List<Vaccine> vaccines,Map<String, Date> receivedVaccine , List<Map<String, Object>> sch);
    }
}
