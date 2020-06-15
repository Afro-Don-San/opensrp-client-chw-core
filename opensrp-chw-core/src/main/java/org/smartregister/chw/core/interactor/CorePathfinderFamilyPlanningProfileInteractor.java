package org.smartregister.chw.core.interactor;

import android.content.Context;

import com.adosa.opensrp.chw.fp.contract.BaseFpProfileContract;
import com.adosa.opensrp.chw.fp.dao.PathfinderFpDao;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.adosa.opensrp.chw.fp.interactor.BasePathfinderFpProfileInteractor;
import com.google.gson.Gson;

import org.joda.time.LocalDate;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.model.BaseUpcomingService;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.contract.CorePathfinderFamilyPlanningMemberProfileContract;
import org.smartregister.chw.core.dao.AlertDao;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.dao.PNCDao;
import org.smartregister.chw.core.dao.VisitDao;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.dao.AbstractDao;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import timber.log.Timber;


public class CorePathfinderFamilyPlanningProfileInteractor extends BasePathfinderFpProfileInteractor implements CorePathfinderFamilyPlanningMemberProfileContract.Interactor {
    private Context context;

    public CorePathfinderFamilyPlanningProfileInteractor(Context context) {
        this.context = context;
    }

    @Override
    public void updateProfileFpStatusInfo(PathfinderFpMemberObject memberObject, BaseFpProfileContract.InteractorCallback callback) {
        Timber.e("Coze :: updateProfileFpStatusInfo = ");
        Runnable runnable = new Runnable() {
            Date lastVisitDate = getLastVisitDate(memberObject);
            AlertStatus familyAlert = AlertDao.getFamilyAlertStatus(memberObject.getFamilyBaseEntityId());
            Alert upcomingService = getAlerts(context, memberObject.getBaseEntityId());

            @Override
            public void run() {
                appExecutors.mainThread().execute(() -> {
                    callback.refreshFamilyStatus(familyAlert);
                    callback.refreshLastVisit(lastVisitDate);
                    if (upcomingService == null) {
                        callback.refreshUpComingServicesStatus("", AlertStatus.complete, new Date());
                    } else {
                        callback.refreshUpComingServicesStatus(upcomingService.scheduleName(), upcomingService.status(), new LocalDate(upcomingService.startDate()).toDate());
                    }
                });
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    private String getMemberVisitType(String baseEntityId) {
        String type = null;

        if (AncDao.isANCMember(baseEntityId)) {
            type = CoreConstants.TABLE_NAME.ANC_MEMBER;
        } else if (PNCDao.isPNCMember(baseEntityId)) {
            type = CoreConstants.TABLE_NAME.PNC_MEMBER;
        }
        return type;
    }

    public Date getLastVisitDate(PathfinderFpMemberObject memberObject) {
        Date lastVisitDate = null;
        List<Visit> visits = new ArrayList<>();
        String memberType = getMemberVisitType(memberObject.getBaseEntityId());

        if (memberType != null) {
            switch (memberType) {
                case CoreConstants.TABLE_NAME.PNC_MEMBER:
                    visits = VisitDao.getPNCVisitsMedicalHistory(memberObject.getBaseEntityId());
                    break;
                case CoreConstants.TABLE_NAME.ANC_MEMBER:
                    visits = getAncVisitsMedicalHistory(memberObject.getBaseEntityId());
                    break;
                default:
                    break;
            }

            if (visits.size() > 0) {
                lastVisitDate = visits.get(0).getDate();
            }
        }
        return lastVisitDate;
    }

    private List<Visit> getAncVisitsMedicalHistory(String baseEntityId) {
        List<Visit> visits = VisitUtils.getVisits(baseEntityId);
        List<Visit> allVisits = new ArrayList<>(visits);

        for (Visit visit : visits) {
            List<Visit> childVisits = VisitUtils.getChildVisits(visit.getVisitId());
            allVisits.addAll(childVisits);
        }
        return allVisits;
    }

    protected Alert getAlerts(Context context, String baseEntityID) {
        try {
            List<BaseUpcomingService> baseUpcomingServices = new ArrayList<>(new CorePathfinderFamilyPlanningUpcomingServicesInteractor().getMemberServices(context, toMember(PathfinderFpDao.getMember(baseEntityID))));
            Timber.e("Coze :: serviceList = "+new Gson().toJson(baseUpcomingServices));
            if (baseUpcomingServices.size() > 0) {
                Comparator<BaseUpcomingService> comparator = (o1, o2) -> o1.getServiceDate().compareTo(o2.getServiceDate());
                Collections.sort(baseUpcomingServices, comparator);

                BaseUpcomingService baseUpcomingService = baseUpcomingServices.get(0);
                Date serviceDate = baseUpcomingService.getServiceDate();
                String serviceName = baseUpcomingService.getServiceName();
                AlertStatus upcomingServiceAlertStatus = serviceDate != null && serviceDate.before(new Date()) ? AlertStatus.urgent : AlertStatus.normal;
                String formattedServiceDate = serviceDate != null ? AbstractDao.getDobDateFormat().format(serviceDate) : null;
                Timber.e("Coze :: upcomingServiceAlertStatus = "+new Gson().toJson(upcomingServiceAlertStatus));
                Timber.e("Coze :: formattedServiceDate = "+new Gson().toJson(formattedServiceDate));
                Timber.e("Coze :: serviceName = "+serviceName);
                return new Alert(baseEntityID, serviceName, serviceName, upcomingServiceAlertStatus, formattedServiceDate, "", true);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    private MemberObject toMember(PathfinderFpMemberObject memberObject) {
        MemberObject res = new MemberObject();
        res.setBaseEntityId(memberObject.getBaseEntityId());
        res.setFirstName(memberObject.getFirstName());
        res.setLastName(memberObject.getLastName());
        res.setMiddleName(memberObject.getMiddleName());
        res.setDob(memberObject.getAge());
        return res;
    }

    @Override
    public void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception {
        CoreReferralUtils.createReferralEvent(allSharedPreferences, jsonString, CoreConstants.TABLE_NAME.FP_REFERRAL, entityID);
    }
}