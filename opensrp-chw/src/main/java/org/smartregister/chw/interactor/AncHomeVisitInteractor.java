package org.smartregister.chw.interactor;

import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.interactor.BaseAncHomeVisitInteractor;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;

import java.util.LinkedHashMap;
import java.util.Map;

public class AncHomeVisitInteractor extends BaseAncHomeVisitInteractor {

    private Flavor flavor = new AncHomeVisitInteractorFlv();

    @Override
    public void calculateActions(final BaseAncHomeVisitContract.View view, final String memberID, final BaseAncHomeVisitContract.InteractorCallBack callBack) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

                try {
                    for (Map.Entry<String, BaseAncHomeVisitAction> entry : flavor.calculateActions(view, memberID, callBack).entrySet()) {
                        actionList.put(entry.getKey(), entry.getValue());
                    }
                } catch (BaseAncHomeVisitAction.ValidationException e) {
                    e.printStackTrace();
                }

                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callBack.preloadActions(actionList);
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    public interface Flavor {

        LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(final BaseAncHomeVisitContract.View view, String memberID, final BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException;

    }
}
