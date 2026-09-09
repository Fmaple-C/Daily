package com.maple.daily.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.maple.daily.model.CheckIn;
import com.maple.daily.model.DailyData;
import com.maple.daily.model.MemoNote;
import com.maple.daily.model.PetState;
import com.maple.daily.model.PlanItem;
import com.maple.daily.model.QuickNote;
import com.maple.daily.util.DateKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.maple.daily.model.PlanConstants.MODE_DAILY_EVERY;
import static com.maple.daily.model.PlanConstants.MODE_MONTHLY_EVERY;
import static com.maple.daily.model.PlanConstants.MODE_YEARLY_EVERY;
import static com.maple.daily.model.PlanConstants.TYPE_DAILY;
import static com.maple.daily.model.PlanConstants.TYPE_MONTHLY;
import static com.maple.daily.model.PlanConstants.TYPE_YEARLY;

public class PlanRepository {
    public static final int CURRENT_SCHEMA_VERSION = 6;

    private static final String PREFS_NAME = "daily_maple_prefs";
    private static final String DATA_KEY = "daily_data";
    private static final String LEGACY_TODOS_KEY = "todos";

    private final SharedPreferences preferences;

    public PlanRepository(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public DailyData loadData() {
        DailyData data = new DailyData();

        String versionedRaw = preferences.getString(DATA_KEY, null);
        if (versionedRaw != null && loadVersionedData(versionedRaw, data)) {
            return data;
        }

        String legacyRaw = preferences.getString(LEGACY_TODOS_KEY, null);
        if (legacyRaw != null && loadLegacyTodos(legacyRaw, data.plans)) {
            saveData(data.plans, data.quickNotes, data.memoNotes, data.pet);
            return data;
        }

        saveData(data.plans, data.quickNotes, data.memoNotes, data.pet);
        return data;
    }

    public List<PlanItem> loadPlans() {
        return loadData().plans;
    }

    public void savePlans(List<PlanItem> plans) {
        DailyData data = loadData();
        saveData(plans, data.quickNotes, data.memoNotes, data.pet);
    }

    public void saveData(List<PlanItem> plans, List<QuickNote> quickNotes) {
        DailyData data = loadData();
        saveData(plans, quickNotes, data.memoNotes, data.pet);
    }

    public void saveData(List<PlanItem> plans, List<QuickNote> quickNotes, List<MemoNote> memoNotes) {
        saveData(plans, quickNotes, memoNotes, loadData().pet);
    }

    public void saveData(
            List<PlanItem> plans,
            List<QuickNote> quickNotes,
            List<MemoNote> memoNotes,
            PetState pet
    ) {
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
            data.put("quickNotes", quickNotesToJson(quickNotes));
            data.put("memoNotes", memoNotesToJson(memoNotes));
            data.put("pet", petToJson(pet));
            preferences.edit()
                    .putString(DATA_KEY, data.toString())
                    .remove(LEGACY_TODOS_KEY)
                    .apply();
        } catch (JSONException ignored) {
        }
    }

