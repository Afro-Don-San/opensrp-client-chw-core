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

import static com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants.EventType.FP_FOLLOW_UP_VISIT;

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
        private Date lastVisitDate;
        private Date fpDate;
        private Rules rule;

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
            lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId());

            if (!pathfinderFpMemberObject.getFpStartDate().equals("") && !pathfinderFpMemberObject.getFpStartDate().equals("0")) {
                if (pathfinderFpMemberObject.isClientIsCurrentlyReferred()) {
                    lastVisitDate = null;
                    if (lastVisit == null) {
                        lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId());
                    }
                    lastVisitDate = lastVisit.getDate();
                    rule = PathfinderFamilyPlanningUtil.getReferralFollowupRules();
                    fpDate = FpUtil.parseFpStartDate(pathfinderFpMemberObject.getFpStartDate());
                } else {

                    lastVisitDate = null;
                    if (lastVisit == null) {
                        lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId());
                    }

                    if (lastVisit == null && pathfinderFpMemberObject.isClientAlreadyUsingFp()) {//for clients already using family planning method
                        lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId(), PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_REGISTRATION, pathfinderFpMemberObject.getFpMethod());
                    }
                    lastVisitDate = lastVisit.getDate();
                    rule = PathfinderFamilyPlanningUtil.getFpRules(pathfinderFpMemberObject.getFpMethod());
                    Integer pillCycles = PathfinderFpDao.getLastPillCycle(pathfinderFpMemberObject.getBaseEntityId(), pathfinderFpMemberObject.getFpMethod());
                    fpDate = FpUtil.parseFpStartDate(pathfinderFpMemberObject.getFpStartDate());
                }
            } else if (pathfinderFpMemberObject.isPregnancyScreeningDone() && !pathfinderFpMemberObject.getEdd().equals("") && pathfinderFpMemberObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.PREGNANT)) {
                lastVisitDate = null;
                if (lastVisit == null) {
                    lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId());
                }
                lastVisitDate = lastVisit.getDate();
                rule = PathfinderFamilyPlanningUtil.getPregnantWomenFpRules();
                fpDate = FpUtil.parseFpStartDate(pathfinderFpMemberObject.getEdd());
            } else if (pathfinderFpMemberObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.NOT_UNLIKELY_PREGNANT)) {
                lastVisitDate = null;
                if (lastVisit == null) {
                    lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId());
                }
                lastVisitDate = lastVisit.getDate();
                rule = PathfinderFamilyPlanningUtil.getPregnantScreeningFollowupRules();
                fpDate = FpUtil.parseFpStartDate(pathfinderFpMemberObject.getFpPregnancyScreeningDate());
            } else if (pathfinderFpMemberObject.isManRequestedMethodForPartner()) {
                lastVisitDate = null;
                if (lastVisit == null) {
                    lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId());
                }
                lastVisitDate = lastVisit.getDate();
                rule = PathfinderFamilyPlanningUtil.getManChosePartnersFpMethodFollowupRules();
                fpDate = FpUtil.parseFpStartDate(pathfinderFpMemberObject.getFpMethodChoiceDate());
            } else if (pathfinderFpMemberObject.isClientIsCurrentlyReferred()) {
                if (lastVisit == null) {
                    lastVisit = PathfinderFpDao.getLatestFpVisit(pathfinderFpMemberObject.getBaseEntityId());
                }
                lastVisitDate = lastVisit.getDate();
                rule = PathfinderFamilyPlanningUtil.getReferralFollowupRules();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (pathfinderFpMemberObject.isClientIsCurrentlyReferred()) {
                fpAlertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpMemberObject.getFpStartDate()), 0, pathfinderFpMemberObject.getFpMethod());
            } else {
                fpAlertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, fpDate, 0, pathfinderFpMemberObject.getFpMethod());

                if(pathfinderFpMemberObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.PREGNANT)){
                    Timber.e("Coze lave :: client is "+pathfinderFpMemberObject.getFirstName());
                    Timber.e("Coze lave :: edd is "+pathfinderFpMemberObject.getEdd());
                    Timber.e("Coze lave :: lastVisitDate is "+lastVisitDate);
                    Timber.e("Coze lave :: fpAlertRule "+new Gson().toJson(fpAlertRule));

                }
            }
            if (lastVisit != null) {
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
