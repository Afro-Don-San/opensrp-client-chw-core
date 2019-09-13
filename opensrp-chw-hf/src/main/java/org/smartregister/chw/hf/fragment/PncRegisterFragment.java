package org.smartregister.chw.hf.fragment;

import android.view.View;

import org.smartregister.chw.core.fragment.CorePncRegisterFragment;
import org.smartregister.chw.core.provider.ChwPncRegisterProvider;
import org.smartregister.chw.hf.provider.HfPncRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.util.Utils;

import java.util.Set;

public class PncRegisterFragment extends CorePncRegisterFragment {

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        ChwPncRegisterProvider provider = new HfPncRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, provider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
    }

    @Override
    protected void openHomeVisit(CommonPersonObjectClient client) {
        //  PncHomeVisitActivity.startMe(getActivity(), new MemberObject(client), false);
    }

    @Override
    protected void openPncMemberProfile(CommonPersonObjectClient client) {
//        PncMemberProfileActivity.startMe(getActivity(), new MemberObject(client), getFamilyName(), getFamilyHeadPhone());
        Utils.showToast(getContext(), "Open PNC profile");
    }
}
