/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.ui.progress

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

private const val DEFAULT_DURATION = 1000

// align to ScaleDrawable implementation
private const val MAX_LEVEL = 10000

/**
 * A drawable to keep shifting its wrapped drawable.
 * Assume the wrapped drawable value is "00000010", this class will keep drawing in this way
 *
 *
 * 00000010 -> 00000001 -> 10000000 -> 01000000 -> 00100000 -> ...
 *
 *
 * This drawable will keep drawing until be invisible.
 */
internal class ShiftDrawable @JvmOverloads constructor(
        drawable: Drawable,
        private val duration: Int = DEFAULT_DURATION,
        private val interpolator: Interpolator? = LinearInterpolator()
) : DrawableWrapper(drawable) {

    /**
     * An animator to trigger redraw and update offset-of-shifting
     */
    private val animator = ValueAnimator.ofFloat(0f, 1f)

    /**
     * Visible rectangle, wrapped-drawable is resized and draw in this rectangle
     */
    private val visibleRect = Rect()

    /**
     * Canvas will clip itself by this Path. Used to draw rounded head.
     */
    private val path = Path()

    init {
        with(animator) {
            repeatCount = ValueAnimator.INFINITE
            duration = this@ShiftDrawable.duration.toLong()
            interpolator = this@ShiftDrawable.interpolator ?: LinearInterpolator()
        }

        animator.also { it.addUpdateListener { if (isVisible) invalidateSelf() } }
                .start()
    }

    /**
     * {@inheritDoc}
     *
     * override to enable / disable animator as well.
     */
    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        return super.setVisible(visible, restart)
                .also { if (isVisible) animator.start() else animator.end() }
    }

    /**
     * {@inheritDoc}
     */
    override fun onBoundsChange(bounds: Rect) {
        return super.onBoundsChange(bounds)
                .also { updateBounds() }
    }

    /**
     * {@inheritDoc}
     */
    override fun onLevelChange(level: Int): Boolean {
        return super.onLevelChange(level)
                .also { updateBounds() }
    }

    override fun draw(canvas: Canvas) {
        val wrapped = wrappedDrawable
        val fraction = animator.animatedFraction
        val width = visibleRect.width()
        val offset = (width * fraction).toInt()

        val stack = canvas.save()

        // To apply path, then we have rounded-head
        canvas.clipPath(path)

        // To draw left-half part of Drawable, shift from right to left
        canvas.save()
        canvas.translate((-offset).toFloat(), 0f)
        wrapped.draw(canvas)
        canvas.restore()

        // Then to draw right-half part of Drawable
        canvas.save()
        canvas.translate((width - offset).toFloat(), 0f)
        wrapped.draw(canvas)
        canvas.restore()

        canvas.restoreToCount(stack)
    }

    private fun updateBounds() {
        val b = bounds
        val width = (b.width().toFloat() * level / MAX_LEVEL).toInt()
        visibleRect.set(b.left, b.top, b.left + width, b.height())

        // to create path to help drawing rounded head. path is enclosed by visibleRect
        val radius = (b.height() / 2).toFloat()
        path.reset()

        // The added rectangle width is smaller than visibleRect, due to semi-circular.
        path.addRect(visibleRect.left.toFloat(),
                visibleRect.top.toFloat(), visibleRect.right - radius,
                visibleRect.height().toFloat(),
                Path.Direction.CCW)
        // To add semi-circular
        path.addCircle(visibleRect.right - radius, radius, radius, Path.Direction.CCW)
    }
}
