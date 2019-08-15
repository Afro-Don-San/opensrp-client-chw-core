package com.opensrp.chw.core.listener;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.opensrp.chw.core.R;
import com.opensrp.chw.core.adapter.NavigationAdapter;
import com.opensrp.chw.core.utils.CoreConstants;

public class NavigationListener implements View.OnClickListener {

    private Activity activity;
    private NavigationAdapter navigationAdapter;

    public NavigationListener(Activity activity, NavigationAdapter adapter) {
        this.activity = activity;
        this.navigationAdapter = adapter;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof String) {
            String tag = (String) v.getTag();

            switch (tag) {
                case CoreConstants.DrawerMenu.CHILD_CLIENTS:
                    startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.CHILD_REGISTER_ACTIVITY));
                    break;
                case CoreConstants.DrawerMenu.ALL_FAMILIES:
                    startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.FAMILY_REGISTER_ACTIVITY));
                    break;
                case CoreConstants.DrawerMenu.ANC:
                    startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.ANC_REGISTER_ACTIVITY));
                    break;
                case CoreConstants.DrawerMenu.LD:
                    Toast.makeText(activity.getApplicationContext(), CoreConstants.DrawerMenu.LD, Toast.LENGTH_SHORT).show();
                    break;
                case CoreConstants.DrawerMenu.PNC:
                    startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.PNC_REGISTER_ACTIVITY));
                    break;
                case CoreConstants.DrawerMenu.FAMILY_PLANNING:
                    Toast.makeText(activity.getApplicationContext(), CoreConstants.DrawerMenu.FAMILY_PLANNING, Toast.LENGTH_SHORT).show();
                    break;
                case CoreConstants.DrawerMenu.MALARIA:
                    startRegisterActivity(getActivity(CoreConstants.REGISTERED_ACTIVITIES.MALARIA_REGISTER_ACTIVITY));
                    break;
                default:
                    break;
            }
            navigationAdapter.setSelectedView(tag);
        }
    }

    public void startRegisterActivity(Class registerClass) {
        Intent intent = new Intent(activity, registerClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        activity.finish();
    }

    private Class getActivity(String key) {
        return navigationAdapter.getRegisteredActivities().get(key);
    }
}
