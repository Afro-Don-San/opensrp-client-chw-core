package org.smartregister.chw.core.interactor;

import android.content.Context;

import com.adosa.opensrp.chw.fp.dao.PathfinderFpDao;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpAlertObject;
import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.jeasy.rules.api.Rules;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.interactor.BaseAncUpcomingServicesInteractor;
import org.smartregister.chw.anc.model.BaseUpcomingService;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.rule.PathfinderFpAlertRule;
import org.smartregister.chw.core.utils.PathfinderFamilyPlanningUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CorePathfinderFamilyPlanningUpcomingServicesInteractor extends BaseAncUpcomingServicesInteractor {
    protected MemberObject memberObject;
    protected Context context;

    @Override
    public List<BaseUpcomingService> getMemberServices(Context context, MemberObject memberObject) {
        this.memberObject = memberObject;
        this.context = context;
        List<BaseUpcomingService> serviceList = new ArrayList<>();
        evaluateFp(serviceList);
        return serviceList;
    }

    private void evaluateFp(List<BaseUpcomingService> serviceList) {
        String fpMethod;
        String fp_date = null;
        Integer fp_pillCycles = null;
        Rules rule = null;
        Date serviceDueDate = null;
        Date serviceOverDueDate = null;
        String serviceName = null;
        String fpMethodUsed = null;
        List<PathfinderFpAlertObject> familyPlanningList = PathfinderFpDao.getFpDetails(memberObject.getBaseEntityId());
        if (familyPlanningList.size() > 0) {
            for (PathfinderFpAlertObject familyPlanning : familyPlanningList) {
                fpMethodUsed = familyPlanning.getFpMethod();
                fp_date = familyPlanning.getFpStartDate();
                fp_pillCycles = PathfinderFpDao.getLastPillCycle(memberObject.getBaseEntityId(), fpMethodUsed);
                rule = PathfinderFamilyPlanningUtil.getFpRules(fpMethodUsed);
            }
        }
        fpMethod = PathfinderFamilyPlanningUtil.getTranslatedMethodValue(fpMethodUsed, context);
        Date lastVisitDate = null;
        Visit lastVisit;
        Date fpDate = PathfinderFamilyPlanningUtil.parseFpStartDate(fp_date);
        lastVisit = PathfinderFpDao.getLatestFpVisit(memberObject.getBaseEntityId(), PathfinderFamilyPlanningConstants.EventType.GIVE_FAMILY_PLANNING_METHOD, fpMethodUsed);
        if (lastVisit == null) {
            lastVisit = PathfinderFpDao.getLatestFpVisit(memberObject.getBaseEntityId(), PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_REGISTRATION, fpMethodUsed);
        }
        lastVisitDate = lastVisit.getDate();
        PathfinderFpAlertRule alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, fpDate, fp_pillCycles, fpMethod);
        if (fpMethodUsed.equalsIgnoreCase(PathfinderFamilyPlanningConstants.DBConstants.FP_COC) || fpMethodUsed.equalsIgnoreCase(PathfinderFamilyPlanningConstants.DBConstants.FP_POP) ||
                fpMethodUsed.equalsIgnoreCase(PathfinderFamilyPlanningConstants.DBConstants.FP_MALE_CONDOM) || fpMethodUsed.equalsIgnoreCase(PathfinderFamilyPlanningConstants.DBConstants.FP_FEMALE_CONDOM) || fpMethodUsed.equalsIgnoreCase(PathfinderFamilyPlanningConstants.DBConstants.FP_SDM)) {
            serviceDueDate = alertRule.getDueDate();
            serviceOverDueDate = alertRule.getOverDueDate();
            serviceName = MessageFormat.format(context.getString(R.string.refill), fpMethod);
        }

        BaseUpcomingService upcomingService = new BaseUpcomingService();
        if (serviceName != null) {
            upcomingService.setServiceDate(serviceDueDate);
            upcomingService.setOverDueDate(serviceOverDueDate);
            upcomingService.setServiceName(serviceName);
            serviceList.add(upcomingService);
        }

    }

}
