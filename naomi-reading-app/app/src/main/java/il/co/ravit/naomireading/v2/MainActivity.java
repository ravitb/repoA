package il.co.ravit.naomireading.v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PREFS = "naomi_reading_v2";
    private static final String KEY_PREFIX = "step_";
    private final TextView[] circles = new TextView[50];
    private TextView progressText;
    private View progressFill;
    private int completed = 0;

    private final int[] colors = {
        Color.rgb(242, 87, 155),
        Color.rgb(75, 200, 222),
        Color.rgb(142, 99, 216),
        Color.rgb(255, 192, 68),
        Color.rgb(116, 201, 140)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        loadState();
        refresh();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(255, 250, 252));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(18), dp(14), dp(30));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        TextView dragons = text("🐉   ✨   🐉", 34, Color.rgb(85, 45, 130), Typeface.BOLD);
        root.addView(dragons);

        TextView title = text("נעמי קוראת\nבדרך למתנה!", 30, Color.rgb(93, 42, 168), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = lpMatchWrap();
        titleLp.setMargins(0, dp(2), 0, dp(12));
        root.addView(title, titleLp);

        root.addView(card("המתנה שלי: ביצי דרקון מעלי אקספרס 🥚🐉",
                Color.rgb(255, 245, 176), Color.rgb(244, 177, 62)));
        root.addView(card("כל 2 עמודים שאני קוראת = מקרב אותי למטרה",
                Color.rgb(240, 230, 255), Color.rgb(180, 145, 230)));

        LinearLayout progressCard = new LinearLayout(this);
        progressCard.setOrientation(LinearLayout.VERTICAL);
        progressCard.setPadding(dp(14), dp(12), dp(14), dp(12));
        progressCard.setBackground(rounded(Color.WHITE, Color.rgb(237, 214, 241), 18, 2));
        LinearLayout.LayoutParams pcLp = lpMatchWrap();
        pcLp.setMargins(0, dp(12), 0, dp(8));
        root.addView(progressCard, pcLp);

        progressText = text("", 19, Color.rgb(62, 24, 100), Typeface.BOLD);
        progressText.setGravity(Gravity.CENTER);
        progressCard.addView(progressText, lpMatchWrap());

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.START);
        bar.setBackground(rounded(Color.rgb(232, 229, 235), Color.rgb(215, 210, 222), 20, 1));
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(14));
        barLp.setMargins(0, dp(8), 0, 0);
        progressCard.addView(bar, barLp);

        progressFill = new View(this);
        progressFill.setBackground(rounded(Color.rgb(242, 87, 155), Color.TRANSPARENT, 20, 0));
        bar.addView(progressFill, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT));

        TextView hint = text("מסיימים שני עמודים? לוחצים על העיגול. לחיצה נוספת מבטלת.", 14,
                Color.rgb(105, 88, 113), Typeface.NORMAL);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintLp = lpMatchWrap();
        hintLp.setMargins(0, dp(8), 0, dp(10));
        root.addView(hint, hintLp);

        for (int row = 0; row < 10; row++) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setGravity(Gravity.CENTER);
            line.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            root.addView(line, lpMatchWrap());

            for (int col = 0; col < 5; col++) {
                int index = row * 5 + col;
                TextView c = text(String.valueOf((index + 1) * 2), 20, Color.rgb(116, 112, 124), Typeface.BOLD);
                c.setGravity(Gravity.CENTER);
                c.setBackground(circleBg(false, index));
                c.setClickable(true);
                c.setFocusable(true);
                c.setContentDescription("עמודים " + ((index + 1) * 2));
                final int i = index;
                c.setOnClickListener(v -> toggle(i));
                circles[index] = c;

                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(dp(58), dp(58));
                cp.setMargins(dp(4), dp(5), dp(4), dp(5));
                line.addView(c, cp);
            }
        }

        TextView goal = text("🥚✨  המטרה: 100 עמודים  ✨🥚\nעוד קצת וביצי הדרקון שלך!", 20,
                Color.rgb(102, 42, 165), Typeface.BOLD);
        goal.setGravity(Gravity.CENTER);
        goal.setPadding(dp(12), dp(15), dp(12), dp(15));
        goal.setBackground(rounded(Color.rgb(255, 247, 188), Color.rgb(255, 205, 80), 24, 2));
        LinearLayout.LayoutParams goalLp = lpMatchWrap();
        goalLp.setMargins(0, dp(12), 0, dp(12));
        root.addView(goal, goalLp);

        Button reset = new Button(this);
        reset.setText("↻  איפוס כל הסימונים");
        reset.setTextSize(16);
        reset.setTextColor(Color.rgb(93, 82, 101));
        reset.setAllCaps(false);
        reset.setBackground(rounded(Color.rgb(240, 237, 243), Color.rgb(218, 211, 223), 18, 1));
        reset.setOnClickListener(v -> confirmReset());
        LinearLayout.LayoutParams resetLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        root.addView(reset, resetLp);

        setContentView(scroll);
    }

    private TextView card(String value, int bg, int border) {
        TextView t = text(value, 17, Color.rgb(70, 45, 80), Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(10), dp(11), dp(10), dp(11));
        t.setBackground(rounded(bg, border, 20, 2));
        LinearLayout.LayoutParams p = lpMatchWrap();
        p.setMargins(0, dp(5), 0, dp(5));
        t.setLayoutParams(p);
        return t;
    }

    private void toggle(int index) {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean now = !p.getBoolean(KEY_PREFIX + index, false);
        p.edit().putBoolean(KEY_PREFIX + index, now).apply();
        refresh();

        if (completed == 50) {
            new AlertDialog.Builder(this)
                    .setTitle("הגעת ל־100! 🐉✨")
                    .setMessage("כל הכבוד נעמי! ביצי הדרקון מחכות לך! 🥚🐉")
                    .setPositiveButton("יששש!", null)
                    .show();
        }
    }

    private void loadState() {
        refresh();
    }

    private void refresh() {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        completed = 0;
        for (int i = 0; i < 50; i++) {
            boolean on = p.getBoolean(KEY_PREFIX + i, false);
            if (on) completed++;
            if (circles[i] != null) {
                circles[i].setBackground(circleBg(on, i));
                circles[i].setTextColor(on ? Color.rgb(50, 28, 78) : Color.rgb(116, 112, 124));
                circles[i].setAlpha(on ? 1f : 0.92f);
            }
        }
        if (progressText != null) {
            progressText.setText("קראתי " + (completed * 2) + " מתוך 100 עמודים");
        }
        if (progressFill != null) {
            LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, completed / 50f);
            progressFill.setLayoutParams(fp);
        }
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("לאפס את כל הסימונים?")
                .setMessage("כל העיגולים יחזרו לאפור.")
                .setNegativeButton("ביטול", null)
                .setPositiveButton("איפוס", (d, which) -> {
                    SharedPreferences.Editor e = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
                    e.clear().apply();
                    refresh();
                    Toast.makeText(this, "הסימונים אופסו", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private TextView text(String s, int sp, int color, int style) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(Typeface.create("sans", style));
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private GradientDrawable circleBg(boolean on, int index) {
        int bg = on ? lighten(colors[index % colors.length]) : Color.rgb(231, 230, 234);
        int border = on ? colors[index % colors.length] : Color.rgb(184, 182, 191);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(bg);
        g.setStroke(dp(3), border);
        return g;
    }

    private int lighten(int c) {
        int r = (Color.red(c) + 255 * 2) / 3;
        int g = (Color.green(c) + 255 * 2) / 3;
        int b = (Color.blue(c) + 255 * 2) / 3;
        return Color.rgb(r, g, b);
    }

    private GradientDrawable rounded(int bg, int border, int radiusDp, int strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(bg);
        g.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) g.setStroke(dp(strokeDp), border);
        return g;
    }

    private LinearLayout.LayoutParams lpMatchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
