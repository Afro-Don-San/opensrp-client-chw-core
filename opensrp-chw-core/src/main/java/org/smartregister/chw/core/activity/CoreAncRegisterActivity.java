package org.smartregister.chw.core.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.activity.BaseAncRegisterActivity;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Arrays;
import java.util.List;

import static org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.TABLE_NAME;

public class CoreAncRegisterActivity extends BaseAncRegisterActivity {
    protected static String phone_number;
    protected static String form_name;
    protected static String unique_id;
    protected static String familyBaseEntityId;
    private static String familyName;


    public static void startAncRegistrationActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name) {
        Intent intent = new Intent(activity, CoreAncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);
        phone_number = phoneNumber;
        familyBaseEntityId = familyBaseID;
        form_name = formName;
        familyName = family_name;
        unique_id = uniqueId;
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(TABLE_NAME, getFormTable());
        activity.startActivity(intent);
    }

    private static String getFormTable() {
        if (form_name != null && form_name.equals(CoreConstants.JSON_FORM.getAncRegistration())) {
            return CoreConstants.TABLE_NAME.ANC_MEMBER;
        }
        return CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME;
    }

    @Override
    public void switchToBaseFragment() {
        /*Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        finish();*/
    }

    @Override
    public String getRegistrationForm() {
        return form_name;
    }

    @Override
    public String getFormRegistrationEvent() {
        return org.smartregister.chw.anc.util.Constants.EVENT_TYPE.ANC_REGISTRATION;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return super.getRegisterFragment();
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();
     /*   if (!BuildConfig.SUPPORT_QR) {
            bottomNavigationView.getMenu().removeItem(R.id.action_scan_qr);
        }

        AncBottomNavigationListener listener = new AncBottomNavigationListener(this, bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(listener);*/
    }

    public void startFamilyRegistration() {
        // CoreFamilyRegisterActivity.startFamilyRegisterForm(this);
    }

    private void startRegisterActivity(Class registerClass) {
        Intent intent = new Intent(this, registerClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        this.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        this.finish();
    }

    @Override
    public void onRegistrationSaved(boolean isEdit) {
        finish();
        startRegisterActivity(CoreAncRegisterActivity.class);
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter()
                    .setSelectedView(CoreConstants.DrawerMenu.ANC);
        }
    }

    @Override
    public List<String> getViewIdentifiers() {
        return Arrays.asList(CoreConstants.CONFIGURATION.ANC_REGISTER);
    }

    private void updateFormField(JSONArray formFieldArrays, String formFeildKey, String updateValue) {
        if (updateValue != null) {
            JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(formFieldArrays, formFeildKey);
            if (formObject != null) {
                try {
                    formObject.remove(org.smartregister.util.JsonFormUtils.VALUE);
                    formObject.put(org.smartregister.util.JsonFormUtils.VALUE, updateValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startFormActivity(JSONObject jsonForm) {

        try {
            JSONObject stepOne = jsonForm.getJSONObject(JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
            updateFormField(jsonArray, DBConstants.KEY.TEMP_UNIQUE_ID, unique_id);
            updateFormField(jsonArray, CoreConstants.JsonAssets.FAM_NAME, familyName);
            updateFormField(jsonArray, CoreConstants.JsonAssets.FAMILY_MEMBER.PHONE_NUMBER, phone_number);
            updateFormField(jsonArray, org.smartregister.family.util.DBConstants.KEY.RELATIONAL_ID, familyBaseEntityId);

            Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setActionBarBackground(R.color.family_actionbar);
            form.setWizard(false);
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
