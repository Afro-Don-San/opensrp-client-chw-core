package com.opensrp.chw.core.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.v4.app.Fragment;

import com.opensrp.chw.core.R;
import com.opensrp.chw.core.contract.CoreChildRegisterContract;
import com.opensrp.chw.core.custom_views.NavigationMenu;
import com.opensrp.chw.core.fragment.CoreChildRegisterFragment;
import com.opensrp.chw.core.listener.CoreBottomNavigationListener;
import com.opensrp.chw.core.model.CoreChildRegisterModel;
import com.opensrp.chw.core.presenter.CoreChildRegisterPresenter;
import com.opensrp.chw.core.utils.CoreConstants;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class CoreChildRegisterActivity extends BaseRegisterActivity implements CoreChildRegisterContract.View {

    @Override
    protected void initializePresenter() {
        presenter = new CoreChildRegisterPresenter(this, new CoreChildRegisterModel());
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new CoreChildRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[0];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }

    @Override
    public void startRegistration() {
        startFormActivity(Utils.metadata().familyRegister.formName, null, null);
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            if (mBaseFragment instanceof CoreChildRegisterFragment) {
                String locationId = Utils.context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
                presenter().startForm(formName, entityId, metaData, locationId, "");
            }
        } catch (Exception e) {
            Timber.e(e);
            displayToast(getString(R.string.error_unable_to_start_form));
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, Utils.metadata().familyFormActivity);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        Form form = new Form();
        form.setName(getString(R.string.add_fam));
        form.setActionBarBackground(R.color.family_actionbar);
        form.setNavigationBackground(R.color.family_navigation);
        form.setHomeAsUpIndicator(R.mipmap.ic_cross_white);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                Timber.d("JSONResult : %s", jsonString);

                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.registerEventType)
                        || form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.CHILD_REGISTRATION)
                ) {
                    presenter().saveForm(jsonString, false);
                }
            } catch (Exception e) {
                Timber.e(e);
            }

        }
    }

    @Override
    protected void registerBottomNavigation() {

        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.getMenu().removeItem(R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(R.id.action_register);
            bottomNavigationView.getMenu().removeItem(R.id.action_search);
            bottomNavigationView.getMenu().removeItem(R.id.action_library);

            bottomNavigationView.inflateMenu(R.menu.bottom_nav_family_menu);

            bottomNavigationHelper.disableShiftMode(bottomNavigationView);

            CoreBottomNavigationListener childBottomNavigationListener = new CoreBottomNavigationListener(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(childBottomNavigationListener);

        }
    }

    @Override
    public void openFamilyListView() {
        bottomNavigationView.setSelectedItemId(R.id.action_family);
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, CoreFamilyRegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public List<String> getViewIdentifiers() {
        return Arrays.asList(Utils.metadata().familyRegister.config);
    }

    @Override
    public CoreChildRegisterContract.Presenter presenter() {
        return (CoreChildRegisterContract.Presenter) presenter;
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu.getInstance(this, null, null).getNavigationAdapter()
                .setSelectedView(CoreConstants.DrawerMenu.CHILD_CLIENTS);
    }
}
