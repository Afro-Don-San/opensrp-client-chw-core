package org.smartregister.chw.interactor;

import android.content.Context;

import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.ANCCardAction;
import org.smartregister.chw.actionhelper.ANCCounselingAction;
import org.smartregister.chw.actionhelper.DangerSignsAction;
import org.smartregister.chw.actionhelper.HealthFacilityVisitAction;
import org.smartregister.chw.actionhelper.IPTPAction;
import org.smartregister.chw.actionhelper.ObservationAction;
import org.smartregister.chw.actionhelper.SleepingUnderLLITNAction;
import org.smartregister.chw.actionhelper.TTAction;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.fragment.BaseAncHomeVisitFragment;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.model.VaccineTaskModel;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.ContactUtil;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineWrapper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.chw.util.RecurringServiceUtil.getRecurringServices;

public abstract class DefaultAncHomeVisitInteractorFlv implements AncHomeVisitInteractor.Flavor {

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EventType.ANC_HOME_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        // get contact schedule
        Map<Integer, LocalDate> dateMap = ContactUtil.getContactSchedule(memberObject);

        // get vaccine schedule if ga > 13
        VaccineTaskModel vaccineTaskModel = null;

        DateTime lastMenstrualPeriod = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(memberObject.getLastMenstrualPeriod());
        int ga = Days.daysBetween(lastMenstrualPeriod, new DateTime()).getDays() / 7;

        if (ga >= 13) {
            vaccineTaskModel = getWomanVaccine(memberObject.getBaseEntityId(), lastMenstrualPeriod, getNotGivenVaccines());
        }

        try {
            evaluateDangerSigns(actionList, details, context);
            evaluateANCCounseling(actionList, details, memberObject, dateMap, context);
            evaluateSleepingUnderLLITN(view, actionList, details, context);
            evaluateANCCard(view, memberObject, actionList, details, context);
            evaluateHealthFacilityVisit(actionList, details, memberObject, dateMap, context);
            evaluateTTImmunization(view, actionList, details, memberObject, vaccineTaskModel, context);
            evaluateIPTP(view, actionList, details, memberObject, context);
            evaluateObservation(actionList, details, context);
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }

