package org.smartgresiter.wcaro.activity;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.widget.LinearLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartgresiter.wcaro.R;
import org.smartgresiter.wcaro.contract.ChildRegisterContract;
import org.smartgresiter.wcaro.custom_view.FamilyFloatingMenu;
import org.smartgresiter.wcaro.fragment.FamilyProfileActivityFragment;
import org.smartgresiter.wcaro.fragment.FamilyProfileDueFragment;
import org.smartgresiter.wcaro.fragment.FamilyProfileMemberFragment;
import org.smartgresiter.wcaro.interactor.ChildRegisterInteractor;
import org.smartgresiter.wcaro.listener.FloatingMenuListener;
import org.smartgresiter.wcaro.model.ChildRegisterModel;
import org.smartgresiter.wcaro.model.FamilyProfileModel;
import org.smartgresiter.wcaro.presenter.FamilyProfilePresenter;
import org.smartgresiter.wcaro.util.JsonFormUtils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.family.activity.BaseFamilyProfileActivity;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileActivityFragment;
import org.smartregister.family.fragment.BaseFamilyProfileDueFragment;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;

import de.hdodenhof.circleimageview.CircleImageView;

public class FamilyProfileActivity extends BaseFamilyProfileActivity {
    private String familyBaseEntityId;


    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
        presenter = new FamilyProfilePresenter(this, new FamilyProfileModel(), familyBaseEntityId);
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        // Update profile border
        CircleImageView profileView = findViewById(R.id.imageview_profile);
        profileView.setBorderWidth(2);

        // add floating menu
        FamilyFloatingMenu familyFloatingMenu = new FamilyFloatingMenu(this);
        LinearLayout.LayoutParams linearLayoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        familyFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        addContentView(familyFloatingMenu, linearLayoutParams);
        familyFloatingMenu.setClickListener(new FloatingMenuListener(this));
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        BaseFamilyProfileMemberFragment profileMemberFragment = FamilyProfileMemberFragment.newInstance(this.getIntent().getExtras());
        BaseFamilyProfileDueFragment profileDueFragment = FamilyProfileDueFragment.newInstance(this.getIntent().getExtras());
        BaseFamilyProfileActivityFragment profileActivityFragment = FamilyProfileActivityFragment.newInstance(this.getIntent().getExtras());

        adapter.addFragment(profileMemberFragment, this.getString(org.smartregister.family.R.string.member));
        adapter.addFragment(profileDueFragment, this.getString(org.smartregister.family.R.string.due));
        adapter.addFragment(profileActivityFragment, this.getString(org.smartregister.family.R.string.activity));

        viewPager.setAdapter(adapter);

        return viewPager;
    }

    public String getFamilyBaseEntityId() {
        return familyBaseEntityId;
    }
}
