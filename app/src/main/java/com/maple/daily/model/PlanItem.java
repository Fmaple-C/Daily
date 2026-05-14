package com.maple.daily.model;

import java.util.ArrayList;
import java.util.List;

public class PlanItem {
    public String id = "";
    public String type = PlanConstants.TYPE_DAILY;
    public String title = "";
    public String note = "";
    public String scheduleMode = PlanConstants.MODE_DAILY_EVERY;
    public String startDate = "";
    public String endDate = "";
    public int deadlineMinutes = 12 * 60;
    public long createdAt = 0L;
    public List<CheckIn> checkIns = new ArrayList<>();
}
