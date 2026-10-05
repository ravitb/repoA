package il.co.ravit.listeningreward;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class RewardBoardView extends View {
    private static final int BOARDS = 2;
    private static final int N = 5;

    private final boolean[][] selected = new boolean[BOARDS][N];
    private final float[][] cx = new float[BOARDS][N];
    private final float[] cy = new float[BOARDS];
    private final float[] resetCx = new float[BOARDS];
    private final float[] resetCy = new float[BOARDS];

    private float radius;
    private float resetR;
    private float halfH;

    private final boolean[] rewardActive = new boolean[BOARDS];
    private final long[] rewardStart = new long[BOARDS];
    private final long[] resetStart = new long[BOARDS];

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random(7);
    private final float[] sx = new float[28], sy = new float[28], sp = new float[28];

    private final int[] colors = {
            Color.rgb(102,190,232),
            Color.rgb(105,194,155),
            Color.rgb(132,91,188),
            Color.rgb(248,170,120),
            Color.rgb(237,112,167)
    };

    public RewardBoardView(Context context) {
        super(context);
        setBackgroundColor(Color.WHITE);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        text.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
        ));
        text.setTextAlign(Paint.Align.CENTER);

        for (int i=0; i<sx.length; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            float d = .65f + random.nextFloat() * .55f;
            sx[i] = (float)Math.cos(a) * d;
            sy[i] = (float)Math.sin(a) * d;
            sp[i] = random.nextFloat();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        halfH = h / 2f;
        float unit = Math.min(w / 14f, halfH / 7f);
        radius = unit * .82f;
        resetR = Math.max(dp(22), unit * .31f);

        for (int b=0; b<BOARDS; b++) {
            float top = b * halfH;
            cy[b] = top + halfH * .55f;

            float start = w * .34f;
            float gap = w * .145f;
            for (int i=0; i<N; i++) {
                cx[b][i] = start + i * gap;
            }

            resetCx[b] = w * .50f;
            resetCy[b] = top + halfH * .88f;
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        drawDivider(c);

        for (int b=0; b<BOARDS; b++) {
            drawTitle(c, b);

            for (int i=0; i<N; i++) {
                drawEarToken(c, b, i);
            }

            drawGift(c, b);
            drawReset(c, b);
        }

        long now = SystemClock.uptimeMillis();
        boolean animate = false;

        for (int b=0; b<BOARDS; b++) {
            if (rewardActive[b] && now - rewardStart[b] < 1500) animate = true;
            if (resetStart[b] != 0 && now - resetStart[b] < 420) animate = true;
        }

        if (animate) postInvalidateOnAnimation();
    }

    private void drawDivider(Canvas c) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(230,230,230));
        c.drawRect(0, halfH - dp(1), getWidth(), halfH + dp(1), p);
    }

    private void drawTitle(Canvas c, int board) {
        float top = board * halfH;
        text.setColor(Color.rgb(96,96,96));
        text.setTextSize(Math.min(getWidth() * .058f, halfH * .09f));
        c.drawText("I'm listening to the team",
                getWidth() * .62f,
                top + halfH * .15f,
                text);
    }

    private void drawEarToken(Canvas c, int board, int i) {
        float x = cx[board][i];
        float y = cy[board];
        int bg = selected[board][i] ? colors[i] : Color.rgb(222,222,222);

        if (selected[board][i]) {
            drawRays(c, x, y, radius, colors[i]);
        }

        p.setStyle(Paint.Style.FILL);
        p.setColor(bg);
        c.drawCircle(x, y, radius, p);

        text.setTextSize(radius * .50f);
        text.setColor(selected[board][i] ? colors[i] : Color.rgb(122,122,122));
        c.drawText(String.valueOf(i + 1), x, y - radius * 1.30f, text);

        drawEar(c, x, y + radius * .02f, radius * .74f);
    }

    private void drawRays(Canvas c, float x, float y, float r, int color) {
        p.setColor(color);
        p.setStrokeWidth(Math.max(dp(2.5f), r * .055f));
        p.setStrokeCap(Paint.Cap.ROUND);

        for (int k=0; k<12; k++) {
            double a = k * Math.PI * 2 / 12.0;
            c.drawLine(
                    x + (float)Math.cos(a) * r * 1.16f,
                    y + (float)Math.sin(a) * r * 1.16f,
                    x + (float)Math.cos(a) * r * 1.38f,
                    y + (float)Math.sin(a) * r * 1.38f,
                    p
            );
        }
    }

    private void drawEar(Canvas c, float x, float y, float s) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(229,171,130));

        Path o = new Path();
        o.moveTo(x+s*.18f, y+s*.78f);
        o.cubicTo(x-s*.42f, y+s*.70f, x-s*.55f, y+s*.18f, x-s*.47f, y-s*.26f);
        o.cubicTo(x-s*.39f, y-s*.88f, x+s*.18f, y-s*1.02f, x+s*.50f, y-s*.55f);
        o.cubicTo(x+s*.74f, y-s*.18f, x+s*.58f, y+s*.05f, x+s*.36f, y+s*.30f);
        o.cubicTo(x+s*.14f, y+s*.57f, x+s*.43f, y+s*.80f, x+s*.18f, y+s*.78f);
        c.drawPath(o, p);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(s * .075f);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(Color.rgb(190,124,89));

        Path in = new Path();
        in.moveTo(x+s*.18f, y-s*.45f);
        in.cubicTo(x-s*.14f, y-s*.70f, x-s*.35f, y-s*.35f, x-s*.25f, y-s*.05f);
        in.cubicTo(x-s*.18f, y+s*.17f, x+s*.18f, y+s*.02f, x+s*.15f, y+s*.28f);
        in.cubicTo(x+s*.10f, y+s*.52f, x-s*.08f, y+s*.50f, x-s*.12f, y+s*.40f);
        c.drawPath(in, p);

        p.setStyle(Paint.Style.FILL);
    }

    private void drawGift(Canvas c, int board) {
        float gx = getWidth() * .115f;
        float gy = cy[board];
        float gw = Math.min(getWidth() * .18f, halfH * .24f);
        float gh = gw * .78f;

        long e = SystemClock.uptimeMillis() - rewardStart[board];
        boolean animate = rewardActive[board] && e < 1500;
        float alpha = rewardActive[board] ? 1f : .34f;

        c.save();

        if (animate) {
            float settle = 1f - Math.min(1f, e / 1500f);
            float bob = (float)Math.sin(e / 1000f * Math.PI * 7) * dp(8) * settle;
            float angle = (float)Math.sin(e / 1000f * Math.PI * 5) * 6f * settle;

            c.translate(0, bob);
            c.rotate(angle, gx, gy);

            float pop = 1f + .11f *
                    (float)Math.sin(Math.min(1f, e / 500f) * Math.PI);
            c.scale(pop, pop, gx, gy);
        }

        p.setAlpha((int)(255 * alpha));
        p.setColor(Color.rgb(248,190,55));
        c.drawRoundRect(
                new RectF(gx-gw/2, gy-gh/2, gx+gw/2, gy+gh/2),
                dp(12), dp(12), p
        );

        p.setColor(Color.rgb(235,99,154));
        c.drawRect(gx-gw*.09f, gy-gh/2, gx+gw*.09f, gy+gh/2, p);
        c.drawRect(gx-gw/2, gy-gh*.10f, gx+gw/2, gy+gh*.10f, p);

        c.drawOval(
                new RectF(gx-gw*.31f, gy-gh*.73f, gx-gw*.02f, gy-gh*.43f),
                p
        );
        c.drawOval(
                new RectF(gx+gw*.02f, gy-gh*.73f, gx+gw*.31f, gy-gh*.43f),
                p
        );

        p.setAlpha(255);
        c.restore();

        if (animate) drawSparkles(c, gx, gy, gw, e);
    }

    private void drawSparkles(Canvas c, float x, float y, float r, long e) {
        float prog = Math.min(1f, e / 1350f);
        int[] sc = {
                0xFFFFC62E,
                0xFFF36FA6,
                0xFF64BEEA,
                0xFF8B55C5
        };

        for (int i=0; i<sx.length; i++) {
            float local = Math.max(
                    0f,
                    Math.min(1f, prog * 1.35f - sp[i] * .35f)
            );

            if (local <= 0) continue;

            float px = x + sx[i] * r * (.55f + local);
            float py = y + sy[i] * r * (.55f + local);
            float wave = (float)Math.sin(Math.PI * local);

            p.setColor(sc[i % sc.length]);
            p.setAlpha((int)(255 * wave));
            c.drawCircle(px, py, dp(2.5f + 4f * wave), p);
        }

        p.setAlpha(255);
    }

    private void drawReset(Canvas c, int board) {
        p.setColor(0xFFF4F4F4);
        p.setShadowLayer(dp(3), 0, dp(1), 0x33000000);
        c.drawCircle(resetCx[board], resetCy[board], resetR, p);
        p.clearShadowLayer();

        text.setColor(Color.rgb(85,85,85));
        text.setTextSize(resetR * 1.25f);

        Paint.FontMetrics fm = text.getFontMetrics();
        float base = resetCy[board] - (fm.ascent + fm.descent) / 2f;
        float rot = 0;

        long e = SystemClock.uptimeMillis() - resetStart[board];

        if (resetStart[board] != 0 && e < 420) {
            rot = 360f * e / 420f;
        }

        c.save();
        c.rotate(rot, resetCx[board], resetCy[board]);
        c.drawText("↻", resetCx[board], base, text);
        c.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_UP) return true;

        float x = ev.getX();
        float y = ev.getY();

        int board = y < halfH ? 0 : 1;

        float dx = x - resetCx[board];
        float dy = y - resetCy[board];

        if (dx*dx + dy*dy <= resetR*resetR*1.8f) {
            for (int i=0; i<N; i++) selected[board][i] = false;

            rewardActive[board] = false;
            rewardStart[board] = 0;
            resetStart[board] = SystemClock.uptimeMillis();

            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            invalidate();
            return true;
        }

        for (int i=0; i<N; i++) {
            dx = x - cx[board][i];
            dy = y - cy[board];

            if (dx*dx + dy*dy <= radius*radius*1.25f) {
                boolean was = rewardActive[board];

                selected[board][i] = !selected[board][i];

                boolean now = allSelected(board);

                if (now && !was) {
                    rewardActive[board] = true;
                    rewardStart[board] = SystemClock.uptimeMillis();
                } else if (!now) {
                    rewardActive[board] = false;
                    rewardStart[board] = 0;
                }

                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                invalidate();
                return true;
            }
        }

        return true;
    }

    private boolean allSelected(int board) {
        for (boolean b : selected[board]) {
            if (!b) return false;
        }
        return true;
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
