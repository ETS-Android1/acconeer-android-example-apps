package com.acconeer.bluetooth.distance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.acconeer.bluetooth.distance.R;
import com.acconeer.bluetooth.distance.utils.Utils;

import androidx.annotation.Nullable;

public class SignalStrengthIndicator extends View {
    private static final int INTERNAL_UPDOWN_PADDING = 15;
    private static final int DEFAULT_NUM_BARS = 6;
    private static final float SPACING_WIDTH_FACTOR = 0.6f;
    private static final float MIN_TO_MAX_FACTOR = 0.25f;
    private static final float HEIGHT_TO_WIDTH_FACTOR = 0.5f;

    private int numBars;
    private int level;

    private float maxBarHeight;
    private float minBarHeight;
    private float barIncrement;
    private float barWidth;
    private float barSpacing;

    private Paint barPaint;
    private Paint emptyBarPaint;

    public SignalStrengthIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.SignalStrengthIndicator, 0, 0);

        try {
            numBars = attributes.getInt(R.styleable.SignalStrengthIndicator_numBars, DEFAULT_NUM_BARS);
        } finally {
            attributes.recycle();
        }

        init();
    }

    public int getNumBars() {
        return numBars;
    }

    public void setNumBars(int numBars) {
        this.numBars = numBars;

        invalidate();
        requestLayout();
    }

    public int getLevel() {
        return level;
    }

    private void init() {
        barPaint = new Paint();
        barPaint.setColor(Color.BLACK);

        emptyBarPaint = new Paint();
        emptyBarPaint.setStyle(Paint.Style.STROKE);
        emptyBarPaint.setStrokeWidth(Utils.dpToPx(1.5f, getContext()));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        maxBarHeight = h - getPaddingTop() - getPaddingBottom();
        minBarHeight = maxBarHeight * MIN_TO_MAX_FACTOR;
        barWidth = minBarHeight * HEIGHT_TO_WIDTH_FACTOR;
        barIncrement = (maxBarHeight - minBarHeight) / numBars;

        barSpacing = barWidth * SPACING_WIDTH_FACTOR;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float currentBarX = getPaddingLeft() + barSpacing / 2;
        float currentTopOffset = getPaddingTop() + maxBarHeight - minBarHeight;

        for (int i = 0; i < numBars; i++, currentBarX += barSpacing + barWidth, currentTopOffset -= barIncrement) {
            canvas.drawRect(currentBarX, currentTopOffset, currentBarX + barWidth,
                    getPaddingTop() + maxBarHeight - INTERNAL_UPDOWN_PADDING,
                    i < level ? barPaint : emptyBarPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        int width;
        int height;

        float translationFactor = 1 / (numBars * MIN_TO_MAX_FACTOR * HEIGHT_TO_WIDTH_FACTOR
            * (1 + SPACING_WIDTH_FACTOR));

        if (heightSize / translationFactor <= widthSize) {
            height = heightSize;
            width = (int) (height / translationFactor);
        } else {
            width = widthSize;
            height = (int) (width * translationFactor);
        }

        setMeasuredDimension(width, height);
    }

    public void setSignalStrength(int signalStrength, int[] boundaries) {
        for (int level = boundaries.length - 1; level >= 0; level--) {
            if (signalStrength >= boundaries[level]) {
                setLevel(level + 1);
                return;
            }
        }
    }

    public void setLevel(int level) {
        if (level < 0 || level > numBars) {
            throw new IllegalArgumentException("Level may not be greater than the number of bars!");
        } else {
            this.level = level;

            invalidate();
            requestLayout();
        }
    }
}