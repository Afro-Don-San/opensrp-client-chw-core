package org.smartregister.chw.core.provider;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;

import com.adosa.opensrp.chw.fp.dao.PathfinderFpDao;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.adosa.opensrp.chw.fp.provider.BasePathfinderFpRegisterProvider;
import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Rules;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.rule.PathfinderFpAlertRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FpUtil;
import org.smartregister.chw.core.utils.PathfinderFamilyPlanningUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Date;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by cozej4 on 4/28/20.
 *
 * @author cozej4 https://github.com/cozej4
 */
public class CorePathfinderFpProvider extends BasePathfinderFpRegisterProvider {

    private Context context;
    private View.OnClickListener onClickListener;

    public CorePathfinderFpProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        super.getView(cursor, client, viewHolder);

        viewHolder.dueButton.setVisibility(View.GONE);
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        viewHolder.dueButton.setOnClickListener(null);
        Utils.startAsyncTask(new UpdateAsyncTask(context, viewHolder, pc), null);
    }

    private void updateDueColumn(Context context, RegisterViewHolder viewHolder, PathfinderFpAlertRule fpAlertRule) {
        viewHolder.dueButton.setVisibility(View.VISIBLE);
        if (fpAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.NOT_DUE_YET)) {
            setVisitButtonNextDueStatus(context, FpUtil.sdf.format(fpAlertRule.getDueDate()), viewHolder.dueButton);
        }
        if (fpAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.DUE)) {
            setVisitButtonDueStatus(context, String.valueOf(Days.daysBetween(new DateTime(fpAlertRule.getDueDate()), new DateTime()).getDays()), viewHolder.dueButton);
        } else if (fpAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.OVERDUE)) {
            setVisitButtonOverdueStatus(context, String.valueOf(Days.daysBetween(new DateTime(fpAlertRule.getOverDueDate()), new DateTime()).getDays()), viewHolder.dueButton);
        } else if (fpAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.VISIT_DONE)) {
            setVisitDone(context, viewHolder.dueButton);
        }
    }

    private void setVisitButtonNextDueStatus(Context context, String visitDue, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.light_grey_text));
        dueButton.setText(context.getString(org.smartregister.chw.core.R.string.fp_visit_day_next_due, visitDue));
        dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.colorless_btn_selector);
        dueButton.setOnClickListener(null);
    }


    private void setVisitButtonDueStatus(Context context, String visitDue, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.alert_in_progress_blue));
        if (visitDue.equalsIgnoreCase("0")) {
            dueButton.setText(context.getString(org.smartregister.chw.core.R.string.fp_visit_day_due_today));
        } else {
            dueButton.setText(context.getString(org.smartregister.chw.core.R.string.fp_visit_day_due, visitDue));
        }
        dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.blue_btn_selector);
        dueButton.setOnClickListener(onClickListener);
    }

    private void setVisitButtonOverdueStatus(Context context, String visitDue, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.white));
        if (visitDue.equalsIgnoreCase("0")) {
            dueButton.setText(context.getString(org.smartregister.chw.core.R.string.fp_visit_day_overdue_today));

        } else {
            dueButton.setText(context.getString(org.smartregister.chw.core.R.string.fp_visit_day_overdue, visitDue));
        }
        dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.overdue_red_btn_selector);
        dueButton.setOnClickListener(onClickListener);
    }

    private void setVisitDone(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.alert_complete_green));
        dueButton.setText(context.getString(org.smartregister.chw.core.R.string.visit_done));
        dueButton.setBackgroundColor(context.getResources().getColor(org.smartregister.chw.core.R.color.transparent));
        dueButton.setOnClickListener(null);
    }


    private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {
        private final RegisterViewHolder viewHolder;
        private final CommonPersonObjectClient pc;
        private final Context context;
        private PathfinderFpAlertRule fpAlertRule;
        private Visit lastVisit;
        private Integer pillCycles;
        private String dayFp;
        private String fpMethod;
        private PathfinderFpMemberObject pathfinderFpMemberObject;

        private UpdateAsyncTask(Context context, RegisterViewHolder viewHolder, CommonPersonObjectClient pc) {
            this.context = context;
            this.viewHolder = viewHolder;
            this.pc = pc;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String baseEntityID = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
            dayFp = Utils.getValue(pc.getColumnmaps(), PathfinderFamilyPlanningConstants.DBConstants.FP_FP_START_DATE, true);
            fpMethod = Utils.getValue(pc.getColumnmaps(), PathfinderFamilyPlanningConstants.DBConstants.FP_METHOD_ACCEPTED, false);
            pillCycles = PathfinderFpDao.getLastPillCycle(baseEntityID, fpMethod);
            pathfinderFpMemberObject = PathfinderFpDao.getMember(baseEntityID);
            lastVisit = PathfinderFpDao.getLatestFpVisit(baseEntityID, PathfinderFamilyPlanningConstants.EventType.FP_FOLLOW_UP_VISIT, fpMethod);

            if (lastVisit == null) {
                lastVisit = PathfinderFpDao.getLatestFpVisit(baseEntityID, PathfinderFamilyPlanningConstants.EventType.GIVE_FAMILY_PLANNING_METHOD, fpMethod);
            }

            if (lastVisit == null && pathfinderFpMemberObject.isClientAlreadyUsingFp()) {//for clients already using family planning method
                lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId(), PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_REGISTRATION, pathfinderFpMemberObject.getFpMethod());
            }

            if (lastVisit == null) {//for pregnant clients
                lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            Date fpDate = null;
            try {
                fpDate = new Date(Long.parseLong(dayFp));
            } catch (Exception e) {
                Timber.e(e);
            }
            Date lastVisitDate;
            if (lastVisit != null) {
                lastVisitDate = lastVisit.getDate();

                if(!pathfinderFpMemberObject.getEdd().isEmpty() && pathfinderFpMemberObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.PREGNANT)){
                    Rules rule = PathfinderFamilyPlanningUtil.getPregnantWomenFpRules();
                    fpAlertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpMemberObject.getEdd()), 0, pathfinderFpMemberObject.getFpMethod());
                }else if(pathfinderFpMemberObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.NOT_UNLIKELY_PREGNANT)){
                    Rules rule = PathfinderFamilyPlanningUtil.getPregnantScreeningFollowupRules();
                    fpAlertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpMemberObject.getFpPregnancyScreeningDate()), 0, pathfinderFpMemberObject.getFpMethod());
                }else if ((pathfinderFpMemberObject.getFpMethod().equals("sdm") && pathfinderFpMemberObject.getPeriodsRegularity().equals("IRREGULAR")) || pathfinderFpMemberObject.isManRequestedMethodForPartner()){
                    Rules rule;
                    if (pathfinderFpMemberObject.isManRequestedMethodForPartner())
                        rule = PathfinderFamilyPlanningUtil.getManChosePartnersFpMethodFollowupRules();
                    else
                        rule = PathfinderFamilyPlanningUtil.getSdmMethodChoiceFollowupRules();
                    fpAlertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpMemberObject.getFpMethodChoiceDate()), 0, pathfinderFpMemberObject.getFpMethod());
                }else if(!pathfinderFpMemberObject.getFpStartDate().isEmpty()) {
                    Timber.e("Coze fp method = "+fpMethod);
                    Timber.e("Coze fpDate = "+fpDate);
                    Timber.e("Coze lastVisitDate = "+lastVisitDate);
                    Rules rule = PathfinderFamilyPlanningUtil.getFpRules(fpMethod);
                    fpAlertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, fpDate, pillCycles, fpMethod);
                    Timber.e("Coze fpAlertRule = "+new Gson().toJson(fpAlertRule));
                }

                if (fpAlertRule != null
                        && StringUtils.isNotBlank(fpAlertRule.getVisitID())
                        && !fpAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.EXPIRED)
                ) {
                    updateDueColumn(context, viewHolder, fpAlertRule);
                }
            }

        }
    }
}
