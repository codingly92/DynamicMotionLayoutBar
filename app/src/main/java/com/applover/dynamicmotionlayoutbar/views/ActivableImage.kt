package com.applover.dynamicmotionlayoutbar.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import com.applover.dynamicmotionlayoutbar.R
import com.applover.dynamicmotionlayoutbar.utils.TwoStateMotionLayout
import com.applover.dynamicmotionlayoutbar.utils.centerInParent
import com.applover.dynamicmotionlayoutbar.utils.createSet

@Suppress("SpellCheckingInspection")
class ActivableImage @JvmOverloads constructor(
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
            R.styleable.MotionLayoutActiveImage,
            0, 0
        ).apply {
            try {
                drawableRes = getResourceIdOrThrow(R.styleable.MotionLayoutActiveImage_android_src)
                activeTint = getColorOrThrow(R.styleable.MotionLayoutActiveImage_active_tint)
                inactiveTint = getColorOrThrow(R.styleable.MotionLayoutActiveImage_inactive_tint)
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
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        imageView.setImageResource(drawableRes)
        layout.addView(imageView, layoutParams)
        set.clone(layout)
        set.centerInParent(imageViewId)

        val startSet = createSet {
            setColorValue(imageViewId, "ColorFilter", inactiveTint)
            setAlpha(imageViewId, 0.3f)
        }

        val endSet = createSet {
            setColorValue(imageViewId, "ColorFilter", activeTint)
            setAlpha(imageViewId, 1f)
            applyTo(this@ActivableImage)
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
