package org.smartregister.chw.core.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import org.smartregister.chw.anc.activity.BaseAncMedicalHistoryActivity;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;

import java.util.List;

import static org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT;

public class CoreAncMedicalHistoryActivity extends BaseAncMedicalHistoryActivity {

    private Flavor flavor;

    public static void startMe(Activity activity, MemberObject memberObject) {
        Intent intent = new Intent(activity, CoreAncMedicalHistoryActivity.class);
        intent.putExtra(MEMBER_PROFILE_OBJECT, memberObject);
        activity.startActivity(intent);
    }

    public Flavor getFlavor() {
        return flavor;
    }

    public void setFlavor(Flavor flavor) {
        this.flavor = flavor;
    }

    @Override
    public View renderView(List<Visit> visits) {
        View view = flavor.bindViews(this);
        displayLoadingState(true);
        flavor.processViewData(visits, this);
        displayLoadingState(false);
        return view;
    }

    public interface Flavor {

        View bindViews(Activity activity);

        void processViewData(List<Visit> visits, Context context);
    }
}
