package com.maple.daily.model;

import java.util.ArrayList;
import java.util.List;

public class DailyData {
    public final List<PlanItem> plans = new ArrayList<>();
    public final List<QuickNote> quickNotes = new ArrayList<>();
    public final List<MemoNote> memoNotes = new ArrayList<>();
}
