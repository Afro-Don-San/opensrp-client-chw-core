package org.smartregister.chw.core.activity;

import android.content.Intent;
import android.os.Bundle;

import com.adosa.opensrp.chw.fp.activity.BasePathfinderFpRegisterActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.isMultiPartForm;

public class CorePathfinderFamilyPlanningRegisterActivity extends BasePathfinderFpRegisterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        Form form = new Form();
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setWizard(false);
        form.setHomeAsUpIndicator(org.smartregister.chw.core.R.mipmap.ic_cross_white);
        form.setSaveLabel(getString(org.smartregister.chw.core.R.string.submit));

        if (isMultiPartForm(jsonForm)) {
            form.setWizard(true);
            form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);

            if (FORM_NAME.equals(CoreConstants.JSON_FORM.getPathfinderFamilyPlanningIntroduction()))
                form.setName(this.getString(R.string.introduction_to_fp));
            else if(FORM_NAME.equals(CoreConstants.JSON_FORM.getPathfinderPregnancyScreening()))
                form.setName(this.getString(R.string.fp_pregnancy_screening));
            else if(FORM_NAME.equals(CoreConstants.JSON_FORM.getPathfinderChooseFamilyPlanningMethod()))
                form.setName(this.getString(R.string.choose_fp_method));
            else if(FORM_NAME.equals(CoreConstants.JSON_FORM.getPathfinderGiveFamilyPlanningMethod()))
                form.setName(this.getString(R.string.give_fp_method));
            else if(FORM_NAME.equals(CoreConstants.JSON_FORM.getPathfinderCitizenReportCard()))
                form.setName(this.getString(R.string.citizen_report_card));
            else
                form.setName(this.getString(org.smartregister.chw.core.R.string.fp_registration));
            form.setNextLabel(this.getResources().getString(org.smartregister.chw.core.R.string.next));
            form.setPreviousLabel(this.getResources().getString(org.smartregister.chw.core.R.string.back));
        }
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.FAMILY_PLANNING);
        }
    }
}