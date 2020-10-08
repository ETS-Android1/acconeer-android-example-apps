package com.acconeer.bluetooth.distance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import com.acconeer.bluetooth.distance.R;

import androidx.constraintlayout.widget.ConstraintLayout;

public class LabeledImageButton extends ConstraintLayout {
    private static final long BLINK_INTERVAL = 500;

    protected ImageButton imageButton;
    protected TextView textView;

    private Animation fadeAnimation;
    private boolean isAnimating = false;

    public LabeledImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.labeled_image_button, this);

        imageButton = findViewById(R.id.button);
        textView = findViewById(R.id.text);

        TypedArray attributes = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.LabeledImageButton, 0, 0);

        try {
            String text = attributes.getString(R.styleable.LabeledImageButton_text);
            textView.setText(text);
            int imageResId = attributes.getResourceId(R.styleable.LabeledImageButton_imgSrc, 0);
            if (imageResId != 0) {
                Drawable image = getResources().getDrawable(imageResId);

                imageButton.setImageDrawable(image);
            }
        } finally {
            attributes.recycle();
        }

        init();
    }

    private void init() {
        fadeAnimation = new AlphaAnimation(0, 1);
        fadeAnimation.setInterpolator(new DecelerateInterpolator());
        fadeAnimation.setDuration(BLINK_INTERVAL);
        fadeAnimation.setRepeatMode(Animation.REVERSE);
        fadeAnimation.setRepeatCount(Animation.INFINITE);
    }

    public void showAnimated(boolean visible) {
        if (visible) {
            setVisibility(View.VISIBLE);
            startAnimation();
        } else {
            setVisibility(View.INVISIBLE);
            stopAnimation();
        }
    }

    public void startAnimation() {
        if (!isAnimating) {
            clearAnimation();
            startAnimation(fadeAnimation);
            isAnimating = true;
        }
    }

    public void stopAnimation() {
        if (isAnimating) {
            clearAnimation();
            isAnimating = false;
        }
    }
}
