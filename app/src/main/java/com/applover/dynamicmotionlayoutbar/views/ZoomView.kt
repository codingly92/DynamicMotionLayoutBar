package com.applover.dynamicmotionlayoutbar.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
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
    private val mZoomIndicators = mutableListOf<RotateLayout>()
    private lateinit var mSelectIndicator: RotateLayout
    private var mSelectZoomIndicatorViewId = 0
    private var mAngle = 0
    private lateinit var mMotionScene: MotionScene
    /**
     * Initialize in the code as it is easier than passing list in xml file
     */
    fun initialize(zooms: List<Int>) {
        createViews(zooms)
        mSelectZoomIndicatorViewId = mZoomIndicators[0].id
        getConstraints().applyTo(this)
        mMotionScene = MotionScene(this)
        setScene(mMotionScene)
        val  transition = mMotionScene.createTransition(createConstraintSet() to createConstraintSet())
        setTransition(transition)
        setTransitionDuration(2000)
        transitionToEnd()
    }

    private fun resetViews() {
        removeAllViews()
        mZoomIndicators.clear()
    }

    private fun createViews(zooms: List<Int>) {
        resetViews()
        zooms.forEach {
            mZoomIndicators.add(createZoomView(it))
        }

        mSelectIndicator = RotateLayout(context)
        mSelectIndicator.id = generateViewId();
        val rotateLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        val selectorChild = View(context)
        selectorChild.id = generateViewId();
        selectorChild.setBackgroundResource(R.drawable.indicator_background)
        val selectorChildLayoutParams = ViewGroup.LayoutParams(54.asDp(),32.asDp())
        mSelectIndicator.addView(selectorChild,selectorChildLayoutParams)
        addView(mSelectIndicator,rotateLayoutParams)
    }

    @SuppressLint("SetTextI18n")
    private fun createZoomView(zoom: Int): RotateLayout {
        val rotateLayout = RotateLayout(context)
        val rotateId = generateViewId();
        rotateLayout.id = rotateId;
        val rotateLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        addView(rotateLayout, rotateLayoutParams)

        val text = TextView(context)
        val viewId = generateViewId()
        text.text = zoom.toString()
        text.id = viewId;
        text.textSize = 8.asDp().toFloat()
        text.setTextColor(ContextCompat.getColor(context,R.color.white))
        text.textAlignment = TEXT_ALIGNMENT_CENTER;
        text.setOnClickListener{
            mMotionScene.definedTransitions.forEach { mMotionScene.removeTransition(it)}
            val startSet = getConstraints()
            mSelectZoomIndicatorViewId=rotateId
            val endSet = getConstraints()
            val  transition = mMotionScene.createTransition(startSet to endSet)
            transition.duration = 200
            setTransition(transition)
            transitionToEnd()
        }
        val textLayoutParams = ViewGroup.LayoutParams(54.asDp(),32.asDp())
        rotateLayout.addView(text,textLayoutParams)
        rotateLayout.angle = mAngle
        return rotateLayout;
    }

    fun getConstraints() = createConstraintSet().apply {
        val viewIds = mZoomIndicators.map { it.id }.toIntArray()
        createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, viewIds, null, ConstraintSet.CHAIN_SPREAD)
        viewIds.forEach {
            connect(it, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(it, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            constrainWidth(it, if (mAngle % 180 == 0 ) 54.asDp() else 32.asDp())
            constrainHeight(it, if (mAngle % 180 == 0 ) 32.asDp() else 54.asDp())
            setIntValue(it,"Angle",mAngle)
        }
        connect(mSelectIndicator.id, ConstraintSet.END, mSelectZoomIndicatorViewId, ConstraintSet.END)
        connect(mSelectIndicator.id, ConstraintSet.START, mSelectZoomIndicatorViewId, ConstraintSet.START)
        connect(mSelectIndicator.id, ConstraintSet.TOP, mSelectZoomIndicatorViewId, ConstraintSet.TOP)
        connect(mSelectIndicator.id, ConstraintSet.BOTTOM, mSelectZoomIndicatorViewId, ConstraintSet.BOTTOM)
        constrainWidth(mSelectIndicator.id, if (mAngle % 180 == 0 ) 54.asDp() else 32.asDp())
        constrainHeight(mSelectIndicator.id, if (mAngle % 180 == 0 ) 32.asDp() else 54.asDp())
        setIntValue(mSelectIndicator.id,"Angle",mAngle)
    }



    fun tranAngle(){
        mMotionScene.definedTransitions.forEach { mMotionScene.removeTransition(it)}
        val startSet = getConstraints()
        mAngle = (mAngle + 90)
        val endSet = getConstraints()
        val  transition = mMotionScene.createTransition(startSet to endSet)
        transition.duration = 2000
        setTransition(transition)
        transitionToEnd()
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
