package com.applover.dynamicmotionlayoutbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getResourceIdOrThrow

class MotionLayoutActiveImage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MotionLayout(context, attrs, defStyleAttr) {

    private var activeTint: Int = -1
    private var inactiveTint: Int = -1
    private var drawableRes: Int = -1

    private var imageViewId = -1

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

    override fun onFinishInflate() {
        super.onFinishInflate()
        createImageView(this)
        val scene = MotionScene(this)
        val updatedTransition = createTransition(scene)
        scene.addTransition(updatedTransition)
        setScene(scene)
        setTransition(updatedTransition)
    }

    private fun createImageView(layout: ConstraintLayout) {
        val set = ConstraintSet()
        val imageView = ImageView(layout.context)
        imageViewId = View.generateViewId()
        imageView.id = imageViewId
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        imageView.setImageResource(drawableRes)
        layout.addView(imageView, layoutParams)
        set.clone(layout)
        set.connect(imageViewId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(imageViewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(imageViewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(imageViewId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
    }

    private fun createTransition(scene: MotionScene): MotionScene.Transition {
        val startSetId = View.generateViewId()
        val startSet = ConstraintSet()
        startSet.clone(this)
        startSet.apply {
            setColorValue(imageViewId, "ColorFilter", inactiveTint)
            setAlpha(imageViewId, 0.3f)
        }
        val endSetId = View.generateViewId()
        val endSet = ConstraintSet()
        endSet.clone(this)
        endSet.apply {
            setColorValue(imageViewId, "ColorFilter", activeTint)
            setAlpha(imageViewId, 1f)
            applyTo(this@MotionLayoutActiveImage)
        }

        val transitionId = View.generateViewId()
        return TransitionBuilder.buildTransition(
            scene,
            transitionId,
            startSetId, startSet,
            endSetId, endSet
        )
    }

    fun setActive(isActive: Boolean) {
        if (isActive) {
            transitionToEnd()
        } else {
            transitionToStart()
        }
    }
}
