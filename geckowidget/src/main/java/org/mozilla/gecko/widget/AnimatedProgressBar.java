/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.gecko.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import org.mozilla.gecko.R;
import org.mozilla.gecko.drawable.ShiftDrawable;

/**
 * A progressbar with some animations on changing progress.
 * When changing progress of this bar, it does not change value directly. Instead, it use
 * {@link Animator} to change value progressively. Moreover, change visibility to View.GONE will
 * cause closing animation.
 */
public class AnimatedProgressBar extends ProgressBar {

    /**
     * Animation duration of progress changing.
     */
    private final static int PROGRESS_DURATION = 200;

    /**
     * Delay before applying closing animation when progress reach max value.
     */
    private final static int CLOSING_DELAY = 300;

    /**
     * Animation duration for closing
     */
    private final static int CLOSING_DURATION = 300;

    private ValueAnimator mPrimaryAnimator;
    private ValueAnimator mClosingAnimator = ValueAnimator.ofFloat(0f, 1f);

    /**
     * For closing animation. To indicate how many visible region should be clipped.
     */
    private float mClipRegion = 0f;

    /**
     * To store the final expected progress to reach, it does matter in animation.
     */
    private int mExpectedProgress = 0;

    private ValueAnimator.AnimatorUpdateListener mListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            setProgressImmediately((int) mPrimaryAnimator.getAnimatedValue());
        }
    };

    public AnimatedProgressBar(@NonNull Context context) {
        super(context, null);
        init(context, null);
    }

    public AnimatedProgressBar(@NonNull Context context,
                               @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AnimatedProgressBar(@NonNull Context context,
                               @Nullable AttributeSet attrs,
                               int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatedProgressBar(Context context,
                               AttributeSet attrs,
                               int defStyleAttr,
                               int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Instead of set progress directly, this method triggers an animator to change progress.
     */
    @Override
    public void setProgress(int nextProgress) {
        nextProgress = Math.min(nextProgress, getMax());
        nextProgress = Math.max(0, nextProgress);
        mExpectedProgress = nextProgress;

        // Animation is not needed for reloading a completed page
        if ((mExpectedProgress == 0) && (getProgress() == getMax())) {
            if (mPrimaryAnimator != null) {
                mPrimaryAnimator.cancel();
            }

            if (mClosingAnimator != null) {
                mClosingAnimator.cancel();
                mClipRegion = 0f;
            }

            setProgressImmediately(0);
            return;
        }

        if (mPrimaryAnimator != null) {
            mPrimaryAnimator.cancel();
            mPrimaryAnimator.setIntValues(getProgress(), nextProgress);
            mPrimaryAnimator.start();
        } else {
            setProgressImmediately(nextProgress);
        }

        if (mClosingAnimator != null) {
            if (nextProgress != getMax()) {
                // stop closing animation
                mClosingAnimator.cancel();
                mClipRegion = 0f;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mClipRegion == 0) {
            super.onDraw(canvas);
        } else {
            Rect rect = canvas.getClipBounds();
            canvas.save();
            canvas.clipRect(rect.left + rect.width() * mClipRegion, rect.top, rect.right, rect.bottom);
            super.onDraw(canvas);
            canvas.restore();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Instead of change visibility directly, this method also applies the closing animation if
     * progress reaches max value.
     */
    @Override
    public void setVisibility(int value) {
        if (value == GONE) {
            if (mExpectedProgress == getMax()) {
                animateClosing();
            } else {
                setVisibilityImmediately(value);
            }
        } else {
            setVisibilityImmediately(value);
        }
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedProgressBar);
        final int duration = a.getInteger(R.styleable.AnimatedProgressBar_shiftDuration, 1000);
        final boolean wrap = a.getBoolean(R.styleable.AnimatedProgressBar_wrapShiftDrawable, false);
        @InterpolatorRes final int itplId = a.getResourceId(R.styleable.AnimatedProgressBar_shiftInterpolator, 0);
        a.recycle();

        setProgressDrawable(buildDrawable(getProgressDrawable(), wrap, duration, itplId));

        mPrimaryAnimator = ValueAnimator.ofInt(getProgress(), getMax());
        mPrimaryAnimator.setInterpolator(new LinearInterpolator());
        mPrimaryAnimator.setDuration(PROGRESS_DURATION);
        mPrimaryAnimator.addUpdateListener(mListener);

        mClosingAnimator.setDuration(CLOSING_DURATION);
        mClosingAnimator.setInterpolator(new LinearInterpolator());
        mClosingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mClipRegion = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        mClosingAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mClipRegion = 0f;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setVisibilityImmediately(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mClipRegion = 0f;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

    private void setVisibilityImmediately(int value) {
        super.setVisibility(value);
    }

    private void animateClosing() {
        mClosingAnimator.cancel();
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mClosingAnimator.start();
            }
        }, CLOSING_DELAY);
    }

    private void setProgressImmediately(int progress) {
        super.setProgress(progress);
    }

    private Drawable buildDrawable(@NonNull Drawable original,
                                   boolean isWrap,
                                   int duration,
                                   @InterpolatorRes int itplId) {
        if (isWrap) {
            final Interpolator interpolator = (itplId > 0)
                    ? AnimationUtils.loadInterpolator(getContext(), itplId)
                    : null;
            return new ShiftDrawable(original, duration, interpolator);
        } else {
            return original;
        }
    }
}
