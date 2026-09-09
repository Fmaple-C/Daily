package com.maple.daily.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;

public class MaplePetView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();

    private int level = 1;
    private int primary = Color.rgb(222, 102, 38);
    private int primaryDark = Color.rgb(145, 70, 28);
    private int text = Color.rgb(42, 40, 37);
    private int surface = Color.WHITE;
    private int muted = Color.rgb(126, 119, 110);
    private boolean animating;
    private long celebrationStartedAt;

    public MaplePetView(Context context) {
        super(context);
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
        invalidate();
    }

    public void setPalette(int primary, int primaryDark, int text, int surface, int muted) {
        this.primary = primary;
        this.primaryDark = primaryDark;
        this.text = text;
        this.surface = surface;
        this.muted = muted;
        invalidate();
    }

    public void celebrate() {
        celebrationStartedAt = SystemClock.uptimeMillis();
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animating = true;
        postInvalidateDelayed(40L);
    }

    @Override
    protected void onDetachedFromWindow() {
        animating = false;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = SystemClock.uptimeMillis();
        int stage = stageForLevel(level);
        float unit = Math.min(getWidth() / 96f, getHeight() / 82f);
        float breath = (float) Math.sin(now / 520f) * 1.2f * unit;
        float bounce = celebrationBounce(now) * unit;

        canvas.save();
        canvas.translate(getWidth() / 2f, getHeight() * 0.76f - bounce);
        drawShadow(canvas, unit, bounce);
        if (stage == 1) {
            drawSeedling(canvas, unit, breath, now);
        } else {
            drawCompanion(canvas, unit, breath, now, stage);
        }
        drawCelebration(canvas, unit, now);
        canvas.restore();

        if (animating) {
            postInvalidateDelayed(40L);
        }
    }

    private void drawShadow(Canvas canvas, float unit, float bounce) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(muted, bounce > 1f ? 32 : 48));
        rect.set(-25f * unit, -3f * unit, 25f * unit, 5f * unit);
        canvas.drawOval(rect, paint);
    }

    private void drawSeedling(Canvas canvas, float unit, float breath, long now) {
        float bodyTop = -46f * unit - breath;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(primary);
        rect.set(-20f * unit, bodyTop, 20f * unit, -3f * unit);
        canvas.drawOval(rect, paint);

        paint.setColor(mix(primary, surface, 34));
        rect.set(-13f * unit, bodyTop + 7f * unit, 4f * unit, bodyTop + 22f * unit);
        canvas.drawOval(rect, paint);

        drawFace(canvas, 0f, bodyTop + 27f * unit, unit, now);

        paint.setColor(primaryDark);
        paint.setStrokeWidth(2.4f * unit);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(0, bodyTop + 1f * unit, 0, bodyTop - 9f * unit, paint);
        paint.setStyle(Paint.Style.FILL);
        drawLeaf(canvas, -7f * unit, bodyTop - 11f * unit, 0.72f * unit, primaryDark, -18f);
        drawLeaf(canvas, 7f * unit, bodyTop - 10f * unit, 0.68f * unit, primary, 24f);
    }

    private void drawCompanion(Canvas canvas, float unit, float breath, long now, int stage) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(primaryDark, 205));
        if (stage >= 3) {
            rect.set(19f * unit, -35f * unit, 41f * unit, -5f * unit);
            canvas.drawOval(rect, paint);
            paint.setColor(surface);
            rect.set(18f * unit, -34f * unit, 33f * unit, -10f * unit);
            canvas.drawOval(rect, paint);
        }

        paint.setColor(primary);
        rect.set(-22f * unit, -42f * unit - breath, 22f * unit, -3f * unit);
        canvas.drawRoundRect(rect, 19f * unit, 19f * unit, paint);

        path.reset();
        path.moveTo(-18f * unit, -36f * unit - breath);
        path.lineTo(-14f * unit, -57f * unit - breath);
        path.lineTo(-2f * unit, -43f * unit - breath);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();
        path.moveTo(18f * unit, -36f * unit - breath);
        path.lineTo(14f * unit, -57f * unit - breath);
        path.lineTo(2f * unit, -43f * unit - breath);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(mix(primary, surface, 48));
        rect.set(-15f * unit, -37f * unit - breath, 15f * unit, -12f * unit);
        canvas.drawOval(rect, paint);
        drawFace(canvas, 0, -25f * unit - breath, unit, now);

        if (stage >= 3) {
            paint.setColor(primaryDark);
            rect.set(-22f * unit, -15f * unit, 22f * unit, -9f * unit);
            canvas.drawRoundRect(rect, 3f * unit, 3f * unit, paint);
            drawLeaf(canvas, 12f * unit, -8f * unit, 0.56f * unit, primaryDark, 18f);
        }
        if (stage >= 4) {
            drawLeaf(canvas, 0, -60f * unit - breath, 0.72f * unit, primaryDark, 0);
            drawLeaf(canvas, -10f * unit, -55f * unit - breath, 0.52f * unit, primary, -35f);
            drawLeaf(canvas, 10f * unit, -55f * unit - breath, 0.52f * unit, primary, 35f);
            paint.setColor(mix(primary, surface, 60));
            canvas.drawCircle(-13f * unit, -20f * unit - breath, 2.2f * unit, paint);
            canvas.drawCircle(13f * unit, -20f * unit - breath, 2.2f * unit, paint);
        }
    }

    private void drawFace(Canvas canvas, float centerX, float centerY, float unit, long now) {
        float blinkPhase = now % 4300L;
        float eyeHeight = blinkPhase > 3880L && blinkPhase < 4050L ? 0.7f : 3.7f;
        paint.setColor(text);
        paint.setStyle(Paint.Style.FILL);
        rect.set(centerX - 10f * unit, centerY - eyeHeight * unit,
                centerX - 6f * unit, centerY + eyeHeight * unit);
        canvas.drawOval(rect, paint);
        rect.set(centerX + 6f * unit, centerY - eyeHeight * unit,
                centerX + 10f * unit, centerY + eyeHeight * unit);
        canvas.drawOval(rect, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.7f * unit);
        paint.setStrokeCap(Paint.Cap.ROUND);
        path.reset();
        path.moveTo(centerX - 3f * unit, centerY + 6f * unit);
        path.quadTo(centerX, centerY + 9f * unit, centerX + 3f * unit, centerY + 6f * unit);
        canvas.drawPath(path, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawLeaf(Canvas canvas, float x, float y, float scale, int color, float rotation) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(rotation);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        path.reset();
        path.moveTo(0, -12f * scale);
        path.lineTo(3f * scale, -5f * scale);
        path.lineTo(9f * scale, -7f * scale);
        path.lineTo(6f * scale, -1f * scale);
        path.lineTo(11f * scale, 2f * scale);
        path.lineTo(4f * scale, 3f * scale);
        path.lineTo(4f * scale, 10f * scale);
        path.lineTo(0, 6f * scale);
        path.lineTo(-4f * scale, 10f * scale);
        path.lineTo(-4f * scale, 3f * scale);
        path.lineTo(-11f * scale, 2f * scale);
        path.lineTo(-6f * scale, -1f * scale);
        path.lineTo(-9f * scale, -7f * scale);
        path.lineTo(-3f * scale, -5f * scale);
        path.close();
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    private void drawCelebration(Canvas canvas, float unit, long now) {
        long elapsed = now - celebrationStartedAt;
        if (celebrationStartedAt == 0L || elapsed < 0L || elapsed > 900L) {
            return;
        }
        float progress = elapsed / 900f;
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * 2 * i / 6d - Math.PI / 2d;
            float radius = (18f + 28f * progress) * unit;
            float x = (float) Math.cos(angle) * radius;
            float y = -29f * unit + (float) Math.sin(angle) * radius;
            paint.setColor(withAlpha(i % 2 == 0 ? primary : primaryDark, (int) (210 * (1f - progress))));
            canvas.drawCircle(x, y, (2.8f - 1.2f * progress) * unit, paint);
        }
    }

    private float celebrationBounce(long now) {
        long elapsed = now - celebrationStartedAt;
        if (celebrationStartedAt == 0L || elapsed < 0L || elapsed > 900L) {
            return 0f;
        }
        float progress = elapsed / 900f;
        return (float) Math.abs(Math.sin(progress * Math.PI * 2f)) * 8f * (1f - progress);
    }

    private int stageForLevel(int value) {
        if (value >= 15) {
            return 4;
        }
        if (value >= 8) {
            return 3;
        }
        if (value >= 4) {
            return 2;
        }
        return 1;
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)), Color.red(color), Color.green(color), Color.blue(color));
    }

    private int mix(int first, int second, int secondPercent) {
        int amount = Math.max(0, Math.min(100, secondPercent));
        int inverse = 100 - amount;
        return Color.rgb(
                (Color.red(first) * inverse + Color.red(second) * amount) / 100,
                (Color.green(first) * inverse + Color.green(second) * amount) / 100,
                (Color.blue(first) * inverse + Color.blue(second) * amount) / 100
        );
    }
}
