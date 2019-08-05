package org.smartregister.chw.interactor;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.DangerSignsAction;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.dao.PNCDao;
import org.smartregister.chw.dao.PersonDao;
import org.smartregister.chw.domain.PNCHealthFacilityVisitSummary;
import org.smartregister.chw.domain.Person;
import org.smartregister.chw.rule.PNCHealthFacilityVisitRule;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.PNCVisitUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import timber.log.Timber;

import static org.smartregister.chw.util.JsonFormUtils.getCheckBoxValue;
import static org.smartregister.chw.util.JsonFormUtils.getValue;

public class PncHomeVisitInteractorFlv extends DefaultPncHomeVisitInteractorFlv {

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        actionList = new LinkedHashMap<>();
        context = view.getContext();
        this.memberObject = memberObject;

        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EventType.PNC_HOME_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        children = PersonDao.getMothersChildren(memberObject.getBaseEntityId());
        if (children == null)
            children = new ArrayList<>();

        try {
            evaluateDangerSignsMother();
            evaluateDangerSignsBaby();
            evaluatePNCHealthFacilityVisit();
            evaluateFamilyPlanning();
            evaluateImmunization();
            evaluateExclusiveBreastFeeding();
            evaluateCounselling();
            evaluateNutritionStatusMother();
            evaluateNutritionStatusBaby();
            evaluateMalariaPrevention();
            evaluateObsIllnessMother();
            evaluateObsIllnessBaby();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
        return actionList;
    }

    private void evaluateDangerSignsMother() throws Exception {

        HomeVisitActionHelper pncDangerSignsMotherHelper = new HomeVisitActionHelper() {
            private String danger_signs_present_mama;

            @Override
            public void onPayloadReceived(String s) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    danger_signs_present_mama = getCheckBoxValue(jsonObject, "danger_signs_present_mama");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", context.getString(R.string.anc_home_visit_danger_signs), danger_signs_present_mama);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(danger_signs_present_mama)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_danger_signs_mother))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSignsMother())
                .withHelper(pncDangerSignsMotherHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_danger_signs_mother), action);
    }

