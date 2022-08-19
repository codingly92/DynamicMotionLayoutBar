package com.applover.dynamicmotionlayoutbar.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.Space
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.widget.ImageViewCompat
import com.applover.dynamicmotionlayoutbar.R
import com.applover.dynamicmotionlayoutbar.utils.createConstraintSet
import com.applover.dynamicmotionlayoutbar.utils.dpToPx

@Suppress("SpellCheckingInspection")
open class StepProgressBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MotionLayout(context, attrs, defStyleAttr) {

    private var activeTint: Int = -1
    private var inactiveTint: Int = -1
    private var animationDuration = DEFAULT_ANIMATION_SPEED

    private val stepViews = mutableListOf<StepView>()
    private val stepConstraints = mutableListOf<StepConstraintSet>()
    private var inactiveBarId: Int = -1
    private var activeBarId: Int = -1

    private var currentStep: Int = 1

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StepProgressBarView,
            0,
            0,
        ).apply {
            try {
                activeTint = getColorOrThrow(R.styleable.StepProgressBarView_active_tint)
                inactiveTint = getColorOrThrow(R.styleable.StepProgressBarView_inactive_tint)
                animationDuration = getIntOrThrow(R.styleable.StepProgressBarView_duration)
            } finally {
                recycle()
            }
        }
    }

    fun initialize(steps: List<Step>) {
        createViews(steps)
        createInitialConstraints()
        createTransitions()
    }

    fun previousStep() {
        if (currentStep == 1) return
        setStep(currentStep - 1)
    }

    fun nextStep() {
        if (currentStep == stepViews.size) return
        setStep(currentStep + 1)
    }

    private fun createViews(steps: List<Step>) {
        resetViews()
        steps.forEach {
            stepViews.add(createStepView(it))
        }
        stepViews.first().setActive(true)
        createBars()
    }

    private fun ConstraintLayout.createStepView(step: Step): StepView {
        val activableImageView = ActivableImageView(
            context,
            drawableRes = step.drawableRes,
            activeTint = step.activeTint,
            inactiveTint = step.inactiveTint,
            animationDuration = animationDuration
        )
        val activableImageViewId = generateViewId()
        activableImageView.id = activableImageViewId
        val layoutParamsWrapMarginMedium = LayoutParams(48.asDp(), 48.asDp())
        layoutParamsWrapMarginMedium.setMargins(16.asDp(), 16.asDp(), 16.asDp(), 16.asDp())
        addView(activableImageView, layoutParamsWrapMarginMedium)

        val anchor = Space(context)
        val anchorId = generateViewId()
        anchor.id = anchorId
        addView(anchor, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

        return StepView(activableImageViewId, anchorId, activableImageView)
    }

    private fun createBars(){
        inactiveBarId = createBar(inactiveTint)
        activeBarId = createBar(activeTint)
    }

    private fun createBar(@ColorInt tint: Int): Int {
        val layoutParams = LayoutParams(LayoutParams.MATCH_CONSTRAINT, 4.asDp())
        layoutParams.setMargins(0.asDp(), 16.asDp(), 0.asDp(), 16.asDp())

        val imageView = ImageView(context)
        val imageViewId = generateViewId()
        imageView.id = imageViewId
        imageView.setImageResource(R.drawable.bar)
        ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(tint))
        addView(imageView, layoutParams)
        return imageViewId
    }

    private fun createInitialConstraints() = createConstraintSet().apply {
        createConstrainsForAllSteps()
        setConstraintsForAllAnchors()
        setConstraintsForInactiveBar()
        setConstraintsForActiveBar()
        applyTo(this@StepProgressBarView)
    }

    private fun ConstraintSet.createConstrainsForAllSteps() {
        val viewIds = stepViews.map { it.imageViewId }.toIntArray()
        createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, viewIds, null, ConstraintSet.CHAIN_SPREAD)
        viewIds.forEach {
            connect(it, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(it, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        }
    }

    private fun ConstraintSet.setConstraintsForAllAnchors() {
        stepViews.forEach {
            connect(it.anchorViewId, ConstraintSet.TOP, it.imageViewId, ConstraintSet.BOTTOM)
            connect(it.anchorViewId, ConstraintSet.START, it.imageViewId, ConstraintSet.START)
            connect(it.anchorViewId, ConstraintSet.END, it.imageViewId, ConstraintSet.END)
        }
    }

    private fun ConstraintSet.setConstraintsForInactiveBar() {
        val firstAnchor = stepViews.first().anchorViewId
        val lastAnchor = stepViews.last().anchorViewId
        connect(inactiveBarId, ConstraintSet.TOP, firstAnchor, ConstraintSet.BOTTOM)
        connect(inactiveBarId, ConstraintSet.START, firstAnchor, ConstraintSet.START)
        connect(inactiveBarId, ConstraintSet.END, lastAnchor, ConstraintSet.END)
    }

    private fun ConstraintSet.setConstraintsForActiveBar() {
        val firstAnchor = stepViews.first().anchorViewId
        connect(activeBarId, ConstraintSet.TOP, inactiveBarId, ConstraintSet.TOP)
        connect(activeBarId, ConstraintSet.BOTTOM, inactiveBarId, ConstraintSet.BOTTOM)
        connect(activeBarId, ConstraintSet.START, firstAnchor, ConstraintSet.START)
        connect(activeBarId, ConstraintSet.END, firstAnchor, ConstraintSet.END)
    }

    private fun createTransitions() {
        createConstraintsForSteps()
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
        setTransition(firstTransition)
    }

    private fun createConstraintsForSteps() {
        stepConstraints.addAll(stepViews.map { StepConstraintSet(generateViewId(), it.createConstraintsForStep()) })
    }

    private fun StepView.createConstraintsForStep() = createConstraintSet {
        connect(activeBarId, ConstraintSet.START, stepViews.first().anchorViewId, ConstraintSet.START)
        connect(activeBarId, ConstraintSet.END, anchorViewId, ConstraintSet.END)
    }

    private fun createConstraintsBetweenSteps(): MutableList<Pair<StepConstraintSet, StepConstraintSet>> {
        val allContraints = mutableListOf<Pair<StepConstraintSet, StepConstraintSet>>()
        stepConstraints.forEachIndexed { index, constraintsSet ->
            // No next step for last one
            if (index == stepViews.lastIndex) return@forEachIndexed
            allContraints.add(constraintsSet to stepConstraints[index + 1])
        }
        return allContraints
    }

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

    private fun resetViews() {
        removeAllViews()
        stepViews.clear()
    }

    private fun setStep(step: Int) {
        if (step == currentStep) return
        val oldStepIndex = currentStep - 1
        val newStepIndex = step - 1
        stepViews[oldStepIndex].setActive(false)
        stepViews[newStepIndex].setActive(true)
        currentStep = step
        setTransitionDuration(animationDuration)
        transitionToState(stepConstraints[newStepIndex].constraintSetId)
    }

    private fun Int.asDp() = context.dpToPx(this)

    private data class StepView(val imageViewId: Int, val anchorViewId: Int, private val activableImageView: ActivableImageView) {
        fun setActive(isActive: Boolean) = activableImageView.setActive(isActive)
    }

    data class Step(@DrawableRes val drawableRes: Int, @ColorRes val activeTint: Int, @ColorRes val inactiveTint: Int)

    data class StepConstraintSet(val constraintSetId: Int, val constraintSet: ConstraintSet)

    companion object {
        private const val DEFAULT_ANIMATION_SPEED = 500
    }
}
