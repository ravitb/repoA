package il.co.ravit.naomireading.v3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PREFS = "naomi_reading_v3";
    private static final String STEP = "step_";

    private final TextView[] circles = new TextView[50];
    private TextView progress;
    private int completed = 0;

    private final int[] palette = {
            Color.rgb(244, 94, 159),
            Color.rgb(75, 198, 221),
            Color.rgb(158, 108, 224),
            Color.rgb(255, 197, 73),
            Color.rgb(105, 194, 126)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildScreen());
        refresh();
    }

    private View buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(250, 248, 255));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(14), dp(16), dp(14), dp(28));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackground(verticalGradient(
                Color.rgb(214, 246, 255),
                Color.rgb(255, 244, 250),
                Color.rgb(236, 252, 225)
        ));
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        TextView sky = makeText("✨  💗  🏰  💗  ✨", 23, Color.rgb(111, 68, 159), Typeface.BOLD);
        root.addView(sky, matchWrap());

        TextView title = makeText("נעמי קוראת\nבדרך למתנה!", 32,
                Color.rgb(117, 59, 181), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = matchWrap();
        titleLp.setMargins(0, dp(4), 0, dp(7));
        root.addView(title, titleLp);

        TextView dragonLine = makeText("🐉📚                         📚🐉", 26,
                Color.rgb(80, 68, 112), Typeface.NORMAL);
        root.addView(dragonLine, matchWrap());

        TextView reward = makeText("🎁  המתנה שלי: ביצי דרקון מעלי אקספרס", 18,
                Color.rgb(55, 36, 96), Typeface.BOLD);
        styleCard(reward, Color.rgb(255, 244, 171), Color.rgb(248, 188, 61));
        LinearLayout.LayoutParams rewardLp = matchWrap();
        rewardLp.setMargins(0, dp(7), 0, dp(7));
        root.addView(reward, rewardLp);

        TextView rule = makeText("📖  כל 2 עמודים שאני קוראת = מקרב אותי למטרה", 17,
                Color.rgb(67, 43, 100), Typeface.BOLD);
        styleCard(rule, Color.rgb(240, 226, 255), Color.rgb(184, 140, 230));
        LinearLayout.LayoutParams ruleLp = matchWrap();
        ruleLp.setMargins(0, 0, 0, dp(10));
        root.addView(rule, ruleLp);

        progress = makeText("", 18, Color.rgb(86, 43, 128), Typeface.BOLD);
        progress.setGravity(Gravity.CENTER);
        progress.setPadding(dp(10), dp(8), dp(10), dp(8));
        progress.setBackground(roundRect(Color.argb(225,255,255,255),
                Color.rgb(221, 199, 233), 18, 2));
        LinearLayout.LayoutParams progLp = matchWrap();
        progLp.setMargins(0, 0, 0, dp(8));
        root.addView(progress, progLp);

        TextView hint = makeText("כל העיגולים אפורים בהתחלה. לחיצה צובעת; לחיצה נוספת מבטלת.", 14,
                Color.rgb(101, 88, 108), Typeface.NORMAL);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintLp = matchWrap();
        hintLp.setMargins(dp(8), 0, dp(8), dp(8));
        root.addView(hint, hintLp);

        for (int row = 0; row < 10; row++) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setGravity(Gravity.CENTER);
            line.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

            for (int col = 0; col < 5; col++) {
                int visualIndex = row * 5 + col;
                int stepIndex;
                if (row % 2 == 0) {
                    stepIndex = visualIndex;
                } else {
                    stepIndex = row * 5 + (4 - col);
                }

                int page = (stepIndex + 1) * 2;
                TextView circle = makeText(String.valueOf(page), 20,
                        Color.rgb(102, 98, 109), Typeface.BOLD);
                circle.setGravity(Gravity.CENTER);
                circle.setClickable(true);
                circle.setFocusable(true);
                circle.setContentDescription("עמודים " + page);
                final int index = stepIndex;
                circle.setOnClickListener(v -> toggle(index));
                circles[index] = circle;

                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                        0, dp(66), 1f);
                cp.setMargins(dp(4), dp(5), dp(4), dp(5));
                line.addView(circle, cp);
            }

            LinearLayout.LayoutParams rowLp = matchWrap();
            root.addView(line, rowLp);

            if (row == 1 || row == 5 || row == 7) {
                TextView deco = makeText(row == 5 ? "💗  ✨  🐲  ✨  💗" : "🌸  💗  ✨  💗  🌸",
                        17, Color.rgb(157, 91, 163), Typeface.NORMAL);
                LinearLayout.LayoutParams decoLp = matchWrap();
                decoLp.setMargins(0, dp(1), 0, dp(1));
                root.addView(deco, decoLp);
            }
        }

        TextView goal = makeText("🥚✨  הגעתי ל־100 עמודים!  ✨🥚\nכל הכבוד! המתנה מחכה לי!", 21,
                Color.rgb(91, 39, 139), Typeface.BOLD);
        goal.setGravity(Gravity.CENTER);
        goal.setPadding(dp(12), dp(14), dp(12), dp(14));
        goal.setBackground(roundRect(Color.rgb(255, 246, 184),
                Color.rgb(255, 192, 52), 24, 2));
        LinearLayout.LayoutParams goalLp = matchWrap();
        goalLp.setMargins(0, dp(12), 0, dp(12));
        root.addView(goal, goalLp);

        TextView bottomDragons = makeText("🐉📚   💗   🥚   💗   📚🐉", 27,
                Color.rgb(106, 62, 142), Typeface.NORMAL);
        LinearLayout.LayoutParams bdLp = matchWrap();
        bdLp.setMargins(0, 0, 0, dp(10));
        root.addView(bottomDragons, bdLp);

        Button reset = new Button(this);
        reset.setText("↻  איפוס כל הסימונים");
        reset.setTextSize(16);
        reset.setAllCaps(false);
        reset.setTextColor(Color.rgb(85, 75, 92));
        reset.setBackground(roundRect(Color.rgb(239, 237, 242),
                Color.rgb(213, 208, 219), 20, 1));
        reset.setOnClickListener(v -> confirmReset());
        LinearLayout.LayoutParams resetLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        root.addView(reset, resetLp);

        return scroll;
    }

    private void toggle(int index) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean newValue = !prefs.getBoolean(STEP + index, false);
        prefs.edit().putBoolean(STEP + index, newValue).apply();
        refresh();

        if (completed == 50) {
            new AlertDialog.Builder(this)
                    .setTitle("נעמי הגיעה ל־100! 🐉✨")
                    .setMessage("כל הכבוד! קראת 100 עמודים — ביצי הדרקון מחכות לך! 🥚💗")
                    .setPositiveButton("יששש!", null)
                    .show();
        }
    }

    private void refresh() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        completed = 0;

        for (int i = 0; i < 50; i++) {
            boolean on = prefs.getBoolean(STEP + i, false);
            if (on) completed++;

            TextView c = circles[i];
            if (c != null) {
                int border = on ? palette[i % palette.length] : Color.rgb(172, 171, 178);
                int fill = on ? pale(palette[i % palette.length]) : Color.rgb(225, 225, 229);
                c.setBackground(oval(fill, border, on ? 4 : 3));
                c.setTextColor(on ? Color.rgb(52, 27, 80) : Color.rgb(108, 105, 114));
                c.setAlpha(on ? 1f : 0.95f);
            }
        }

        if (progress != null) {
            progress.setText("קראתי " + (completed * 2) + " מתוך 100 עמודים  •  "
                    + completed + " מתוך 50 סימונים");
        }
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("לאפס את כל הסימונים?")
                .setMessage("כל העיגולים יחזרו לאפור.")
                .setNegativeButton("ביטול", null)
                .setPositiveButton("איפוס", (dialog, which) -> {
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply();
                    refresh();
                    Toast.makeText(this, "הסימונים אופסו", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private TextView makeText(String value, int sp, int color, int style) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(Typeface.create("sans", style));
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private void styleCard(TextView t, int fill, int stroke) {
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(11), dp(12), dp(11));
        t.setBackground(roundRect(fill, stroke, 22, 2));
    }

    private GradientDrawable verticalGradient(int top, int middle, int bottom) {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{top, middle, bottom});
        return g;
    }

    private GradientDrawable roundRect(int fill, int stroke, int radiusDp, int strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) g.setStroke(dp(strokeDp), stroke);
        return g;
    }

    private GradientDrawable oval(int fill, int stroke, int strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(fill);
        g.setStroke(dp(strokeDp), stroke);
        return g;
    }

    private int pale(int color) {
        int r = (Color.red(color) + 510) / 3;
        int g = (Color.green(color) + 510) / 3;
        int b = (Color.blue(color) + 510) / 3;
        return Color.rgb(r, g, b);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
