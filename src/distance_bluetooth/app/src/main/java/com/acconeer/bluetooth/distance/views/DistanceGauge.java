package com.acconeer.bluetooth.distance.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.acconeer.bluetooth.distance.utils.Utils;
import com.acconeer.bluetooth.distance.R;

import java.text.DecimalFormat;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class DistanceGauge extends View {
    private static final int INTERNAL_UPDOWN_PADDING = 15;
    private static final double SMALL_TO_BIG_RATIO = 0.7;
    private static final int DEFAULT_NUM_BIG_SECTIONS = 7;
    private static final int DEFAULT_NUM_SMALL_SECTIONS = 10;
    private static final int LABEL_MARGIN = 10;
    private static final int NOT_SET = -1;

    private static final String MEASUREMENT_UNIT = "mm";

    private int bigWidth;
    private int smallWidth;
    private int numBigSections;
    private int numSmallSections;
    private float sectionHeight;
    private int numSections;
    private int height;
    private float smallOffset;
    private int range;
    private int labelWidth;

    private int start;
    private int end;
    private int level;

    private float levelDrawn;

    private Paint smallSectionPaint;
    private Paint bigSectionPaint;
    private Paint levelPaint;
    private Paint labelPaint;

    private ValueAnimator animator;

    private DecimalFormat format;

    public DistanceGauge(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.DistanceGauge, 0, 0);

        try {
            start = attributes.getInt(R.styleable.DistanceGauge_rangeStart, NOT_SET);
            end = attributes.getInt(R.styleable.DistanceGauge_rangeEnd, NOT_SET);
            numBigSections = attributes
                    .getInt(R.styleable.DistanceGauge_numBigSections, DEFAULT_NUM_BIG_SECTIONS);
            numSmallSections = attributes
                    .getInt(R.styleable.DistanceGauge_numSmallSections, DEFAULT_NUM_SMALL_SECTIONS);
        } finally {
            attributes.recycle();
        }

        init();
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
        this.level = start;
        this.levelDrawn = start;

        invalidate();
        requestLayout();
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
        this.level = start;
        this.levelDrawn = start;

        invalidate();
        requestLayout();
    }

    public int getLevel() {
        return level;
    }

    private void init() {
        smallSectionPaint = new Paint();
        smallSectionPaint.setColor(Color.BLACK);
        smallSectionPaint.setStrokeWidth(Utils.dpToPx(1, getContext()));

        bigSectionPaint = new Paint();
        bigSectionPaint.setColor(Color.BLACK);
        bigSectionPaint.setStrokeWidth(Utils.dpToPx(1.5f, getContext()));

        levelPaint = new Paint();
        levelPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        levelPaint.setAlpha(60);

        labelPaint = new Paint();
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
        labelPaint.setTextSize(Utils.spToPx(13, getContext()));

        format = new DecimalFormat("#");
        format.setNegativePrefix(""); //To get rid of -0
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        numSections = numBigSections * numSmallSections;
        range = end - start;

        level = (int) (levelDrawn = start);
        labelWidth = (int) getLongestLabelLength();

        bigWidth = w - getPaddingLeft() - getPaddingRight() - LABEL_MARGIN - labelWidth;
        smallWidth = (int) (SMALL_TO_BIG_RATIO * bigWidth);

        height = h - getPaddingTop() - getPaddingBottom() - INTERNAL_UPDOWN_PADDING * 2;

        sectionHeight = height / (float) numSections;
        smallOffset = (bigWidth - smallWidth) / 2;
    }

    private float getLongestLabelLength() {
        if (start == NOT_SET) {
            return minimalTextWidth("");
        }
        float longest = 0;
        float increment = range / (float) numBigSections;
        float currentLabel = start;

        for (int i = 0; i <= numBigSections; i++, currentLabel += increment) {
            float currentLength = minimalTextWidth(format.format(currentLabel) + " " + MEASUREMENT_UNIT);
            if (currentLength > longest) {
                longest = currentLength;
            }
        }

        return longest;
    }

    private float minimalTextWidth(String text) {
        return Math.max(labelPaint.measureText("???? mm"), labelPaint.measureText(text));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int sectionCounter = 0;
        float increment = range / (float) numBigSections;
        float currentLabel = end; //We are drawing from the top

        for (float pos = INTERNAL_UPDOWN_PADDING; (int) pos <= height + INTERNAL_UPDOWN_PADDING; pos += sectionHeight) {
            if (sectionCounter++ % numSmallSections == 0) {
                canvas.drawLine(0, pos, bigWidth, pos, bigSectionPaint);
                if (start == NOT_SET) {
                    canvas.drawText("?",bigWidth + LABEL_MARGIN, pos + 10, labelPaint);
                } else {
                    canvas.drawText(format.format(currentLabel) + " " + MEASUREMENT_UNIT,
                            bigWidth + LABEL_MARGIN, pos + 10, labelPaint);
                }

                currentLabel -= increment;
            } else {
                canvas.drawLine(smallOffset, pos, smallWidth + smallOffset,
                        pos, smallSectionPaint);
            }
        }

        float percentLevel = (levelDrawn - start) / range;
        canvas.drawRect(0, (int) (height - height * percentLevel) + INTERNAL_UPDOWN_PADDING,
                bigWidth, height + INTERNAL_UPDOWN_PADDING, levelPaint);
    }

    public void setLevel(int level) {
        if (start == NOT_SET || end == NOT_SET) {
            throw new IllegalArgumentException("Set start and end before the value!");
        }
        level = normalizeLevel(level);
        this.levelDrawn = this.level;
        startAnimation(levelDrawn, level, 1000);
        this.level = level;

        invalidate();
        requestLayout();
    }

    private int normalizeLevel(int level) {
        return level < start ? start : level > end ? end : level;
    }

    private void startAnimation(float startLevel, float goalLevel, long time) {
        if (animator != null) {
            this.level  = (int) startLevel;

            animator.end();
        }

        animator = ValueAnimator.ofFloat(startLevel, goalLevel);
        animator.setDuration(time);
        animator.addUpdateListener(valueAnimator -> {
            levelDrawn = (float) valueAnimator.getAnimatedValue();

            invalidate();
        });

        animator.start();
    }
}
