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
    private static final int CHILDREN = 2;
    private static final int EARS = 5;

    private final int[] progress = new int[CHILDREN];
    private final boolean[] isGirl = new boolean[] { true, false };

    private final float[] childCx = new float[CHILDREN];
    private final float[] avatarCy = new float[CHILDREN];
    private final float[] toggleCy = new float[CHILDREN];
    private final float[][] earCy = new float[CHILDREN][EARS];
    private final float[] giftCy = new float[CHILDREN];
    private final float[] resetCy = new float[CHILDREN];

    private float columnWidth;
    private float topDividerY;
    private float earRadius;
    private float avatarRadius;
    private float toggleW;
    private float toggleH;
    private float giftW;
    private float giftH;
    private float resetR;

    private final boolean[] rewardActive = new boolean[CHILDREN];
    private final long[] rewardStart = new long[CHILDREN];
    private final long[] resetStart = new long[CHILDREN];

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Random random = new Random(7);
    private final float[] sx = new float[28], sy = new float[28], sp = new float[28];

    private final int[] colors = {
            Color.rgb(102,190,232),
            Color.rgb(105,194,155),
            Color.rgb(132,91,188),
            Color.rgb(248,196,71),
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        columnWidth = w / 2f;
        topDividerY = h * .205f;

        avatarRadius = Math.min(columnWidth * .155f, h * .058f);
        toggleW = columnWidth * .48f;
        toggleH = Math.max(dp(30), h * .027f);

        earRadius = Math.min(columnWidth * .135f, h * .044f);
        giftW = Math.min(columnWidth * .35f, h * .105f);
        giftH = giftW * .76f;
        resetR = Math.max(dp(22), Math.min(columnWidth * .055f, h * .024f));

        for (int c=0; c<CHILDREN; c++) {
            childCx[c] = columnWidth * (c + .5f);
            avatarCy[c] = h * .073f;
            toggleCy[c] = h * .154f;

            float first = h * .292f;
            float step = h * .113f;
            for (int i=0; i<EARS; i++) {
                earCy[c][i] = first + step * i;
            }

            giftCy[c] = h * .875f;
            resetCy[c] = h * .963f;
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        drawDividers(c);

        for (int child=0; child<CHILDREN; child++) {
            drawChildAvatar(c, child);
            drawGenderToggle(c, child);

            for (int i=0; i<EARS; i++) {
                drawEarToken(c, child, i);
            }

            drawGift(c, child);
            drawReset(c, child);
        }

        long now = SystemClock.uptimeMillis();
        boolean animate = false;

        for (int child=0; child<CHILDREN; child++) {
            if (rewardActive[child] && now - rewardStart[child] < 1500) animate = true;
            if (resetStart[child] != 0 && now - resetStart[child] < 420) animate = true;
        }

        if (animate) postInvalidateOnAnimation();
    }

    private void drawDividers(Canvas c) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(225,225,225));

        c.drawRect(
                0,
                topDividerY - dp(1),
                getWidth(),
                topDividerY + dp(1),
                p
        );

        c.drawRect(
                getWidth() / 2f - dp(1),
                topDividerY,
                getWidth() / 2f + dp(1),
                getHeight(),
                p
        );
    }

    private void drawChildAvatar(Canvas c, int child) {
        float x = childCx[child];
        float y = avatarCy[child];
        float r = avatarRadius;

        int shirt = child == 0
                ? Color.rgb(237,112,167)
                : Color.rgb(102,190,232);

        p.setStyle(Paint.Style.FILL);

        // shirt / shoulders
        p.setColor(shirt);
        c.drawOval(new RectF(
                x-r*.72f,
                y+r*.63f,
                x+r*.72f,
                y+r*1.23f
        ), p);

        // neck
        p.setColor(Color.rgb(229,171,130));
        c.drawRoundRect(
                new RectF(x-r*.18f, y+r*.43f, x+r*.18f, y+r*.80f),
                r*.10f, r*.10f, p
        );

        // ears
        c.drawCircle(x-r*.73f, y, r*.19f, p);
        c.drawCircle(x+r*.73f, y, r*.19f, p);

        // face
        p.setColor(Color.rgb(242,187,148));
        c.drawCircle(x, y, r*.74f, p);

        // hair base
        int hair = isGirl[child]
                ? Color.rgb(108,72,48)
                : Color.rgb(74,57,45);
        p.setColor(hair);

        if (isGirl[child]) {
            // soft cap of hair
            Path hairTop = new Path();
            hairTop.moveTo(x-r*.70f, y-r*.08f);
            hairTop.cubicTo(
                    x-r*.63f, y-r*.72f,
                    x-r*.16f, y-r*.92f,
                    x+r*.12f, y-r*.84f
            );
            hairTop.cubicTo(
                    x+r*.55f, y-r*.81f,
                    x+r*.72f, y-r*.48f,
                    x+r*.70f, y-r*.02f
            );
            hairTop.cubicTo(
                    x+r*.44f, y-r*.30f,
                    x+r*.22f, y-r*.40f,
                    x-r*.02f, y-r*.37f
            );
            hairTop.cubicTo(
                    x-r*.28f, y-r*.41f,
                    x-r*.46f, y-r*.30f,
                    x-r*.70f, y-r*.08f
            );
            c.drawPath(hairTop, p);

            // pigtails
            c.drawCircle(x-r*.82f, y-r*.05f, r*.27f, p);
            c.drawCircle(x+r*.82f, y-r*.05f, r*.27f, p);

            // hair bands
            p.setColor(Color.rgb(237,112,167));
            c.drawCircle(x-r*.64f, y-r*.13f, r*.10f, p);
            c.drawCircle(x+r*.64f, y-r*.13f, r*.10f, p);
        } else {
            // boy's tousled hair
            Path hairTop = new Path();
            hairTop.moveTo(x-r*.70f, y-r*.06f);
            hairTop.lineTo(x-r*.62f, y-r*.55f);
            hairTop.lineTo(x-r*.36f, y-r*.43f);
            hairTop.lineTo(x-r*.22f, y-r*.78f);
            hairTop.lineTo(x+r*.02f, y-r*.52f);
            hairTop.lineTo(x+r*.23f, y-r*.82f);
            hairTop.lineTo(x+r*.35f, y-r*.49f);
            hairTop.lineTo(x+r*.64f, y-r*.61f);
            hairTop.lineTo(x+r*.70f, y-r*.08f);
            hairTop.cubicTo(
                    x+r*.28f, y-r*.35f,
                    x-r*.30f, y-r*.35f,
                    x-r*.70f, y-r*.06f
            );
            c.drawPath(hairTop, p);
        }

        // eyes
        p.setColor(Color.rgb(70,60,55));
        c.drawCircle(x-r*.25f, y+r*.02f, r*.045f, p);
        c.drawCircle(x+r*.25f, y+r*.02f, r*.045f, p);

        // smile
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(dp(2), r*.045f));
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(Color.rgb(110,75,62));

        RectF smile = new RectF(
                x-r*.24f,
                y+r*.02f,
                x+r*.24f,
                y+r*.38f
        );
        c.drawArc(smile, 15, 150, false, p);

        p.setStyle(Paint.Style.FILL);
    }

    private void drawGenderToggle(Canvas c, int child) {
        float x = childCx[child];
        float y = toggleCy[child];

        RectF outer = new RectF(
                x-toggleW/2,
                y-toggleH/2,
                x+toggleW/2,
                y+toggleH/2
        );

        p.setColor(Color.rgb(239,239,239));
        p.setStyle(Paint.Style.FILL);
        c.drawRoundRect(outer, toggleH/2, toggleH/2, p);

        float half = toggleW / 2f;

        RectF selectedHalf;
        if (isGirl[child]) {
            selectedHalf = new RectF(
                    x-toggleW/2,
                    y-toggleH/2,
                    x-toggleW/2+half,
                    y+toggleH/2
            );
            p.setColor(Color.rgb(252,220,234));
        } else {
            selectedHalf = new RectF(
                    x,
                    y-toggleH/2,
                    x+toggleW/2,
                    y+toggleH/2
            );
            p.setColor(Color.rgb(216,239,250));
        }
        c.drawRoundRect(selectedHalf, toggleH/2, toggleH/2, p);

        text.setTextSize(toggleH*.48f);
        Paint.FontMetrics fm = text.getFontMetrics();
        float base = y - (fm.ascent + fm.descent)/2f;

        text.setColor(isGirl[child] ? Color.rgb(190,70,125) : Color.rgb(135,135,135));
        c.drawText("Girl", x-toggleW*.25f, base, text);

        text.setColor(!isGirl[child] ? Color.rgb(55,135,175) : Color.rgb(135,135,135));
        c.drawText("Boy", x+toggleW*.25f, base, text);
    }

    private void drawEarToken(Canvas c, int child, int index) {
        float x = childCx[child];
        float y = earCy[child][index];

        boolean active = index < progress[child];
        int color = active ? colors[index] : Color.rgb(222,222,222);

        if (active) {
            drawRays(c, x, y, earRadius, colors[index]);
        }

        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        c.drawCircle(x, y, earRadius, p);

        text.setTextSize(earRadius*.46f);
        text.setColor(active ? colors[index] : Color.rgb(120,120,120));

        Paint.FontMetrics fm = text.getFontMetrics();
        float base = y - earRadius*1.28f - (fm.ascent + fm.descent)/2f;
        c.drawText(String.valueOf(index + 1), x, base, text);

        drawEar(c, x, y + earRadius*.02f, earRadius*.73f);
    }

    private void drawRays(Canvas c, float x, float y, float r, int color) {
        p.setStyle(Paint.Style.STROKE);
        p.setColor(color);
        p.setStrokeWidth(Math.max(dp(2.5f), r*.052f));
        p.setStrokeCap(Paint.Cap.ROUND);

        for (int k=0; k<12; k++) {
            double a = k * Math.PI * 2 / 12.0;
            c.drawLine(
                    x + (float)Math.cos(a)*r*1.15f,
                    y + (float)Math.sin(a)*r*1.15f,
                    x + (float)Math.cos(a)*r*1.36f,
                    y + (float)Math.sin(a)*r*1.36f,
                    p
            );
        }

        p.setStyle(Paint.Style.FILL);
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
        p.setStrokeWidth(s*.075f);
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

    private void drawGift(Canvas c, int child) {
        float gx = childCx[child];
        float gy = giftCy[child];

        long e = SystemClock.uptimeMillis() - rewardStart[child];
        boolean animate = rewardActive[child] && e < 1500;
        float alpha = rewardActive[child] ? 1f : .34f;

        c.save();

        if (animate) {
            float settle = 1f - Math.min(1f, e/1500f);
            float bob = (float)Math.sin(e/1000f*Math.PI*7) * dp(8) * settle;
            float angle = (float)Math.sin(e/1000f*Math.PI*5) * 6f * settle;

            c.translate(0, bob);
            c.rotate(angle, gx, gy);

            float pop = 1f + .11f *
                    (float)Math.sin(Math.min(1f, e/500f)*Math.PI);
            c.scale(pop, pop, gx, gy);
        }

        p.setAlpha((int)(255*alpha));

        p.setColor(Color.rgb(248,190,55));
        c.drawRoundRect(
                new RectF(
                        gx-giftW/2,
                        gy-giftH/2,
                        gx+giftW/2,
                        gy+giftH/2
                ),
                dp(12),
                dp(12),
                p
        );

        p.setColor(Color.rgb(235,99,154));
        c.drawRect(
                gx-giftW*.09f,
                gy-giftH/2,
                gx+giftW*.09f,
                gy+giftH/2,
                p
        );

        c.drawRect(
                gx-giftW/2,
                gy-giftH*.10f,
                gx+giftW/2,
                gy+giftH*.10f,
                p
        );

        c.drawOval(
                new RectF(
                        gx-giftW*.31f,
                        gy-giftH*.73f,
                        gx-giftW*.02f,
                        gy-giftH*.43f
                ),
                p
        );

        c.drawOval(
                new RectF(
                        gx+giftW*.02f,
                        gy-giftH*.73f,
                        gx+giftW*.31f,
                        gy-giftH*.43f
                ),
                p
        );

        p.setAlpha(255);
        c.restore();

        if (animate) drawSparkles(c, gx, gy, giftW, e);
    }

    private void drawSparkles(Canvas c, float x, float y, float r, long e) {
        float prog = Math.min(1f, e/1350f);

        int[] sc = {
                0xFFFFC62E,
                0xFFF36FA6,
                0xFF64BEEA,
                0xFF8B55C5
        };

        for (int i=0; i<sx.length; i++) {
            float local = Math.max(
                    0f,
                    Math.min(1f, prog*1.35f - sp[i]*.35f)
            );

            if (local <= 0) continue;

            float px = x + sx[i]*r*(.55f + local);
            float py = y + sy[i]*r*(.55f + local);
            float wave = (float)Math.sin(Math.PI*local);

            p.setColor(sc[i % sc.length]);
            p.setAlpha((int)(255*wave));
            c.drawCircle(px, py, dp(2.5f + 4f*wave), p);
        }

        p.setAlpha(255);
    }

    private void drawReset(Canvas c, int child) {
        float x = childCx[child];
        float y = resetCy[child];

        p.setColor(0xFFF4F4F4);
        p.setShadowLayer(dp(3), 0, dp(1), 0x33000000);
        c.drawCircle(x, y, resetR, p);
        p.clearShadowLayer();

        text.setColor(Color.rgb(85,85,85));
        text.setTextSize(resetR*1.25f);

        Paint.FontMetrics fm = text.getFontMetrics();
        float base = y - (fm.ascent + fm.descent)/2f;

        float rot = 0;
        long e = SystemClock.uptimeMillis() - resetStart[child];

        if (resetStart[child] != 0 && e < 420) {
            rot = 360f * e / 420f;
        }

        c.save();
        c.rotate(rot, x, y);
        c.drawText("↻", x, base, text);
        c.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_UP) return true;

        float x = ev.getX();
        float y = ev.getY();

        int child = x < getWidth()/2f ? 0 : 1;

        // Gender toggle
        float tx = childCx[child];
        float ty = toggleCy[child];

        if (x >= tx-toggleW/2 &&
                x <= tx+toggleW/2 &&
                y >= ty-toggleH*.80f &&
                y <= ty+toggleH*.80f) {

            isGirl[child] = !isGirl[child];
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            invalidate();
            return true;
        }

        // Reset
        float dx = x - childCx[child];
        float dy = y - resetCy[child];

        if (dx*dx + dy*dy <= resetR*resetR*2.0f) {
            progress[child] = 0;
            rewardActive[child] = false;
            rewardStart[child] = 0;
            resetStart[child] = SystemClock.uptimeMillis();

            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            invalidate();
            return true;
        }

        // Ears: cumulative progress
        for (int i=0; i<EARS; i++) {
            dx = x - childCx[child];
            dy = y - earCy[child][i];

            if (dx*dx + dy*dy <= earRadius*earRadius*1.35f) {
                int newProgress;

                // Tapping the current last active ear steps back by one.
                if (progress[child] == i + 1) {
                    newProgress = i;
                } else {
                    newProgress = i + 1;
                }

                boolean wasComplete = progress[child] == EARS;
                progress[child] = newProgress;
                boolean nowComplete = progress[child] == EARS;

                if (nowComplete && !wasComplete) {
                    rewardActive[child] = true;
                    rewardStart[child] = SystemClock.uptimeMillis();
                } else if (!nowComplete) {
                    rewardActive[child] = false;
                    rewardStart[child] = 0;
                }

                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                invalidate();
                return true;
            }
        }

        return true;
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
