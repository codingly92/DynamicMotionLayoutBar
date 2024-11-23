package com.applover.dynamicmotionlayoutbar.views;


import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

import com.applover.dynamicmotionlayoutbar.R;

public class RotateTextView extends AppCompatTextView {

    private int angle;

    private int mOriginWidth = 0;
    private int mOriginHeight = 0;
    PathMeasure mPathMeasure = new PathMeasure();
    Path mRoundRectPath = new Path();
    Path mPath;
    Matrix mMatrix = new Matrix();
    RectF mBoundRect = new RectF();
    float[] pos = new float[2];
    float[] tan = new float[2];

    public RotateTextView(Context context) {
        this(context, null);
    }


    public RotateTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        angle = 0;
        mOriginWidth = dp2px(context, 54);
        mOriginHeight = dp2px(context, 32);
        mRoundRectPath.addRoundRect(0, 0, mOriginWidth, mOriginHeight, dp2px(context, 16), dp2px(context, 16), Path.Direction.CCW);
        mPath = new Path(mRoundRectPath);
    }

    /**
     * Returns current angle of this layout
     */
    public int getAngle() {
        return angle;
    }

    /**
     * Sets current angle of this layout.
     */
    public void setAngle(int angle) {
        if (this.angle != angle) {
            this.angle = angle;
            setRotation(-angle);
            invalidate();
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (abs(angle % 180) == 90) {
            setMeasuredDimension(
                    resolveSize(mOriginHeight, widthMeasureSpec),
                    resolveSize(mOriginWidth, heightMeasureSpec));
        } else if (abs(angle % 180) == 0) {
            setMeasuredDimension(
                    resolveSize(mOriginWidth, widthMeasureSpec),
                    resolveSize(mOriginHeight, heightMeasureSpec));
        } else {
            mMatrix.reset();
            mPath.set(mRoundRectPath);
            mPath.computeBounds(mBoundRect, true);
            mMatrix.postRotate(angle, mBoundRect.centerX(), mBoundRect.centerY());
            mPath.transform(mMatrix);
            mPathMeasure.setPath(mPath, false);
            float minX = 0, minY = 0, maxX = 0, maxY = 0;
            for (int len = 0; len < mPathMeasure.getLength(); len++) {
                mPathMeasure.getPosTan(len, pos, tan);
                pos[1] = Math.max(0,pos[1]);
                minX = Math.min(minX, pos[0]);
                minY = Math.min(minY, pos[1]);
                maxX = Math.max(maxX, pos[0]);
                maxY = Math.max(maxY, pos[1]);
            }
            Log.d("liuyang", "angle " + angle
                    + " minX:" + minX
                    + " minY:" + minY
                    + " maxX:" + maxX
                    + " maxY:" + maxY
            );


            int measuredWidth = (int) (maxX - minX);
            int measuredHeight = (int) (maxY - minY);

            setMeasuredDimension(
                    resolveSize(measuredWidth, widthMeasureSpec),
                    resolveSize(measuredHeight, heightMeasureSpec));
        }

    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.rotate(-angle, getWidth() / 2f, getHeight() / 2f);
        super.dispatchDraw(canvas);
        canvas.restore();
    }


    /**
     * Circle angle, from 0 to TAU
     */
    private Double angle_c() {
        // True circle constant, not that petty imposter known as "PI"
        double TAU = 2 * PI;
        return TAU * angle / 360;
    }

    /**
     * dp转px
     */
    private static int dp2px(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }


}
