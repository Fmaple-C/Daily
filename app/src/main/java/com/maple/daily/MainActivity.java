package com.maple.daily;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class MainActivity extends Activity {
    private static final String MODULE_PLAN = "module_plan";
    private static final String MODULE_QUICK_NOTE = "module_quick_note";
    private static final String MODULE_MEMO = "module_memo";

    private int COLOR_BG;
    private int COLOR_SURFACE;
    private int COLOR_RAIL;
    private int COLOR_PRIMARY;
    private int COLOR_PRIMARY_DARK;
    private int COLOR_LEAF;
    private int COLOR_TEXT;
    private int COLOR_MUTED;
    private int COLOR_BORDER;
    private int COLOR_SUCCESS;
    private int COLOR_DANGER;
    private int COLOR_DISABLED;
    private int COLOR_CONTROL_BG;
    private int COLOR_FIELD_BG;
    private int COLOR_HINT;
    private int COLOR_SUCCESS_BG;
    private int COLOR_SUCCESS_BORDER;
    private int COLOR_DANGER_BG;
    private int COLOR_DANGER_BORDER;
    private int COLOR_PENDING_BG;
    private int COLOR_PENDING_BORDER;
    private int COLOR_INACTIVE_BG;
    private int COLOR_DONE_BORDER;

    private final List<PlanItem> plans = new ArrayList<>();
    private final List<QuickNote> quickNotes = new ArrayList<>();
    private final List<MemoNote> memoNotes = new ArrayList<>();
    private final Set<String> expandedPlanIds = new HashSet<>();
    private final Handler midnightHandler = new Handler(Looper.getMainLooper());
    private final Runnable midnightRefresh = new Runnable() {
        @Override
        public void run() {
            renderPlans();
            scheduleMidnightRefresh();
        }
    };

    private PlanRepository repository;
    private ThemePreferences themePreferences;
    private LinearLayout railContainer;
    private LinearLayout planTypeContainer;
    private LinearLayout formContainer;
    private LinearLayout listContainer;
    private TextView screenTitle;
    private TextView screenSubtitle;
    private TextView summaryText;
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
            COLOR_BG = Color.rgb(30, 24, 20);
            COLOR_SURFACE = Color.rgb(43, 34, 28);
            COLOR_RAIL = Color.rgb(54, 39, 30);
            COLOR_PRIMARY = Color.rgb(255, 138, 61);
            COLOR_PRIMARY_DARK = Color.rgb(255, 188, 128);
            COLOR_LEAF = Color.rgb(255, 156, 74);
            COLOR_TEXT = Color.rgb(250, 238, 228);
            COLOR_MUTED = Color.rgb(204, 172, 151);
            COLOR_BORDER = Color.rgb(100, 72, 56);
            COLOR_SUCCESS = Color.rgb(116, 205, 128);
            COLOR_DANGER = Color.rgb(255, 139, 112);
            COLOR_DISABLED = Color.rgb(139, 116, 103);
            COLOR_CONTROL_BG = Color.rgb(50, 39, 32);
            COLOR_FIELD_BG = Color.rgb(37, 29, 24);
            COLOR_HINT = Color.rgb(152, 123, 106);
            COLOR_SUCCESS_BG = Color.rgb(33, 67, 39);
            COLOR_SUCCESS_BORDER = Color.rgb(70, 126, 76);
            COLOR_DANGER_BG = Color.rgb(78, 39, 33);
            COLOR_DANGER_BORDER = Color.rgb(136, 75, 61);
            COLOR_PENDING_BG = Color.rgb(69, 48, 33);
            COLOR_PENDING_BORDER = Color.rgb(122, 79, 47);
            COLOR_INACTIVE_BG = Color.rgb(47, 38, 33);
            COLOR_DONE_BORDER = Color.rgb(83, 137, 89);
        } else {
            COLOR_BG = Color.rgb(255, 248, 242);
            COLOR_SURFACE = Color.WHITE;
            COLOR_RAIL = Color.rgb(255, 241, 229);
            COLOR_PRIMARY = Color.rgb(216, 107, 31);
            COLOR_PRIMARY_DARK = Color.rgb(135, 61, 15);
            COLOR_LEAF = Color.rgb(242, 126, 33);
            COLOR_TEXT = Color.rgb(45, 34, 27);
            COLOR_MUTED = Color.rgb(122, 98, 83);
            COLOR_BORDER = Color.rgb(242, 218, 198);
            COLOR_SUCCESS = Color.rgb(46, 125, 50);
            COLOR_DANGER = Color.rgb(183, 65, 42);
            COLOR_DISABLED = Color.rgb(180, 160, 147);
            COLOR_CONTROL_BG = Color.rgb(255, 249, 244);
            COLOR_FIELD_BG = Color.rgb(255, 252, 248);
            COLOR_HINT = Color.rgb(169, 142, 126);
            COLOR_SUCCESS_BG = Color.rgb(232, 247, 233);
            COLOR_SUCCESS_BORDER = Color.rgb(198, 226, 200);
            COLOR_DANGER_BG = Color.rgb(255, 238, 233);
            COLOR_DANGER_BORDER = Color.rgb(240, 188, 176);
            COLOR_PENDING_BG = Color.rgb(255, 244, 232);
            COLOR_PENDING_BORDER = Color.rgb(236, 203, 174);
            COLOR_INACTIVE_BG = Color.rgb(247, 242, 237);
            COLOR_DONE_BORDER = Color.rgb(190, 222, 191);
        }
    }

    private void applySystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(COLOR_BG);
        window.setNavigationBarColor(COLOR_BG);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int flags = isNightTheme ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void buildUi() {
        applySystemBars();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(COLOR_BG);
        root.setPadding(dp(16), dp(22), dp(16), 0);
        setContentView(root);

        TextView eyebrow = new TextView(this);
        eyebrow.setText("MAPLE PLAN");
        eyebrow.setTextColor(COLOR_LEAF);
        eyebrow.setTextSize(12);
        eyebrow.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(eyebrow);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(titleRow);

        screenTitle = new TextView(this);
        screenTitle.setTextColor(COLOR_TEXT);
        screenTitle.setTextSize(28);
        screenTitle.setTypeface(Typeface.DEFAULT_BOLD);
        titleRow.addView(screenTitle, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        backButton = new Button(this);
        backButton.setAllCaps(false);
        backButton.setText("返回");
        backButton.setTextSize(14);
        backButton.setTypeface(Typeface.DEFAULT_BOLD);
        backButton.setTextColor(COLOR_PRIMARY_DARK);
        backButton.setBackground(rounded(COLOR_CONTROL_BG, COLOR_BORDER, 8));
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
        titleRow.addView(backButton, new LinearLayout.LayoutParams(dp(70), dp(42)));

        addHorizontalSpace(titleRow, 8);

        themeButton = new Button(this);
        themeButton.setAllCaps(false);
        themeButton.setTextSize(14);
        themeButton.setTypeface(Typeface.DEFAULT_BOLD);
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
        titleRow.addView(themeButton, new LinearLayout.LayoutParams(dp(46), dp(42)));

        addHorizontalSpace(titleRow, 8);

        createButton = new Button(this);
        createButton.setAllCaps(false);
        createButton.setTextSize(14);
        createButton.setTypeface(Typeface.DEFAULT_BOLD);
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
        titleRow.addView(createButton, new LinearLayout.LayoutParams(dp(82), dp(42)));

        screenSubtitle = new TextView(this);
        screenSubtitle.setTextColor(COLOR_MUTED);
        screenSubtitle.setTextSize(14);
        screenSubtitle.setPadding(0, dp(4), 0, dp(10));
        root.addView(screenSubtitle);

        railContainer = new LinearLayout(this);
        railContainer.setOrientation(LinearLayout.HORIZONTAL);
        railContainer.setPadding(dp(4), dp(4), dp(4), dp(4));
        railContainer.setBackground(rounded(COLOR_RAIL, COLOR_BORDER, 8));
        root.addView(railContainer, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
        ));

        planTypeContainer = new LinearLayout(this);
        planTypeContainer.setOrientation(LinearLayout.HORIZONTAL);
        planTypeContainer.setPadding(dp(4), dp(4), dp(4), dp(4));
        planTypeContainer.setBackground(rounded(COLOR_CONTROL_BG, COLOR_BORDER, 8));
        LinearLayout.LayoutParams planTypeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44)
        );
        planTypeParams.setMargins(0, dp(8), 0, 0);
        root.addView(planTypeContainer, planTypeParams);

        formContainer = new LinearLayout(this);
        formContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(formContainer);

        summaryText = new TextView(this);
        summaryText.setTextColor(COLOR_MUTED);
        summaryText.setTextSize(14);
        summaryText.setPadding(0, dp(14), 0, dp(8));
        root.addView(summaryText);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(0, 0, 0, dp(20));
        scrollView.addView(listContainer);

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
        if (planTypeContainer != null) {
            planTypeContainer.removeAllViews();
        }
        railContainer.setVisibility(historyPlan == null ? View.VISIBLE : View.GONE);
        if (planTypeContainer != null) {
            planTypeContainer.setVisibility(historyPlan == null && MODULE_PLAN.equals(activeModule) ? View.VISIBLE : View.GONE);
        }
        if (historyPlan != null) {
            return;
        }

        railContainer.addView(moduleButton("计划", MODULE_PLAN), new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));

        addHorizontalSpace(railContainer, 4);

        railContainer.addView(moduleButton("一言", MODULE_QUICK_NOTE), new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));

        addHorizontalSpace(railContainer, 4);

        railContainer.addView(moduleButton("随记", MODULE_MEMO), new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));

        renderPlanTypeNavigation();
    }

    private void renderPlanTypeNavigation() {
        if (planTypeContainer == null || !MODULE_PLAN.equals(activeModule) || historyPlan != null) {
            return;
        }

        planTypeContainer.addView(planTypeButton("日计划", TYPE_DAILY), new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));

        addHorizontalSpace(planTypeContainer, 4);

        planTypeContainer.addView(planTypeButton("月计划", TYPE_MONTHLY), new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));

        addHorizontalSpace(planTypeContainer, 4);

        planTypeContainer.addView(planTypeButton("年计划", TYPE_YEARLY), new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));
    }

    private Button moduleButton(String text, final String module) {
        boolean selected = activeModule.equals(module);
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(selected ? Color.WHITE : COLOR_PRIMARY_DARK);
        button.setBackground(rounded(
                selected ? COLOR_PRIMARY : COLOR_CONTROL_BG,
                selected ? COLOR_PRIMARY : COLOR_BORDER,
                8
        ));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activeModule.equals(module)) {
                    activeModule = module;
                    editingPlan = null;
                    editingMemo = null;
                    resetCreateForm();
                    isCreatePanelVisible = false;
                    renderNavigation();
                    renderHeaderAndForm();
                    renderPlans();
                }
            }
        });
        return button;
    }

    private Button planTypeButton(String text, final String type) {
        boolean selected = activePlanType.equals(type);
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(selected ? Color.WHITE : COLOR_PRIMARY_DARK);
        button.setBackground(rounded(
                selected ? COLOR_PRIMARY : COLOR_CONTROL_BG,
                selected ? COLOR_PRIMARY : COLOR_BORDER,
                8
        ));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activePlanType.equals(type)) {
                    activePlanType = type;
                    editingPlan = null;
                    editingMemo = null;
                    resetCreateForm();
                    isCreatePanelVisible = false;
                    renderNavigation();
                    renderHeaderAndForm();
                    renderPlans();
                }
            }
        });
        return button;
    }

    private void renderHeaderAndForm() {
        if (historyPlan != null) {
            screenTitle.setText("打卡历史");
            screenSubtitle.setText(historyPlan.title + " · 共 " + historyPlan.checkIns.size() + " 条记录");
            summaryText.setText("");
            formContainer.removeAllViews();
            updateHeaderButtons();
            return;
        }

        if (MODULE_QUICK_NOTE.equals(activeModule)) {
            screenTitle.setText("一言");
            screenSubtitle.setText("随时写下一句话、灵感、想法或生活片段。");
        } else if (MODULE_MEMO.equals(activeModule)) {
            screenTitle.setText("随记");
            screenSubtitle.setText("用 Markdown 记录更完整的想法、复盘和草稿。");
        } else if (TYPE_DAILY.equals(activePlanType)) {
            screenTitle.setText("日计划");
            screenSubtitle.setText("记录每天、当日或一段日期内要坚持完成的事。");
        } else if (TYPE_MONTHLY.equals(activePlanType)) {
            screenTitle.setText("月计划");
            screenSubtitle.setText("记录每月、当月或一段日期内的规划。");
        } else if (TYPE_YEARLY.equals(activePlanType)) {
            screenTitle.setText("年计划");
            screenSubtitle.setText("记录每年、当年或一段日期内的长期目标。");
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
        if (editingPlan != null || editingMemo != null) {
            createButton.setText("取消");
        } else if (MODULE_QUICK_NOTE.equals(activeModule)) {
            createButton.setText(isCreatePanelVisible ? "收起" : "记录");
        } else if (MODULE_MEMO.equals(activeModule)) {
            createButton.setText(isCreatePanelVisible ? "收起" : "新建");
        } else {
            createButton.setText(isCreatePanelVisible ? "收起" : "新建");
        }
        createButton.setTextColor(isCreatePanelVisible ? COLOR_PRIMARY_DARK : Color.WHITE);
        createButton.setBackground(rounded(
                isCreatePanelVisible ? COLOR_CONTROL_BG : COLOR_PRIMARY,
                isCreatePanelVisible ? COLOR_BORDER : COLOR_PRIMARY,
                8
        ));
    }

    private void updateThemeButton() {
        if (themeButton == null) {
            return;
        }
        themeButton.setText(isNightTheme ? "日" : "夜");
        themeButton.setTextColor(COLOR_PRIMARY_DARK);
        themeButton.setBackground(rounded(COLOR_CONTROL_BG, COLOR_BORDER, 8));
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
        int overdue = 0;

        for (PlanItem plan : visible) {
            if (isActiveToday(plan, today)) {
                active++;
                if (hasCheckInForCurrentPeriod(plan)) {
                    completed++;
                } else if (TYPE_DAILY.equals(plan.type) && nowMinutes() > plan.deadlineMinutes) {
                    overdue++;
                }
            }
        }

        if (TYPE_DAILY.equals(activePlanType)) {
            String summary = "今天 " + today + "，活跃 " + active + " 项，" + completed + " 项已打卡";
            if (overdue > 0) {
                summary += "，" + overdue + " 项已超时";
            }
            summaryText.setText(summary);
        } else if (TYPE_MONTHLY.equals(activePlanType)) {
            summaryText.setText("本月 " + monthKey() + "，活跃 " + active + " 项，" + completed + " 项已打卡");
        } else {
            summaryText.setText("今年 " + yearKey() + "，活跃 " + active + " 项，" + completed + " 项已打卡");
        }

        if (visible.isEmpty()) {
            listContainer.addView(buildEmptyState());
            return;
        }

        for (PlanItem plan : visible) {
            listContainer.addView(buildPlanCard(plan));
            addVerticalSpace(listContainer, 10);
        }
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
        empty.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));

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
        summaryText.setText("共 " + quickNotes.size() + " 条记录，最新想法会显示在最上方");

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
        summaryText.setText("共 " + memoNotes.size() + " 篇随记，支持标题、列表、引用和代码等轻量 Markdown 展示");

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
        boolean expanded = expandedPlanIds.contains(plan.id);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(14));
        int borderColor = checked ? COLOR_DONE_BORDER : (overdue ? COLOR_DANGER_BORDER : COLOR_BORDER);
        card.setBackground(rounded(COLOR_SURFACE, borderColor, 8));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(topRow);

        TextView check = new TextView(this);
        check.setText(checked ? "✓" : "");
        check.setGravity(Gravity.CENTER);
        check.setTextColor(Color.WHITE);
        check.setTypeface(Typeface.DEFAULT_BOLD);
        check.setTextSize(18);
        check.setBackground(circle(checked ? COLOR_SUCCESS : Color.TRANSPARENT, checked ? COLOR_SUCCESS : COLOR_BORDER));
        topRow.addView(check, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView name = new TextView(this);
        name.setText(plan.title);
        name.setTextColor(COLOR_TEXT);
        name.setTextSize(18);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setPadding(dp(12), 0, dp(8), 0);
        topRow.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView badge = statusBadge(statusText(plan, active, checked, overdue), checked, overdue, active);
        topRow.addView(badge);

        TextView arrow = new TextView(this);
        arrow.setText(expanded ? "▲" : "▼");
        arrow.setTextColor(COLOR_PRIMARY_DARK);
        arrow.setTextSize(16);
        arrow.setTypeface(Typeface.DEFAULT_BOLD);
        arrow.setGravity(Gravity.CENTER);
        arrow.setBackground(rounded(COLOR_CONTROL_BG, COLOR_BORDER, 8));
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (expandedPlanIds.contains(plan.id)) {
                    expandedPlanIds.remove(plan.id);
                } else {
                    expandedPlanIds.add(plan.id);
                }
                renderPlans();
            }
        });
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        arrowParams.setMargins(dp(8), 0, 0, 0);
        topRow.addView(arrow, arrowParams);

        TextView schedule = new TextView(this);
        schedule.setText(scheduleText(plan));
        schedule.setTextColor(COLOR_MUTED);
        schedule.setTextSize(14);
        schedule.setPadding(dp(42), dp(8), 0, 0);
        card.addView(schedule);

        if (!plan.note.trim().isEmpty()) {
            TextView note = new TextView(this);
            note.setText(plan.note);
            note.setTextColor(COLOR_TEXT);
            note.setTextSize(14);
            note.setLineSpacing(dp(2), 1.0f);
            note.setPadding(dp(12), dp(10), dp(12), dp(10));
            note.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));
            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            noteParams.setMargins(0, dp(10), 0, 0);
            card.addView(note, noteParams);
        }

        TextView history = new TextView(this);
        history.setText(historyText(plan));
        history.setTextColor(COLOR_MUTED);
        history.setTextSize(13);
        history.setPadding(0, dp(10), 0, dp(10));
        card.addView(history);

        if (expanded) {
            card.addView(buildCheckInDetails(plan));
            addVerticalSpace(card, 10);
        }

        LinearLayout primaryActions = new LinearLayout(this);
        primaryActions.setOrientation(LinearLayout.HORIZONTAL);
        primaryActions.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(primaryActions, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button checkButton = secondaryButton(checkButtonText(plan, checked, overdue, active));
        checkButton.setEnabled(active);
        checkButton.setTextColor(active ? Color.WHITE : COLOR_DISABLED);
        checkButton.setBackground(active
                ? rounded(COLOR_PRIMARY, COLOR_PRIMARY, 8)
                : rounded(COLOR_INACTIVE_BG, COLOR_BORDER, 8));
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptCheckInNote(plan);
            }
        });
        primaryActions.addView(checkButton, new LinearLayout.LayoutParams(0, dp(42), 1f));

        addHorizontalSpace(primaryActions, 8);
        Button editButton = secondaryButton("编辑");
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditPlan(plan);
            }
        });
        primaryActions.addView(editButton, new LinearLayout.LayoutParams(0, dp(42), 1f));

        addHorizontalSpace(primaryActions, 8);
        Button historyButton = secondaryButton("历史");
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openHistoryPage(plan);
            }
        });
        primaryActions.addView(historyButton, new LinearLayout.LayoutParams(0, dp(42), 1f));

        addVerticalSpace(card, 8);

        LinearLayout secondaryActions = new LinearLayout(this);
        secondaryActions.setOrientation(LinearLayout.HORIZONTAL);
        secondaryActions.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(secondaryActions, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        if (TYPE_DAILY.equals(plan.type)) {
            Button timeButton = secondaryButton("改时间");
            timeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pickTime(plan.deadlineMinutes, new TimeSelectedCallback() {
                        @Override
                        public void onTimeSelected(int minutes) {
                            plan.deadlineMinutes = minutes;
                            savePlans();
                            renderPlans();
                        }
                    });
                }
            });
            secondaryActions.addView(timeButton, new LinearLayout.LayoutParams(0, dp(42), 1f));
            addHorizontalSpace(secondaryActions, 8);
        }

        Button deleteButton = secondaryButton("删除");
        deleteButton.setTextColor(COLOR_DANGER);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete(plan);
            }
        });
        secondaryActions.addView(deleteButton, new LinearLayout.LayoutParams(0, dp(42), 1f));

        return card;
    }

    private TextView statusBadge(String text, boolean checked, boolean overdue, boolean active) {
        TextView badge = new TextView(this);
        badge.setGravity(Gravity.CENTER);
        badge.setText(text);
        badge.setTextSize(12);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setPadding(dp(9), dp(5), dp(9), dp(5));
        if (checked) {
            badge.setTextColor(COLOR_SUCCESS);
            badge.setBackground(rounded(COLOR_SUCCESS_BG, COLOR_SUCCESS_BORDER, 8));
        } else if (overdue) {
            badge.setTextColor(COLOR_DANGER);
            badge.setBackground(rounded(COLOR_DANGER_BG, COLOR_DANGER_BORDER, 8));
        } else if (!active) {
            badge.setTextColor(COLOR_DISABLED);
            badge.setBackground(rounded(COLOR_INACTIVE_BG, COLOR_BORDER, 8));
        } else {
            badge.setTextColor(COLOR_PRIMARY_DARK);
            badge.setBackground(rounded(COLOR_PENDING_BG, COLOR_PENDING_BORDER, 8));
        }
        return badge;
    }

    private String statusText(PlanItem plan, boolean active, boolean checked, boolean overdue) {
        if (checked) {
            if (TYPE_DAILY.equals(plan.type)) {
                return "今日已打卡";
            }
            if (TYPE_MONTHLY.equals(plan.type)) {
                return "本月已打卡";
            }
            return "今年已打卡";
        }
        if (!active) {
            return inactiveText(plan);
        }
        if (overdue) {
            return "已超时";
        }
        return "待打卡";
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
                return "每日 · 每天 " + formatMinutes(plan.deadlineMinutes) + " 前";
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

    private String historyText(PlanItem plan) {
        if (plan.checkIns.isEmpty()) {
            return "暂无打卡记录";
        }

        StringBuilder builder = new StringBuilder("最近打卡：");
        int count = 0;
        for (int i = plan.checkIns.size() - 1; i >= 0 && count < 3; i--) {
            CheckIn checkIn = plan.checkIns.get(i);
            if (count > 0) {
                builder.append("；");
            }
            builder.append(checkIn.checkedAt);
            if (checkIn.note != null && !checkIn.note.trim().isEmpty()) {
                builder.append("：").append(checkIn.note.trim());
            }
            count++;
        }
        return builder.toString();
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

    private View buildCheckInDetails(PlanItem plan) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(12), dp(10), dp(12), dp(10));
        panel.setBackground(rounded(COLOR_FIELD_BG, COLOR_BORDER, 8));

        TextView title = new TextView(this);
        title.setText("全部打卡备注");
        title.setTextColor(COLOR_TEXT);
        title.setTextSize(14);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        panel.addView(title);

        if (plan.checkIns.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("暂无打卡备注");
            empty.setTextColor(COLOR_MUTED);
            empty.setTextSize(13);
            empty.setPadding(0, dp(8), 0, 0);
            panel.addView(empty);
            return panel;
        }

        for (int i = plan.checkIns.size() - 1; i >= 0; i--) {
            CheckIn checkIn = plan.checkIns.get(i);
            TextView item = new TextView(this);
            String note = checkIn.note == null || checkIn.note.trim().isEmpty()
                    ? "未填写备注"
                    : checkIn.note.trim();
            item.setText(checkIn.checkedAt + "\n" + note);
            item.setTextColor(COLOR_TEXT);
            item.setTextSize(13);
            item.setLineSpacing(dp(2), 1.0f);
            item.setPadding(0, dp(8), 0, 0);
            panel.addView(item);
        }

        return panel;
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
        expandedPlanIds.add(plan.id);
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
        button.setTextSize(13);
        button.setTextColor(COLOR_PRIMARY_DARK);
        button.setBackground(rounded(COLOR_CONTROL_BG, COLOR_BORDER, 8));
        return button;
    }

    private GradientDrawable rounded(int fill, int stroke, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(fill);
        drawable.setStroke(dp(1), stroke);
        drawable.setCornerRadius(dp(radiusDp));
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
