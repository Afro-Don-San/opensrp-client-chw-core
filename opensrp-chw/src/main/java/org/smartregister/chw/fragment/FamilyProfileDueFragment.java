package org.smartregister.chw.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.FamilyProfileActivity;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.WashCheck;
import org.smartregister.chw.interactor.ChildProfileInteractor;
import org.smartregister.chw.model.FamilyProfileDueModel;
import org.smartregister.chw.presenter.FamilyProfileDuePresenter;
import org.smartregister.chw.provider.ChwDueRegisterProvider;
import org.smartregister.chw.util.WashCheckFlv;
import org.smartregister.family.adapter.FamilyRecyclerViewCustomAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileDueFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.HashMap;
import java.util.Set;

import timber.log.Timber;

public class FamilyProfileDueFragment extends BaseFamilyProfileDueFragment {

    private int dueCount = 0;
    private View emptyView;
    private String familyName;
    private long dateFamilyCreated;
    private String familyBaseEntityId;
    private LinearLayout washCheckView;
    private Flavor flavorWashCheck = new WashCheckFlv();

    public static BaseFamilyProfileDueFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        BaseFamilyProfileDueFragment fragment = new FamilyProfileDueFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        familyName = getArguments().getString(Constants.INTENT_KEY.FAMILY_NAME);
        presenter = new FamilyProfileDuePresenter(this, new FamilyProfileDueModel(), null, familyBaseEntityId);
        //TODO need to pass this value as this value using at homevisit rule
        dateFamilyCreated = getArguments().getLong("");

    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //TODO
        Timber.d("setAdvancedSearchFormData");
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        emptyView = view.findViewById(R.id.empty_view);
        washCheckView = view.findViewById(R.id.wash_check_layout);
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        ChwDueRegisterProvider chwDueRegisterProvider = new ChwDueRegisterProvider(this.getActivity(), this.commonRepository(), visibleColumns, this.registerActionHandler, this.paginationViewHandler);
        this.clientAdapter = new FamilyRecyclerViewCustomAdapter(null, chwDueRegisterProvider, this.context().commonrepository(this.tablename), Utils.metadata().familyDueRegister.showPagination);
        this.clientAdapter.setCurrentlimit(Utils.metadata().familyDueRegister.currentLimit);
        this.clientsView.setAdapter(this.clientAdapter);
    }

    @Override
    protected void onViewClicked(View view) {
        super.onViewClicked(view);
        switch (view.getId()) {
            case R.id.patient_column:
                if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
                    ((FamilyProfileActivity) getActivity()).goToProfileActivity(view, getArguments());
                }
                break;
            case R.id.next_arrow:
                if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NEXT_ARROW) {
                    ((FamilyProfileActivity) getActivity()).goToProfileActivity(view, getArguments());
                }
                break;
            default:
                break;
        }
    }

    public FamilyProfileDuePresenter getPresenter() {
        return (FamilyProfileDuePresenter) presenter;
    }

    @Override
    public void countExecute() {
        final int count = getPresenter().getDueCount();
        clientAdapter.setTotalcount(count);

        if (getActivity() != null && count != dueCount) {
            dueCount = count;
            ((FamilyProfileActivity) getActivity()).updateDueCount(dueCount);
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                onEmptyRegisterCount(count < 1 && (washCheckView != null && washCheckView.getVisibility() != View.VISIBLE));
                //need some delay to ready the adapter
                new Handler().postDelayed(() -> {
                    if (flavorWashCheck.isWashCheckVisible()) {
                        ((FamilyProfileDuePresenter) presenter).fetchLastWashCheck(dateFamilyCreated);
                    }

                }, 500);
            });
        }

    }

    public void onEmptyRegisterCount(final boolean has_no_records) {
        if (emptyView != null) {
            emptyView.setVisibility(has_no_records ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case org.smartregister.chw.util.JsonFormUtils.REQUEST_CODE_GET_JSON_WASH:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                        JSONObject form = new JSONObject(jsonString);
                        if (form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE).equals(org.smartregister.chw.util.Constants.EventType.WASH_CHECK)
                        ) {
                            boolean isSave = ((FamilyProfileDuePresenter) presenter).saveData(jsonString);
                            if (isSave) {
                                visibilityWashView(false);
                                if (getActivity() != null && getActivity() instanceof FamilyProfileActivity) {
                                    FamilyProfileActivity familyProfileActivity = (FamilyProfileActivity) getActivity();
                                    familyProfileActivity.updateWashCheckActivity();
                                }
                            }
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }

                }
                break;
            default:
                break;
        }
    }

    private void visibilityWashView(boolean isVisible) {
        if ((isVisible)) {
            washCheckView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            dueCount = clientAdapter.getTotalcount() + 1;
            onEmptyRegisterCount(false);
        } else {
            dueCount = clientAdapter.getTotalcount() - 1;
            washCheckView.setVisibility(View.GONE);
            onEmptyRegisterCount(dueCount < 1);
        }
        if (getActivity() != null) {
            ((FamilyProfileActivity) getActivity()).updateDueCount(dueCount);
        }
    }

    public void updateWashCheckBar(WashCheck washCheck) {
        if (washCheckView.getVisibility() == View.VISIBLE) {
            visibilityWashView(true);
            return;
        }
        CustomFontTextView name = washCheckView.findViewById(R.id.patient_name_age);
        name.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimensionPixelSize(R.dimen.member_due_list_title_size));
        TextView lastVisit = washCheckView.findViewById(R.id.last_visit);
        ImageView status = washCheckView.findViewById(R.id.status);
        if (washCheck == null || washCheck.getStatus().equalsIgnoreCase(ChildProfileInteractor.VisitType.DUE.name())) {
            visibilityWashView(true);
            status.setImageResource(org.smartregister.chw.util.Utils.getDueProfileImageResourceIDentifier());
            if (washCheck == null) {
                lastVisit.setVisibility(View.GONE);
            } else {
                lastVisit.setText(String.format(getActivity().getString(R.string.last_visit_prefix), washCheck.getLastVisitDate()));
            }
            name.setText(getActivity().getString(R.string.family, familyName) + " " + getActivity().getString(R.string.wash_check_suffix));


        } else if (washCheck.getStatus().equalsIgnoreCase(ChildProfileInteractor.VisitType.OVERDUE.name())) {
            visibilityWashView(true);
            status.setImageResource(org.smartregister.chw.util.Utils.getOverDueProfileImageResourceIDentifier());
            lastVisit.setText(String.format(getMyContext().getString(R.string.last_visit_prefix), washCheck.getLastVisitDate()));
            name.setText(getActivity().getString(R.string.family, familyName) + " " + getActivity().getString(R.string.wash_check_suffix));

        } else {
            washCheckView.setVisibility(View.GONE);
        }
        washCheckView.setOnClickListener(v -> {
            try {
                JSONObject jsonForm = FormUtils.getInstance(getActivity()).getFormJson(org.smartregister.chw.util.Constants.JSON_FORM.getWashCheck());
                jsonForm.put(JsonFormUtils.ENTITY_ID, familyBaseEntityId);
                Intent intent = new Intent(getActivity(), Utils.metadata().familyMemberFormActivity);
                intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

                Form form = new Form();
                form.setWizard(false);
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                intent.putExtra(Constants.WizardFormActivity.EnableOnCloseDialog, true);
                if (getActivity() != null) {
                    getActivity().startActivityForResult(intent, org.smartregister.chw.util.JsonFormUtils.REQUEST_CODE_GET_JSON_WASH);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private Context getMyContext(){
        Context context = getActivity();
        if(context == null)
            context = CoreChwApplication.getInstance();

        return context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        switch (id) {
            case LOADER_ID:
                // Returns a new CursorLoader
                return new CursorLoader(getActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        // Count query
                        if (args != null && args.getBoolean("count_execute")) {
                            countExecute();
                        }
                        return commonRepository().rawCustomQueryForAdapter(mainSelect);
                    }
                };
            default:
                // An invalid id was passed in
                return null;
        }

    }

    public interface Flavor {
        boolean isWashCheckVisible();
    }

}