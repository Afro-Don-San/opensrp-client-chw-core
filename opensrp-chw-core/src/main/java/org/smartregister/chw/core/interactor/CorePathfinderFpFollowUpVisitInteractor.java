package org.smartregister.chw.core.interactor;

import androidx.annotation.Nullable;

import com.adosa.opensrp.chw.fp.interactor.BasePathfinderFpFollowUpVisitInteractor;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.domain.Task;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;

import java.util.Map;
import java.util.UUID;

public class CorePathfinderFpFollowUpVisitInteractor extends BasePathfinderFpFollowUpVisitInteractor {


    @Override
    protected @Nullable
    Visit saveVisit(boolean editMode, String memberID, String encounterType,
                    final Map<String, String> jsonString,
                    String parentEventType
    ) throws Exception {

        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();

        String derivedEncounterType = StringUtils.isBlank(parentEventType) ? encounterType : "";
        Event baseEvent = JsonFormUtils.processVisitJsonForm(allSharedPreferences, memberID, derivedEncounterType, jsonString, getTableName());

        // only tag the first event with the date
        if (StringUtils.isBlank(parentEventType)) {
            prepareEvent(baseEvent);
        } else {
            prepareSubEvent(baseEvent);
        }

        if (baseEvent != null) {
            baseEvent.setFormSubmissionId(JsonFormUtils.generateRandomUUIDString());
            JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);


            NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
            if (baseEvent.getEventType().equals(CoreConstants.EventType.ANC_REFERRAL)) {
                String facilityLocationId = null;
                for (Obs ob : baseEvent.getObs()) {
                    if (ob.getFieldCode().equals("referral_facility")) {
                        if (!ob.getHumanReadableValues().isEmpty())
                            facilityLocationId = ob.getHumanReadableValues().get(0).toString();
                        else
                            facilityLocationId = ob.getValues().get(0).toString();
                        break;
                    }
                }


                createReferralTask(baseEvent.getBaseEntityId(), allSharedPreferences, CoreConstants.TASKS_FOCUS.ANC_DANGER_SIGNS, "", baseEvent.getFormSubmissionId(), facilityLocationId);
            }

            String visitID = (editMode) ?
                    visitRepository().getLatestVisit(memberID, getEncounterType()).getVisitId() :
                    JsonFormUtils.generateRandomUUIDString();

            // reset database
            if (editMode)
                deleteOldVisit(visitID);

            Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
            visit.setPreProcessedJson(new Gson().toJson(baseEvent));
            visit.setParentVisitID(getParentVisitEventID(visit, parentEventType));

            visitRepository().addVisit(visit);
            return visit;
        }
        return null;
    }

    private void createReferralTask(String baseEntityId, AllSharedPreferences allSharedPreferences, String focus, String referralProblems, String eventFormSubmissionId, String facilityLocationUUID) {
        Task task = new Task();
        task.setIdentifier(UUID.randomUUID().toString());
        task.setReasonReference(eventFormSubmissionId);
        task.setPlanIdentifier(CoreConstants.REFERRAL_PLAN_ID);
        task.setStatus(Task.TaskStatus.READY);
        task.setPriority(3);
        task.setBusinessStatus(CoreConstants.BUSINESS_STATUS.REFERRED);
        task.setCode(CoreConstants.JsonAssets.REFERRAL_CODE);
        task.setFocus(focus);
        task.setDescription(referralProblems);
        task.setForEntity(baseEntityId);
        DateTime now = new DateTime();
        task.setExecutionStartDate(now);
        task.setAuthoredOn(now);
        task.setLastModified(now);
        task.setOwner(allSharedPreferences.fetchRegisteredANM());
        task.setSyncStatus(BaseRepository.TYPE_Created);
        task.setRequester(allSharedPreferences.getANMPreferredName(allSharedPreferences.fetchRegisteredANM()));
        task.setLocation(allSharedPreferences.fetchUserLocalityId(allSharedPreferences.fetchRegisteredANM()));
        task.setGroupIdentifier(facilityLocationUUID);
        CoreChwApplication.getInstance().getTaskRepository().addOrUpdate(task);
    }


}
