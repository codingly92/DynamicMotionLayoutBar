package com.applover.dynamicmotionlayoutbar.utils

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

abstract class TwoStateMotionLayout(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MotionLayout(context, attrs, defStyleAttr) {

    protected var animationDuration = DEFAULT_ANIMATION_SPEED
    protected var startAtTheEndTransition = false

    override fun onFinishInflate() {
        super.onFinishInflate()
        val constraintSets = createViewsAndConstraintSets(this)
        val scene = MotionScene(this)
        val updatedTransition = createTransitions(scene, constraintSets)
        scene.addTransition(updatedTransition)
        setScene(scene)
        setTransition(updatedTransition)
        setTransitionDuration(animationDuration)
        if(startAtTheEndTransition){
            progress = 1f
        }
    }

    abstract fun createViewsAndConstraintSets(layout: ConstraintLayout): Pair<ConstraintSet, ConstraintSet>

    private fun createTransitions(scene: MotionScene, sets: Pair<ConstraintSet, ConstraintSet>) =
        TransitionBuilder.buildTransition(
            scene,
            generateViewId(),
            generateViewId(),
            sets.first,
            generateViewId(),
            sets.second,
        )

    companion object {
        private const val DEFAULT_ANIMATION_SPEED = 500
    }
}
