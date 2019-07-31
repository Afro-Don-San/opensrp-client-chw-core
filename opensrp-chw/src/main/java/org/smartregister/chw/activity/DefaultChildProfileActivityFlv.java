package org.smartregister.chw.activity;

import android.app.Activity;

import com.opensrp.chw.core.fragment.FamilyCallDialogFragment;
import com.opensrp.chw.core.listener.OnClickFloatingMenu;
import com.opensrp.chw.core.presenter.CoreChildProfilePresenter;

import org.smartregister.chw.R;
import org.smartregister.chw.presenter.ChildProfilePresenter;

public abstract class DefaultChildProfileActivityFlv implements ChildProfileActivity.Flavor {

    @Override
    public OnClickFloatingMenu getOnClickFloatingMenu(final Activity activity, final ChildProfilePresenter presenter) {
        return new OnClickFloatingMenu() {
            @Override
            public void onClickMenu(int viewId) {
                switch (viewId) {
                    case R.id.fab:
                        FamilyCallDialogFragment.launchDialog(activity, presenter.getFamilyId());
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public boolean showMalariaConfirmationMenu(){
        return false;
    }
}