    private boolean loadVersionedData(String raw, DailyData dailyData) {
        try {
            JSONObject data = new JSONObject(raw);
            int schemaVersion = data.optInt("schemaVersion", 0);
            if (schemaVersion >= 2) {
                JSONArray array = data.optJSONArray("plans");
                if (array == null) {
                    return false;
                }
                parsePlansArray(array, dailyData.plans);
                JSONArray quickNotes = data.optJSONArray("quickNotes");
                if (quickNotes != null) {
                    parseQuickNotesArray(quickNotes, dailyData.quickNotes);
                }
                JSONArray memoNotes = data.optJSONArray("memoNotes");
                if (memoNotes != null) {
                    parseMemoNotesArray(memoNotes, dailyData.memoNotes);
                }
                JSONObject pet = data.optJSONObject("pet");
                if (pet != null) {
                    dailyData.pet = parsePetState(pet);
                }
                if (schemaVersion < CURRENT_SCHEMA_VERSION) {
                    saveData(dailyData.plans, dailyData.quickNotes, dailyData.memoNotes, dailyData.pet);
                }
                return true;
            }

            JSONArray oldTodos = data.optJSONArray("todos");
            if (oldTodos != null) {
                parseLegacyTodosArray(oldTodos, dailyData.plans);
                saveData(dailyData.plans, dailyData.quickNotes, dailyData.memoNotes, dailyData.pet);
                return true;
            }
            return false;
        } catch (JSONException ignored) {
            dailyData.plans.clear();
            dailyData.quickNotes.clear();
            dailyData.memoNotes.clear();
            dailyData.pet = new PetState();
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

    private void parseQuickNotesArray(JSONArray array, List<QuickNote> quickNotes) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            QuickNote note = new QuickNote();
            note.id = object.optString("id", String.valueOf(System.currentTimeMillis() + i));
            note.content = object.optString("content", "");
            note.createdAt = object.optLong("createdAt", System.currentTimeMillis());
            note.createdAtText = object.optString("createdAtText", "");
            if (!note.content.trim().isEmpty()) {
                quickNotes.add(note);
            }
        }
    }

    private JSONArray quickNotesToJson(List<QuickNote> quickNotes) throws JSONException {
        JSONArray array = new JSONArray();
        for (QuickNote note : quickNotes) {
            JSONObject object = new JSONObject();
            object.put("id", note.id);
            object.put("content", note.content);
            object.put("createdAt", note.createdAt);
            object.put("createdAtText", note.createdAtText);
            array.put(object);
        }
        return array;
    }

    private void parseMemoNotesArray(JSONArray array, List<MemoNote> memoNotes) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            MemoNote note = new MemoNote();
            note.id = object.optString("id", String.valueOf(System.currentTimeMillis() + i));
            note.title = object.optString("title", "");
            note.content = object.optString("content", "");
            note.createdAt = object.optLong("createdAt", System.currentTimeMillis());
            note.updatedAt = object.optLong("updatedAt", note.createdAt);
            note.createdAtText = object.optString("createdAtText", "");
            note.updatedAtText = object.optString("updatedAtText", "");
            if (!note.title.trim().isEmpty() || !note.content.trim().isEmpty()) {
                memoNotes.add(note);
            }
        }
    }

    private JSONArray memoNotesToJson(List<MemoNote> memoNotes) throws JSONException {
        JSONArray array = new JSONArray();
        for (MemoNote note : memoNotes) {
            JSONObject object = new JSONObject();
            object.put("id", note.id);
            object.put("title", note.title);
            object.put("content", note.content);
            object.put("createdAt", note.createdAt);
            object.put("updatedAt", note.updatedAt);
            object.put("createdAtText", note.createdAtText);
            object.put("updatedAtText", note.updatedAtText);
            array.put(object);
        }
        return array;
    }

    private PetState parsePetState(JSONObject object) {
        PetState pet = new PetState();
        pet.level = Math.min(999999, Math.max(1, object.optInt("level", 1)));
        pet.growthDays = Math.min(999999, Math.max(0, object.optInt("growthDays", 0)));
        pet.lastInteractionDate = object.optString("lastInteractionDate", "");
        pet.lastInteractionAt = Math.max(0L, object.optLong("lastInteractionAt", 0L));
        return pet;
    }

    private JSONObject petToJson(PetState petState) throws JSONException {
        PetState pet = petState == null ? new PetState() : petState;
        JSONObject object = new JSONObject();
        object.put("level", Math.min(999999, Math.max(1, pet.level)));
        object.put("growthDays", Math.min(999999, Math.max(0, pet.growthDays)));
        object.put("lastInteractionDate", pet.lastInteractionDate == null ? "" : pet.lastInteractionDate);
        object.put("lastInteractionAt", Math.max(0L, pet.lastInteractionAt));
        return object;
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
