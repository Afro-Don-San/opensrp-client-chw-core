package org.smartregister.chw.core.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.child.activity.BaseActivity;
import org.smartregister.child.toolbar.SimpleToolbar;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.domain.MonthlyTally;
import org.smartregister.chw.core.domain.Tally;
import org.smartregister.chw.core.repository.MonthlyTalliesRepository;
import org.smartregister.chw.core.view.IndicatorCategoryView;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.smartregister.chw.fp.util.FamilyPlanningConstants.EventType.FP_FOLLOW_UP_VISIT;

public class ReportSummaryActivity extends BaseActivity {
    public static final String EXTRA_TALLIES = "tallies";
    public static final String EXTRA_SUB_TITLE = "sub_title";
    public static final String EXTRA_TITLE = "title";
    protected AppBarLayout appBarLayout;
    private LinkedHashMap<String, ArrayList<Tally>> tallies;
    private String subTitle;
    private SimpleDateFormat simpleDateFormat;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        SimpleDateFormat simpleDateFormat;
        Toolbar toolbar = findViewById(R.id.back_to_nav_toolbar);
        CustomFontTextView toolBarTextView = toolbar.findViewById(R.id.toolbar_title);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolBarTextView.setOnClickListener(v -> finish());
        refreshViewsOnResult();
        appBarLayout = findViewById(R.id.app_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(null);
        }

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable talliesSerializable = extras.getSerializable(EXTRA_TALLIES);
            if (talliesSerializable != null && talliesSerializable instanceof ArrayList) {
                ArrayList<Tally> tallies = (ArrayList<Tally>) talliesSerializable;
                setTallies(tallies, false);
            }

            Serializable submittedBySerializable = extras.getSerializable(EXTRA_SUB_TITLE);
            if (submittedBySerializable != null && submittedBySerializable instanceof String) {
                subTitle = (String) submittedBySerializable;
            }

            Serializable titleSerializable = extras.getSerializable(EXTRA_TITLE);
            if (titleSerializable != null && titleSerializable instanceof String) {
                toolBarTextView.setText((String) titleSerializable);
            }
        }
    }
    private void refreshViewsOnResult() {
        Observable<MonthlyTalliesRepository> observable = Observable.create(monthlyTallyEmitter -> {
            MonthlyTalliesRepository monthlyTalliesRepository = new MonthlyTalliesRepository();
            monthlyTallyEmitter.onNext(monthlyTalliesRepository);
            monthlyTallyEmitter.onComplete();
        });

        final Disposable[] disposable = new Disposable[1];
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MonthlyTalliesRepository>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable[0] = d;
                    }

                    @Override
                    public void onNext(MonthlyTalliesRepository monthlyTally) {
                        monthlyTally.findAllSent(simpleDateFormat);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {
                        disposable[0].dispose();
                        disposable[0] = null;
                    }
                });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CustomFontTextView submittedBy = findViewById(R.id.submitted_by);
        if (!TextUtils.isEmpty(this.subTitle)) {
            submittedBy.setVisibility(View.VISIBLE);
            submittedBy.setText(this.subTitle);
        } else {
            submittedBy.setVisibility(View.GONE);
        }
        refreshIndicatorViews();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_report_summary;
    }

    @Override
    protected int getDrawerLayoutId() {
        return R.id.drawer_layout;
    }

    @Override
    protected int getToolbarId() {
        return SimpleToolbar.TOOLBAR_ID;
    }

    @Override
    protected Class onBackActivity() {
        return null;
    }

    private void setTallies(ArrayList<Tally> tallies, boolean refreshViews) {
        this.tallies = new LinkedHashMap<>();
        Collections.sort(tallies, new Comparator<Tally>() {
            @Override
            public int compare(Tally lhs, Tally rhs) {
                long lhsId = lhs.getIndicator().getId();
                long rhsId = rhs.getIndicator().getId();
                return (int) (lhsId - rhsId);
            }
        });

        for (Tally curTally : tallies) {
            if (curTally != null && !TextUtils.isEmpty(curTally.getIndicator().getCategory())) {
                if (!this.tallies.containsKey(curTally.getIndicator().getCategory())
                        || this.tallies.get(curTally.getIndicator().getCategory()) == null) {
                    this.tallies.put(curTally.getIndicator().getCategory(), new ArrayList<Tally>());
                }

                this.tallies.get(curTally.getIndicator().getCategory()).add(curTally);
            }
        }

        if (refreshViews) refreshIndicatorViews();
    }

    private void refreshIndicatorViews() {
        LinearLayout indicatorCanvas = findViewById(R.id.indicator_canvas);
        indicatorCanvas.removeAllViews();

        if (tallies != null) {
            for (String curCategoryName : tallies.keySet()) {
                IndicatorCategoryView curCategoryView = new IndicatorCategoryView(this);
                curCategoryView.setTallies(curCategoryName, tallies.get(curCategoryName));
                indicatorCanvas.addView(curCategoryView);
            }
        }
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId) {
        //Override Super
    }

    @Override
    public void onNoUniqueId() {
        //Override Super

    }

    @Override
    public void onRegistrationSaved(boolean isEdit) {
        //Override Super
    }
}
