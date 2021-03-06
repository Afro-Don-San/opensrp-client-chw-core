package org.smartregister.chw.core.rule;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.smartregister.chw.core.utils.CoreConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class PathfinderFpAlertRule implements ICommonRule {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String visitID;
    private DateTime fpDate;
    private DateTime dueDate;
    private DateTime overDueDate;
    private DateTime lastVisitDate;
    private DateTime expiryDate;
    private int fpDifference;
    private Integer pillCycles;
    private String fpMethod;

    public PathfinderFpAlertRule(Date fpDate, Date lastVisitDate, Integer pillCycles, String fpMethod) {
        this.pillCycles = pillCycles == null ? 0 : pillCycles;
        this.fpDate = fpDate != null ? new DateTime(sdf.format(fpDate)) : null;
        this.lastVisitDate = lastVisitDate == null ? null : new DateTime(lastVisitDate);
        this.fpMethod = fpMethod;
        fpDifference = Days.daysBetween(new DateTime(fpDate), new DateTime()).getDays();
    }

    public String getVisitID() {
        return visitID;
    }

    public void setVisitID(String visitID) {
        this.visitID = visitID;
    }

    public boolean isCocPopValid(int dueDay, int overdueDate, int daysFromOverdueTillExpiry) {
        if (lastVisitDate != null) {
            this.dueDate = (new DateTime(this.lastVisitDate)).plusDays(dueDay);

        } else {
            this.dueDate = (new DateTime(this.fpDate)).plusDays(dueDay);
        }
        this.overDueDate = (new DateTime(this.dueDate)).plusDays(overdueDate);
        this.expiryDate = (new DateTime(this.overDueDate)).plusDays(daysFromOverdueTillExpiry);
        return true;
    }

    public boolean isCondomValid(int dueDay, int overdueDate, int daysFromOverdueTillExpiry) {
        if (lastVisitDate != null) {
            this.dueDate = (new DateTime(this.lastVisitDate)).plusDays(dueDay);
        } else {
            this.dueDate = (new DateTime(this.fpDate)).plusDays(dueDay);
        }
        this.overDueDate = (new DateTime(this.dueDate)).plusDays(overdueDate);
        this.expiryDate = (new DateTime(this.overDueDate)).plusDays(daysFromOverdueTillExpiry);
        return true;
    }

    public boolean isPregnantWomanFollowupValid(int dueDay, int overdueDate, int expiry) {
        this.dueDate = new DateTime(fpDate).plusDays(dueDay);
        this.overDueDate = new DateTime(fpDate).plusDays(overdueDate);
        this.expiryDate = new DateTime(fpDate).plusDays(expiry);
        return true;
    }

    public boolean isPregnancyScreeningFollowupValid(int dueDay, int overdueDate, int expiry) {
        this.dueDate = new DateTime(fpDate).plusDays(dueDay);
        this.overDueDate = new DateTime(fpDate).plusDays(overdueDate);
        this.expiryDate = new DateTime(fpDate).plusDays(expiry);
        return true;
    }

    public boolean isFollowupValid(int dueDay, int overdueDate, int expiry) {
        this.dueDate = new DateTime(lastVisitDate).plusDays(dueDay);
        this.overDueDate = new DateTime(lastVisitDate).plusDays(overdueDate);
        this.expiryDate = new DateTime(lastVisitDate).plusDays(expiry);
        return true;
    }

    public boolean isFpChoiceSdmFollowupValid(int dueDay, int overdueDate, int expiry) {
        this.dueDate = new DateTime(fpDate).plusDays(dueDay);
        this.overDueDate = new DateTime(fpDate).plusDays(overdueDate);
        this.expiryDate = new DateTime(fpDate).plusDays(expiry);
        return true;
    }

    public boolean isInjectionValid(int dueDay, int overdueDate) {
        if (lastVisitDate != null) {
            this.dueDate = new DateTime(lastVisitDate).plusDays(dueDay);
            this.overDueDate = new DateTime(lastVisitDate).plusDays(overdueDate);
        } else {
            this.dueDate = new DateTime(fpDate).plusDays(dueDay);
            this.overDueDate = new DateTime(fpDate).plusDays(overdueDate);
        }

        return true;
    }

    public boolean isFemaleSterilizationFollowUpOneValid(int dueDay, int overdueDate, int expiry) {
        if (fpDifference >= dueDay && fpDifference < expiry) {
            this.dueDate = new DateTime(fpDate).plusDays(dueDay);
            this.overDueDate = new DateTime(fpDate).plusDays(overdueDate);
            this.expiryDate = new DateTime(fpDate).plusDays(expiry);
            return true;
        }
        return false;
    }

    public boolean isFemaleSterilizationFollowUpTwoValid(int dueDay, int overdueDate, int expiry) {
        int expiryDiff = Days.daysBetween(new DateTime(fpDate), new DateTime(fpDate).plusMonths(expiry)).getDays();
        if (fpDifference >= dueDay && fpDifference < expiryDiff) {
            this.dueDate = new DateTime(fpDate).plusDays(dueDay);
            this.overDueDate = new DateTime(fpDate).plusDays(overdueDate);
            this.expiryDate = new DateTime(fpDate).plusMonths(expiry);
            return true;
        }
        return false;
    }

    public boolean isFemaleSterilizationFollowUpThreeValid(int dueDay, int overdueDate, int expiry) {
        int dueDiff = Days.daysBetween(new DateTime(fpDate), new DateTime(fpDate).plusMonths(dueDay)).getDays();
        int expiryDiff = Days.daysBetween(new DateTime(fpDate), new DateTime(fpDate).plusMonths(expiry)).getDays();
        if (fpDifference >= dueDiff && fpDifference < expiryDiff) {
            this.dueDate = new DateTime(fpDate).plusMonths(dueDay);
            this.overDueDate = new DateTime(fpDate).plusMonths(overdueDate).plusDays(2);
            this.expiryDate = new DateTime(fpDate).plusMonths(expiry);
            return true;
        }
        return false;
    }

    public boolean isIUCDValid(int dueDay, int overdueDate, int expiry) {
        int dueDiff = Days.daysBetween(new DateTime(fpDate), new DateTime(fpDate).plusMonths(dueDay)).getDays();
        int expiryDiff = Days.daysBetween(new DateTime(fpDate), new DateTime(fpDate).plusMonths(expiry)).getDays();
        if (fpDifference >= dueDiff && fpDifference < expiryDiff) {
            this.dueDate = new DateTime(fpDate).plusMonths(dueDay);
            this.overDueDate = new DateTime(fpDate).plusMonths(overdueDate).plusDays(2);
            this.expiryDate = new DateTime(fpDate).plusMonths(expiry);
            return true;
        } else if (fpDifference < dueDiff && fpDifference < expiryDiff && dueDiff <= 31) {
            this.dueDate = new DateTime(fpDate).plusMonths(dueDay);
            return true;
        }
        return false;
    }

    public Integer getPillCycles() {
        return pillCycles;
    }

    public Date getDueDate() {
        return dueDate != null ? dueDate.toDate() : null;
    }

    public Date getOverDueDate() {
        return overDueDate != null ? overDueDate.toDate() : null;
    }

    public Date getExpiryDate() {
        return expiryDate != null ? expiryDate.toDate() : null;
    }

    public Date getCompletionDate() {
        if (lastVisitDate != null && ((lastVisitDate.isAfter(dueDate) || lastVisitDate.isEqual(dueDate)) && lastVisitDate.isBefore(expiryDate)))
            return lastVisitDate.toDate();

        return null;
    }

    @Override
    public String getRuleKey() {
        return "pathfinderFpAlertRule";
    }

    @Override
    public String getButtonStatus() {
        DateTime lastVisit = lastVisitDate;
        DateTime currentDate = new DateTime(new LocalDate().toDate());
        if (expiryDate != null) {
            if ((lastVisit.isAfter(dueDate) || lastVisit.isEqual(dueDate)) && lastVisit.isBefore(expiryDate))
                return CoreConstants.VISIT_STATE.VISIT_DONE;
            if (lastVisit.isBefore(dueDate)) {
                if (currentDate.isBefore(overDueDate) && (Days.daysBetween(dueDate, currentDate).getDays() >= 0))
                    return CoreConstants.VISIT_STATE.DUE;

                if (currentDate.isBefore(expiryDate) && (currentDate.isAfter(overDueDate) || currentDate.isEqual(overDueDate)))
                    return CoreConstants.VISIT_STATE.OVERDUE;
                if (currentDate.isBefore(dueDate) && currentDate.isBefore(expiryDate)) {
                    return CoreConstants.VISIT_STATE.NOT_DUE_YET;
                }
            }
        }


        return CoreConstants.VISIT_STATE.EXPIRED;
    }
}