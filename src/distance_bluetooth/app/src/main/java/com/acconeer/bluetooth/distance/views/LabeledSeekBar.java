package com.acconeer.bluetooth.distance.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.acconeer.bluetooth.distance.R;
import com.acconeer.bluetooth.distance.utils.Utils;

import androidx.appcompat.widget.AppCompatSeekBar;

public class LabeledSeekBar extends AppCompatSeekBar {
    private static final int PROGRESS_LABEL_SPACING = 55;

    private Paint labelPaint;
    private Paint inactiveLabelPaint;
    private Paint hintPaint;

    private float finalHeight;
    private float finalWidth;
    private int textHeight;

    public LabeledSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        labelPaint = new Paint();
        labelPaint.setTextSize(Utils.spToPx(15, getContext()));
        labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
        labelPaint.setColor(getResources().getColor(R.color.colorAccent));

        hintPaint = new Paint();
        hintPaint.setTextSize(Utils.spToPx(13, getContext()));
        hintPaint.setColor(getResources().getColor(R.color.colorAccent));
        hintPaint.setAntiAlias(true);

        inactiveLabelPaint = new Paint();
        inactiveLabelPaint.setTextSize(Utils.spToPx(15, getContext()));
        inactiveLabelPaint.setTypeface(Typeface.DEFAULT_BOLD);
        inactiveLabelPaint.setColor(getResources().getColor(R.color.colorAccentBleak));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        finalWidth = w;
        finalHeight = h;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = getMeasuredWidth();
        int barHeight = getMeasuredHeight();

        Rect bounds = new Rect();
        labelPaint.getTextBounds("Ty", 0, 2, bounds);
        textHeight = bounds.height();

        // 2 because of the above labels
        setMeasuredDimension(viewWidth, barHeight + 2 * textHeight + PROGRESS_LABEL_SPACING);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (labelPaint != null) {
            labelPaint.setAlpha(isEnabled() ? 255 : (int) (0.54 * 255));
        }
        if (inactiveLabelPaint != null) {
            inactiveLabelPaint.setAlpha(isEnabled() ? 255 : (int) (0.54 * 255));
        }
        if (hintPaint != null) {
            hintPaint.setAlpha(isEnabled() ? 255 : (int) (0.54 * 255));
        }

        //I dont know why i have to offset it with PROGRESS_LABEL_SPACING
        canvas.drawText(getContext().getString(R.string.max_depth_res), 0,
                getPaddingTop() + PROGRESS_LABEL_SPACING, hintPaint);
        canvas.drawText(getContext().getString(R.string.max_gain),
                finalWidth - hintPaint.measureText("Max gain"),
                getPaddingTop() + PROGRESS_LABEL_SPACING, hintPaint);

        //I dont know how this works without translating
        super.onDraw(canvas);

        float currentX = 0;
        float barStartY = finalHeight - textHeight;

        float increment = finalWidth / getMax();

        for (int i = 0; i <= getMax(); i++, currentX += increment) {
            float labelWidth = labelPaint.measureText(Integer.toString(i + 1));
            canvas.drawText(Integer.toString(i + 1), currentX - labelWidth / 2, barStartY,
                    getProgress() == i ? labelPaint : inactiveLabelPaint);
        }
    }
}
