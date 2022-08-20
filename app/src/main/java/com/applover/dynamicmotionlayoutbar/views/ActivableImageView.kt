package com.applover.dynamicmotionlayoutbar.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import com.applover.dynamicmotionlayoutbar.R
import com.applover.dynamicmotionlayoutbar.utils.TwoStateMotionLayout
import com.applover.dynamicmotionlayoutbar.utils.centerInParent
import com.applover.dynamicmotionlayoutbar.utils.createConstraintSet

/**
 * Example view that animates tint change for imageView
 */
@Suppress("SpellCheckingInspection")
class ActivableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : TwoStateMotionLayout(context, attrs, defStyleAttr) {

    private var activeTint: Int = -1
    private var inactiveTint: Int = -1
    private var drawableRes: Int = -1

    /**
     * Initialize from code and get attributes for colors of icons, animation duration and initial state
     * Note that we need to call setupView as onFinishInflate in TwoStateMotionLayout will not be called as it was not created in XML
     */
    constructor(
        context: Context,
        @DrawableRes drawableRes: Int,
        @ColorRes activeTint: Int,
        @ColorRes inactiveTint: Int,
        animationDuration: Int,
        startAtTheEndTransition: Boolean = false,
    ) : this(context, null, 0) {
        this.drawableRes = drawableRes
        this.activeTint = ContextCompat.getColor(context, activeTint)
        this.inactiveTint = ContextCompat.getColor(context, inactiveTint)
        this.animationDuration = animationDuration
        this.startAtTheEndTransition = startAtTheEndTransition
        setupView()
    }

    /**
     * Initialize from xml and get attributes for colors of icons, animation duration and initial state
     * Note that in normal application you shouldn't pass colors, but styles
     * For the sake of simplicity, let's leave colors for now :)
     */
    init {
        attrs?.let {
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ActivableImageView,
                0,
                0,
            ).apply {
                try {
                    drawableRes = getResourceIdOrThrow(R.styleable.ActivableImageView_android_src)
                    activeTint = getColorOrThrow(R.styleable.ActivableImageView_active_tint)
                    inactiveTint = getColorOrThrow(R.styleable.ActivableImageView_inactive_tint)
                    animationDuration = getIntOrThrow(R.styleable.ActivableImageView_duration)
                    startAtTheEndTransition = getBoolean(R.styleable.ActivableImageView_is_active, false)
                } finally {
                    recycle()
                }
            }
        }
    }

    /**
     * We need to create views first and then set constraints for initial state and end state of animation
     */
    override fun ConstraintLayout.createViewsAndConstraintSets(): Pair<ConstraintSet, ConstraintSet> {
        val imageView = ImageView(context)
        val imageViewId = generateViewId()
        imageView.id = imageViewId
        val layoutParams = LayoutParams(LayoutParams.MATCH_CONSTRAINT, LayoutParams.MATCH_CONSTRAINT)
        imageView.setImageResource(drawableRes)
        addView(imageView, layoutParams)

        val startSet = createConstraintSet {
            setColorValue(imageViewId, "ColorFilter", inactiveTint)
            setAlpha(imageViewId, 0.3f)
            centerInParent(imageViewId)
        }

        val endSet = createConstraintSet {
            setColorValue(imageViewId, "ColorFilter", activeTint)
            setAlpha(imageViewId, 1f)
            centerInParent(imageViewId)
        }

        return startSet to endSet
    }

    /**
     * The easiest way to manipulate single transation is transitionToEnd and transitionToStart as we don't need any ids of constraints nor transitions
     */
    fun setActive(isActive: Boolean) {
        if (isActive) {
            transitionToEnd()
        } else {
            transitionToStart()
        }
    }
}
