package org.smartregister.chw.core.utils;

import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.jeasy.rules.api.Rules;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.rule.PathfinderFpAlertRule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class PathfinderFamilyPlanningUtil extends com.adosa.opensrp.chw.fp.util.FpUtil {

    public static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public static Rules getFpRules(String fpMethod) {
        Rules fpRule = new Rules();

        switch (fpMethod) {
            case PathfinderFamilyPlanningConstants.DBConstants.FP_SDM:
            case PathfinderFamilyPlanningConstants.DBConstants.FP_IMPLANTS:
            case PathfinderFamilyPlanningConstants.DBConstants.FP_INJECTABLE:
            case PathfinderFamilyPlanningConstants.DBConstants.FP_IUD:
            case PathfinderFamilyPlanningConstants.DBConstants.FP_LAM:
            case PathfinderFamilyPlanningConstants.DBConstants.FP_TUBAL_LIGATION:
            case PathfinderFamilyPlanningConstants.DBConstants.FP_VASECTOMY:
            case PathfinderFamilyPlanningConstants.DBConstants.FP_POP:
            case PathfinderFamilyPlanningConstants.DBConstants.FP_COC:
                fpRule = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.PATHFINDER_FP_COC_POP_REFILL);
                break;
            case PathfinderFamilyPlanningConstants.DBConstants.FP_FEMALE_CONDOM:
            case PathfinderFamilyPlanningConstants.DBConstants.FP_MALE_CONDOM:
                fpRule = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.PATHFINDER_FP_CONDOM_REFILL);
                break;
            default:
                break;
        }
        return fpRule;
    }

    public static Rules getPregnantWomenFpRules() {
        Rules fpRule = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.PATHFINDER_PREGNANT_WOMAN_FP_FOLLOWUP);
        return fpRule;
    }
    public static Rules getPregnantScreeningFollowupRules() {
        Rules fpRule = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.PATHFINDER_PREGNANCY_SCREENING_AND_REFERRAL_FOLLOWUP);
        return fpRule;
    }
    public static Rules getReferralFollowupRules() {
        Rules fpRule = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.PATHFINDER_REFERRAL_REFERRAL_FOLLOWUP);
        return fpRule;
    }
    public static Rules getSdmMethodChoiceFollowupRules() {
        Rules fpRule = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.PATHFINDER_SDM_FAMILY_PLANNING_METHOD_CHOICE_FOLLOWUP);
        return fpRule;
    }
    public static Rules getManChosePartnersFpMethodFollowupRules() {
        Rules fpRule = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.PATHFINDER_MAN_CHOSE_PARTNER_FP_METHOD_FOLLOWUP);
        return fpRule;
    }

    public static Date parseFpStartDate(String startDate) {
        try {
            return sdf.parse(startDate);
        } catch (ParseException e) {
            Timber.e(e);
        }

        return null;
    }

    public static MemberObject toMember(PathfinderFpMemberObject memberObject) {
        MemberObject res = new MemberObject();
        res.setBaseEntityId(memberObject.getBaseEntityId());
        res.setFirstName(memberObject.getFirstName());
        res.setLastName(memberObject.getLastName());
        res.setMiddleName(memberObject.getMiddleName());
        res.setDob(memberObject.getAge());
        return res;
    }

    public static PathfinderFpAlertRule getFpVisitStatus(Rules rules, Date lastVisitDate, Date fpDate, Integer pillCycles, String fpMethod) {
        PathfinderFpAlertRule fpAlertRule = new PathfinderFpAlertRule(fpDate, lastVisitDate, pillCycles, fpMethod);
        CoreChwApplication.getInstance().getRulesEngineHelper().getButtonAlertStatus(fpAlertRule, rules);
        return fpAlertRule;
    }
}
