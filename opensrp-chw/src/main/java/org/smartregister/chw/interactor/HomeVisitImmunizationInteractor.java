package org.smartregister.chw.interactor;

import android.text.TextUtils;
import android.util.Log;

import org.joda.time.DateTime;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.contract.HomeVisitImmunizationContract;
import org.smartregister.chw.model.VaccineTaskModel;
import org.smartregister.chw.util.ChwServiceSchedule;
import org.smartregister.chw.util.HomeVisitVaccineGroupDetails;
import org.smartregister.chw.util.ImmunizationState;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.family.util.DBConstants;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.util.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.smartregister.chw.util.Constants.IMMUNIZATION_CONSTANT.DATE;
import static org.smartregister.immunization.util.VaccinatorUtils.generateScheduleList;
import static org.smartregister.immunization.util.VaccinatorUtils.receivedVaccines;

public class HomeVisitImmunizationInteractor implements HomeVisitImmunizationContract.Interactor {
    private static String TAG = HomeVisitImmunizationInteractor.class.toString();

    public HomeVisitImmunizationInteractor() {
        // this(new AppExecutors());
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        Log.d(TAG,"onDestroy unimplemented");
    }

    @Override
    public HomeVisitVaccineGroupDetails getCurrentActiveHomeVisitVaccineGroupDetail(ArrayList<HomeVisitVaccineGroupDetails> allGroups) {
        HomeVisitVaccineGroupDetails currentActiveHomeVisit = null;
        int index = 0;
        for (HomeVisitVaccineGroupDetails toReturn : allGroups) {
            if (toReturn.getDueVaccines().size() > 0) {
                if (toReturn.getNotGivenInThisVisitVaccines().size() == 0 && toReturn.getGivenVaccines().size() == 0) {
                    if (!toReturn.getAlert().equals(ImmunizationState.NO_ALERT)) {
                        currentActiveHomeVisit = toReturn;
                        break;
                    }
                }
            }
            index++;
        }

        //check if this is not the last due group
        boolean completedExistsAfterCurrentGroup = false;
        if (index < allGroups.size() - 1) {
            for (int i = index + 1; i < allGroups.size(); i++) {
                HomeVisitVaccineGroupDetails toReturn = allGroups.get(i);
                if (toReturn.getDueVaccines().size() > 0) {
                    if ((toReturn.getNotGivenInThisVisitVaccines().size() > 0 || toReturn.getGivenVaccines().size() > 0)) {
                        if (!toReturn.getAlert().equals(ImmunizationState.NO_ALERT)) {
                            completedExistsAfterCurrentGroup = true;
                            break;
                        }
                    }
                }
            }
        }
        if (completedExistsAfterCurrentGroup) {
            currentActiveHomeVisit = null;
            for (int i = index + 1; i < allGroups.size(); i++) {
                HomeVisitVaccineGroupDetails toReturn = allGroups.get(i);
                if (toReturn.getDueVaccines().size() > 0) {
                    if ((toReturn.getNotGivenInThisVisitVaccines().size() > 0 || toReturn.getGivenVaccines().size() > 0)) {
                        if (!toReturn.getAlert().equals(ImmunizationState.NO_ALERT)) {
                            currentActiveHomeVisit = toReturn;
                            break;
                        }
                    }
                }
            }

        }

        return currentActiveHomeVisit;
    }


    @Override
    public HomeVisitVaccineGroupDetails getLastActiveHomeVisitVaccineGroupDetail(ArrayList<HomeVisitVaccineGroupDetails> allGroups) {
        HomeVisitVaccineGroupDetails toReturn = null;
        for (int i = 0; i < allGroups.size(); i++) {
            if (!allGroups.get(i).getAlert().equals(ImmunizationState.NO_ALERT)) {
                toReturn = allGroups.get(i);
            }
        }
        return toReturn;
    }

    @Override
    public boolean isPartiallyComplete(HomeVisitVaccineGroupDetails toprocess) {
        if (toprocess != null && toprocess.getDueVaccines() != null && toprocess.getDueVaccines().size() > 0) {
//            if(toprocess.getNotGivenInThisVisitVaccines().size()>0){
//                return true;
//            }
            if (toprocess.getGivenVaccines().size() < toprocess.getDueVaccines().size()) {
                if (toprocess.getGivenVaccines().size() > 0) {
                    return true;
                } else {
                    return toprocess.getNotGivenInThisVisitVaccines().size() == toprocess.getDueVaccines().size();
                }
            }
        }
        return false;
    }

    @Override
    public boolean isComplete(HomeVisitVaccineGroupDetails toprocess) {
        if (toprocess != null && toprocess.getDueVaccines() != null && toprocess.getDueVaccines().size() > 0) {
            if (toprocess.getGivenVaccines().size() == toprocess.getDueVaccines().size()) {
                return toprocess.getNotGivenInThisVisitVaccines().size() == 0;
            }
        }
        return false;
    }

