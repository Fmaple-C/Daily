package com.maple.daily;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.TimePicker;

import com.maple.daily.data.PlanRepository;
import com.maple.daily.data.ThemePreferences;
import com.maple.daily.domain.PlanRules;
import com.maple.daily.model.CheckIn;
import com.maple.daily.model.DailyData;
import com.maple.daily.model.MemoNote;
import com.maple.daily.model.PlanItem;
import com.maple.daily.model.QuickNote;
import com.maple.daily.util.DateKeys;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.maple.daily.model.PlanConstants.MODE_DAILY_EVERY;
import static com.maple.daily.model.PlanConstants.MODE_DAILY_TODAY;
import static com.maple.daily.model.PlanConstants.MODE_MONTHLY_EVERY;
import static com.maple.daily.model.PlanConstants.MODE_MONTHLY_THIS;
import static com.maple.daily.model.PlanConstants.MODE_RANGE;
import static com.maple.daily.model.PlanConstants.MODE_YEARLY_EVERY;
import static com.maple.daily.model.PlanConstants.MODE_YEARLY_THIS;
import static com.maple.daily.model.PlanConstants.TYPE_DAILY;
import static com.maple.daily.model.PlanConstants.TYPE_MONTHLY;
import static com.maple.daily.model.PlanConstants.TYPE_YEARLY;

@SuppressLint("SetTextI18n")
public class MainActivity extends Activity {
    private static final String MODULE_PLAN = "module_plan";
    private static final String MODULE_QUICK_NOTE = "module_quick_note";
    private static final String MODULE_MEMO = "module_memo";

    private int COLOR_BG;
    private int COLOR_SURFACE;
    private int COLOR_PRIMARY;
    private int COLOR_PRIMARY_DARK;
    private int COLOR_TEXT;
    private int COLOR_MUTED;
    private int COLOR_BORDER;
    private int COLOR_SUCCESS;
    private int COLOR_DANGER;
    private int COLOR_DISABLED;
    private int COLOR_CONTROL_BG;
    private int COLOR_FIELD_BG;
    private int COLOR_HINT;
    private int COLOR_DANGER_BG;
    private int COLOR_INACTIVE_BG;

    private final List<PlanItem> plans = new ArrayList<>();
    private final List<QuickNote> quickNotes = new ArrayList<>();
    private final List<MemoNote> memoNotes = new ArrayList<>();
    private final Handler midnightHandler = new Handler(Looper.getMainLooper());
    private final Runnable midnightRefresh = new Runnable() {
        @Override
        public void run() {
            renderHeaderAndForm();
            renderNavigation();
            renderPlans();
            scheduleMidnightRefresh();
        }
    };

    private PlanRepository repository;
    private ThemePreferences themePreferences;
    private LinearLayout railContainer;
    private LinearLayout bottomNavContainer;
    private LinearLayout formContainer;
    private LinearLayout listContainer;
    private LinearLayout overviewContainer;
    private LinearLayout progressBarContainer;
    private TextView screenTitle;
    private TextView screenSubtitle;
    private TextView summaryText;
    private TextView summaryCountText;
    private View bottomNavDivider;
    private Button backButton;
    private Button createButton;
    private Button themeButton;
    private EditText titleInput;
    private EditText noteInput;
    private Button deadlineButton;
    private Button startDateButton;
    private Button endDateButton;
    private Button everyModeButton;
    private Button currentModeButton;
    private Button rangeModeButton;