        return actionList;
    }

    private JSONObject getJson(String formName, String baseEntityID) throws Exception {
        String locationId = ChwApplication.getInstance().getContext().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
        JSONObject jsonObject = JsonFormUtils.getFormAsJson(formName);
        JsonFormUtils.getRegistrationForm(jsonObject, baseEntityID, locationId);
        return jsonObject;
    }

    private void evaluateDangerSigns(LinkedHashMap<String, BaseAncHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, final Context context) throws Exception {
        BaseAncHomeVisitAction danger_signs = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_danger_signs))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_danger_signs), danger_signs);
    }

    private void evaluateANCCounseling(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                       Map<String, List<VisitDetail>> details,
                                       MemberObject memberObject,
                                       Map<Integer, LocalDate> dateMap,
                                       final Context context) throws Exception {
        BaseAncHomeVisitAction counseling = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_counseling))
                .withOptional(false)
                .withDetails(details)
                .withHelper(new ANCCounselingAction(memberObject, dateMap))
                .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getAncCounseling())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_counseling), counseling);
    }

    private void evaluateSleepingUnderLLITN(BaseAncHomeVisitContract.View view,
                                            LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                            Map<String, List<VisitDetail>> details,
                                            final Context context) throws Exception {
        BaseAncHomeVisitAction sleeping = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_sleeping_under_llitn_net))
                .withOptional(false)
                .withDetails(details)
                .withHelper(new SleepingUnderLLITNAction())
                .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, Constants.JSON_FORM.ANC_HOME_VISIT.getSleepingUnderLlitn(), null, details, null))
                .build();

        actionList.put(context.getString(R.string.anc_home_visit_sleeping_under_llitn_net), sleeping);
    }

    private void evaluateANCCard(BaseAncHomeVisitContract.View view,
                                 MemberObject memberObject,
                                 LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                 Map<String, List<VisitDetail>> details,
                                 final Context context) throws Exception {
        if (memberObject.getHasAncCard() != null && memberObject.getHasAncCard().equals("Yes"))
            return;

        BaseAncHomeVisitAction anc_card = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_anc_card_received))
                .withOptional(false)
                .withDetails(details)
                .withHelper(new ANCCardAction())
                .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, Constants.JSON_FORM.ANC_HOME_VISIT.getAncCardReceived(), null, details, null))
                .build();

        actionList.put(context.getString(R.string.anc_home_visit_anc_card_received), anc_card);
    }

    private void evaluateHealthFacilityVisit(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                             Map<String, List<VisitDetail>> details,
                                             final MemberObject memberObject,
                                             Map<Integer, LocalDate> dateMap, final Context context) throws Exception {
        String visit_title = MessageFormat.format(context.getString(R.string.anc_home_visit_facility_visit), memberObject.getConfirmedContacts() + 1);
        BaseAncHomeVisitAction facility_visit = new BaseAncHomeVisitAction.Builder(context, visit_title)
                .withOptional(false)
                .withDetails(details)
                .withHelper(new HealthFacilityVisitAction(memberObject, dateMap))
                .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getHealthFacilityVisit())
                .build();

        actionList.put(visit_title, facility_visit);
    }

    private void evaluateTTImmunization(BaseAncHomeVisitContract.View view,
                                        LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                        Map<String, List<VisitDetail>> details,
                                        final MemberObject memberObject,
                                        VaccineTaskModel vaccineTaskModel,
                                        final Context context) throws Exception {
        // if there are no pending vaccines
        if (vaccineTaskModel == null || vaccineTaskModel.getScheduleList().size() < 1) {
            return;
        }
        // compute the due date
        final Triple<DateTime, VaccineRepo.Vaccine, String> individualVaccine = ContactUtil.getIndividualVaccine(vaccineTaskModel, "TT");
        if (individualVaccine == null || individualVaccine.getLeft().isAfter(new DateTime())) {
            return;
        }

        String title = MessageFormat.format(context.getString(R.string.anc_home_visit_tt_immunization), individualVaccine.getRight());
        int overdueMonth = new Period(individualVaccine.getLeft(), new DateTime()).getMonths();
        String dueState = (overdueMonth < 1) ? context.getString(R.string.due) : context.getString(R.string.overdue);

        TTAction helper = new TTAction(individualVaccine, context);
        JSONObject jsonObject = getJson(Constants.JSON_FORM.ANC_HOME_VISIT.getTtImmunization(), memberObject.getBaseEntityId());
        JSONObject preProcessObject = helper.preProcess(jsonObject, individualVaccine.getRight());

        BaseAncHomeVisitAction tt_immunization = new BaseAncHomeVisitAction.Builder(context, title)
                .withHelper(helper)
                .withDetails(details)
                .withOptional(false)
                .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, null, preProcessObject, details, individualVaccine.getRight()))
                .withVaccineWrapper(getVaccineWrapper(individualVaccine.getMiddle(), vaccineTaskModel))
                .withScheduleStatus((overdueMonth < 1) ? BaseAncHomeVisitAction.ScheduleStatus.DUE : BaseAncHomeVisitAction.ScheduleStatus.OVERDUE)
                .withSubtitle(MessageFormat.format("{0} {1}", dueState, DateTimeFormat.forPattern("dd MMM yyyy").print(new DateTime(individualVaccine.getLeft()))))
                .build();

        // don't show if its after now
        if (!individualVaccine.getLeft().isAfterNow()) {
            actionList.put(title, tt_immunization);
        }
    }

    private void evaluateIPTP(BaseAncHomeVisitContract.View view, LinkedHashMap<String, BaseAncHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, final Context context) throws Exception {
        // if there are no pending vaccines
        DateTime lmp = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(memberObject.getLastMenstrualPeriod());
        Map<String, ServiceWrapper> serviceWrapperMap = getRecurringServices(memberObject.getBaseEntityId(), lmp);
        ServiceWrapper serviceWrapper = serviceWrapperMap.get("IPTp-SP");

        if (serviceWrapper == null) {
            return;
        }

        final String serviceIteration = serviceWrapper.getName().substring(serviceWrapper.getName().length() - 1);

        String iptp = MessageFormat.format(context.getString(R.string.anc_home_visit_iptp_sp), serviceIteration);
        int overdueMonth = new Period(serviceWrapper.getVaccineDate(), new DateTime()).getMonths();
        String dueState = (overdueMonth < 1) ? context.getString(R.string.due) : context.getString(R.string.overdue);

        IPTPAction helper = new IPTPAction(context, serviceIteration);
        JSONObject jsonObject = getJson(Constants.JSON_FORM.ANC_HOME_VISIT.getIptpSp(), memberObject.getBaseEntityId());
        JSONObject preProcessObject = helper.preProcess(jsonObject, serviceIteration);

        BaseAncHomeVisitAction iptp_action = new BaseAncHomeVisitAction.Builder(context, iptp)
                .withHelper(helper)
                .withDetails(details)
                .withOptional(false)
                .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, null, preProcessObject, details, serviceIteration))
                .withServiceWrapper(serviceWrapper)
                .withScheduleStatus((overdueMonth < 1) ? BaseAncHomeVisitAction.ScheduleStatus.DUE : BaseAncHomeVisitAction.ScheduleStatus.OVERDUE)
                .withSubtitle(MessageFormat.format("{0} {1}", dueState, DateTimeFormat.forPattern("dd MMM yyyy").print(new DateTime(serviceWrapper.getVaccineDate()))))
                .build();

        // don't show if its after now
        if (!serviceWrapper.getVaccineDate().isAfterNow()) {
            actionList.put(iptp, iptp_action);
        }
    }

    private void evaluateObservation(LinkedHashMap<String, BaseAncHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, final Context context) throws Exception {
        BaseAncHomeVisitAction observation = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_observations_n_illnes))
                .withOptional(true)
                .withDetails(details)
                .withHelper(new ObservationAction())
                .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getObservationAndIllness())
                .build();

        actionList.put(context.getString(R.string.anc_home_visit_observations_n_illnes), observation);
    }

    public VaccineTaskModel getWomanVaccine(String baseEntityID, DateTime lmpDate, List<VaccineWrapper> notDoneVaccines) {
        return ContactUtil.getWomanVaccine(baseEntityID, lmpDate, notDoneVaccines);
    }

    // read vaccine repo for all not given vaccines
    private List<VaccineWrapper> getNotGivenVaccines() {
        return new ArrayList<>();
    }

    private VaccineWrapper getVaccineWrapper(VaccineRepo.Vaccine vaccine, VaccineTaskModel vaccineTaskModel) {
        VaccineWrapper vaccineWrapper = new VaccineWrapper();
        vaccineWrapper.setVaccine(vaccine);
        vaccineWrapper.setName(vaccine.display());
        vaccineWrapper.setDbKey(getVaccineId(vaccine.display(), vaccineTaskModel));
        vaccineWrapper.setDefaultName(vaccine.display());
        return vaccineWrapper;
    }

    private Long getVaccineId(String vaccineName, VaccineTaskModel vaccineTaskModel) {
        for (Vaccine vaccine : vaccineTaskModel.getVaccines()) {
            if (vaccine.getName().equalsIgnoreCase(vaccineName)) {
                return vaccine.getId();
            }
        }
        return null;
    }

}