    @Override
    public boolean groupIsDue(HomeVisitVaccineGroupDetails toprocess) {
        if (toprocess != null && toprocess.getDueVaccines() != null && toprocess.getDueVaccines().size() > 0) {
            if (toprocess.getGivenVaccines().size() == 0) {
                return toprocess.getNotGivenInThisVisitVaccines().size() == 0;
            }
        }
        return false;
    }

    @Override
    public boolean hasVaccinesNotGivenSinceLastVisit(ArrayList<HomeVisitVaccineGroupDetails> allGroup) {
        int indexofCurrentGroup = getIndexOfCurrentGroup(allGroup);
        if (isPartiallyComplete(allGroup.get(indexofCurrentGroup))) {
            indexofCurrentGroup = indexofCurrentGroup + 1;
        }
        for (int i = 0; i < indexofCurrentGroup + 1; i++) {
            HomeVisitVaccineGroupDetails toReturn = allGroup.get(i);
            if (
                    toReturn.getDueVaccines().size() > toReturn.getGivenVaccines().size()
                            && (toReturn.getNotGivenInThisVisitVaccines().size() <= 0)
            ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getIndexOfCurrentGroup(ArrayList<HomeVisitVaccineGroupDetails> allGroup) {
        HomeVisitVaccineGroupDetails currentActiveGroup = getCurrentActiveHomeVisitVaccineGroupDetail(allGroup);
        if (currentActiveGroup == null) {
            currentActiveGroup = getLastActiveHomeVisitVaccineGroupDetail(allGroup);
        }
        int indexofCurrentGroup = 0;
        for (int i = 0; i < allGroup.size(); i++) {
            HomeVisitVaccineGroupDetails toReturn = allGroup.get(i);
            if (toReturn.getDueVaccines().size() > 0) {
                if (toReturn.getGroup().equalsIgnoreCase(currentActiveGroup.getGroup())) {
                    indexofCurrentGroup = i;
                }
            }
        }
        return indexofCurrentGroup;
    }

    @Override
    public ArrayList<VaccineRepo.Vaccine> getNotGivenVaccinesLastVisitList(ArrayList<HomeVisitVaccineGroupDetails> allGroup) {
        ArrayList<VaccineRepo.Vaccine> toReturn = new ArrayList<>();
        if (hasVaccinesNotGivenSinceLastVisit(allGroup)) {
            int indexOfCurrentGroup = getIndexOfCurrentGroup(allGroup);
            if (isPartiallyComplete(allGroup.get(indexOfCurrentGroup))) {
                indexOfCurrentGroup = indexOfCurrentGroup + 1;
            }
            for (int i = 0; i < indexOfCurrentGroup; i++) {
//                allGroup.get(i).calculateNotGivenVaccines();
                toReturn.addAll(getNotGivenVaccinesNotInNotGivenThisVisit(allGroup.get(i)));
            }
        }
        return toReturn;
    }

    @Override
    public ArrayList<VaccineRepo.Vaccine> getNotGivenVaccinesNotInNotGivenThisVisit(HomeVisitVaccineGroupDetails allGroup) {
        ArrayList<VaccineRepo.Vaccine> getNotGivenVaccinesNotInNotGivenThisVisit = new ArrayList<>();
        for (VaccineRepo.Vaccine toProcess : allGroup.getNotGivenVaccines()) {
            boolean isInNotGivenThisVisit = false;
            for (VaccineRepo.Vaccine notGivenThisVisit : allGroup.getNotGivenInThisVisitVaccines()) {
                if (notGivenThisVisit.display().equalsIgnoreCase(toProcess.display())) {
                    isInNotGivenThisVisit = true;
                }
            }
            if (!isInNotGivenThisVisit) {
                getNotGivenVaccinesNotInNotGivenThisVisit.add(toProcess);
            }
        }

        return getNotGivenVaccinesNotInNotGivenThisVisit;
    }

    @Override
    public ArrayList<HomeVisitVaccineGroupDetails> determineAllHomeVisitVaccineGroupDetails(List<Alert> alerts, List<Vaccine> vaccines, ArrayList<VaccineWrapper> notGivenVaccines, List<Map<String, Object>> sch) {
        ArrayList<HomeVisitVaccineGroupDetails> homeVisitVaccineGroupDetailsArrayList = new ArrayList<>();
        Map<String, Date> receivedvaccines = receivedVaccines(vaccines);
        List<VaccineRepo.Vaccine> vList = Arrays.asList(VaccineRepo.Vaccine.values());
        ArrayList<String> vaccineGroupName = new ArrayList<>();
        for (VaccineRepo.Vaccine vaccine : vList) {
            if (vaccine.category().equalsIgnoreCase("child")) {
                if (!isEmpty(VaccinateActionUtils.stateKey(vaccine)) && !vaccineGroupName.contains(VaccinateActionUtils.stateKey(vaccine))) {
                    vaccineGroupName.add(VaccinateActionUtils.stateKey(vaccine));
                }
            }
        }

        for (int i = 0; i < vaccineGroupName.size(); i++) {
            HomeVisitVaccineGroupDetails homeVisitVaccineGroupDetails = new HomeVisitVaccineGroupDetails();
            homeVisitVaccineGroupDetails.setGroup(vaccineGroupName.get(i));
            homeVisitVaccineGroupDetailsArrayList.add(homeVisitVaccineGroupDetails);
        }
        assignDueVaccine(vList, homeVisitVaccineGroupDetailsArrayList, alerts);
        assignGivenVaccine(homeVisitVaccineGroupDetailsArrayList, receivedvaccines);
        assignDate(homeVisitVaccineGroupDetailsArrayList, sch);

        for (HomeVisitVaccineGroupDetails singlegroup : homeVisitVaccineGroupDetailsArrayList) {
            for (int i = 0; i < singlegroup.getDueVaccines().size(); i++) {
                for (VaccineWrapper notgivenVaccine : notGivenVaccines) {
                    if (singlegroup.getDueVaccines().get(i).display().equalsIgnoreCase(notgivenVaccine.getName())) {
                        singlegroup.getNotGivenInThisVisitVaccines().add(notgivenVaccine.getVaccine());
                    }
                }
            }
        }

        return homeVisitVaccineGroupDetailsArrayList;
    }

    private void assignDate(ArrayList<HomeVisitVaccineGroupDetails> homeVisitVaccineGroupDetailsArrayList, List<Map<String, Object>> sch) {
        for (int i = 0; i < homeVisitVaccineGroupDetailsArrayList.size(); i++) {
            for (VaccineRepo.Vaccine vaccine : homeVisitVaccineGroupDetailsArrayList.get(i).getDueVaccines()) {
                for (Map<String, Object> toprocess : sch) {
                    if (((VaccineRepo.Vaccine) (toprocess.get("vaccine"))).name().equalsIgnoreCase(vaccine.name())) {
                        DateTime dueDate = (DateTime) toprocess.get(DATE);
                        homeVisitVaccineGroupDetailsArrayList.get(i).setDueDate(dueDate.toLocalDate() + "");
                        String duedateString = DateUtil.formatDate(dueDate.toLocalDate(), "dd MMM yyyy");
                        homeVisitVaccineGroupDetailsArrayList.get(i).setDueDisplayDate(duedateString);
                    }
                    toprocess.size();
                }
            }
        }
    }

    private void assignGivenVaccine(ArrayList<HomeVisitVaccineGroupDetails> homeVisitVaccineGroupDetailsArrayList, Map<String, Date> receivedvaccines) {
        for (int i = 0; i < homeVisitVaccineGroupDetailsArrayList.size(); i++) {
            ArrayList<VaccineRepo.Vaccine> dueVaccines = homeVisitVaccineGroupDetailsArrayList.get(i).getDueVaccines();
            for (VaccineRepo.Vaccine checkVaccine : dueVaccines) {
                if (isReceived(checkVaccine.display(), receivedvaccines)) {
                    homeVisitVaccineGroupDetailsArrayList.get(i).getGivenVaccines().add(checkVaccine);
                }
            }
            homeVisitVaccineGroupDetailsArrayList.get(i).calculateNotGivenVaccines();
        }
    }

    private void assignDueVaccine(List<VaccineRepo.Vaccine> vList, ArrayList<HomeVisitVaccineGroupDetails> homeVisitVaccineGroupDetailsArrayList, List<Alert> alerts) {
        for (int i = 0; i < homeVisitVaccineGroupDetailsArrayList.size(); i++) {
            for (VaccineRepo.Vaccine vaccine : vList) {
                if (VaccinateActionUtils.stateKey(vaccine).equalsIgnoreCase(homeVisitVaccineGroupDetailsArrayList.get(i).getGroup())) {
                    if (hasAlert(vaccine, alerts)) {
                        homeVisitVaccineGroupDetailsArrayList.get(i).getDueVaccines().add(vaccine);
                        ImmunizationState state = assignAlert(vaccine, alerts);
                        if (state == (ImmunizationState.DUE) || state == (ImmunizationState.OVERDUE)
                                || state == (ImmunizationState.UPCOMING) || state == (ImmunizationState.EXPIRED)) {
                            homeVisitVaccineGroupDetailsArrayList.get(i).setAlert(state);
                        }
                    }
                }
            }
        }
    }

    private boolean hasAlert(VaccineRepo.Vaccine vaccine, List<Alert> alerts) {
        for (Alert alert : alerts) {
            if (alert.scheduleName().equalsIgnoreCase(vaccine.display())) {
                return true;
            }
        }
        return false;
    }

    private ImmunizationState alertState(Alert toProcess) {
        if (toProcess == null) {
            return ImmunizationState.NO_ALERT;
        } else if (toProcess.status().value().equalsIgnoreCase(ImmunizationState.NORMAL.name())) {
            return ImmunizationState.DUE;
        } else if (toProcess.status().value().equalsIgnoreCase(ImmunizationState.UPCOMING.name())) {
            return ImmunizationState.UPCOMING;
        } else if (toProcess.status().value().equalsIgnoreCase(ImmunizationState.URGENT.name())) {
            return ImmunizationState.OVERDUE;
        } else if (toProcess.status().value().equalsIgnoreCase(ImmunizationState.EXPIRED.name())) {
            return ImmunizationState.EXPIRED;
        }
        return ImmunizationState.NO_ALERT;
    }

    private boolean isReceived(String s, Map<String, Date> receivedvaccines) {
        for (String name : receivedvaccines.keySet()) {
            if (s.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public ImmunizationState assignAlert(VaccineRepo.Vaccine vaccine, List<Alert> alerts) {
        for (Alert alert : alerts) {
            if (alert.scheduleName().equalsIgnoreCase(vaccine.display())) {
                return alertState(alert);
            }
        }
        return ImmunizationState.NO_ALERT;
    }

    @Override
    public void updateImmunizationState(CommonPersonObjectClient childClient, ArrayList<VaccineWrapper> notGivenVaccines, final HomeVisitImmunizationContract.InteractorCallBack callBack) {

        getVaccineTask(childClient.getColumnmaps(),childClient.getCaseId(),notGivenVaccines)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<VaccineTaskModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(VaccineTaskModel vaccineTaskModel) {
                        callBack.immunizationState(vaccineTaskModel.getAlerts(), vaccineTaskModel.getVaccines()
                                ,vaccineTaskModel.getReceivedVaccines(), vaccineTaskModel.getScheduleList());

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                    }
                });


    }

    /**
     * Replacement of previous vaccinationasynctask
     * it'll calculate the received vaccine list of a child.
     * @param getColumnMaps
     * @param entityId
     * @param notDoneVaccines
     * @return
     */
    private Observable<VaccineTaskModel> getVaccineTask(final Map<String, String> getColumnMaps,final String entityId,final ArrayList<VaccineWrapper> notDoneVaccines){

        return Observable.create(new ObservableOnSubscribe<VaccineTaskModel>() {
            @Override
            public void subscribe(ObservableEmitter<VaccineTaskModel> emmiter) throws Exception {
                String dobString = org.smartregister.util.Utils.getValue(getColumnMaps, DBConstants.KEY.DOB, false);
                DateTime dob = org.smartregister.chw.util.Utils.dobStringToDateTime(dobString);
                if (dob == null) {
                    dob = new DateTime();
                }

                if (!TextUtils.isEmpty(dobString)) {
                    DateTime dateTime = new DateTime(dobString);
                    try{
                        VaccineSchedule.updateOfflineAlerts(entityId, dateTime, "child");
                    }catch (Exception e){

                    }
                    try{
                        ChwServiceSchedule.updateOfflineAlerts(entityId, dateTime);
                    }catch (Exception e){

                    }
                }
                List<Alert> alerts= ChwApplication.getInstance().getContext().alertService().findByEntityIdAndAlertNames(entityId, VaccinateActionUtils.allAlertNames("child"));
                List<Vaccine> vaccines = ChwApplication.getInstance().vaccineRepository().findByEntityId(entityId);
                Map<String, Date> recievedVaccines = receivedVaccines(vaccines);
                int size = notDoneVaccines.size();
                for (int i = 0; i < size; i++) {
                    recievedVaccines.put(notDoneVaccines.get(i).getName().toLowerCase(), new Date());
                }

                List<Map<String, Object>> sch = generateScheduleList("child", dob, recievedVaccines, alerts);
                VaccineTaskModel vaccineTaskModel = new VaccineTaskModel();
                vaccineTaskModel.setAlerts(alerts);
                vaccineTaskModel.setVaccines(vaccines);
                vaccineTaskModel.setReceivedVaccines(recievedVaccines);
                vaccineTaskModel.setScheduleList(sch);
                emmiter.onNext(vaccineTaskModel);
            }
        });
    }
}
