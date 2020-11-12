package org.smartregister.chw.core.interactor;

import android.content.Context;

import com.adosa.opensrp.chw.fp.dao.PathfinderFpDao;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.jeasy.rules.api.Rules;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.interactor.BaseAncUpcomingServicesInteractor;
import org.smartregister.chw.anc.model.BaseUpcomingService;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.rule.PathfinderFpAlertRule;
import org.smartregister.chw.core.utils.FpUtil;
import org.smartregister.chw.core.utils.PathfinderFamilyPlanningUtil;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;


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
        PathfinderFpMemberObject pathfinderFpAlertObject = PathfinderFpDao.getMember(memberObject.getBaseEntityId());

        fpMethodUsed = pathfinderFpAlertObject.getFpMethod();
        fp_date = pathfinderFpAlertObject.getFpStartDate();
        fp_pillCycles = PathfinderFpDao.getLastPillCycle(memberObject.getBaseEntityId(), fpMethodUsed);
        rule = PathfinderFamilyPlanningUtil.getFpRules(fpMethodUsed);

        fpMethod = PathfinderFamilyPlanningUtil.getTranslatedMethodValue(fpMethodUsed, context);

        Date lastVisitDate;
        Visit lastVisit;

        Date fpDate = null;
        try {
            fpDate = PathfinderFamilyPlanningUtil.parseFpStartDate(fp_date);
        } catch (Exception e) {
            Timber.e(e);
        }

        if (fpDate == null) {
            try {
                fpDate = new Date(new BigDecimal(fp_date).longValue());
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        lastVisit = PathfinderFpDao.getLatestFpVisit(memberObject.getBaseEntityId(), PathfinderFamilyPlanningConstants.EventType.FP_FOLLOW_UP_VISIT, fpMethod);
        if (lastVisit == null) {
            lastVisit = PathfinderFpDao.getLatestFpVisit(memberObject.getBaseEntityId(), PathfinderFamilyPlanningConstants.EventType.GIVE_FAMILY_PLANNING_METHOD, fpMethod);
        }

        if (lastVisit == null) {
            lastVisit = PathfinderFpDao.getLatestFpVisit(memberObject.getBaseEntityId());
        }
        lastVisitDate = lastVisit.getDate();
        PathfinderFpAlertRule alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, fpDate, fp_pillCycles, fpMethod);
        if (!pathfinderFpAlertObject.getFpStartDate().equals("0") && !pathfinderFpAlertObject.getFpStartDate().equals("") && !fpMethodUsed.isEmpty()) {
            serviceDueDate = alertRule.getDueDate();
            serviceOverDueDate = alertRule.getOverDueDate();
            serviceName = MessageFormat.format(context.getString(R.string.pathfinder_refill), fpMethod);
            BaseUpcomingService baseUpcomingService = generateUpcomingService(serviceName, serviceDueDate, serviceOverDueDate);
            if (baseUpcomingService != null)
                serviceList.add(baseUpcomingService);
        }

        if (!pathfinderFpAlertObject.getEdd().isEmpty() && pathfinderFpAlertObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.PREGNANT)) {
            lastVisit = PathfinderFpDao.getLatestFpVisit(memberObject.getBaseEntityId());
            lastVisitDate = lastVisit.getDate();
            alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(PathfinderFamilyPlanningUtil.getPregnantWomenFpRules(), lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpAlertObject.getEdd()), fp_pillCycles, fpMethod);

            serviceDueDate = alertRule.getDueDate();
            serviceOverDueDate = alertRule.getOverDueDate();
            serviceName = context.getString(R.string.pregnant_client_followup);

            BaseUpcomingService baseUpcomingService = generateUpcomingService(serviceName, serviceDueDate, serviceOverDueDate);
            if (baseUpcomingService != null)
                serviceList.add(baseUpcomingService);
        }

        if (pathfinderFpAlertObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.NOT_UNLIKELY_PREGNANT)) {
            alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(PathfinderFamilyPlanningUtil.getPregnantScreeningFollowupRules(), lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpAlertObject.getFpPregnancyScreeningDate()), fp_pillCycles, fpMethod);
            serviceDueDate = alertRule.getDueDate();
            serviceOverDueDate = alertRule.getOverDueDate();
            if (pathfinderFpAlertObject.getChoosePregnancyTestReferral().equals(PathfinderFamilyPlanningConstants.ChoosePregnancyTestReferral.WAIT_FOR_NEXT_VISIT))
                serviceName = context.getString(R.string.pregnancy_screening_followup);
            else
                serviceName = context.getString(R.string.fp_pregnancy_test_followup);

                BaseUpcomingService baseUpcomingService = generateUpcomingService(serviceName, serviceDueDate, serviceOverDueDate);
            if (baseUpcomingService != null)
                serviceList.add(baseUpcomingService);
        }

        if (pathfinderFpAlertObject.isManRequestedMethodForPartner()) {
            rule = PathfinderFamilyPlanningUtil.getManChosePartnersFpMethodFollowupRules();
            serviceName = context.getString(R.string.man_chose_fp_method_for_partner_followup);

            alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpAlertObject.getFpMethodChoiceDate()), fp_pillCycles, fpMethod);
            serviceDueDate = alertRule.getDueDate();
            serviceOverDueDate = alertRule.getOverDueDate();


            BaseUpcomingService baseUpcomingService = generateUpcomingService(serviceName, serviceDueDate, serviceOverDueDate);
            if (baseUpcomingService != null)
                serviceList.add(baseUpcomingService);
        }

        if (pathfinderFpAlertObject.isClientIsCurrentlyReferred()) {
            rule = PathfinderFamilyPlanningUtil.getReferralFollowupRules();
            serviceName = context.getString(R.string.fp_referral_followup);

            alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpAlertObject.getFpStartDate()), fp_pillCycles, fpMethod);
            serviceDueDate = alertRule.getDueDate();
            serviceOverDueDate = alertRule.getOverDueDate();


            BaseUpcomingService baseUpcomingService = generateUpcomingService(serviceName, serviceDueDate, serviceOverDueDate);
            if (baseUpcomingService != null)
                serviceList.add(baseUpcomingService);
        }
    }

    private BaseUpcomingService generateUpcomingService(String serviceName, Date serviceDueDate, Date serviceOverDueDate) {
        BaseUpcomingService upcomingService = new BaseUpcomingService();
        if (serviceName != null) {
            upcomingService.setServiceDate(serviceDueDate);
            upcomingService.setOverDueDate(serviceOverDueDate);
            upcomingService.setServiceName(serviceName);
            return upcomingService;
        }
        return null;
    }

}
