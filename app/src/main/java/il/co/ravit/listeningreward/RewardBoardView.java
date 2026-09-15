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
    private static final int N = 5;
    private final boolean[] selected = new boolean[N];
    private final float[] cx = new float[N];
    private float cy, radius, resetCx, resetCy, resetR;
    private boolean rewardActive;
    private long rewardStart, resetStart;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random(7);
    private final float[] sx = new float[28], sy = new float[28], sp = new float[28];
    private final int[] colors = {
            Color.rgb(102,190,232), Color.rgb(248,196,71), Color.rgb(237,112,167),
            Color.rgb(132,91,188), Color.rgb(105,194,155)
    };

    public RewardBoardView(Context context) {
        super(context);
        setBackgroundColor(Color.WHITE);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        text.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        text.setTextAlign(Paint.Align.CENTER);
        for (int i=0;i<sx.length;i++) {
            double a=random.nextDouble()*Math.PI*2; float d=.65f+random.nextFloat()*.55f;
            sx[i]=(float)Math.cos(a)*d; sy[i]=(float)Math.sin(a)*d; sp[i]=random.nextFloat();
        }
    }

    @Override protected void onSizeChanged(int w,int h,int ow,int oh) {
        float unit=Math.min(w/16f,h/8f); radius=unit*1.12f; cy=h*.53f;
        float start=w*.82f, gap=w*.145f; for(int i=0;i<N;i++) cx[i]=start-i*gap;
        resetCx=w*.50f; resetCy=h*.91f; resetR=Math.max(dp(24),unit*.34f);
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c); drawTitle(c);
        for(int i=0;i<N;i++) drawEarToken(c,i);
        drawGift(c); drawReset(c);
        if(rewardActive && SystemClock.uptimeMillis()-rewardStart<1500) postInvalidateOnAnimation();
        if(resetStart!=0 && SystemClock.uptimeMillis()-resetStart<420) postInvalidateOnAnimation();
    }

    private void drawTitle(Canvas c) {
        text.setColor(Color.rgb(96,96,96)); text.setTextSize(Math.min(getWidth()*.055f,getHeight()*.11f));
        c.drawText("אני מקשיב לצוות",getWidth()*.63f,getHeight()*.16f,text);
    }

    private void drawEarToken(Canvas c,int i) {
        float x=cx[i]; int bg=selected[i]?colors[i]:Color.rgb(222,222,222);
        if(selected[i]) drawRays(c,x,cy,radius,colors[i]);
        p.setStyle(Paint.Style.FILL); p.setColor(bg); c.drawCircle(x,cy,radius,p);
        text.setTextSize(radius*.50f); text.setColor(selected[i]?colors[i]:Color.rgb(122,122,122));
        c.drawText(String.valueOf(i+1),x,cy-radius*1.28f,text);
        drawEar(c,x,cy+radius*.02f,radius*.74f);
    }

    private void drawRays(Canvas c,float x,float y,float r,int color) {
        p.setColor(color); p.setStrokeWidth(Math.max(dp(3),r*.055f)); p.setStrokeCap(Paint.Cap.ROUND);
        for(int k=0;k<12;k++) { double a=k*Math.PI*2/12.0;
            c.drawLine(x+(float)Math.cos(a)*r*1.16f,y+(float)Math.sin(a)*r*1.16f,
                    x+(float)Math.cos(a)*r*1.38f,y+(float)Math.sin(a)*r*1.38f,p);
        }
    }

    private void drawEar(Canvas c,float x,float y,float s) {
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(229,171,130));
        Path o=new Path(); o.moveTo(x+s*.18f,y+s*.78f);
        o.cubicTo(x-s*.42f,y+s*.70f,x-s*.55f,y+s*.18f,x-s*.47f,y-s*.26f);
        o.cubicTo(x-s*.39f,y-s*.88f,x+s*.18f,y-s*1.02f,x+s*.50f,y-s*.55f);
        o.cubicTo(x+s*.74f,y-s*.18f,x+s*.58f,y+s*.05f,x+s*.36f,y+s*.30f);
        o.cubicTo(x+s*.14f,y+s*.57f,x+s*.43f,y+s*.80f,x+s*.18f,y+s*.78f); c.drawPath(o,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s*.075f); p.setStrokeCap(Paint.Cap.ROUND); p.setColor(Color.rgb(190,124,89));
        Path in=new Path(); in.moveTo(x+s*.18f,y-s*.45f);
        in.cubicTo(x-s*.14f,y-s*.70f,x-s*.35f,y-s*.35f,x-s*.25f,y-s*.05f);
        in.cubicTo(x-s*.18f,y+s*.17f,x+s*.18f,y+s*.02f,x+s*.15f,y+s*.28f);
        in.cubicTo(x+s*.10f,y+s*.52f,x-s*.08f,y+s*.50f,x-s*.12f,y+s*.40f); c.drawPath(in,p);
        p.setStyle(Paint.Style.FILL);
    }

    private void drawGift(Canvas c) {
        float gx=getWidth()*.105f, gy=getHeight()*.54f;
        float gw=Math.min(getWidth()*.135f,getHeight()*.30f), gh=gw*.78f;
        long e=SystemClock.uptimeMillis()-rewardStart;
        boolean animate=rewardActive && e<1500; float alpha=rewardActive?1f:.34f;
        c.save();
        if(animate) { float settle=1f-Math.min(1f,e/1500f);
            float bob=(float)Math.sin(e/1000f*Math.PI*7)*dp(8)*settle;
            float angle=(float)Math.sin(e/1000f*Math.PI*5)*6f*settle;
            c.translate(0,bob); c.rotate(angle,gx,gy);
            float pop=1f+.11f*(float)Math.sin(Math.min(1f,e/500f)*Math.PI); c.scale(pop,pop,gx,gy);
        }
        p.setAlpha((int)(255*alpha)); p.setColor(Color.rgb(248,190,55));
        c.drawRoundRect(new RectF(gx-gw/2,gy-gh/2,gx+gw/2,gy+gh/2),dp(12),dp(12),p);
        p.setColor(Color.rgb(235,99,154)); c.drawRect(gx-gw*.09f,gy-gh/2,gx+gw*.09f,gy+gh/2,p);
        c.drawRect(gx-gw/2,gy-gh*.10f,gx+gw/2,gy+gh*.10f,p);
        c.drawOval(new RectF(gx-gw*.31f,gy-gh*.73f,gx-gw*.02f,gy-gh*.43f),p);
        c.drawOval(new RectF(gx+gw*.02f,gy-gh*.73f,gx+gw*.31f,gy-gh*.43f),p); p.setAlpha(255); c.restore();
        if(animate) drawSparkles(c,gx,gy,gw,e);
    }

    private void drawSparkles(Canvas c,float x,float y,float r,long e) {
        float prog=Math.min(1f,e/1350f); int[] sc={0xFFFFC62E,0xFFF36FA6,0xFF64BEEA,0xFF8B55C5};
        for(int i=0;i<sx.length;i++) { float local=Math.max(0f,Math.min(1f,prog*1.35f-sp[i]*.35f)); if(local<=0) continue;
            float px=x+sx[i]*r*(.55f+local), py=y+sy[i]*r*(.55f+local), wave=(float)Math.sin(Math.PI*local);
            p.setColor(sc[i%sc.length]); p.setAlpha((int)(255*wave)); c.drawCircle(px,py,dp(2.5f+4f*wave),p);
        } p.setAlpha(255);
    }

    private void drawReset(Canvas c) {
        p.setColor(0xFFF4F4F4); p.setShadowLayer(dp(3),0,dp(1),0x33000000); c.drawCircle(resetCx,resetCy,resetR,p); p.clearShadowLayer();
        text.setColor(Color.rgb(85,85,85)); text.setTextSize(resetR*1.25f); Paint.FontMetrics fm=text.getFontMetrics();
        float base=resetCy-(fm.ascent+fm.descent)/2f, rot=0; long e=SystemClock.uptimeMillis()-resetStart;
        if(resetStart!=0 && e<420) rot=360f*e/420f; c.save(); c.rotate(rot,resetCx,resetCy); c.drawText("↻",resetCx,base,text); c.restore();
    }

    @Override public boolean onTouchEvent(MotionEvent ev) {
        if(ev.getAction()!=MotionEvent.ACTION_UP) return true; float x=ev.getX(),y=ev.getY(),dx=x-resetCx,dy=y-resetCy;
        if(dx*dx+dy*dy<=resetR*resetR*1.6f) { for(int i=0;i<N;i++) selected[i]=false; rewardActive=false; rewardStart=0; resetStart=SystemClock.uptimeMillis(); performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); invalidate(); return true; }
        for(int i=0;i<N;i++) { dx=x-cx[i]; dy=y-cy; if(dx*dx+dy*dy<=radius*radius*1.18f) {
            boolean was=rewardActive; selected[i]=!selected[i]; boolean now=allSelected();
            if(now&&!was){rewardActive=true;rewardStart=SystemClock.uptimeMillis();} else if(!now){rewardActive=false;rewardStart=0;}
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); invalidate(); return true;
        }} return true;
    }

    private boolean allSelected(){for(boolean b:selected)if(!b)return false;return true;}
    private float dp(float v){return v*getResources().getDisplayMetrics().density;}
}
