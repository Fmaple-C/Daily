package com.maple.daily.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.maple.daily.model.CheckIn;
import com.maple.daily.model.PlanItem;
import com.maple.daily.util.DateKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.maple.daily.model.PlanConstants.MODE_DAILY_EVERY;
import static com.maple.daily.model.PlanConstants.MODE_MONTHLY_EVERY;
import static com.maple.daily.model.PlanConstants.MODE_YEARLY_EVERY;
import static com.maple.daily.model.PlanConstants.TYPE_DAILY;
import static com.maple.daily.model.PlanConstants.TYPE_MONTHLY;
import static com.maple.daily.model.PlanConstants.TYPE_YEARLY;

public class PlanRepository {
    public static final int CURRENT_SCHEMA_VERSION = 3;

    private static final String PREFS_NAME = "daily_maple_prefs";
    private static final String DATA_KEY = "daily_data";
    private static final String LEGACY_TODOS_KEY = "todos";

    private final SharedPreferences preferences;

    public PlanRepository(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<PlanItem> loadPlans() {
        List<PlanItem> plans = new ArrayList<>();

        String versionedRaw = preferences.getString(DATA_KEY, null);
        if (versionedRaw != null && loadVersionedData(versionedRaw, plans)) {
            return plans;
        }

        String legacyRaw = preferences.getString(LEGACY_TODOS_KEY, null);
        if (legacyRaw != null && loadLegacyTodos(legacyRaw, plans)) {
            savePlans(plans);
            return plans;
        }

        savePlans(plans);
        return plans;
    }

    public void savePlans(List<PlanItem> plans) {
        JSONArray array = new JSONArray();
        for (PlanItem plan : plans) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", plan.id);
                object.put("type", plan.type);
                object.put("title", plan.title);
                object.put("note", plan.note);
                object.put("scheduleMode", plan.scheduleMode);
                object.put("startDate", plan.startDate);
                object.put("endDate", plan.endDate);
                object.put("deadlineMinutes", plan.deadlineMinutes);
                object.put("createdAt", plan.createdAt);
                object.put("checkIns", checkInsToJson(plan.checkIns));
                array.put(object);
            } catch (JSONException ignored) {
            }
        }

        JSONObject data = new JSONObject();
        try {
            data.put("schemaVersion", CURRENT_SCHEMA_VERSION);
            data.put("plans", array);
            preferences.edit()
                    .putString(DATA_KEY, data.toString())
                    .remove(LEGACY_TODOS_KEY)
                    .apply();
        } catch (JSONException ignored) {
        }
    }

    private boolean loadVersionedData(String raw, List<PlanItem> plans) {
        try {
            JSONObject data = new JSONObject(raw);
            int schemaVersion = data.optInt("schemaVersion", 0);
            if (schemaVersion >= 2) {
                JSONArray array = data.optJSONArray("plans");
                if (array == null) {
                    return false;
                }
                parsePlansArray(array, plans);
                if (schemaVersion < CURRENT_SCHEMA_VERSION) {
                    savePlans(plans);
                }
                return true;
            }

            JSONArray oldTodos = data.optJSONArray("todos");
            if (oldTodos != null) {
                parseLegacyTodosArray(oldTodos, plans);
                savePlans(plans);
                return true;
            }
            return false;
        } catch (JSONException ignored) {
            plans.clear();
            return false;
        }
    }

    private boolean loadLegacyTodos(String raw, List<PlanItem> plans) {
        try {
            parseLegacyTodosArray(new JSONArray(raw), plans);
            return true;
        } catch (JSONException ignored) {
            plans.clear();
            return false;
        }
    }

    private void parsePlansArray(JSONArray array, List<PlanItem> plans) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            PlanItem plan = new PlanItem();
            plan.id = object.optString("id", String.valueOf(System.currentTimeMillis() + i));
            plan.type = object.optString("type", TYPE_DAILY);
            plan.title = object.optString("title", "");
            plan.note = object.optString("note", "");
            plan.scheduleMode = object.optString("scheduleMode", defaultModeForType(plan.type));
            plan.startDate = object.optString("startDate", "");
            plan.endDate = object.optString("endDate", "");
            plan.deadlineMinutes = object.optInt("deadlineMinutes", 12 * 60);
            plan.createdAt = object.optLong("createdAt", System.currentTimeMillis());
            JSONArray checkIns = object.optJSONArray("checkIns");
            if (checkIns != null) {
                parseCheckInsArray(plan, checkIns);
            }
            if (!plan.title.trim().isEmpty()) {
                plans.add(plan);
            }
        }
    }

    private void parseLegacyTodosArray(JSONArray array, List<PlanItem> plans) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            PlanItem plan = new PlanItem();
            plan.id = object.optString("id", String.valueOf(System.currentTimeMillis() + i));
            plan.type = TYPE_DAILY;
            plan.title = object.optString("title", "");
            plan.note = "";
            plan.scheduleMode = MODE_DAILY_EVERY;
            plan.startDate = "";
            plan.endDate = "";
            plan.deadlineMinutes = object.optInt("deadlineMinutes", 12 * 60);
            plan.createdAt = object.optLong("createdAt", System.currentTimeMillis());
            String lastCompletedDate = object.optString("lastCompletedDate", "");
            if (!lastCompletedDate.isEmpty()) {
                CheckIn migrated = new CheckIn();
                migrated.id = "migrated-" + plan.id + "-" + lastCompletedDate;
                migrated.targetType = TYPE_DAILY;
                migrated.targetKey = lastCompletedDate;
                migrated.checkedAtMillis = DateKeys.millisAtStartOfDay(lastCompletedDate);
                migrated.checkedAt = lastCompletedDate + " 00:00";
                plan.checkIns.add(migrated);
            }
            if (!plan.title.trim().isEmpty()) {
                plans.add(plan);
            }
        }
    }

    private void parseCheckInsArray(PlanItem plan, JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            CheckIn checkIn = new CheckIn();
            checkIn.id = object.optString("id", plan.id + "-check-" + i);
            checkIn.targetType = object.optString("targetType", plan.type);
            checkIn.targetKey = object.optString("targetKey", "");
            checkIn.checkedAt = object.optString("checkedAt", "");
            checkIn.note = object.optString("note", "");
            checkIn.checkedAtMillis = object.optLong("checkedAtMillis", 0L);
            if (!checkIn.targetKey.isEmpty()) {
                plan.checkIns.add(checkIn);
            }
        }
    }

    private JSONArray checkInsToJson(List<CheckIn> checkIns) throws JSONException {
        JSONArray array = new JSONArray();
        for (CheckIn checkIn : checkIns) {
            JSONObject object = new JSONObject();
            object.put("id", checkIn.id);
            object.put("targetType", checkIn.targetType);
            object.put("targetKey", checkIn.targetKey);
            object.put("checkedAt", checkIn.checkedAt);
            object.put("note", checkIn.note);
            object.put("checkedAtMillis", checkIn.checkedAtMillis);
            array.put(object);
        }
        return array;
    }

    private String defaultModeForType(String type) {
        if (TYPE_MONTHLY.equals(type)) {
            return MODE_MONTHLY_EVERY;
        }
        if (TYPE_YEARLY.equals(type)) {
            return MODE_YEARLY_EVERY;
        }
        return MODE_DAILY_EVERY;
    }
}
