package org.smartgresiter.wcaro.fragment;

import android.support.v7.widget.Toolbar;
import android.view.View;

import org.smartgresiter.wcaro.R;
import org.smartgresiter.wcaro.custom_view.NavigationMenu;
import org.smartgresiter.wcaro.model.FamilyRegisterFramentModel;
import org.smartgresiter.wcaro.presenter.FamilyRegisterFragmentPresenter;
import org.smartregister.family.fragment.BaseFamilyRegisterFragment;
import org.smartregister.view.activity.BaseRegisterActivity;

public class FamilyRegisterFragment extends BaseFamilyRegisterFragment {


    @Override
    public void setupViews(View view) {
        super.setupViews(view);

        Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
        NavigationMenu.getInstance(getActivity(), null, toolbar);

        View topLeftLayout = view.findViewById(R.id.top_left_layout);
        topLeftLayout.setVisibility(View.GONE);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new FamilyRegisterFragmentPresenter(this, new FamilyRegisterFramentModel(), viewConfigurationIdentifier);
    }

    @Override
    protected String getMainCondition() {
        return presenter().getMainCondition();
    }

    @Override
    protected String getDefaultSortQuery() {
        return presenter().getDefaultSortQuery();
    }

}
