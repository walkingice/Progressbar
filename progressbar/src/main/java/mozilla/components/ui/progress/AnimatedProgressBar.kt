/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.ui.progress

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.InterpolatorRes
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import mozilla.components.ui.progressbar.R

/**
 * Animation duration of progress changing.
 */
private const val PROGRESS_DURATION = 200

/**
 * Delay before applying closing animation when progress reach max value.
 */
private const val CLOSING_DELAY = 300

/**
 * Animation duration for closing
 */
private const val CLOSING_DURATION = 300L

private fun createAnimator(max: Int, listener: ValueAnimator.AnimatorUpdateListener): ValueAnimator {
    val animator = ValueAnimator.ofInt(0, max)
    animator.interpolator = LinearInterpolator()
    animator.duration = PROGRESS_DURATION.toLong()
    animator.addUpdateListener(listener)
    return animator
}

/**
 * A progressbar with some animations on changing progress.
 * When changing progress of this bar, it does not change value directly. Instead, it use
 * [Animator] to change value progressively. Moreover, change visibility to View.GONE will
 * cause closing animation.
 */
class AnimatedProgressBar : ProgressBar {

    private lateinit var primaryAnimator: ValueAnimator
    private val closingAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

    /**
     * For closing animation. To indicate how many visible region should be clipped.
     */
    private var clipRatio = 0f
    private val rect = Rect()

    /**
     * To store the final expected progress to reach, it does matter in animation.
     */
    private var expectedProgress = 0

    /**
     * setProgress() might be invoked in constructor. Add to flag to avoid null checking for animators.
     */
    private var initialized = false

    private var isRtl = false

    private val endingRunner = EndingRunner()

    private val listener = ValueAnimator.AnimatorUpdateListener { setProgressImmediately(primaryAnimator.animatedValue as Int) }

    constructor(context: Context) : super(context, null) {
        init(context, null)
    }

    constructor(context: Context,
                attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context,
                attrs: AttributeSet?,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context,
                attrs: AttributeSet,
                defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        initialized = true

        progressDrawable = context
                .run { obtainStyledAttributes(attrs, R.styleable.AnimatedProgressBar) }
                .let { typedArray ->
                    @InterpolatorRes
                    val id = typedArray.getResourceId(R.styleable.AnimatedProgressBar_shiftInterpolator, 0)
                    val duration = typedArray.getInteger(R.styleable.AnimatedProgressBar_shiftDuration, 1000)
                    val wrap = typedArray.getBoolean(R.styleable.AnimatedProgressBar_wrapShiftDrawable, false)
                    typedArray.recycle()

                    buildDrawable(progressDrawable, wrap, duration, id)
                }

        primaryAnimator = createAnimator(max, listener)

        with(closingAnimator) {
            duration = CLOSING_DURATION
            interpolator = LinearInterpolator()
        }

        closingAnimator.addUpdateListener { valueAnimator ->
            clipRatio = valueAnimator.animatedValue as Float
            invalidate()
        }

        closingAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                clipRatio = 0f
            }

            override fun onAnimationEnd(animator: Animator) {
                setVisibilityImmediately(View.GONE)
            }

            override fun onAnimationCancel(animator: Animator) {
                clipRatio = 0f
            }

            override fun onAnimationRepeat(animator: Animator) {}
        })
    }

    /**
     * {@inheritDoc}
     */
    @Synchronized
    override fun setMax(max: Int) {
        super.setMax(max)
        primaryAnimator = createAnimator(getMax(), listener)
    }

    /**
     * {@inheritDoc}
     *
     *
     * Instead of set progress directly, this method triggers an animator to change progress.
     */
    override fun setProgress(nextProgress: Int) {
        var nextProgress = nextProgress
        nextProgress = Math.min(nextProgress, max)
        nextProgress = Math.max(0, nextProgress)
        expectedProgress = nextProgress
        if (!initialized) {
            setProgressImmediately(expectedProgress)
            return
        }

        // if regress, jump to the expected value without any animation
        if (expectedProgress < progress) {
            cancelAnimations()
            setProgressImmediately(expectedProgress)
            return
        }

        // Animation is not needed for reloading a completed page
        if (expectedProgress == 0 && progress == max) {
            cancelAnimations()
            setProgressImmediately(0)
            return
        }

        cancelAnimations()
        primaryAnimator.setIntValues(progress, nextProgress)
        primaryAnimator.start()
    }

    public override fun onDraw(canvas: Canvas) {
        if (clipRatio == 0f) {
            super.onDraw(canvas)
        } else {
            canvas.getClipBounds(rect)
            val clipWidth = rect.width() * clipRatio
            canvas.save()
            if (isRtl) {
                canvas.clipRect(rect.left.toFloat(), rect.top.toFloat(), rect.right - clipWidth, rect.bottom.toFloat())
            } else {
                canvas.clipRect(rect.left + clipWidth, rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat())
            }
            super.onDraw(canvas)
            canvas.restore()
        }
    }

    /**
     * {@inheritDoc}
     *
     *
     * Instead of change visibility directly, this method also applies the closing animation if
     * progress reaches max value.
     */
    override fun setVisibility(value: Int) {
        // nothing changed
        if (visibility == value) {
            return
        }

        if (value == View.GONE) {
            if (expectedProgress == max) {
                setProgressImmediately(expectedProgress)
                animateClosing()
            } else {
                setVisibilityImmediately(value)
            }
        } else {
            val handler = handler
            // if this view is detached from window, the handler would be null
            handler?.removeCallbacks(endingRunner)

            clipRatio = 0f
            closingAnimator.cancel()
            setVisibilityImmediately(value)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
    }


    private fun cancelAnimations() {
        primaryAnimator.cancel()
        closingAnimator.cancel()

        clipRatio = 0f
    }

    private fun setVisibilityImmediately(value: Int) {
        super.setVisibility(value)
    }

    private fun animateClosing() {
        closingAnimator.cancel()
        val handler = handler
        // if this view is detached from window, the handler would be null
        if (handler != null) {
            handler.removeCallbacks(endingRunner)
            handler.postDelayed(endingRunner, CLOSING_DELAY.toLong())
        }
    }

    private fun setProgressImmediately(progress: Int) {
        super.setProgress(progress)
    }

    private fun buildDrawable(original: Drawable,
                              isWrap: Boolean,
                              duration: Int,
                              @InterpolatorRes itplId: Int): Drawable {
        if (isWrap) {
            val interpolator = if (itplId > 0)
                AnimationUtils.loadInterpolator(context, itplId)
            else
                null
            return ShiftDrawable(original, duration, interpolator)
        } else {
            return original
        }
    }

    private inner class EndingRunner : Runnable {
        override fun run() {
            closingAnimator.start()
        }
    }
}
