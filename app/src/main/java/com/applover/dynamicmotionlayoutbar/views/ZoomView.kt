package com.applover.dynamicmotionlayoutbar.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.applover.dynamicmotionlayoutbar.R
import com.applover.dynamicmotionlayoutbar.utils.createConstraintSet
import com.applover.dynamicmotionlayoutbar.utils.dpToPx
import kotlin.collections.forEach

class ZoomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MotionLayout(context, attrs, defStyleAttr) {
    private val textviews = mutableListOf<TextView>()
    private lateinit var indicator: View
    private var selectViewId = 0
    private lateinit var scene: MotionScene
    /**
     * Initialize in the code as it is easier than passing list in xml file
     */
    fun initialize(zooms: List<Int>) {
        createViews(zooms)
        selectViewId = textviews[0].id
        createInitialConstraints()
        scene = MotionScene(this)
        setScene(scene)
        val  transition = scene.createTransition(createConstraintSet() to createConstraintSet())
        setTransition(transition)
        setTransitionDuration(2000)
        transitionToEnd()
    }

    private fun resetViews() {
        removeAllViews()
        textviews.clear()
    }

    private fun createViews(zooms: List<Int>) {
        resetViews()
        zooms.forEach {
            textviews.add(createZoomView(it))
        }
        indicator = View(context)
        indicator.id = generateViewId();
        indicator.setBackgroundResource(R.drawable.indicator_background)
        addView(indicator)
    }

    @SuppressLint("SetTextI18n")
    private fun createZoomView(zoom: Int): TextView {
        val text = TextView(context)
        val viewId = generateViewId()
        text.text = zoom.toString()
        text.id = viewId;
        text.textSize = 8.asDp().toFloat()
        text.setTextColor(ContextCompat.getColor(context,R.color.white))
        text.textAlignment = TEXT_ALIGNMENT_CENTER;
        val layoutParamsWrapMarginMedium = LayoutParams(54.asDp(), 32.asDp())
        addView(text, layoutParamsWrapMarginMedium)
        text.setOnClickListener{
            scene.definedTransitions.forEach { scene.removeTransition(it)}
            val startSet = updateConstraints()
            selectViewId=viewId
            val endSet = updateConstraints()
            val  transition = scene.createTransition(startSet to endSet)
            transition.duration = 200
            setTransition(transition)
            transitionToEnd()
        }
        return text;
    }

    fun createInitialConstraints() = createConstraintSet().apply {
        val viewIds = textviews.map { it.id }.toIntArray()
        createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, viewIds, null, ConstraintSet.CHAIN_SPREAD)
        viewIds.forEach {
            connect(it, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(it, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        }
        connect(indicator.id, ConstraintSet.END, selectViewId, ConstraintSet.END)
        connect(indicator.id, ConstraintSet.START, selectViewId, ConstraintSet.START)
        connect(indicator.id, ConstraintSet.TOP, selectViewId, ConstraintSet.TOP)
        connect(indicator.id, ConstraintSet.BOTTOM, selectViewId, ConstraintSet.BOTTOM)
        constrainWidth(indicator.id, ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(indicator.id, ConstraintSet.MATCH_CONSTRAINT)
        applyTo(this@ZoomView)
    }

    fun updateConstraints(): ConstraintSet = createConstraintSet {
        connect(indicator.id, ConstraintSet.END, selectViewId, ConstraintSet.END)
        connect(indicator.id, ConstraintSet.START, selectViewId, ConstraintSet.START)
        connect(indicator.id, ConstraintSet.TOP, selectViewId, ConstraintSet.TOP)
        connect(indicator.id, ConstraintSet.BOTTOM, selectViewId, ConstraintSet.BOTTOM)

    }

    fun stopTran(){
        scene.definedTransitions.forEach{scene.removeTransition(it)}
    }


    private fun Int.asDp() = context.dpToPx(this)

    /**
     * To create transition we need to link start and end constraint sets together
     * For such simple case we don't need to remember ids generated by generateViewId
     */
    private fun MotionScene.createTransition(sets: Pair<ConstraintSet, ConstraintSet>): MotionScene.Transition =
        TransitionBuilder.buildTransition(
            this,
            generateViewId(),
            generateViewId(),
            sets.first,
            generateViewId(),
            sets.second,
        )

}