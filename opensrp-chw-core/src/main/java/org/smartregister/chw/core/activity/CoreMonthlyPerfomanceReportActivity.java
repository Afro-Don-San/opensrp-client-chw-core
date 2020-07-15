package org.smartregister.chw.core.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import org.smartregister.chw.core.R;
import org.smartregister.chw.core.adapter.CoreStockMonthlyReportAdapter;
import org.smartregister.chw.core.adapter.CoreStockUsageItemAdapter;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.dao.StockUsageReportDao;
import org.smartregister.chw.core.model.MonthStockUsageModel;
import org.smartregister.chw.core.model.StockUsageItemModel;
import org.smartregister.chw.core.utils.StockUsageReportUtils;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CoreMonthlyPerfomanceReportActivity extends SecuredActivity {
    protected AppBarLayout appBarLayout;
    protected RecyclerView recyclerView;
    protected CustomFontTextView toolBarTextView;
    public StockUsageReportUtils stockUsageReportUtils = new StockUsageReportUtils();

    public static List<String> getItems() {
      return  new ArrayList<>(
                Arrays.asList("Clients", "Stipend Reg. (Tsh)", "Visits: Scheduled Ref", "Visits: Completed Ref", "Visits: Completed Ref", "Visits: % Ref", "Stipend Visit Ref (Tsh)", "Standard day method",
                        "Visits: Scheduled Non-ref",  "Visits: Completed Non-ref","Visits:% (Non Ref)", "Stipend Visit Non-ref(Tsh)","Total Stipend(Tsh)")
        ); }

    private List<MonthStockUsageModel> getMonthStockUsageReportList() {
        List<MonthStockUsageModel> monthStockUsageReportList = new ArrayList<>();

        if (stockUsageReportUtils.getPreviousMonths().size() > 0) {
            for (Map.Entry<String, String> entry : stockUsageReportUtils.getPreviousMonths().entrySet()) {
                monthStockUsageReportList.add(new MonthStockUsageModel(entry.getKey(), entry.getValue()));
            }
        }
        return monthStockUsageReportList;
    }

    public List<StockUsageItemModel> getStockUsageItemReportList(String month, String year) {
        List<StockUsageItemModel> stockUsageItemModelsList = new ArrayList<>();
        String providerName = CoreChwApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        for (String item : getItems()) {
            String usage = StockUsageReportDao.getStockUsageForMonth(month, item, year, providerName);
            stockUsageItemModelsList.add(new StockUsageItemModel(stockUsageReportUtils.getFormattedItem(item), stockUsageReportUtils.getUnitOfMeasure(item), usage, providerName));
        }
        return stockUsageItemModelsList;
    }

    protected void reloadRecycler(MonthStockUsageModel selected) {
        String stockMonth = stockUsageReportUtils.getMonthNumber(selected.getMonth().substring(0, 3));
        String stockYear = selected.getYear();


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        CoreStockUsageItemAdapter coreStockUsageItemAdapter = new CoreStockUsageItemAdapter(getStockUsageItemReportList(stockMonth, stockYear), this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(coreStockUsageItemAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_stock_usage_report);

        Spinner spinner = findViewById(R.id.spinner);
        CoreStockMonthlyReportAdapter adapter = new CoreStockMonthlyReportAdapter(getMonthStockUsageReportList(), this);
        spinner.setAdapter(adapter);

        recyclerView = findViewById(R.id.rv_stock_usage_report);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MonthStockUsageModel selected = getMonthStockUsageReportList().get(position);
                reloadRecycler(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                   //Implements Method From super Class
            }
        });

        Toolbar toolbar = findViewById(R.id.back_to_nav_toolbar);
        toolBarTextView = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setElevation(0);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolBarTextView.setText(getString(R.string.stock_usage_report));
        toolBarTextView.setOnClickListener(v -> finish());
        appBarLayout = findViewById(R.id.app_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onResumption() {
//Implements Method From super Class
    }
}
