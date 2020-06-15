package org.smartregister.chw.core.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.adosa.opensrp.chw.fp.activity.BasePathfinderFpFollowUpVisitActivity;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.interactor.CorePathfinderFpFollowUpVisitInteractor;
import org.smartregister.chw.core.presenter.CorePathfinderFpFollowupVisitPresenter;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.util.LangUtils;

import java.text.MessageFormat;

public class CorePathfinderFollowupVisitActivity extends BasePathfinderFpFollowUpVisitActivity {

    @Override
    public void redrawHeader(MemberObject memberObject) {
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", memberObject.getFullName(), memberObject.getAge(), getString(com.adosa.opensrp.chw.fp.R.string.fp_pregnancy_screening)));
    }

    @Override
    protected void registerPresenter() {
        presenter = new CorePathfinderFpFollowupVisitPresenter(memberObject, this, new CorePathfinderFpFollowUpVisitInteractor());
    }

    @Override
    public void onBackPressed() {
        displayExitDialog(() -> finish());

    }

    @Override
    public void close() {
        finish();
    }


    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Form form = new Form();
        form.setActionBarBackground(R.color.family_actionbar);
        form.setWizard(false);

        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        intent.putExtra(Constants.WizardFormActivity.EnableOnCloseDialog, false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void attachBaseContext(Context base) {
        // get language from prefs
        String lang = LangUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(LangUtils.setAppLocale(base, lang));
    }
}

