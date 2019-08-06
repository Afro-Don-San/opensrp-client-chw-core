package com.opensrp.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import com.opensrp.chw.core.holders.RegisterViewHolder;
import com.opensrp.chw.core.provider.CoreChildRegisterProvider;
import com.opensrp.chw.core.task.UpdateLastAsyncTask;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

/**
 * Created by keyman on 13/11/2018.
 */

public class HfChildRegisterProvider extends CoreChildRegisterProvider {

    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    private View.OnClickListener onClickListener;
    private Context context;
    private CommonRepository commonRepository;

    public HfChildRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, visibleColumns, onClickListener, paginationClickListener);
        this.visibleColumns = visibleColumns;
        this.onClickListener = onClickListener;
        this.context = context;
        this.commonRepository = commonRepository;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, viewHolder);
            populateIdentifierColumn(pc, viewHolder);
            populateLastColumn(pc, viewHolder);

            return;
        }
    }

    @Override
    public void addButtonClickListeners(SmartRegisterClient client, RegisterViewHolder viewHolder) {
        viewHolder.dueButtonLayout.setVisibility(View.GONE);
        viewHolder.goToProfileLayout.setVisibility(View.VISIBLE);

        View patient = viewHolder.childColumn;
        attachPatientOnclickListener(patient, client);

        View goToProfileImage = viewHolder.goToProfileImage;
        attachPatientOnclickListener(goToProfileImage, client);

        View goToProfileLayout = viewHolder.goToProfileLayout;
        attachPatientOnclickListener(goToProfileLayout, client);

    }

    private void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        Utils.startAsyncTask(new UpdateLastAsyncTask(context, commonRepository, viewHolder, pc.entityId(), onClickListener), null);
    }

}
