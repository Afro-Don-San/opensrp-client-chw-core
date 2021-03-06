package org.smartregister.chw.core.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import org.smartregister.chw.tb.provider.BaseTbRegisterProvider;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

public class CoreTbCommunityFollowupProvider extends BaseTbRegisterProvider {

    public CoreTbCommunityFollowupProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        super.getView(cursor, client, viewHolder);
        viewHolder.getDueWrapper().setVisibility(View.GONE);
    }

}