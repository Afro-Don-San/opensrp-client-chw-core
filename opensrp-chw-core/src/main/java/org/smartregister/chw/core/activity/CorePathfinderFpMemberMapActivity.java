package org.smartregister.chw.core.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.adosa.opensrp.chw.fp.dao.PathfinderFpDao;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.adosa.opensrp.chw.fp.fragment.BasePathfinderFpCallDialogFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.maps.Style;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.view.customcontrols.CustomFontTextView;

import io.ona.kujaku.views.KujakuMapView;

import static org.smartregister.chw.core.utils.CoreConstants.DB_CONSTANTS.BASE_ENTITY_ID;

public class CorePathfinderFpMemberMapActivity extends CoreAncMemberMapActivity {
    private static int BOUNDING_BOX_PADDING = 100;
    protected AppBarLayout appBarLayout;
    private KujakuMapView kujakuMapView;
    private PathfinderFpMemberObject pathfinderFpMemberObject;
    private String ancWomanName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String baseEntityId= getIntent().getStringExtra(BASE_ENTITY_ID);
        pathfinderFpMemberObject = PathfinderFpDao.getMember(baseEntityId);
        ancWomanName = getIntent().getStringExtra(CoreConstants.KujakuConstants.NAME);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anc_member_map);
        kujakuMapView = findViewById(R.id.kujakuMapView);
        kujakuMapView.onCreate(savedInstanceState);
        kujakuMapView.showCurrentLocationBtn(true);
        kujakuMapView.setDisableMyLocationOnMapMove(true);
        kujakuMapView.getMapAsync(mapBoxMap -> {
            Style.Builder builder = new Style.Builder().fromUri("asset://ba_anc_style.json");
            mapBoxMap.setStyle(builder, new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {

                    FeatureCollection featureCollection = loadCommunityTransporters();
                    BoundingBox boundingBox = showCommunityTransporters(mapBoxMap, featureCollection);

                    zoomToPatientLocation(mapBoxMap, boundingBox);
                    addCommunityTransporterClickListener(kujakuMapView);
                }
            });
        });

        inflateToolbar();

    }

    protected void inflateToolbar() {
        Toolbar toolbar = findViewById(R.id.back_anc_toolbar);
        CustomFontTextView toolBarTextView = toolbar.findViewById(R.id.anc_map_toolbar_title);
        toolBarTextView.setText(String.format(getString(R.string.return_to_profile), ancWomanName.substring(0, ancWomanName.indexOf(" "))));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setElevation(0);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
        toolBarTextView.setOnClickListener(v -> finish());
        appBarLayout = findViewById(R.id.map_app_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(null);
        }

        TextView fpMemberNameView = findViewById(R.id.text_view_name);
        TextView familyNameView = findViewById(R.id.text_view_family);
        TextView landMarkView = findViewById(R.id.text_view_landmark);
        fpMemberNameView.setText(ancWomanName);
        familyNameView.setText(getString(R.string.house_hold_family_name, getIntent().getStringExtra(CoreConstants.KujakuConstants.FAMILY_NAME)));
        landMarkView.setText(getString(R.string.house_hold_discription, getIntent().getStringExtra(CoreConstants.KujakuConstants.LAND_MARK)));
        final View imageButton = findViewById(R.id.call_woman);
        imageButton.setOnClickListener(view -> {
            if (StringUtils.isNotBlank(pathfinderFpMemberObject.getPhoneNumber()) || StringUtils.isNotBlank(pathfinderFpMemberObject.getFamilyHeadPhoneNumber()))
                BasePathfinderFpCallDialogFragment.launchDialog(this, pathfinderFpMemberObject);
        });
    }
}
