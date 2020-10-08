package com.acconeer.bluetooth.presence.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.acconeer.bluetooth.presence.R;
import com.acconeer.bluetooth.presence.util.Utils;

public class PresenceView extends View {
    private final int INTERNAL_UPDOWN_PADDING;
    private final float ARROW_HEAD_HEIGHT;
    private final float DISTANCE_ARROW_OFFSET;
    private static final float ARROW_HEIGHT_TO_WIDTH_RATIO = 0.4f;
    private final int DEFAULT_SENSOR_SIZE;

    private static final int DEFAULT_NUM_LINES = 6;
    private static final int DEFAULT_RANGE_START = 0;
    private static final int DEFAULT_RANGE_LENGTH = 1500;

    public static final int NONE = -1;

    private int numLines;
    private int presenceColor;
    private int rangeStart;
    private int rangeLength;
    private int imageSize;
    private BitmapDrawable sourceIcon;

    private Paint presencePaint;
    private Paint arcPaint;
    private Paint textPaint;
    private Paint rangeLinePaint;
    private Paint rangeArrowPaint;
    private Paint distancePointerPaint;

    private float angle, startAngle;
    private float sectionHeight;
    private float centerX, centerY;
    private float halfTextHeight;

    private RectF oval;
    private Path triangleHeadPath;

    private int activeSection = NONE;