    private String activeModule = MODULE_PLAN;
    private String activePlanType = TYPE_DAILY;
    private String selectedMode = MODE_DAILY_EVERY;
    private int selectedDeadlineMinutes = 12 * 60;
    private String selectedStartDate = "";
    private String selectedEndDate = "";
    private boolean isCreatePanelVisible = false;
    private boolean isNightTheme = false;
    private PlanItem historyPlan = null;
    private PlanItem editingPlan = null;
    private MemoNote editingMemo = null;
    private String formTitleDraft = "";
    private String formNoteDraft = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new PlanRepository(this);
        themePreferences = new ThemePreferences(this);
        isNightTheme = themePreferences.isNightTheme();
        applyThemeColors();
        loadPlans();
        buildUi();
        scheduleMidnightRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderHeaderAndForm();
        renderNavigation();
        renderPlans();
        scheduleMidnightRefresh();
    }

    @Override
    protected void onDestroy() {
        midnightHandler.removeCallbacks(midnightRefresh);
        super.onDestroy();
    }

    private void applyThemeColors() {
        if (isNightTheme) {
            COLOR_BG = Color.rgb(24, 24, 23);
            COLOR_SURFACE = Color.rgb(34, 34, 33);
            COLOR_PRIMARY = Color.rgb(238, 124, 65);
            COLOR_PRIMARY_DARK = Color.rgb(255, 168, 112);
            COLOR_TEXT = Color.rgb(245, 245, 241);
            COLOR_MUTED = Color.rgb(177, 174, 168);
            COLOR_BORDER = Color.rgb(63, 63, 60);
            COLOR_SUCCESS = Color.rgb(134, 169, 129);
            COLOR_DANGER = Color.rgb(233, 129, 109);
            COLOR_DISABLED = Color.rgb(121, 120, 116);
            COLOR_CONTROL_BG = Color.rgb(44, 44, 42);
            COLOR_FIELD_BG = Color.rgb(38, 38, 36);
            COLOR_HINT = Color.rgb(135, 133, 128);
            COLOR_DANGER_BG = Color.rgb(65, 38, 34);
            COLOR_INACTIVE_BG = Color.rgb(42, 42, 40);
        } else {
            COLOR_BG = Color.rgb(251, 251, 249);
            COLOR_SURFACE = Color.WHITE;
            COLOR_PRIMARY = Color.rgb(226, 107, 45);
            COLOR_PRIMARY_DARK = Color.rgb(168, 63, 18);
            COLOR_TEXT = Color.rgb(41, 39, 36);
            COLOR_MUTED = Color.rgb(124, 119, 112);
            COLOR_BORDER = Color.rgb(232, 230, 225);
            COLOR_SUCCESS = Color.rgb(98, 128, 94);
            COLOR_DANGER = Color.rgb(188, 84, 62);
            COLOR_DISABLED = Color.rgb(170, 164, 156);
            COLOR_CONTROL_BG = Color.rgb(244, 243, 240);
            COLOR_FIELD_BG = Color.rgb(248, 248, 246);
            COLOR_HINT = Color.rgb(170, 164, 156);
            COLOR_DANGER_BG = Color.rgb(250, 236, 232);
            COLOR_INACTIVE_BG = Color.rgb(243, 242, 239);
        }
    }

    private void applySystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(COLOR_BG);
        window.setNavigationBarColor(COLOR_BG);
        int flags = isNightTheme ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        window.getDecorView().setSystemUiVisibility(flags);
    }

    private void buildUi() {
        applySystemBars();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(COLOR_BG);
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(18), dp(20), 0);
        root.addView(header);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(titleRow);

        backButton = new Button(this);
        backButton.setAllCaps(false);
        backButton.setText("‹");
        backButton.setTextSize(28);
        backButton.setTypeface(Typeface.DEFAULT_BOLD);
        backButton.setTextColor(COLOR_PRIMARY_DARK);
        backButton.setBackgroundColor(Color.TRANSPARENT);
        compactIconButton(backButton);
        backButton.setVisibility(View.GONE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyPlan = null;
                renderHeaderAndForm();
                renderNavigation();
                renderPlans();
            }
        });
        titleRow.addView(backButton, new LinearLayout.LayoutParams(dp(34), dp(42)));

        screenTitle = new TextView(this);
        screenTitle.setTextColor(COLOR_TEXT);
        screenTitle.setTextSize(29);
        screenTitle.setTypeface(Typeface.DEFAULT_BOLD);
        titleRow.addView(screenTitle, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        addHorizontalSpace(titleRow, 8);

        themeButton = new Button(this);
        themeButton.setAllCaps(false);
        themeButton.setTextSize(20);
        compactIconButton(themeButton);
        themeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureFormDraft();
                isNightTheme = !isNightTheme;
                themePreferences.setNightTheme(isNightTheme);
                applyThemeColors();
                buildUi();
            }
        });
        titleRow.addView(themeButton, new LinearLayout.LayoutParams(dp(42), dp(42)));

        addHorizontalSpace(titleRow, 8);

        createButton = new Button(this);
        createButton.setAllCaps(false);
        createButton.setTextSize(24);
        createButton.setTypeface(Typeface.DEFAULT_BOLD);
        compactIconButton(createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCreatePanelVisible) {
                    editingPlan = null;
                    editingMemo = null;
                    resetCreateForm();
                    isCreatePanelVisible = false;
                } else {
                    editingPlan = null;
                    editingMemo = null;
                    resetCreateForm();
                    isCreatePanelVisible = true;
                }
                renderNavigation();
                renderHeaderAndForm();
                renderPlans();
            }
        });
        titleRow.addView(createButton, new LinearLayout.LayoutParams(dp(42), dp(42)));

        screenSubtitle = new TextView(this);
        screenSubtitle.setTextColor(COLOR_MUTED);
        screenSubtitle.setTextSize(13);
        screenSubtitle.setPadding(0, dp(5), 0, 0);
        header.addView(screenSubtitle);

        railContainer = new LinearLayout(this);
        railContainer.setOrientation(LinearLayout.VERTICAL);
        railContainer.setPadding(dp(20), dp(18), dp(20), 0);
        root.addView(railContainer, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        formContainer = new LinearLayout(this);
        formContainer.setOrientation(LinearLayout.VERTICAL);
        formContainer.setPadding(dp(20), dp(16), dp(20), 0);
        root.addView(formContainer);

        overviewContainer = new LinearLayout(this);
        overviewContainer.setOrientation(LinearLayout.VERTICAL);
        overviewContainer.setPadding(dp(20), dp(22), dp(20), 0);
        root.addView(overviewContainer);

        LinearLayout summaryRow = new LinearLayout(this);
        summaryRow.setOrientation(LinearLayout.HORIZONTAL);
        summaryRow.setGravity(Gravity.BOTTOM);
        overviewContainer.addView(summaryRow);

        summaryText = new TextView(this);
        summaryText.setTextColor(COLOR_TEXT);
        summaryText.setTextSize(20);
        summaryText.setTypeface(Typeface.DEFAULT_BOLD);
        summaryRow.addView(summaryText, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        summaryCountText = new TextView(this);
        summaryCountText.setTextColor(COLOR_MUTED);
        summaryCountText.setTextSize(12);
        summaryRow.addView(summaryCountText);

        progressBarContainer = new LinearLayout(this);
        progressBarContainer.setOrientation(LinearLayout.HORIZONTAL);
        progressBarContainer.setBackgroundColor(COLOR_BORDER);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(3)
        );
        progressParams.setMargins(0, dp(10), 0, 0);
        overviewContainer.addView(progressBarContainer, progressParams);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(dp(20), dp(4), dp(20), dp(20));
        scrollView.addView(listContainer);

        bottomNavDivider = new View(this);
        bottomNavDivider.setBackgroundColor(COLOR_BORDER);
        root.addView(bottomNavDivider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
        ));

        bottomNavContainer = new LinearLayout(this);
        bottomNavContainer.setOrientation(LinearLayout.HORIZONTAL);
        bottomNavContainer.setGravity(Gravity.CENTER_VERTICAL);
        bottomNavContainer.setPadding(dp(20), dp(5), dp(20), dp(5));
        bottomNavContainer.setBackgroundColor(COLOR_SURFACE);
        LinearLayout.LayoutParams bottomNavParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(68)
        );
        root.addView(bottomNavContainer, bottomNavParams);

        renderNavigation();
        renderHeaderAndForm();
        updateHeaderButtons();
        renderPlans();
    }

    private void renderNavigation() {
        if (railContainer == null) {
            return;
        }
        railContainer.removeAllViews();
        if (bottomNavContainer != null) {
            bottomNavContainer.removeAllViews();
        }

        boolean showNavigation = historyPlan == null;
        boolean showPlanNavigation = showNavigation && MODULE_PLAN.equals(activeModule);
        railContainer.setVisibility(showPlanNavigation ? View.VISIBLE : View.GONE);
        if (bottomNavContainer != null) {
            bottomNavContainer.setVisibility(showNavigation ? View.VISIBLE : View.GONE);
        }
        if (bottomNavDivider != null) {
            bottomNavDivider.setVisibility(showNavigation ? View.VISIBLE : View.GONE);
        }
        if (!showNavigation) {
            return;
        }

        if (showPlanNavigation) {
            if (TYPE_DAILY.equals(activePlanType)) {
                railContainer.addView(buildWeekStrip());
                addVerticalSpace(railContainer, 20);
            }
            railContainer.addView(buildPlanTypeTabs());
        }
        renderBottomNavigation();
    }

    private View buildWeekStrip() {
        LinearLayout week = new LinearLayout(this);
        week.setOrientation(LinearLayout.HORIZONTAL);
        week.setGravity(Gravity.CENTER_VERTICAL);

        Calendar today = Calendar.getInstance();
        Calendar firstDay = (Calendar) today.clone();
        int dayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - Calendar.MONDAY;
        firstDay.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
        String[] labels = {"一", "二", "三", "四", "五", "六", "日"};

        for (int index = 0; index < 7; index++) {
            Calendar dayValue = (Calendar) firstDay.clone();
            dayValue.add(Calendar.DAY_OF_YEAR, index);
            boolean selected = dayValue.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && dayValue.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);

            LinearLayout day = new LinearLayout(this);
            day.setOrientation(LinearLayout.VERTICAL);
            day.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView label = new TextView(this);
            label.setText(labels[index]);
            label.setTextColor(COLOR_MUTED);
            label.setTextSize(11);
            label.setGravity(Gravity.CENTER);
            day.addView(label, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(20)
            ));

            TextView number = new TextView(this);
            number.setText(String.valueOf(dayValue.get(Calendar.DAY_OF_MONTH)));
            number.setTextColor(selected ? Color.WHITE : COLOR_TEXT);
            number.setTextSize(14);
            number.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            number.setGravity(Gravity.CENTER);
            number.setBackground(selected
                    ? circle(COLOR_PRIMARY, COLOR_PRIMARY)
                    : circle(Color.TRANSPARENT, Color.TRANSPARENT));
            day.addView(number, new LinearLayout.LayoutParams(dp(30), dp(30)));

            week.addView(day, new LinearLayout.LayoutParams(
                    0,
                    dp(52),
                    1f
            ));
        }
        return week;
    }

    private View buildPlanTypeTabs() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        tabs.addView(planTypeTab("日计划", TYPE_DAILY), new LinearLayout.LayoutParams(0, dp(38), 1f));
        tabs.addView(planTypeTab("月计划", TYPE_MONTHLY), new LinearLayout.LayoutParams(0, dp(38), 1f));
        tabs.addView(planTypeTab("年计划", TYPE_YEARLY), new LinearLayout.LayoutParams(0, dp(38), 1f));
        return tabs;
    }

    private View planTypeTab(String title, final String type) {
        boolean selected = type.equals(activePlanType);
        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER_HORIZONTAL);
        tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPlanType(type);
            }
        });

        TextView label = new TextView(this);
        label.setText(title);
        label.setTextColor(selected ? COLOR_TEXT : COLOR_MUTED);
        label.setTextSize(14);
        label.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        label.setGravity(Gravity.CENTER);
        tab.addView(label, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        View indicator = new View(this);
        indicator.setBackgroundColor(selected ? COLOR_PRIMARY : Color.TRANSPARENT);
        tab.addView(indicator, new LinearLayout.LayoutParams(dp(44), dp(2)));
        return tab;
    }

    private void renderBottomNavigation() {
        if (bottomNavContainer == null || historyPlan != null) {
            return;
        }
        bottomNavContainer.addView(bottomNavButton("计划", "✓", MODULE_PLAN), new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));
        bottomNavContainer.addView(bottomNavButton("一言", "“", MODULE_QUICK_NOTE), new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));
        bottomNavContainer.addView(bottomNavButton("随记", "✎", MODULE_MEMO), new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));
    }

    private View bottomNavButton(String title, String subTitle, final String module) {
        boolean selected = activeModule.equals(module);
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setBackgroundColor(Color.TRANSPARENT);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectModule(module);
            }
        });

        TextView icon = new TextView(this);
        icon.setText(subTitle);
        icon.setTextColor(selected ? COLOR_PRIMARY : COLOR_HINT);
        icon.setTextSize(20);
        icon.setGravity(Gravity.CENTER);
        item.addView(icon);

        TextView label = new TextView(this);
        label.setText(title);
        label.setTextColor(selected ? COLOR_PRIMARY : COLOR_HINT);
        label.setTextSize(10);
        label.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        label.setPadding(0, dp(3), 0, 0);
        item.addView(label);
        return item;
    }

    private void selectModule(String module) {
        if (activeModule.equals(module)) {
            return;
        }
        activeModule = module;
        editingPlan = null;
        editingMemo = null;
        resetCreateForm();
        isCreatePanelVisible = false;
        renderNavigation();
        renderHeaderAndForm();
        renderPlans();
        animateListEntrance();
    }

    private void selectPlanType(String type) {
        if (activePlanType.equals(type)) {
            return;
        }
        activePlanType = type;
        editingPlan = null;
        editingMemo = null;
        resetCreateForm();
        isCreatePanelVisible = false;
        renderNavigation();
        renderHeaderAndForm();
        renderPlans();
        animateListEntrance();
    }

    private void animateListEntrance() {
        if (listContainer == null) {
            return;
        }
        listContainer.setAlpha(0f);
        listContainer.setTranslationY(dp(10));
        listContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void renderHeaderAndForm() {
        if (historyPlan != null) {
            screenTitle.setText("打卡历史");
            screenSubtitle.setText(historyPlan.title + " · 共 " + historyPlan.checkIns.size() + " 条记录");
            if (overviewContainer != null) {
                overviewContainer.setVisibility(View.GONE);
            }
            formContainer.removeAllViews();
            updateHeaderButtons();
            return;
        }

        if (MODULE_QUICK_NOTE.equals(activeModule)) {
            screenTitle.setText("一言");
            screenSubtitle.setText("把此刻轻轻放在这里。");
        } else if (MODULE_MEMO.equals(activeModule)) {
            screenTitle.setText("随记");
            screenSubtitle.setText("写下值得回看的内容。");
        } else {
            screenTitle.setText(todayHeaderTitle());
            if (TYPE_MONTHLY.equals(activePlanType)) {
                screenSubtitle.setText(todayWeekLabel() + " · 给这个月一个清晰方向");
            } else if (TYPE_YEARLY.equals(activePlanType)) {
                screenSubtitle.setText(todayWeekLabel() + " · 把长期目标放在眼前");
            } else {
                screenSubtitle.setText(todayWeekLabel() + " · 把今天过得具体一点");
            }
        }

        if (editingPlan != null) {
            screenSubtitle.setText("正在编辑：" + editingPlan.title);
        } else if (editingMemo != null) {
            screenSubtitle.setText("正在编辑：" + editingMemoTitle(editingMemo));
        }
        updateHeaderButtons();
        buildCreatePanel();
    }

    private void updateHeaderButtons() {
        updateCreateButton();
        updateThemeButton();
        if (backButton != null) {
            backButton.setVisibility(historyPlan == null ? View.GONE : View.VISIBLE);
        }
        if (createButton != null) {
            createButton.setVisibility(historyPlan == null ? View.VISIBLE : View.GONE);
        }
        if (themeButton != null) {
            themeButton.setVisibility(historyPlan == null ? View.VISIBLE : View.GONE);
        }
    }

    private void updateCreateButton() {
        if (createButton == null) {
            return;
        }
        boolean opened = isCreatePanelVisible || editingPlan != null || editingMemo != null;
        createButton.setText(opened ? "×" : "+");
        createButton.setTextColor(opened ? COLOR_PRIMARY_DARK : Color.WHITE);
        createButton.setBackground(circle(
                opened ? COLOR_CONTROL_BG : COLOR_PRIMARY,
                opened ? COLOR_BORDER : COLOR_PRIMARY
        ));
    }

    private void updateThemeButton() {
        if (themeButton == null) {
            return;
        }
        themeButton.setText(isNightTheme ? "☀" : "☾");
        themeButton.setTextColor(COLOR_MUTED);
        themeButton.setBackground(circle(COLOR_SURFACE, COLOR_BORDER));
    }

    private String todayHeaderTitle() {
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日";
    }

    private String todayWeekLabel() {
        String[] labels = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        return labels[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
    }

    private String activePlanName() {
        if (TYPE_DAILY.equals(activePlanType)) {
            return "日计划";
        }
        if (TYPE_MONTHLY.equals(activePlanType)) {
            return "月计划";
        }
        if (TYPE_YEARLY.equals(activePlanType)) {
            return "年计划";
        }
        return "计划";
    }

    private String titleHint() {
        if (MODULE_MEMO.equals(activeModule)) {
            return "例如：今天的复盘";
        }
        if (TYPE_DAILY.equals(activePlanType)) {
            return "例如：每日记账";
        }
        if (TYPE_MONTHLY.equals(activePlanType)) {
            return "例如：每月规划";
        }
        if (TYPE_YEARLY.equals(activePlanType)) {
            return "例如：年度成长计划";
        }
        return "例如：今天想到的一句话";
    }

    private String noteHint() {
        if (MODULE_QUICK_NOTE.equals(activeModule)) {
            return "写下一句话、灵感、今天发生的小事...";
        }
        if (MODULE_MEMO.equals(activeModule)) {
            return "# 标题\n\n- 事项一\n- 事项二\n\n> 想法引用\n\n`关键字` 或 **重点**";
        }
        if (TYPE_DAILY.equals(activePlanType)) {
            return "备注，例如：\n1. 上班交通地铁-12元\n2. 早餐-4元\n3. 日支出=...";
        }
        if (TYPE_MONTHLY.equals(activePlanType)) {
            return "备注，例如：\n1. internal本\n   1. 运用 ai 学习 java\n2. 理财本";
        }
        if (TYPE_YEARLY.equals(activePlanType)) {
            return "备注，例如：\n1. 技术主线\n   1. Java 深入\n   2. 项目上线\n2. 健康主线\n   1. 稳定锻炼";
        }
        return "写下备注...";
    }

    private void buildCreatePanel() {
        formContainer.removeAllViews();
        if (!isCreatePanelVisible) {
            return;
        }

        if (MODULE_QUICK_NOTE.equals(activeModule)) {
            buildQuickNotePanel();
            return;
        }
        if (MODULE_MEMO.equals(activeModule)) {
            buildMemoPanel();
            return;
        }

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(12), dp(14), dp(14));
        panel.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 8));
        formContainer.addView(panel);

        TextView panelTitle = new TextView(this);
        panelTitle.setText(editingPlan == null ? "新建" + activePlanName() : "编辑" + activePlanName());
        panelTitle.setTextColor(COLOR_TEXT);
        panelTitle.setTypeface(Typeface.DEFAULT_BOLD);
        panelTitle.setTextSize(17);
        panel.addView(panelTitle);

        titleInput = new EditText(this);
        titleInput.setHint(titleHint());
        titleInput.setSingleLine(true);
        titleInput.setTextColor(COLOR_TEXT);
        titleInput.setHintTextColor(COLOR_HINT);
        titleInput.setTextSize(16);
        titleInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        titleInput.setBackgroundColor(Color.TRANSPARENT);
        titleInput.setPadding(0, dp(10), 0, dp(8));
        titleInput.setText(formTitleDraft);
        panel.addView(titleInput);

        noteInput = new EditText(this);
        noteInput.setHint(noteHint());
        noteInput.setMinLines(4);
        noteInput.setGravity(Gravity.TOP);
        noteInput.setTextColor(COLOR_TEXT);
        noteInput.setHintTextColor(COLOR_HINT);
        noteInput.setTextSize(15);
        noteInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        noteInput.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));
        noteInput.setPadding(dp(10), dp(8), dp(10), dp(8));
        noteInput.setText(formNoteDraft);
        panel.addView(noteInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(112)
        ));

        addVerticalSpace(panel, 10);
        panel.addView(buildModeRow());

        if (MODE_RANGE.equals(selectedMode)) {
            addVerticalSpace(panel, 8);
            panel.addView(buildDateRangeRow());
        }

        if (TYPE_DAILY.equals(activePlanType)) {
            addVerticalSpace(panel, 8);
            deadlineButton = secondaryButton("截止 " + formatMinutes(selectedDeadlineMinutes));
            deadlineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    captureFormDraft();
                    pickTime(selectedDeadlineMinutes, new TimeSelectedCallback() {
                        @Override
                        public void onTimeSelected(int minutes) {
                            selectedDeadlineMinutes = minutes;
                            deadlineButton.setText("截止 " + formatMinutes(minutes));
                        }
                    });
                }
            });
            panel.addView(deadlineButton, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(44)
            ));
        }

        addVerticalSpace(panel, 10);
        Button addButton = new Button(this);
        addButton.setAllCaps(false);
        addButton.setText(editingPlan == null ? "添加" + activePlanName() : "保存修改");
        addButton.setTextColor(Color.WHITE);
        addButton.setTextSize(15);
        addButton.setTypeface(Typeface.DEFAULT_BOLD);
        addButton.setBackground(rounded(COLOR_PRIMARY, COLOR_PRIMARY, 8));
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPlanForm();
            }
        });
        panel.addView(addButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
        ));
    }

    private void buildQuickNotePanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(12), dp(14), dp(14));
        panel.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 8));
        formContainer.addView(panel);

        TextView panelTitle = new TextView(this);
        panelTitle.setText("写一言");
        panelTitle.setTextColor(COLOR_TEXT);
        panelTitle.setTypeface(Typeface.DEFAULT_BOLD);
        panelTitle.setTextSize(17);
        panel.addView(panelTitle);

        noteInput = new EditText(this);
        noteInput.setHint(noteHint());
        noteInput.setMinLines(4);
        noteInput.setGravity(Gravity.TOP);
        noteInput.setTextColor(COLOR_TEXT);
        noteInput.setHintTextColor(COLOR_HINT);
        noteInput.setTextSize(15);
        noteInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        noteInput.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));
        noteInput.setPadding(dp(10), dp(8), dp(10), dp(8));
        noteInput.setText(formNoteDraft);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(120)
        );
        inputParams.setMargins(0, dp(10), 0, 0);
        panel.addView(noteInput, inputParams);

        addVerticalSpace(panel, 10);
        Button saveButton = new Button(this);
        saveButton.setAllCaps(false);
        saveButton.setText("保存一言");
        saveButton.setTextColor(Color.WHITE);
        saveButton.setTextSize(15);
        saveButton.setTypeface(Typeface.DEFAULT_BOLD);
        saveButton.setBackground(rounded(COLOR_PRIMARY, COLOR_PRIMARY, 8));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addQuickNote();
            }
        });
        panel.addView(saveButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
        ));
    }

    private void buildMemoPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(12), dp(14), dp(14));
        panel.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 8));
        formContainer.addView(panel);

        TextView panelTitle = new TextView(this);
        panelTitle.setText(editingMemo == null ? "新建随记" : "编辑随记");
        panelTitle.setTextColor(COLOR_TEXT);
        panelTitle.setTypeface(Typeface.DEFAULT_BOLD);
        panelTitle.setTextSize(17);
        panel.addView(panelTitle);

        titleInput = new EditText(this);
        titleInput.setHint(titleHint());
        titleInput.setSingleLine(true);
        titleInput.setTextColor(COLOR_TEXT);
        titleInput.setHintTextColor(COLOR_HINT);
        titleInput.setTextSize(16);
        titleInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        titleInput.setBackgroundColor(Color.TRANSPARENT);
        titleInput.setPadding(0, dp(10), 0, dp(8));
        titleInput.setText(formTitleDraft);
        panel.addView(titleInput);

        noteInput = new EditText(this);
        noteInput.setHint(noteHint());
        noteInput.setMinLines(7);
        noteInput.setGravity(Gravity.TOP);
        noteInput.setTextColor(COLOR_TEXT);
        noteInput.setHintTextColor(COLOR_HINT);
        noteInput.setTextSize(15);
        noteInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        noteInput.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));
        noteInput.setPadding(dp(10), dp(8), dp(10), dp(8));
        noteInput.setText(formNoteDraft);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(180)
        );
        inputParams.setMargins(0, dp(6), 0, 0);
        panel.addView(noteInput, inputParams);

        addVerticalSpace(panel, 10);
        Button saveButton = new Button(this);
        saveButton.setAllCaps(false);
        saveButton.setText(editingMemo == null ? "保存随记" : "保存修改");
        saveButton.setTextColor(Color.WHITE);
        saveButton.setTextSize(15);
        saveButton.setTypeface(Typeface.DEFAULT_BOLD);
        saveButton.setBackground(rounded(COLOR_PRIMARY, COLOR_PRIMARY, 8));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitMemoForm();
            }
        });
        panel.addView(saveButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
        ));
    }

    private LinearLayout buildModeRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        everyModeButton = modeButton(everyModeLabel(), everyModeValue());
        currentModeButton = modeButton(currentModeLabel(), currentModeValue());
        rangeModeButton = modeButton("日期范围", MODE_RANGE);

        row.addView(everyModeButton, new LinearLayout.LayoutParams(0, dp(42), 1f));
        addHorizontalSpace(row, 6);
        row.addView(currentModeButton, new LinearLayout.LayoutParams(0, dp(42), 1f));
        addHorizontalSpace(row, 6);
        row.addView(rangeModeButton, new LinearLayout.LayoutParams(0, dp(42), 1f));
        return row;
    }

    private String everyModeLabel() {
        if (TYPE_DAILY.equals(activePlanType)) {
            return "每日";
        }
        if (TYPE_MONTHLY.equals(activePlanType)) {
            return "每月";
        }
        return "每年";
    }

    private String everyModeValue() {
        if (TYPE_DAILY.equals(activePlanType)) {
            return MODE_DAILY_EVERY;
        }
        if (TYPE_MONTHLY.equals(activePlanType)) {
            return MODE_MONTHLY_EVERY;
        }
        if (TYPE_YEARLY.equals(activePlanType)) {
            return MODE_YEARLY_EVERY;
        }
        return MODE_DAILY_EVERY;
    }

    private String currentModeLabel() {
        if (TYPE_DAILY.equals(activePlanType)) {
            return "当日";
        }
        if (TYPE_MONTHLY.equals(activePlanType)) {
            return "当月";
        }
        return "当年";
    }

    private String currentModeValue() {
        if (TYPE_DAILY.equals(activePlanType)) {
            return MODE_DAILY_TODAY;
        }
        if (TYPE_MONTHLY.equals(activePlanType)) {
            return MODE_MONTHLY_THIS;
        }
        if (TYPE_YEARLY.equals(activePlanType)) {
            return MODE_YEARLY_THIS;
        }
        return MODE_DAILY_TODAY;
    }

    private Button modeButton(String text, final String mode) {
        boolean selected = selectedMode.equals(mode);
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextSize(13);
        button.setTextColor(selected ? Color.WHITE : COLOR_PRIMARY_DARK);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setBackground(rounded(
                selected ? COLOR_PRIMARY : COLOR_CONTROL_BG,
                selected ? COLOR_PRIMARY : COLOR_BORDER,
                8
        ));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureFormDraft();
                selectedMode = mode;
                if (MODE_RANGE.equals(mode) && selectedStartDate.isEmpty()) {
                    selectedStartDate = todayKey();
                    selectedEndDate = todayKey();
                }
                buildCreatePanel();
            }
        });
        return button;
    }

    private LinearLayout buildDateRangeRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        startDateButton = secondaryButton("开始 " + selectedStartDate);
        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureFormDraft();
                pickDate(selectedStartDate, new DateSelectedCallback() {
                    @Override
                    public void onDateSelected(String dateKey) {
                        selectedStartDate = dateKey;
                        if (!selectedEndDate.isEmpty() && selectedEndDate.compareTo(selectedStartDate) < 0) {
                            selectedEndDate = selectedStartDate;
                        }
                        buildCreatePanel();
                    }
                });
            }
        });
        row.addView(startDateButton, new LinearLayout.LayoutParams(0, dp(42), 1f));

        addHorizontalSpace(row, 8);

        endDateButton = secondaryButton("结束 " + selectedEndDate);
        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureFormDraft();
                pickDate(selectedEndDate, new DateSelectedCallback() {
                    @Override
                    public void onDateSelected(String dateKey) {
                        selectedEndDate = dateKey;
                        if (!selectedStartDate.isEmpty() && selectedEndDate.compareTo(selectedStartDate) < 0) {
                            selectedStartDate = selectedEndDate;
                        }
                        buildCreatePanel();
                    }
                });
            }
        });
        row.addView(endDateButton, new LinearLayout.LayoutParams(0, dp(42), 1f));
        return row;
    }

    private void renderPlans() {
        if (listContainer == null) {
            return;
        }

        if (historyPlan != null) {
            renderHistoryPage();
            return;
        }

        if (MODULE_QUICK_NOTE.equals(activeModule)) {
            renderQuickNotes();
            return;
        }
        if (MODULE_MEMO.equals(activeModule)) {
            renderMemoNotes();
            return;
        }

        listContainer.removeAllViews();
        List<PlanItem> visible = visiblePlans();
        String today = todayKey();
        int active = 0;
        int completed = 0;

        for (PlanItem plan : visible) {
            if (isActiveToday(plan, today)) {
                active++;
                if (hasCheckInForCurrentPeriod(plan)) {
                    completed++;
                }
            }
        }

        updatePlanOverview(active, completed);

        if (visible.isEmpty()) {
            listContainer.addView(buildEmptyState());
            return;
        }

        for (PlanItem plan : visible) {
            listContainer.addView(buildPlanCard(plan));
        }
    }

    private void updatePlanOverview(int active, int completed) {
        if (overviewContainer == null) {
            return;
        }
        overviewContainer.setVisibility(View.VISIBLE);
        progressBarContainer.setVisibility(View.VISIBLE);
        summaryText.setText(TYPE_DAILY.equals(activePlanType)
                ? "今天"
                : (TYPE_MONTHLY.equals(activePlanType) ? "本月" : "今年"));
        summaryCountText.setText(completed + " / " + active + " 已完成");

        progressBarContainer.removeAllViews();
        if (active <= 0) {
            View empty = new View(this);
            empty.setBackgroundColor(COLOR_BORDER);
            progressBarContainer.addView(empty, new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
            ));
            return;
        }

        if (completed > 0) {
            View fill = new View(this);
            fill.setBackgroundColor(COLOR_PRIMARY);
            progressBarContainer.addView(fill, new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    completed
            ));
        }
        if (completed < active) {
            View rest = new View(this);
            rest.setBackgroundColor(COLOR_BORDER);
            progressBarContainer.addView(rest, new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    active - completed
            ));
        }
    }

    private void updateCollectionOverview(String title, int count, String unit) {
        if (overviewContainer == null) {
            return;
        }
        overviewContainer.setVisibility(View.VISIBLE);
        summaryText.setText(title);
        summaryCountText.setText("共 " + count + " " + unit);
        progressBarContainer.setVisibility(View.GONE);
    }

    private List<PlanItem> visiblePlans() {
        List<PlanItem> visible = new ArrayList<>();
        for (PlanItem plan : plans) {
            if (activePlanType.equals(plan.type)) {
                visible.add(plan);
            }
        }
        return visible;
    }

    private View buildEmptyState() {
        LinearLayout empty = new LinearLayout(this);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(Gravity.CENTER_HORIZONTAL);
        empty.setPadding(dp(18), dp(32), dp(18), dp(32));
        empty.setBackgroundColor(Color.TRANSPARENT);

        TextView mark = new TextView(this);
        mark.setText(emptyMarkText());
        mark.setTextSize(28);
        mark.setTypeface(Typeface.DEFAULT_BOLD);
        mark.setTextColor(COLOR_PRIMARY);
        mark.setGravity(Gravity.CENTER);
        empty.addView(mark);

        TextView text = new TextView(this);
        text.setText(emptyStateText());
        text.setTextColor(COLOR_MUTED);
        text.setTextSize(15);
        text.setPadding(0, dp(8), 0, 0);
        empty.addView(text);
        return empty;
    }

    private String emptyMarkText() {
        if (MODULE_QUICK_NOTE.equals(activeModule)) {
            return "言";
        }
        if (MODULE_MEMO.equals(activeModule)) {
            return "记";
        }
        if (TYPE_DAILY.equals(activePlanType)) {
            return "日";
        }
        if (TYPE_MONTHLY.equals(activePlanType)) {
            return "月";
        }
        if (TYPE_YEARLY.equals(activePlanType)) {
            return "年";
        }
        return "计";
    }

    private String emptyStateText() {
        if (MODULE_QUICK_NOTE.equals(activeModule)) {
            return "先记录一句一言";
        }
        if (MODULE_MEMO.equals(activeModule)) {
            return "先写一篇随记";
        }
        return "先添加一个" + activePlanName();
    }

    private void renderQuickNotes() {
        listContainer.removeAllViews();
        updateCollectionOverview("全部一言", quickNotes.size(), "条");

        if (quickNotes.isEmpty()) {
            listContainer.addView(buildEmptyState());
            return;
        }

        for (int i = 0; i < quickNotes.size(); i++) {
            listContainer.addView(buildQuickNoteCard(quickNotes.get(i), quickNotes.size() - i));
            addVerticalSpace(listContainer, 10);
        }
    }

    private View buildQuickNoteCard(final QuickNote quickNote, int displayIndex) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 8));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(topRow);

        TextView mark = new TextView(this);
        mark.setText("言");
        mark.setGravity(Gravity.CENTER);
        mark.setTextColor(Color.WHITE);
        mark.setTypeface(Typeface.DEFAULT_BOLD);
        mark.setTextSize(14);
        mark.setBackground(circle(COLOR_PRIMARY, COLOR_PRIMARY));
        topRow.addView(mark, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView title = new TextView(this);
        title.setText("第 " + displayIndex + " 条一言");
        title.setTextColor(COLOR_PRIMARY_DARK);
        title.setTextSize(14);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(dp(12), 0, 0, 0);
        topRow.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView time = new TextView(this);
        time.setText(quickNoteTimeText(quickNote));
        time.setTextColor(COLOR_MUTED);
        time.setTextSize(12);
        topRow.addView(time);

        TextView content = new TextView(this);
        content.setText(quickNote.content);
        content.setTextColor(COLOR_TEXT);
        content.setTextSize(16);
        content.setLineSpacing(dp(3), 1.0f);
        content.setPadding(dp(12), dp(12), dp(12), dp(12));
        content.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        contentParams.setMargins(0, dp(10), 0, 0);
        card.addView(content, contentParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        actionsParams.setMargins(0, dp(10), 0, 0);
        card.addView(actions, actionsParams);

        Button deleteButton = secondaryButton("删除");
        deleteButton.setTextColor(COLOR_DANGER);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDeleteQuickNote(quickNote);
            }
        });
        actions.addView(deleteButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(40)
        ));

        return card;
    }

    private String quickNoteTimeText(QuickNote quickNote) {
        if (quickNote.createdAtText != null && !quickNote.createdAtText.isEmpty()) {
            return quickNote.createdAtText;
        }
        if (quickNote.createdAt > 0L) {
            return formatDateTime(new Date(quickNote.createdAt));
        }
        return "";
    }

    private void renderMemoNotes() {
        listContainer.removeAllViews();
        updateCollectionOverview("全部随记", memoNotes.size(), "篇");

        if (memoNotes.isEmpty()) {
            listContainer.addView(buildEmptyState());
            return;
        }

        for (int i = 0; i < memoNotes.size(); i++) {
            listContainer.addView(buildMemoNoteCard(memoNotes.get(i), memoNotes.size() - i));
            addVerticalSpace(listContainer, 10);
        }
    }

    private View buildMemoNoteCard(final MemoNote memoNote, int displayIndex) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 8));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(topRow);

        TextView mark = new TextView(this);
        mark.setText("记");
        mark.setGravity(Gravity.CENTER);
        mark.setTextColor(Color.WHITE);
        mark.setTypeface(Typeface.DEFAULT_BOLD);
        mark.setTextSize(14);
        mark.setBackground(circle(COLOR_PRIMARY, COLOR_PRIMARY));
        topRow.addView(mark, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView title = new TextView(this);
        title.setText(memoNoteTitle(memoNote, displayIndex));
        title.setTextColor(COLOR_TEXT);
        title.setTextSize(17);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(dp(12), 0, dp(8), 0);
        topRow.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView time = new TextView(this);
        time.setText(memoNoteTimeText(memoNote));
        time.setTextColor(COLOR_MUTED);
        time.setTextSize(12);
        topRow.addView(time);

        TextView content = new TextView(this);
        content.setText(markdownPreview(memoNote.content));
        content.setTextColor(COLOR_TEXT);
        content.setTextSize(15);
        content.setLineSpacing(dp(3), 1.0f);
        content.setPadding(dp(12), dp(12), dp(12), dp(12));
        content.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        contentParams.setMargins(0, dp(10), 0, 0);
        card.addView(content, contentParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        actionsParams.setMargins(0, dp(10), 0, 0);
        card.addView(actions, actionsParams);

        Button editButton = secondaryButton("编辑");
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditMemo(memoNote);
            }
        });
        actions.addView(editButton, new LinearLayout.LayoutParams(0, dp(40), 1f));

        addHorizontalSpace(actions, 8);

        Button deleteButton = secondaryButton("删除");
        deleteButton.setTextColor(COLOR_DANGER);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDeleteMemo(memoNote);
            }
        });
        actions.addView(deleteButton, new LinearLayout.LayoutParams(0, dp(40), 1f));

        return card;
    }

    private String memoNoteTitle(MemoNote memoNote, int displayIndex) {
        if (memoNote.title != null && !memoNote.title.trim().isEmpty()) {
            return memoNote.title.trim();
        }
        return "第 " + displayIndex + " 篇随记";
    }

    private String editingMemoTitle(MemoNote memoNote) {
        if (memoNote.title != null && !memoNote.title.trim().isEmpty()) {
            return memoNote.title.trim();
        }
        return "未命名随记";
    }

    private String memoNoteTimeText(MemoNote memoNote) {
        if (memoNote.updatedAtText != null && !memoNote.updatedAtText.isEmpty()) {
            return memoNote.updatedAtText;
        }
        if (memoNote.updatedAt > 0L) {
            return formatDateTime(new Date(memoNote.updatedAt));
        }
        return "";
    }

    private CharSequence markdownPreview(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "未填写内容";
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] lines = raw.split("\\r?\\n", -1);
        for (int i = 0; i < lines.length; i++) {
            appendMarkdownLine(builder, lines[i]);
            if (i < lines.length - 1) {
                builder.append('\n');
            }
        }
        return builder;
    }

    private void appendMarkdownLine(SpannableStringBuilder builder, String line) {
        String trimmed = line.trim();
        String display = line;
        boolean boldLine = false;
        boolean italicLine = false;
        boolean codeLine = false;
        float size = 1.0f;
        int color = COLOR_TEXT;

        if (trimmed.startsWith("### ")) {
            display = trimmed.substring(4);
            boldLine = true;
            size = 1.05f;
        } else if (trimmed.startsWith("## ")) {
            display = trimmed.substring(3);
            boldLine = true;
            size = 1.12f;
        } else if (trimmed.startsWith("# ")) {
            display = trimmed.substring(2);
            boldLine = true;
            size = 1.2f;
        } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            display = "• " + trimmed.substring(2);
        } else if (isNumberedMarkdownLine(trimmed)) {
            display = "• " + trimmed.substring(trimmed.indexOf(". ") + 2);
        } else if (trimmed.startsWith("> ")) {
            display = "│ " + trimmed.substring(2);
            italicLine = true;
            color = COLOR_PRIMARY_DARK;
        } else if (trimmed.startsWith("```")) {
            display = "代码块";
            codeLine = true;
            color = COLOR_MUTED;
        } else if (trimmed.startsWith("    ")) {
            display = trimmed;
            codeLine = true;
            color = COLOR_PRIMARY_DARK;
        }

        display = display.replace("**", "").replace("`", "");
        int start = builder.length();
        builder.append(display);
        int end = builder.length();
        if (end <= start) {
            return;
        }
        if (boldLine) {
            builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (italicLine) {
            builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (codeLine) {
            builder.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (size != 1.0f) {
            builder.setSpan(new RelativeSizeSpan(size), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (color != COLOR_TEXT) {
            builder.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private boolean isNumberedMarkdownLine(String text) {
        int marker = text.indexOf(". ");
        if (marker <= 0) {
            return false;
        }
        for (int i = 0; i < marker; i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private View buildPlanCard(final PlanItem plan) {
        String today = todayKey();
        boolean active = isActiveToday(plan, today);
        boolean checked = hasCheckInForCurrentPeriod(plan);
        boolean overdue = active && !checked && TYPE_DAILY.equals(plan.type) && nowMinutes() > plan.deadlineMinutes;

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));
        item.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(84)
        ));

        LinearLayout checkTarget = new LinearLayout(this);
        checkTarget.setGravity(Gravity.CENTER);
        checkTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isActiveToday(plan, todayKey())) {
                    promptCheckInNote(plan);
                }
            }
        });
        row.addView(checkTarget, new LinearLayout.LayoutParams(dp(44), dp(52)));

        TextView check = new TextView(this);
        check.setText(checked ? "✓" : "");
        check.setGravity(Gravity.CENTER);
        check.setTextColor(Color.WHITE);
        check.setTypeface(Typeface.DEFAULT_BOLD);
        check.setTextSize(16);
        check.setBackground(circle(checked ? COLOR_SUCCESS : Color.TRANSPARENT, checked ? COLOR_SUCCESS : COLOR_BORDER));
        check.setAlpha(active ? 1f : 0.45f);
        checkTarget.addView(check, new LinearLayout.LayoutParams(dp(32), dp(32)));

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.setPadding(dp(12), 0, dp(8), 0);
        row.addView(textColumn, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        textColumn.addView(titleRow);

        TextView name = new TextView(this);
        name.setText(plan.title);
        name.setTextColor(checked ? COLOR_MUTED : COLOR_TEXT);
        name.setTextSize(16);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        titleRow.addView(name, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        if (overdue || !active) {
            TextView badge = compactStatusBadge(overdue ? "已超时" : inactiveText(plan), overdue);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            badgeParams.setMargins(dp(6), 0, 0, 0);
            titleRow.addView(badge, badgeParams);
        }

        TextView meta = new TextView(this);
        meta.setText(planRowMetaText(plan, checked));
        meta.setTextColor(checked ? COLOR_SUCCESS : COLOR_MUTED);
        meta.setTextSize(12);
        meta.setSingleLine(true);
        meta.setEllipsize(TextUtils.TruncateAt.END);
        meta.setPadding(0, dp(6), 0, 0);
        textColumn.addView(meta);

        TextView more = new TextView(this);
        more.setText("⋯");
        more.setContentDescription("打开" + plan.title + "的操作面板");
        more.setTextColor(COLOR_HINT);
        more.setTextSize(22);
        more.setGravity(Gravity.CENTER);
        more.setBackground(circle(Color.TRANSPARENT, Color.TRANSPARENT));
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlanActionSheet(plan);
            }
        });
        row.addView(more, new LinearLayout.LayoutParams(dp(44), dp(48)));

        View divider = new View(this);
        divider.setBackgroundColor(COLOR_BORDER);
        item.addView(divider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
        ));
        return item;
    }

    private TextView compactStatusBadge(String text, boolean overdue) {
        TextView badge = new TextView(this);
        badge.setText(text);
        badge.setTextSize(10);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setTextColor(overdue ? COLOR_DANGER : COLOR_DISABLED);
        badge.setPadding(dp(6), dp(3), dp(6), dp(3));
        badge.setBackground(rounded(
                overdue ? COLOR_DANGER_BG : COLOR_INACTIVE_BG,
                overdue ? COLOR_DANGER_BG : COLOR_INACTIVE_BG,
                5
        ));
        return badge;
    }

    private String planRowMetaText(PlanItem plan, boolean checked) {
        if (!checked) {
            return scheduleText(plan);
        }
        String targetKey = currentTargetKey(plan);
        for (int index = plan.checkIns.size() - 1; index >= 0; index--) {
            CheckIn checkIn = plan.checkIns.get(index);
            if (!targetKey.equals(checkIn.targetKey)) {
                continue;
            }
            String time = checkIn.checkedAt == null ? "" : checkIn.checkedAt;
            int separator = time.lastIndexOf(' ');
            if (separator >= 0 && separator < time.length() - 1) {
                time = time.substring(separator + 1);
            }
            StringBuilder text = new StringBuilder(time).append(" 已打卡");
            if (checkIn.note != null && !checkIn.note.trim().isEmpty()) {
                text.append(" · ").append(checkIn.note.trim());
            }
            return text.toString();
        }
        return "本周期已打卡";
    }

    private void showPlanActionSheet(final PlanItem plan) {
        String today = todayKey();
        boolean active = isActiveToday(plan, today);
        boolean checked = hasCheckInForCurrentPeriod(plan);
        boolean overdue = active && !checked && TYPE_DAILY.equals(plan.type) && nowMinutes() > plan.deadlineMinutes;
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout dialogRoot = new LinearLayout(this);
        dialogRoot.setOrientation(LinearLayout.VERTICAL);
        dialogRoot.setPadding(dp(12), 0, dp(12), 0);

        final LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(20), dp(10), dp(20), dp(22));
        sheet.setBackground(topRounded(COLOR_SURFACE, 22));
        dialogRoot.addView(sheet);

        View handle = new View(this);
        handle.setBackground(rounded(COLOR_BORDER, COLOR_BORDER, 2));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(36), dp(4));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(16));
        sheet.addView(handle, handleParams);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        sheet.addView(titleRow);

        LinearLayout titleColumn = new LinearLayout(this);
        titleColumn.setOrientation(LinearLayout.VERTICAL);
        titleRow.addView(titleColumn, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        TextView schedule = new TextView(this);
        schedule.setText(scheduleText(plan));
        schedule.setTextColor(COLOR_MUTED);
        schedule.setTextSize(11);
        titleColumn.addView(schedule);

        TextView title = new TextView(this);
        title.setText(plan.title);
        title.setTextColor(COLOR_TEXT);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, 0);
        titleColumn.addView(title);

        TextView close = new TextView(this);
        close.setText("×");
        close.setContentDescription("关闭操作面板");
        close.setTextColor(COLOR_MUTED);
        close.setTextSize(22);
        close.setGravity(Gravity.CENTER);
        close.setBackground(circle(COLOR_INACTIVE_BG, COLOR_INACTIVE_BG));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        titleRow.addView(close, new LinearLayout.LayoutParams(dp(34), dp(34)));

        if (plan.note != null && !plan.note.trim().isEmpty()) {
            TextView note = new TextView(this);
            note.setText(plan.note.trim());
            note.setTextColor(COLOR_MUTED);
            note.setTextSize(12);
            note.setLineSpacing(dp(2), 1f);
            note.setMaxLines(2);
            note.setEllipsize(TextUtils.TruncateAt.END);
            note.setPadding(dp(10), dp(8), dp(10), dp(8));
            note.setBackground(rounded(COLOR_FIELD_BG, COLOR_FIELD_BG, 8));
            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            noteParams.setMargins(0, dp(12), 0, 0);
            sheet.addView(note, noteParams);
        }

        Button checkButton = new Button(this);
        checkButton.setAllCaps(false);
        checkButton.setText(checkButtonText(plan, checked, overdue, active));
        checkButton.setTextColor(active ? Color.WHITE : COLOR_DISABLED);
        checkButton.setTextSize(14);
        checkButton.setTypeface(Typeface.DEFAULT_BOLD);
        checkButton.setEnabled(active);
        checkButton.setBackground(active
                ? rounded(COLOR_PRIMARY, COLOR_PRIMARY, 8)
                : rounded(COLOR_INACTIVE_BG, COLOR_BORDER, 8));
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                promptCheckInNote(plan);
            }
        });
        LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
        );
        checkParams.setMargins(0, dp(16), 0, 0);
        sheet.addView(checkButton, checkParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(72)
        );
        actionsParams.setMargins(0, dp(10), 0, 0);
        sheet.addView(actions, actionsParams);

        actions.addView(sheetAction("✎", "编辑", COLOR_MUTED, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                openEditPlan(plan);
            }
        }), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        addHorizontalSpace(actions, 6);

        actions.addView(sheetAction("↺", "历史", COLOR_MUTED, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                openHistoryPage(plan);
            }
        }), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        addHorizontalSpace(actions, 6);

        if (TYPE_DAILY.equals(plan.type)) {
            actions.addView(sheetAction("◷", "改时间", COLOR_MUTED, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    pickTime(plan.deadlineMinutes, new TimeSelectedCallback() {
                        @Override
                        public void onTimeSelected(int minutes) {
                            plan.deadlineMinutes = minutes;
                            savePlans();
                            renderPlans();
                        }
                    });
                }
            }), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            addHorizontalSpace(actions, 6);
        }

        actions.addView(sheetAction("⌫", "删除", COLOR_DANGER, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                confirmDelete(plan);
            }
        }), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        addRecentCheckIns(sheet, plan, dialog);
        dialog.setContentView(dialogRoot);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.30f;
            window.setAttributes(attributes);
        }
        dialog.show();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        sheet.setAlpha(0f);
        sheet.setTranslationY(dp(24));
        sheet.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private View sheetAction(String icon, String label, int color, View.OnClickListener listener) {
        LinearLayout action = new LinearLayout(this);
        action.setOrientation(LinearLayout.VERTICAL);
        action.setGravity(Gravity.CENTER);
        action.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));
        action.setOnClickListener(listener);

        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setTextColor(color);
        iconView.setTextSize(19);
        iconView.setGravity(Gravity.CENTER);
        action.addView(iconView);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(color);
        labelView.setTextSize(11);
        labelView.setPadding(0, dp(5), 0, 0);
        action.addView(labelView);
        return action;
    }

    private void addRecentCheckIns(LinearLayout sheet, final PlanItem plan, final Dialog dialog) {
        View divider = new View(this);
        divider.setBackgroundColor(COLOR_BORDER);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
        );
        dividerParams.setMargins(0, dp(16), 0, dp(14));
        sheet.addView(divider, dividerParams);

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        sheet.addView(heading);

        TextView title = new TextView(this);
        title.setText("最近打卡");
        title.setTextColor(COLOR_TEXT);
        title.setTextSize(12);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        heading.addView(title, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        TextView all = new TextView(this);
        all.setText("查看全部");
        all.setTextColor(COLOR_PRIMARY_DARK);
        all.setTextSize(11);
        all.setPadding(dp(8), dp(4), 0, dp(4));
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                openHistoryPage(plan);
            }
        });
        heading.addView(all);

        if (plan.checkIns.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("暂无打卡记录");
            empty.setTextColor(COLOR_MUTED);
            empty.setTextSize(12);
            empty.setPadding(0, dp(12), 0, 0);
            sheet.addView(empty);
            return;
        }

        int shown = 0;
        for (int index = plan.checkIns.size() - 1; index >= 0 && shown < 2; index--, shown++) {
            CheckIn checkIn = plan.checkIns.get(index);
            LinearLayout entry = new LinearLayout(this);
            entry.setOrientation(LinearLayout.HORIZONTAL);
            entry.setGravity(Gravity.CENTER_VERTICAL);
            entry.setPadding(0, dp(10), 0, 0);
            sheet.addView(entry);

            View dot = new View(this);
            dot.setBackground(circle(COLOR_SUCCESS, COLOR_SUCCESS));
            entry.addView(dot, new LinearLayout.LayoutParams(dp(7), dp(7)));

            TextView note = new TextView(this);
            String noteText = checkIn.note == null || checkIn.note.trim().isEmpty()
                    ? "已完成本次打卡"
                    : checkIn.note.trim();
            note.setText(noteText);
            note.setTextColor(COLOR_MUTED);
            note.setTextSize(11);
            note.setSingleLine(true);
            note.setEllipsize(TextUtils.TruncateAt.END);
            note.setPadding(dp(9), 0, dp(8), 0);
            entry.addView(note, new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));

            TextView time = new TextView(this);
            time.setText(checkIn.checkedAt);
            time.setTextColor(COLOR_HINT);
            time.setTextSize(10);
            entry.addView(time);
        }
    }

    private String checkButtonText(PlanItem plan, boolean checked, boolean overdue, boolean active) {
        if (!active) {
            return "不在周期";
        }
        if (checked) {
            return "再记一次";
        }
        if (overdue) {
            return "补打卡";
        }
        return "打卡";
    }

    private String scheduleText(PlanItem plan) {
        if (TYPE_DAILY.equals(plan.type)) {
            if (MODE_DAILY_EVERY.equals(plan.scheduleMode)) {
                return "每日 · " + formatMinutes(plan.deadlineMinutes) + " 前";
            }
            if (MODE_DAILY_TODAY.equals(plan.scheduleMode)) {
                return "当日 · " + plan.startDate + " · " + formatMinutes(plan.deadlineMinutes) + " 前";
            }
            return "日期范围 · " + plan.startDate + " 至 " + plan.endDate + " · 每天 " + formatMinutes(plan.deadlineMinutes) + " 前";
        }

        if (TYPE_MONTHLY.equals(plan.type) && MODE_MONTHLY_EVERY.equals(plan.scheduleMode)) {
            return "每月 · 每个月记录一次";
        }
        if (TYPE_MONTHLY.equals(plan.type) && MODE_MONTHLY_THIS.equals(plan.scheduleMode)) {
            return "当月 · " + plan.startDate + " 至 " + plan.endDate;
        }
        if (TYPE_YEARLY.equals(plan.type) && MODE_YEARLY_EVERY.equals(plan.scheduleMode)) {
            return "每年 · 每年记录一次";
        }
        if (TYPE_YEARLY.equals(plan.type) && MODE_YEARLY_THIS.equals(plan.scheduleMode)) {
            return "当年 · " + plan.startDate + " 至 " + plan.endDate;
        }
        return "日期范围 · " + plan.startDate + " 至 " + plan.endDate;
    }

    private void openHistoryPage(PlanItem plan) {
        historyPlan = plan;
        editingPlan = null;
        editingMemo = null;
        isCreatePanelVisible = false;
        resetCreateForm();
        renderNavigation();
        renderHeaderAndForm();
        renderHistoryPage();
    }

    private void renderHistoryPage() {
        if (listContainer == null || historyPlan == null) {
            return;
        }

        listContainer.removeAllViews();
        if (historyPlan.checkIns.isEmpty()) {
            listContainer.addView(buildHistoryEmptyState());
            return;
        }

        for (int i = historyPlan.checkIns.size() - 1; i >= 0; i--) {
            listContainer.addView(buildHistoryRecordCard(historyPlan.checkIns.get(i), historyPlan.checkIns.size() - i));
            addVerticalSpace(listContainer, 10);
        }
    }

    private View buildHistoryEmptyState() {
        LinearLayout empty = new LinearLayout(this);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(Gravity.CENTER_HORIZONTAL);
        empty.setPadding(dp(18), dp(32), dp(18), dp(32));
        empty.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));

        TextView title = new TextView(this);
        title.setText("暂无完整打卡记录");
        title.setTextColor(COLOR_TEXT);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        empty.addView(title);

        TextView sub = new TextView(this);
        sub.setText("返回计划列表后点击打卡即可生成记录");
        sub.setTextColor(COLOR_MUTED);
        sub.setTextSize(14);
        sub.setPadding(0, dp(8), 0, 0);
        empty.addView(sub);
        return empty;
    }

    private View buildHistoryRecordCard(CheckIn checkIn, int displayIndex) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 8));

        TextView title = new TextView(this);
        title.setText("第 " + displayIndex + " 条 · " + periodLabel(checkIn));
        title.setTextColor(COLOR_PRIMARY_DARK);
        title.setTextSize(14);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(title);

        TextView time = new TextView(this);
        time.setText(checkIn.checkedAt);
        time.setTextColor(COLOR_TEXT);
        time.setTextSize(16);
        time.setTypeface(Typeface.DEFAULT_BOLD);
        time.setPadding(0, dp(6), 0, 0);
        card.addView(time);

        TextView note = new TextView(this);
        String noteText = checkIn.note == null || checkIn.note.trim().isEmpty()
                ? "未填写备注"
                : checkIn.note.trim();
        note.setText(noteText);
        note.setTextColor(checkIn.note == null || checkIn.note.trim().isEmpty() ? COLOR_MUTED : COLOR_TEXT);
        note.setTextSize(14);
        note.setLineSpacing(dp(2), 1.0f);
        note.setPadding(0, dp(8), 0, 0);
        card.addView(note);

        return card;
    }

    private String periodLabel(CheckIn checkIn) {
        if (TYPE_DAILY.equals(checkIn.targetType)) {
            return "日计划 " + checkIn.targetKey;
        }
        if (TYPE_MONTHLY.equals(checkIn.targetType)) {
            return "月计划 " + checkIn.targetKey;
        }
        return "年计划 " + checkIn.targetKey;
    }

    private void submitPlanForm() {
        String title = titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            titleInput.setError("写一个计划名称");
            titleInput.requestFocus();
            return;
        }

        PlanItem plan = editingPlan == null ? new PlanItem() : editingPlan;
        if (editingPlan == null) {
            plan.id = String.valueOf(System.currentTimeMillis());
            plan.createdAt = System.currentTimeMillis();
        }
        plan.type = activePlanType;
        plan.title = title;
        plan.note = noteInput.getText().toString().trim();
        plan.scheduleMode = selectedMode;
        plan.deadlineMinutes = TYPE_DAILY.equals(activePlanType) ? selectedDeadlineMinutes : 0;

        applySelectedRange(plan);
        if (MODE_RANGE.equals(plan.scheduleMode) && plan.endDate.compareTo(plan.startDate) < 0) {
            titleInput.setError("结束日期不能早于开始日期");
            return;
        }

        if (editingPlan == null) {
            plans.add(0, plan);
        }
        savePlans();
        hideKeyboard(titleInput);
        editingPlan = null;
        resetCreateForm();
        isCreatePanelVisible = false;
        renderHeaderAndForm();
        renderNavigation();
        renderPlans();
    }

    private void openEditPlan(PlanItem plan) {
        historyPlan = null;
        editingPlan = plan;
        editingMemo = null;
        activeModule = MODULE_PLAN;
        activePlanType = plan.type;
        selectedMode = plan.scheduleMode;
        selectedDeadlineMinutes = plan.deadlineMinutes > 0 ? plan.deadlineMinutes : 12 * 60;
        selectedStartDate = plan.startDate;
        selectedEndDate = plan.endDate;
        formTitleDraft = plan.title;
        formNoteDraft = plan.note;
        isCreatePanelVisible = true;
        renderNavigation();
        renderHeaderAndForm();
        renderPlans();
    }

    private void openEditMemo(MemoNote memoNote) {
        historyPlan = null;
        editingPlan = null;
        editingMemo = memoNote;
        activeModule = MODULE_MEMO;
        formTitleDraft = memoNote.title;
        formNoteDraft = memoNote.content;
        isCreatePanelVisible = true;
        renderNavigation();
        renderHeaderAndForm();
        renderPlans();
    }

    private void captureFormDraft() {
        if (titleInput != null) {
            formTitleDraft = titleInput.getText().toString();
        }
        if (noteInput != null) {
            formNoteDraft = noteInput.getText().toString();
        }
    }

    private void addQuickNote() {
        String content = noteInput.getText().toString().trim();
        if (content.isEmpty()) {
            noteInput.setError("写下一句话");
            noteInput.requestFocus();
            return;
        }

        long now = System.currentTimeMillis();
        QuickNote quickNote = new QuickNote();
        quickNote.id = "note-" + now;
        quickNote.content = content;
        quickNote.createdAt = now;
        quickNote.createdAtText = formatDateTime(new Date(now));
        quickNotes.add(0, quickNote);
        savePlans();
        hideKeyboard(noteInput);
        resetCreateForm();
        isCreatePanelVisible = false;
        renderHeaderAndForm();
        renderPlans();
    }

    private void submitMemoForm() {
        String title = titleInput.getText().toString().trim();
        String content = noteInput.getText().toString().trim();
        if (title.isEmpty() && content.isEmpty()) {
            noteInput.setError("写一点内容");
            noteInput.requestFocus();
            return;
        }

        long now = System.currentTimeMillis();
        MemoNote memoNote = editingMemo == null ? new MemoNote() : editingMemo;
        if (editingMemo == null) {
            memoNote.id = "memo-" + now;
            memoNote.createdAt = now;
            memoNote.createdAtText = formatDateTime(new Date(now));
            memoNotes.add(0, memoNote);
        }
        memoNote.title = title;
        memoNote.content = content;
        memoNote.updatedAt = now;
        memoNote.updatedAtText = formatDateTime(new Date(now));

        savePlans();
        hideKeyboard(noteInput);
        editingMemo = null;
        resetCreateForm();
        isCreatePanelVisible = false;
        renderNavigation();
        renderHeaderAndForm();
        renderPlans();
    }

    private void applySelectedRange(PlanItem plan) {
        if (TYPE_DAILY.equals(plan.type)) {
            if (MODE_DAILY_TODAY.equals(plan.scheduleMode)) {
                plan.startDate = todayKey();
                plan.endDate = todayKey();
            } else if (MODE_RANGE.equals(plan.scheduleMode)) {
                plan.startDate = selectedStartDate.isEmpty() ? todayKey() : selectedStartDate;
                plan.endDate = selectedEndDate.isEmpty() ? plan.startDate : selectedEndDate;
            } else {
                plan.startDate = "";
                plan.endDate = "";
            }
            return;
        }

        if (MODE_MONTHLY_THIS.equals(plan.scheduleMode)) {
            plan.startDate = monthStartKey();
            plan.endDate = monthEndKey();
        } else if (MODE_YEARLY_THIS.equals(plan.scheduleMode)) {
            plan.startDate = yearStartKey();
            plan.endDate = yearEndKey();
        } else if (MODE_RANGE.equals(plan.scheduleMode)) {
            plan.startDate = selectedStartDate.isEmpty() ? todayKey() : selectedStartDate;
            plan.endDate = selectedEndDate.isEmpty() ? plan.startDate : selectedEndDate;
        } else {
            plan.startDate = "";
            plan.endDate = "";
        }
    }

    private void resetCreateForm() {
        selectedMode = everyModeValue();
        selectedDeadlineMinutes = 12 * 60;
        selectedStartDate = "";
        selectedEndDate = "";
        formTitleDraft = "";
        formNoteDraft = "";
    }

    private void promptCheckInNote(final PlanItem plan) {
        if (!isActiveToday(plan, todayKey())) {
            return;
        }

        final EditText input = new EditText(this);
        input.setHint("写下这次打卡备注，可留空");
        input.setHintTextColor(COLOR_HINT);
        input.setTextColor(COLOR_TEXT);
        input.setMinLines(3);
        input.setGravity(Gravity.TOP);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));
        input.setPadding(dp(10), dp(8), dp(10), dp(8));

        new AlertDialog.Builder(this)
                .setTitle("打卡备注")
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存打卡", (dialog, which) -> addCheckIn(plan, input.getText().toString().trim()))
                .show();
    }

    private void addCheckIn(PlanItem plan, String note) {
        if (!isActiveToday(plan, todayKey())) {
            return;
        }
        long now = System.currentTimeMillis();
        CheckIn checkIn = new CheckIn();
        checkIn.id = plan.id + "-" + now;
        checkIn.targetType = plan.type;
        checkIn.targetKey = currentTargetKey(plan);
        checkIn.checkedAtMillis = now;
        checkIn.checkedAt = formatDateTime(new Date(now));
        checkIn.note = note;
        plan.checkIns.add(checkIn);
        savePlans();
        renderPlans();
    }

    private void confirmDelete(final PlanItem plan) {
        new AlertDialog.Builder(this)
                .setTitle("删除计划")
                .setMessage("确定删除“" + plan.title + "”吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    plans.remove(plan);
                    if (historyPlan == plan) {
                        historyPlan = null;
                    }
                    if (editingPlan == plan) {
                        editingPlan = null;
                        resetCreateForm();
                        isCreatePanelVisible = false;
                    }
                    savePlans();
                    renderHeaderAndForm();
                    renderNavigation();
                    renderPlans();
                })
                .show();
    }

    private void confirmDeleteQuickNote(final QuickNote quickNote) {
        new AlertDialog.Builder(this)
                .setTitle("删除一言")
                .setMessage("确定删除这条记录吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    quickNotes.remove(quickNote);
                    savePlans();
                    renderPlans();
                })
                .show();
    }

    private void confirmDeleteMemo(final MemoNote memoNote) {
        new AlertDialog.Builder(this)
                .setTitle("删除随记")
                .setMessage("确定删除这篇随记吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    memoNotes.remove(memoNote);
                    if (editingMemo == memoNote) {
                        editingMemo = null;
                        resetCreateForm();
                        isCreatePanelVisible = false;
                    }
                    savePlans();
                    renderHeaderAndForm();
                    renderNavigation();
                    renderPlans();
                })
                .show();
    }

    private boolean isActiveToday(PlanItem plan, String today) {
        return PlanRules.isActiveOnDate(plan, today);
    }

    private String inactiveText(PlanItem plan) {
        return PlanRules.inactiveText(plan, todayKey());
    }

    private boolean hasCheckInForCurrentPeriod(PlanItem plan) {
        return PlanRules.hasCheckInForPeriod(plan, currentTargetKey(plan));
    }

    private String currentTargetKey(PlanItem plan) {
        if (TYPE_DAILY.equals(plan.type)) {
            return todayKey();
        }
        if (TYPE_MONTHLY.equals(plan.type)) {
            return monthKey();
        }
        return yearKey();
    }

    private void pickTime(int currentMinutes, final TimeSelectedCallback callback) {
        int hour = currentMinutes / 60;
        int minute = currentMinutes % 60;
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int selectedMinute) {
                        callback.onTimeSelected(hourOfDay * 60 + selectedMinute);
                    }
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    private void pickDate(String currentDate, final DateSelectedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        if (currentDate != null && !currentDate.isEmpty()) {
            Date parsed = DateKeys.parseDate(currentDate);
            if (parsed != null) {
                calendar.setTime(parsed);
            }
        }
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selected = Calendar.getInstance();
                        selected.set(Calendar.YEAR, year);
                        selected.set(Calendar.MONTH, month);
                        selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        callback.onDateSelected(DateKeys.formatDate(selected.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private Button secondaryButton(String text) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextSize(12);
        button.setTextColor(COLOR_PRIMARY_DARK);
        button.setBackground(rounded(COLOR_CONTROL_BG, COLOR_BORDER, 8));
        return button;
    }

    private void compactIconButton(Button button) {
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(0, 0, 0, 0);
    }

    private GradientDrawable rounded(int fill, int stroke, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(fill);
        drawable.setStroke(dp(1), stroke);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable topRounded(int fill, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(fill);
        float radius = dp(radiusDp);
        drawable.setCornerRadii(new float[]{radius, radius, radius, radius, 0f, 0f, 0f, 0f});
        return drawable;
    }

    private GradientDrawable circle(int fill, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(fill);
        drawable.setStroke(dp(2), stroke);
        return drawable;
    }

    private void loadPlans() {
        plans.clear();
        quickNotes.clear();
        memoNotes.clear();
        DailyData data = repository.loadData();
        plans.addAll(data.plans);
        quickNotes.addAll(data.quickNotes);
        memoNotes.addAll(data.memoNotes);
    }

    private void savePlans() {
        repository.saveData(plans, quickNotes, memoNotes);
    }

    private void scheduleMidnightRefresh() {
        midnightHandler.removeCallbacks(midnightRefresh);
        midnightHandler.postDelayed(midnightRefresh, millisecondsUntilNextMidnight());
    }

    private long millisecondsUntilNextMidnight() {
        Calendar now = Calendar.getInstance();
        Calendar next = (Calendar) now.clone();
        next.add(Calendar.DAY_OF_YEAR, 1);
        next.set(Calendar.HOUR_OF_DAY, 0);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        return Math.max(1000L, next.getTimeInMillis() - now.getTimeInMillis());
    }

    private String todayKey() {
        return DateKeys.todayKey();
    }

    private String monthKey() {
        return DateKeys.monthKey();
    }

    private String yearKey() {
        return DateKeys.yearKey();
    }

    private String monthStartKey() {
        return DateKeys.monthStartKey();
    }

    private String monthEndKey() {
        return DateKeys.monthEndKey();
    }

    private String yearStartKey() {
        return DateKeys.yearStartKey();
    }

    private String yearEndKey() {
        return DateKeys.yearEndKey();
    }

    private int nowMinutes() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }

    private String formatMinutes(int minutes) {
        return DateKeys.formatMinutes(minutes);
    }

    private String formatDateTime(Date date) {
        return DateKeys.formatDateTime(date);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void addVerticalSpace(LinearLayout parent, int dpValue) {
        Space space = new Space(this);
        parent.addView(space, new LinearLayout.LayoutParams(1, dp(dpValue)));
    }

    private void addHorizontalSpace(LinearLayout parent, int dpValue) {
        Space space = new Space(this);
        parent.addView(space, new LinearLayout.LayoutParams(dp(dpValue), 1));
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private interface TimeSelectedCallback {
        void onTimeSelected(int minutes);
    }

    private interface DateSelectedCallback {
        void onDateSelected(String dateKey);
    }

}
