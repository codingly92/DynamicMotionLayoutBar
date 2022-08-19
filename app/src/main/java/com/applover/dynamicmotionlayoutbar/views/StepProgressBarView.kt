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

    /**
     * Initialize from xml and get attributes for colors of bars and animation duration
     */
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

    /**
     * Initialize in the code as it is easier than passing list in xml file
     */
    fun initialize(steps: List<Step>) {
        createViews(steps)
        createInitialConstraints()
        createTransitions()
    }

    /**
     * Simple logic for changing current step by one
     */

    fun previousStep() {
        if (currentStep == 1) return
        setStep(currentStep - 1)
    }

    fun nextStep() {
        if (currentStep == stepViews.size) return
        setStep(currentStep + 1)
    }

    /**
     * It is possible to set steps that are not directly connected by transition
     * Note that in our example we have connections one by one: 1 to 2, 2 to 3, 3 to 4
     * But we can jump from 1 to 4 or 4 to 1 without any issue
     */

    fun firstStep() {
        setStep(1)
    }

    fun lastStep() {
        setStep(4)
    }

    /**
     * Initial step is to create all views
     *
     * Beside that we create bars: inactive and active
     */
    private fun createViews(steps: List<Step>) {
        resetViews()
        steps.forEach {
            stepViews.add(createStepView(it))
        }
        stepViews.first().setActive(true)
        createBars()
    }

    /**
     * We need one image and anchor per step
     * Anchors are needed to easly create constraints for bars
     */
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

    /**
     * Bars needs to be created in an order
     * We want to have active bar above inactive, so we create inactive one first
     */
    private fun createBars() {
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

    /**
     * The next step, when we have views, is to create initial constraints for our view
     * It is the first view that user will see, so in our case it is first step
     */
    private fun createInitialConstraints() = createConstraintSet().apply {
        createConstrainsForAllSteps()
        setConstraintsForAllAnchors()
        setConstraintsForInactiveBar()
        setConstraintsForActiveBar()
        applyTo(this@StepProgressBarView)
    }

    /**
     * We want to constraint each step to top of parent view
     * Beside that we want to create horizontal chain to spread views to make it look nicer
     */
    private fun ConstraintSet.createConstrainsForAllSteps() {
        val viewIds = stepViews.map { it.imageViewId }.toIntArray()
        createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, viewIds, null, ConstraintSet.CHAIN_SPREAD)
        viewIds.forEach {
            connect(it, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(it, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        }
    }

    /**
     * We create anchors below of our images, horizontally in the middle of them
     * It is then much easier to manipulate constraints of bars
     */
    private fun ConstraintSet.setConstraintsForAllAnchors() {
        stepViews.forEach {
            connect(it.anchorViewId, ConstraintSet.TOP, it.imageViewId, ConstraintSet.BOTTOM)
            connect(it.anchorViewId, ConstraintSet.START, it.imageViewId, ConstraintSet.START)
            connect(it.anchorViewId, ConstraintSet.END, it.imageViewId, ConstraintSet.END)
        }
    }

    /**
     *  Inactive bar has to go from first to last step's anchor
     *  Anchor makes it easier to position horizontally to middle of imageView
     */
    private fun ConstraintSet.setConstraintsForInactiveBar() {
        val firstAnchor = stepViews.first().anchorViewId
        val lastAnchor = stepViews.last().anchorViewId
        connect(inactiveBarId, ConstraintSet.TOP, firstAnchor, ConstraintSet.BOTTOM)
        connect(inactiveBarId, ConstraintSet.START, firstAnchor, ConstraintSet.START)
        connect(inactiveBarId, ConstraintSet.END, lastAnchor, ConstraintSet.END)
    }

    /**
     * Initial active bar is constrained to first step, meaning it will not be visible
     */
    private fun ConstraintSet.setConstraintsForActiveBar() {
        val firstAnchor = stepViews.first().anchorViewId
        connect(activeBarId, ConstraintSet.TOP, inactiveBarId, ConstraintSet.TOP)
        connect(activeBarId, ConstraintSet.BOTTOM, inactiveBarId, ConstraintSet.BOTTOM)
        connect(activeBarId, ConstraintSet.START, firstAnchor, ConstraintSet.START)
        connect(activeBarId, ConstraintSet.END, firstAnchor, ConstraintSet.END)
    }

    /**
     * The last step is to create transitions to let motion layout know how to transform our views
     * Note that we have to set first transition to initialize first view's state
     */
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
        setTransition(firstTransition!!)
    }

    /**
     * We need to create constraints for all steps
     * We don't need to recreate every constraint possible, only things that has to be changes across steps
     */
    private fun createConstraintsForSteps() {
        stepConstraints.addAll(stepViews.map { StepConstraintSet(generateViewId(), it.createConstraintsForStep()) })
    }

    /**
     * The only things that changes in our example is active bar
     * Image's color animation is handled by our custom view, so we don't have to worry about that here
     */
    private fun StepView.createConstraintsForStep() = createConstraintSet {
        connect(activeBarId, ConstraintSet.START, stepViews.first().anchorViewId, ConstraintSet.START)
        connect(activeBarId, ConstraintSet.END, anchorViewId, ConstraintSet.END)
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
            if (index == stepViews.lastIndex) return@forEachIndexed
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

    private fun resetViews() {
        removeAllViews()
        stepViews.clear()
    }

    /**
     * To set the active step we just need to use our custom imageView function to trigger it's own motion layout
     * Then we can call transitionToState to let motion layout handle the transition
     */
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