    public PresenceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        INTERNAL_UPDOWN_PADDING = Utils.dpToPx(10, context);
        ARROW_HEAD_HEIGHT = Utils.dpToPx(15, context);
        DISTANCE_ARROW_OFFSET = Utils.dpToPx(5, context);
        DEFAULT_SENSOR_SIZE = Utils.dpToPx(30, context);

        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.PresenceView, 0, 0);

        try {
            numLines = a.getInt(R.styleable.PresenceView_num_lines, DEFAULT_NUM_LINES);
            presenceColor = a.getColor(R.styleable.PresenceView_presence_color, context.getResources()
                    .getColor(R.color.colorPrimary));
            rangeStart = a.getInt(R.styleable.PresenceView_range_start, DEFAULT_RANGE_START);
            rangeLength = a.getInt(R.styleable.PresenceView_range_length, DEFAULT_RANGE_LENGTH);
            sourceIcon = (BitmapDrawable) a.getDrawable(R.styleable.PresenceView_src_icon);
            imageSize = a.getDimensionPixelSize(R.styleable.PresenceView_src_size, DEFAULT_SENSOR_SIZE);
        } finally {
            a.recycle();
        }

        init();
    }

    public void setSection(int section) {
        if (section < 0) {
            activeSection = NONE;
        } else {
            activeSection = section < 0 ? 0 : section > numLines ? numLines : section;
        }

        invalidate();
    }

    public void setLevel(int level) {
        if (level < 0) {
            activeSection = NONE;
        } else {
            level -= rangeStart;
            level = level < 0 ? 0 : level > rangeLength ? rangeLength : level;

            float sectionHeight = rangeLength / (float) numLines;

            activeSection = (int) Math.floor(level / sectionHeight);
            Log.d("PV", "Active section: " + activeSection);
        }

        invalidate();
    }

    public void setNumZones(int num) {
        numLines = num < 1 ? 1 : num;

        invalidate();
    }

    public void setStart(int start) {
        rangeStart = start;

        invalidate();
    }

    public void setLength(int length) {
        rangeLength = length;

        invalidate();
    }

    private void init() {
        presencePaint = new Paint();
        presencePaint.setColor(presenceColor);
        presencePaint.setStrokeCap(Paint.Cap.BUTT);
        presencePaint.setStyle(Paint.Style.STROKE);
        presencePaint.setAlpha(60);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.BLACK);
        arcPaint.setStrokeWidth(Utils.dpToPx(2f, getContext()));
        arcPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(Utils.spToPx(13, getContext()));

        rangeLinePaint = new Paint();
        rangeLinePaint.setColor(Color.BLACK);
        rangeLinePaint.setStyle(Paint.Style.STROKE);
        rangeLinePaint.setStrokeWidth(3);
        rangeLinePaint.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));

        rangeArrowPaint = new Paint();
        rangeArrowPaint.setColor(Color.BLACK);
        rangeArrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        distancePointerPaint = new Paint();
        distancePointerPaint.setColor(getContext().getResources().getColor(R.color.colorPrimary));
        distancePointerPaint.setAlpha((int) (255 * 0.6));
        distancePointerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        Rect r = new Rect();
        textPaint.getTextBounds("1234567890", 0 , 10, r);
        halfTextHeight = r.height() / 2.0f;

        triangleHeadPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float effectiveHeight = h - getPaddingBottom() - getPaddingTop() -
                2 * INTERNAL_UPDOWN_PADDING;
        float effectiveWidth = w - getPaddingStart() - getPaddingEnd();

        float radius = effectiveHeight - imageSize;
        sectionHeight = radius / numLines;
        angle = (float) Math.toDegrees(2 * Math.asin((effectiveWidth - 2 * INTERNAL_UPDOWN_PADDING) / 2 / radius));
        startAngle = 270 - angle / 2;

        centerX = effectiveWidth / 2;
        centerY = effectiveHeight + INTERNAL_UPDOWN_PADDING;

        oval = new RectF();

        presencePaint.setStrokeWidth(sectionHeight); //Thickness depends on the size
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawArcs(canvas);
        drawSensor(canvas);
        drawRangeArrow(canvas);
        drawRangeLabels(canvas);
        drawPresence(canvas);
    }

    private void drawRangeLabels(Canvas canvas) {
        int rangeIncrement = rangeLength / numLines;

        float currentLevel = (float) (numLines * sectionHeight - 0.5 * sectionHeight);
        for (int i = 0; i < numLines; i++, currentLevel -= sectionHeight) {
            drawArrowHead(canvas, distancePointerPaint, -90,
                    centerX, currentLevel + INTERNAL_UPDOWN_PADDING, DISTANCE_ARROW_OFFSET);
            canvas.drawText(Integer.toString(rangeStart + rangeIncrement / 2 + rangeIncrement * (i + 1)),
                    centerX + 2 * DISTANCE_ARROW_OFFSET + ARROW_HEAD_HEIGHT,
                    currentLevel + INTERNAL_UPDOWN_PADDING + halfTextHeight, textPaint);
        }
    }

    private void drawArcs(Canvas canvas) {
        float currentRadius = sectionHeight;

        for (int i = 0; i < numLines; i++) {
            float left = centerX - currentRadius;
            float right = centerX + currentRadius;
            float top = centerY - imageSize - currentRadius;
            float bottom = centerY - imageSize + currentRadius;
            oval.set(left, top, right, bottom);

            canvas.drawArc(oval, startAngle, angle, false, arcPaint);
            currentRadius += sectionHeight;
        }
    }

    private void drawSensor(Canvas canvas) {
        int offset = 3 * INTERNAL_UPDOWN_PADDING;
        oval.set(centerX - imageSize / 2.0f,
                centerY - imageSize / 2.0f - offset,
                centerX + imageSize / 2.0f,
                centerY + imageSize / 2.0f - offset);
        canvas.drawBitmap(sourceIcon.getBitmap(), null, oval, null);
    }

    private void drawRangeArrow(Canvas canvas) {
        canvas.drawLine(centerX, centerY - imageSize,
                centerX, INTERNAL_UPDOWN_PADDING, rangeLinePaint);
        drawArrowHead(canvas, rangeArrowPaint, 0, centerX, INTERNAL_UPDOWN_PADDING, 0);
    }

    private void drawPresence(Canvas canvas) {
        float currentRadius;
        if (activeSection != NONE) {
            currentRadius = (activeSection + 1) * sectionHeight - sectionHeight / 2;
            float left = centerX - currentRadius;
            float right = centerX + currentRadius;
            float top = centerY - imageSize - currentRadius;
            float bottom = centerY - imageSize + currentRadius;
            oval.set(left, top, right, bottom);

            canvas.drawArc(oval, startAngle, angle, false, presencePaint);
        }
    }

    private void drawArrowHead(Canvas canvas, Paint paint, int angle, float whatX, float whatY, float offset) {
        float arrowWidth = ARROW_HEAD_HEIGHT * ARROW_HEIGHT_TO_WIDTH_RATIO;
        triangleHeadPath.reset();

        triangleHeadPath.rMoveTo(0, offset);
        triangleHeadPath.lineTo(arrowWidth, ARROW_HEAD_HEIGHT + offset);
        triangleHeadPath.lineTo(-arrowWidth, ARROW_HEAD_HEIGHT + offset);
        triangleHeadPath.close();

        int matrixStackIndex = canvas.save();
        canvas.translate(whatX, whatY);
        canvas.rotate(angle);
        canvas.drawPath(triangleHeadPath, paint);
        canvas.restoreToCount(matrixStackIndex);
    }
}
