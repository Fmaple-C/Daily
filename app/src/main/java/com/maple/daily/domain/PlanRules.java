package com.maple.daily.domain;

import com.maple.daily.model.CheckIn;
import com.maple.daily.model.PlanItem;

import static com.maple.daily.model.PlanConstants.MODE_DAILY_EVERY;
import static com.maple.daily.model.PlanConstants.MODE_MONTHLY_EVERY;
import static com.maple.daily.model.PlanConstants.MODE_YEARLY_EVERY;

public final class PlanRules {
    private PlanRules() {
    }

    public static boolean isActiveOnDate(PlanItem plan, String dateKey) {
        if (MODE_DAILY_EVERY.equals(plan.scheduleMode)
                || MODE_MONTHLY_EVERY.equals(plan.scheduleMode)
                || MODE_YEARLY_EVERY.equals(plan.scheduleMode)) {
            return true;
        }
        if (plan.startDate == null || plan.startDate.isEmpty() || plan.endDate == null || plan.endDate.isEmpty()) {
            return false;
        }
        return dateKey.compareTo(plan.startDate) >= 0 && dateKey.compareTo(plan.endDate) <= 0;
    }

    public static String inactiveText(PlanItem plan, String dateKey) {
        if (plan.startDate != null && !plan.startDate.isEmpty() && dateKey.compareTo(plan.startDate) < 0) {
            return "未开始";
        }
        if (plan.endDate != null && !plan.endDate.isEmpty() && dateKey.compareTo(plan.endDate) > 0) {
            return "已结束";
        }
        return "不在周期";
    }

    public static boolean hasCheckInForPeriod(PlanItem plan, String targetKey) {
        for (CheckIn checkIn : plan.checkIns) {
            if (targetKey.equals(checkIn.targetKey)) {
                return true;
            }
        }
        return false;
    }
}
