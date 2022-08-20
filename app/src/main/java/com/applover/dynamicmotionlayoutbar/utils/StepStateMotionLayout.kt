package com.applover.dynamicmotionlayoutbar.utils

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.constraintlayout.widget.ConstraintSet

/**
 * Helper class for creating motion layout with steps transitions
 */
abstract class StepStateMotionLayout(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MotionLayout(context, attrs, defStyleAttr) {

    private val stepConstraints = mutableListOf<StepConstraintSet>()

    var currentStep: Int = 1
        private set

    protected var animationDuration = DEFAULT_ANIMATION_SPEED

    /**
     * We can call transitionToState to let motion layout handle the transition
     */
    protected fun setStep(step: Int) {
        if (step == currentStep) return
        val oldStepIndex = currentStep - 1
        val newStepIndex = step - 1
        currentStep = step
        transitionToState(stepConstraints[newStepIndex].constraintSetId, animationDuration)
        onStepChanged(oldStepIndex, newStepIndex)
    }

    /**
     * Create constraints for first step
     */
    protected abstract fun createInitialConstraints(): ConstraintSet

    /**
     * Create constraints that have to change per given step
     */
    protected abstract fun createConstraintsForSteps(): List<StepConstraintSet>

    /**
     * The last step is to create transitions to let motion layout know how to transform our views
     * Note that we have to set first transition to initialize first view's state
     */
    protected fun createTransitions() {
        stepConstraints.clear()
        stepConstraints.addAll(createConstraintsForSteps())
        val scene = MotionScene(this)
        var firstTransition: MotionScene.Transition? = null

        createConstraintsBetweenSteps().forEachIndexed { index, pair ->
            val transition = scene.createTransition(pair)
            scene.addTransition(transition)
            if (index == 0) {
                firstTransition = transition
            }
        }

        setScene(scene)
        setTransition(firstTransition!!)
    }

    /**
     * We need to create transitions for every step
     * We don't need every combination, but motion layout needs to know how to get to each step
     * So we create transitions like: 1 to 2, 2 to 3, 3 to 4 etc.
     */
    private fun createConstraintsBetweenSteps(): MutableList<Pair<StepConstraintSet, StepConstraintSet>> {
        val allContraints = mutableListOf<Pair<StepConstraintSet, StepConstraintSet>>()
        stepConstraints.forEachIndexed { index, constraintsSet ->
            // No next step for last one
            if (index == stepConstraints.lastIndex) return@forEachIndexed
            allContraints.add(constraintsSet to stepConstraints[index + 1])
        }
        return allContraints
    }

    /**
     * To build the transition we need start and end constraint sets
     * We don't need to create reversable constraints, motion layout works fine without that
     */
    private fun MotionScene.createTransition(sets: Pair<StepConstraintSet, StepConstraintSet>): MotionScene.Transition {
        val startConstraintSet = sets.first
        val endConstraintSet = sets.second
        return TransitionBuilder.buildTransition(
            this,
            generateViewId(),
            startConstraintSet.constraintSetId,
            startConstraintSet.constraintSet,
            endConstraintSet.constraintSetId,
            endConstraintSet.constraintSet,
        )
    }

    protected abstract fun onStepChanged(oldIndex: Int, newIndex: Int)

    protected data class StepConstraintSet(val constraintSetId: Int, val constraintSet: ConstraintSet)

    companion object {
        private const val DEFAULT_ANIMATION_SPEED = 500
    }
}