    private void evaluateDangerSignsBaby() throws Exception {
        class PNCDangerSignsBabyHelper extends HomeVisitActionHelper {
            private String danger_signs_present_child;

            @Override
            public void onPayloadReceived(String s) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    danger_signs_present_child = getCheckBoxValue(jsonObject, "danger_signs_present_child");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", context.getString(R.string.anc_home_visit_danger_signs), danger_signs_present_child);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(danger_signs_present_child)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        }

        for (Person baby : children) {
            BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, MessageFormat.format(context.getString(R.string.pnc_danger_signs_baby), baby.getFullName()))
                    .withOptional(false)
                    .withDetails(details)
                    .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSignsBaby())
                    .withHelper(new PNCDangerSignsBabyHelper())
                    .build();
            actionList.put(MessageFormat.format(context.getString(R.string.pnc_danger_signs_baby), baby.getFullName()), action);
        }
    }

    protected void evaluatePNCHealthFacilityVisit() throws Exception {

        PNCHealthFacilityVisitSummary summary = PNCDao.getLastHealthFacilityVisitSummary(memberObject.getBaseEntityId());
        if (summary != null) {
            PNCHealthFacilityVisitRule visitRule = PNCVisitUtil.getNextPNCHealthFacilityVisit(summary.getDeliveryDate(), summary.getLastVisitDate());

            if(visitRule != null && visitRule.getVisitName() != null){
                String title;
                switch (visitRule.getVisitName()) {
                    case "2":
                        title = context.getString(R.string.pnc_health_facility_visit_days_three_to_seven);
                        break;
                    case "3":
                        title = context.getString(R.string.pnc_health_facility_visit_days_eight_to_twenty_eight);
                        break;
                    case "4":
                        title = context.getString(R.string.pnc_health_facility_visit_days_twenty_nine_to_forty_two);
                        break;
                    default:
                        title = context.getString(R.string.pnc_health_facility_visit_within_fourty_eight_hours);
                        break;
                }

                BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, title)
                        .withOptional(false)
                        .withDetails(details)
                        .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getHealthFacilityVisit())
                        .withHelper(new PNCHealthFacilityVisitHelper())
                        .build();
                actionList.put(title, action);
            }
        }
    }

    private void evaluateFamilyPlanning() throws Exception {
        HomeVisitActionHelper familyPlanningHelper = new HomeVisitActionHelper() {
            private String fp_counseling;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    fp_counseling = getValue(jsonObject, "fp_counseling");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", "Family Planning", fp_counseling);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(fp_counseling)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_family_planning))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getFamilyPlanning())
                .withHelper(familyPlanningHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_family_planning), action);
    }

    protected void evaluateImmunization() throws Exception {
        for (Person baby : children) {
            BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, MessageFormat.format(context.getString(R.string.pnc_immunization_at_birth), baby.getFullName()))
                    .withOptional(false)
                    .withDetails(details)
                    .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                    .withHelper(new DangerSignsAction())
                    .build();
            actionList.put(MessageFormat.format(context.getString(R.string.pnc_immunization_at_birth), baby.getFullName()), action);
        }
    }

    private void evaluateExclusiveBreastFeeding() throws Exception {
        for (Person baby : children) {
            BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, MessageFormat.format(context.getString(R.string.pnc_exclusive_breastfeeding), baby.getFullName()))
                    .withOptional(false)
                    .withDetails(details)
                    .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                    .withHelper(new DangerSignsAction())
                    .build();
            actionList.put(MessageFormat.format(context.getString(R.string.pnc_exclusive_breastfeeding), baby.getFullName()), action);
        }
    }

    private void evaluateCounselling() throws Exception {
        HomeVisitActionHelper counsellingHelper = new HomeVisitActionHelper() {
            private String couselling_pnc;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    couselling_pnc = getCheckBoxValue(jsonObject, "couselling_pnc");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", "Counselling", couselling_pnc);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(couselling_pnc)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_counselling))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getCOUNSELLING())
                .withHelper(counsellingHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_counselling), action);
    }

    private void evaluateNutritionStatusMother() throws Exception {
        HomeVisitActionHelper nutritionStatusMotherHelper = new HomeVisitActionHelper() {
            private String nutrition_status_mama;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    nutrition_status_mama = getValue(jsonObject, "nutrition_status_mama");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", "Nutrition Status ", nutrition_status_mama);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(nutrition_status_mama)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_nutrition_status))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getNutritionStatusMother())
                .withHelper(nutritionStatusMotherHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_nutrition_status), action);
    }

    private void evaluateNutritionStatusBaby() throws Exception {
        class NutritionStatusBabyHelper extends HomeVisitActionHelper {
            private String nutrition_status_1m;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    nutrition_status_1m = getValue(jsonObject, "nutrition_status_1m");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", "Nutrition Status ", nutrition_status_1m);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(nutrition_status_1m)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        }

        for (Person baby : children) {
            BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, MessageFormat.format(context.getString(R.string.pnc_nutrition_status_baby_name), baby.getFullName()))
                    .withOptional(false)
                    .withDetails(details)
                    .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getNutritionStatusInfant())
                    .withHelper(new NutritionStatusBabyHelper())
                    .build();
            actionList.put(MessageFormat.format(context.getString(R.string.pnc_nutrition_status_baby_name), baby.getFullName()), action);
        }
    }

    private void evaluateMalariaPrevention() throws Exception {
        HomeVisitActionHelper malariaPreventionHelper = new HomeVisitActionHelper() {
            private String fam_llin;
            private String llin_2days;
            private String llin_condition;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    fam_llin = getValue(jsonObject, "fam_llin");
                    llin_2days = getValue(jsonObject, "llin_2days");
                    llin_condition = getValue(jsonObject, "llin_condition");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String evaluateSubTitle() {
                StringBuilder stringBuilder = new StringBuilder();
                if (fam_llin.equalsIgnoreCase("No")) {
                    stringBuilder.append(MessageFormat.format("{0}: {1}\n", context.getString(R.string.uses_net), StringUtils.capitalize(fam_llin.trim().toLowerCase())));
                } else {
                    stringBuilder.append(MessageFormat.format("{0}: {1} · ", context.getString(R.string.uses_net), StringUtils.capitalize(fam_llin.trim().toLowerCase())));
                    stringBuilder.append(MessageFormat.format("{0}: {1} · ", context.getString(R.string.slept_under_net), StringUtils.capitalize(llin_2days.trim().toLowerCase())));
                    stringBuilder.append(MessageFormat.format("{0}: {1}", context.getString(R.string.net_condition), StringUtils.capitalize(llin_condition.trim().toLowerCase())));
                }
                return stringBuilder.toString();
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isBlank(fam_llin))
                    return BaseAncHomeVisitAction.Status.PENDING;

                if (fam_llin.equalsIgnoreCase("Yes") && llin_2days.equalsIgnoreCase("Yes") && llin_condition.equalsIgnoreCase("Okay")) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_malaria_prevention))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getMalariaPrevention())
                .withHelper(malariaPreventionHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_malaria_prevention), action);
    }

    private void evaluateObsIllnessMother() throws Exception {
        HomeVisitActionHelper obsIllnessMotherHelper = new HomeVisitActionHelper() {
            private String date_of_illness;
            private String illness_description;
            private String action_taken;
            private LocalDate illnessDate;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    date_of_illness = getValue(jsonObject, "date_of_illness_mama");
                    illness_description = getValue(jsonObject, "illness_description_mama");
                    action_taken = getCheckBoxValue(jsonObject, "action_taken_mama");
                    illnessDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(date_of_illness);
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                if (illnessDate == null)
                    return "";

                return MessageFormat.format("{0}: {1}\n {2}: {3}",
                        DateTimeFormat.forPattern("dd MMM yyyy").print(illnessDate),
                        illness_description, context.getString(R.string.action_taken), action_taken
                );
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isBlank(date_of_illness))
                    return BaseAncHomeVisitAction.Status.PENDING;

                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_observation_and_illness_mother))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getObservationAndIllnessMother())
                .withHelper(obsIllnessMotherHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_observation_and_illness_mother), action);
    }

    private void evaluateObsIllnessBaby() throws Exception {
        class ObsIllnessBabyHelper extends HomeVisitActionHelper {
            private String date_of_illness;
            private String illness_description;
            private String action_taken;
            private LocalDate illnessDate;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    date_of_illness = getValue(jsonObject, "date_of_illness_child");
                    illness_description = getValue(jsonObject, "illness_description_child");
                    action_taken = getCheckBoxValue(jsonObject, "action_taken_child");
                    illnessDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(date_of_illness);
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                if (illnessDate == null)
                    return "";

                return MessageFormat.format("{0}: {1}\n {2}: {3}",
                        DateTimeFormat.forPattern("dd MMM yyyy").print(illnessDate),
                        illness_description, context.getString(R.string.action_taken), action_taken
                );
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isBlank(date_of_illness))
                    return BaseAncHomeVisitAction.Status.PENDING;

                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        for (Person baby : children) {
            BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, MessageFormat.format(context.getString(R.string.pnc_observation_and_illness_baby), baby.getFullName()))
                    .withOptional(true)
                    .withDetails(details)
                    .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getObservationAndIllnessInfant())
                    .withHelper(new ObsIllnessBabyHelper())
                    .build();
            actionList.put(MessageFormat.format(context.getString(R.string.pnc_observation_and_illness_baby), baby.getFullName()), action);
        }
    }

    private class PNCHealthFacilityVisitHelper extends HomeVisitActionHelper {
        private String fp_counseling;

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                fp_counseling = getValue(jsonObject, "fp_counseling");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String evaluateSubTitle() {
            return MessageFormat.format("{0}: {1}", "Family Planning", fp_counseling);
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(fp_counseling)) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PENDING;
            }
        }
    }

}
