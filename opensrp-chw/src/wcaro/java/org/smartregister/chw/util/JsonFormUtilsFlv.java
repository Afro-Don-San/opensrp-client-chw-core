package org.smartregister.chw.util;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.family.util.DBConstants;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.util.FormUtils;
import org.smartregister.util.ImageUtils;
import org.smartregister.view.LocationPickerView;

import java.util.Date;
import java.util.HashMap;

import timber.log.Timber;

public class JsonFormUtilsFlv implements JsonFormUtils.Flavor {
    public static final String TITLE = "title";
    private HashMap<String, String> JSON_DB_MAP;

    public JsonFormUtilsFlv() {
        JSON_DB_MAP = new HashMap<>();
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.SEX, DBConstants.KEY.GENDER);
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.NATIONAL_ID, org.smartregister.chw.util.Constants.JsonAssets.NATIONAL_ID);
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.VOTER_ID, ChwDBConstants.VOTER_ID);
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.DRIVER_LICENSE, ChwDBConstants.DRIVER_LICENSE);
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.PASSPORT, ChwDBConstants.PASSPORT);
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.INSURANCE_PROVIDER, ChwDBConstants.INSURANCE_PROVIDER);
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.INSURANCE_PROVIDER_OTHER, ChwDBConstants.INSURANCE_PROVIDER_OTHER);
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.INSURANCE_PROVIDER_NUMBER, ChwDBConstants.INSURANCE_PROVIDER_NUMBER);
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.DISABILITIES, ChwDBConstants.DISABILITIES);
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.DISABILITY_TYPE, ChwDBConstants.DISABILITY_TYPE);
        JSON_DB_MAP.put(org.smartregister.chw.util.Constants.JsonAssets.OTHER_LEADER, ChwDBConstants.OTHER_LEADER);
    }

    @Override
    public JSONObject getAutoJsonEditMemberFormString(String title, String formName, Context context, CommonPersonObjectClient client, String eventType, String familyName, boolean isPrimaryCaregiver) {
        try {

            // get the event and the client from ec model
            Pair<Event, Client> eventClientPair = Pair.create(null, null);

            JSONObject form = FormUtils.getInstance(context).getFormJson(formName);
            LocationPickerView lpv = new LocationPickerView(context);
            lpv.init();
            // JsonFormUtils.addWomanRegisterHierarchyQuestions(form);
            if (form != null) {
                form.put(org.smartregister.family.util.JsonFormUtils.ENTITY_ID, client.getCaseId());
                form.put(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE, eventType);

                JSONObject metadata = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.METADATA);
                String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());

                metadata.put(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_LOCATION, lastLocationId);

                form.put(org.smartregister.family.util.JsonFormUtils.CURRENT_OPENSRP_ID, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false));

                //inject opensrp id into the form
                JSONObject stepOne = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);

                if (StringUtils.isNotBlank(title)) {
                    stepOne.put(TITLE, title);
                }

                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    processFieldsForMemberEdit(client, jsonObject, jsonArray, familyName, isPrimaryCaregiver, eventClientPair.first, eventClientPair.second);

                }

                return form;
            }
        } catch (Exception e) {
            Timber.e(Log.getStackTraceString(e));
        }

        return null;
    }

    @Override
    public void processFieldsForMemberEdit(CommonPersonObjectClient client, JSONObject jsonObject, JSONArray jsonArray, String familyName, boolean isPrimaryCaregiver, Event ecEvent, Client ecClient) throws JSONException {


        switch (jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).toLowerCase()) {
            case org.smartregister.family.util.Constants.JSON_FORM_KEY.DOB_UNKNOWN: {
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, false);
                JSONObject optionsObject = jsonObject.getJSONArray(org.smartregister.family.util.Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
                optionsObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), org.smartregister.family.util.Constants.JSON_FORM_KEY.DOB_UNKNOWN, false));
            }
            break;
            case org.smartregister.chw.util.Constants.JsonAssets.AGE: {

                String dobString = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
                dobString = org.smartregister.family.util.Utils.getDuration(dobString);
                dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : "0";
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Integer.valueOf(dobString));
            }
            break;
            case DBConstants.KEY.DOB:

                String dobString = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
                if (StringUtils.isNotBlank(dobString)) {
                    Date dob = Utils.dobStringToDate(dobString);
                    if (dob != null) {
                        jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, JsonFormUtils.dd_MM_yyyy.format(dob));
                    }
                }
                break;

            case org.smartregister.family.util.Constants.KEY.PHOTO:

                Photo photo = ImageUtils.profilePhotoByClientID(client.getCaseId(), Utils.getProfileImageResourceIDentifier());
                if (StringUtils.isNotBlank(photo.getFilePath())) {
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, photo.getFilePath());
                }
                break;

            case DBConstants.KEY.UNIQUE_ID:

                String uniqueId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, uniqueId.replace("-", ""));
                break;

            case org.smartregister.chw.util.Constants.JsonAssets.PREGNANT_1_YR:
                if (ecEvent != null) {
                    String id = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
                    for (Obs obs : ecEvent.getObs()) {
                        if (obs.getValues() != null && obs.getFieldCode().contains(id)) {
                            jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, obs.getHumanReadableValues().get(0));
                        }
                    }
                }
                break;

            case org.smartregister.chw.util.Constants.JsonAssets.FAM_NAME:

                if (ecClient.getLastName() != null) {

                    final String SAME_AS_FAM_NAME = "same_as_fam_name";
                    final String SURNAME = "surname";

                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, familyName);

                    String lastName = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.LAST_NAME, false);

                    JSONObject sameAsFamName = JsonFormUtils.getFieldJSONObject(jsonArray, SAME_AS_FAM_NAME);
                    JSONObject sameOptions = sameAsFamName.getJSONArray(org.smartregister.family.util.Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);

                    if (familyName.equals(lastName)) {
                        sameOptions.put(org.smartregister.family.util.JsonFormUtils.VALUE, true);
                    } else {
                        sameOptions.put(org.smartregister.family.util.JsonFormUtils.VALUE, false);
                    }

                    JSONObject surname = JsonFormUtils.getFieldJSONObject(jsonArray, SURNAME);
                    if (!familyName.equals(lastName)) {
                        surname.put(org.smartregister.family.util.JsonFormUtils.VALUE, lastName);
                    } else {
                        surname.put(org.smartregister.family.util.JsonFormUtils.VALUE, "");
                    }

                }
                break;

            case org.smartregister.chw.util.Constants.JsonAssets.PRIMARY_CARE_GIVER:
            case org.smartregister.chw.util.Constants.JsonAssets.IS_PRIMARY_CARE_GIVER:
                if (isPrimaryCaregiver) {
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, "Yes");
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);
                } else {
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, "No");
                }
                break;

            case org.smartregister.chw.util.Constants.JsonAssets.SERVICE_PROVIDER:

                // iterate and update all options wit the values from ec object

                if (ecEvent != null) {
                    for (int i = 0; i < jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME).length(); i++) {
                        JSONObject obj = jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME).getJSONObject(i);
                        String id = obj.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
                        for (Obs obs : ecEvent.getObs()) {
                            if (obs.getValues() != null && obs.getValues().contains(id)) {
                                obj.put(JsonFormConstants.VALUE, true);
                            }
                        }
                    }
                }
                break;

            default:
                String db_key = JSON_DB_MAP.get(jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).toLowerCase());
                if (StringUtils.isNotBlank(db_key)) {
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), db_key, false));
                } else {
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY), false));
                }

                break;

        }
    }

}
