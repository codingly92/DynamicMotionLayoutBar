package com.applover.dynamicmotionlayoutbar.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import com.applover.dynamicmotionlayoutbar.R
import com.applover.dynamicmotionlayoutbar.utils.TwoStateMotionLayout
import com.applover.dynamicmotionlayoutbar.utils.centerInParent
import com.applover.dynamicmotionlayoutbar.utils.createSet

@Suppress("SpellCheckingInspection")
class ActivableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : TwoStateMotionLayout(context, attrs, defStyleAttr) {

    private var activeTint: Int = -1
    private var inactiveTint: Int = -1
    private var drawableRes: Int = -1

    init {
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
            } finally {
                recycle()
            }
        }
    }

    override fun createViewsAndConstraintSets(layout: ConstraintLayout): Pair<ConstraintSet, ConstraintSet> {
        val set = ConstraintSet()
        val imageView = ImageView(layout.context)
        val imageViewId = generateViewId()
        imageView.id = imageViewId
        val layoutParams = LayoutParams(LayoutParams.MATCH_CONSTRAINT, LayoutParams.MATCH_CONSTRAINT)
        imageView.setImageResource(drawableRes)
        layout.addView(imageView, layoutParams)
        set.clone(layout)
        set.centerInParent(imageViewId)

        val startSet = createSet {
            setColorValue(imageViewId, "ColorFilter", inactiveTint)
            setAlpha(imageViewId, 0.3f)
            centerInParent(imageViewId)
        }

        val endSet = createSet {
            setColorValue(imageViewId, "ColorFilter", activeTint)
            setAlpha(imageViewId, 1f)
            applyTo(this@ActivableImageView)
            centerInParent(imageViewId)
        }

        return startSet to endSet
    }

    fun setActive(isActive: Boolean) {
        if (isActive) {
            transitionToEnd()
        } else {
            transitionToStart()
        }
    }
}
