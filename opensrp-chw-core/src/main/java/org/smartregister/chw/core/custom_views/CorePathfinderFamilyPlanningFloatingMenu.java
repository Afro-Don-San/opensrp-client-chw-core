package org.smartregister.chw.core.custom_views;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.adosa.opensrp.chw.fp.custom_views.BaseFpFloatingMenu;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.adosa.opensrp.chw.fp.fragment.BasePathfinderFpCallDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.smartregister.chw.core.listener.OnClickFloatingMenu;

import static org.smartregister.chw.core.utils.Utils.redrawWithOption;

public class CorePathfinderFamilyPlanningFloatingMenu extends BaseFpFloatingMenu {

    public FloatingActionButton fab;
    private Animation fabOpen;
    private Animation fabClose;
    private Animation rotateForward;
    private Animation rotateBack;
    private View callLayout;
    private View referLayout;
    private RelativeLayout activityMain;
    private boolean isFabMenuOpen = false;
    private LinearLayout menuBar;

    private PathfinderFpMemberObject fpMemberObject;
    private OnClickFloatingMenu onClickFloatingMenu;

    public CorePathfinderFamilyPlanningFloatingMenu(Context context, PathfinderFpMemberObject fpMemberObject) {
        super(context, fpMemberObject);
        this.fpMemberObject = fpMemberObject;
    }

    // public CoreFamilyPlanningFloatingMenu(Context context, AttributeSet attrs) { super(context, attrs); }

    public void setFloatingMenuOnClickListener(OnClickFloatingMenu onClickFloatingMenu) {
        this.onClickFloatingMenu = onClickFloatingMenu;
    }

    @Override
    protected void initUi() {
        inflate(getContext(), org.smartregister.chw.core.R.layout.family_planning_floating_menu, this);
        fabOpen = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.rotate_forward);
        rotateBack = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.rotate_back);

        activityMain = findViewById(org.smartregister.chw.core.R.id.activity_main);
        menuBar = findViewById(org.smartregister.chw.core.R.id.menu_bar);

        fab = findViewById(org.smartregister.chw.core.R.id.family_planning_fab);
        fab.setOnClickListener(this);

        callLayout = findViewById(org.smartregister.chw.core.R.id.call_layout);
        callLayout.setOnClickListener(this);
        callLayout.setClickable(false);

        referLayout = findViewById(org.smartregister.chw.core.R.id.refer_to_facility_layout);
        referLayout.setOnClickListener(this);
        referLayout.setClickable(false);

        menuBar.setVisibility(GONE);
    }

    @Override
    public void onClick(View view) {
        onClickFloatingMenu.onClickMenu(view.getId());
    }

    public void animateFAB() {
        if (menuBar.getVisibility() == GONE) {
            menuBar.setVisibility(VISIBLE);
        }

        if (isFabMenuOpen) {
            activityMain.setBackgroundResource(org.smartregister.chw.core.R.color.transparent);
            fab.startAnimation(rotateBack);
            fab.setImageResource(org.smartregister.chw.core.R.drawable.ic_edit_white);

            callLayout.startAnimation(fabClose);
            callLayout.setClickable(false);

            referLayout.startAnimation(fabClose);
            referLayout.setClickable(false);
            isFabMenuOpen = false;
        } else {
            activityMain.setBackgroundResource(org.smartregister.chw.core.R.color.grey_tranparent_50);
            fab.startAnimation(rotateForward);
            fab.setImageResource(org.smartregister.chw.core.R.drawable.ic_input_add);

            callLayout.startAnimation(fabOpen);
            callLayout.setClickable(true);

            referLayout.startAnimation(fabOpen);
            referLayout.setClickable(true);
            isFabMenuOpen = true;
        }
    }

    public void launchCallWidget() {
        BasePathfinderFpCallDialogFragment.launchDialog((Activity) this.getContext(), fpMemberObject);
    }

    public void redraw(boolean hasPhoneNumber) {
        redrawWithOption(this, hasPhoneNumber);
    }

    public View getCallLayout() {
        return callLayout;
    }
}
