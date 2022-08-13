package com.example.circularseekbar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.NumberPicker
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.*


private const val TAG = "CircularSeekBar"


class CircularSeekBar(context: Context, attrs: AttributeSet): View(context, attrs) {

    companion object {
        const val MODE_LOCKED: Int = 0
        const val MODE_UNLOCKED: Int = 1
    }

    private var mTrackThickness: Float = 0f // dp
    private var mTrackColor: Int = Color.RED
    private var mTrackThicknessActive = 0f // dp
    private var mTrackColorActive: Int = Color.CYAN
    private var mCenterX: Float= 0F
    private var mCenterY: Float = 0F
    private var mRadius: Float = 0F
    private var mPadding: Int = 20

    private var mThumbThickness: Float = 0f
    private var mThumbColor: Int = Color.GREEN

    //0 degrees is considered 12 o'clock
    private var mStartAngle: Float = 0f

    private var mOnChangeListener: OnChangeListener? = null

    private var mValue: Float = 0f

    private var mTargetValue: Float = 0f
    private var mMaxValue: Int = 100
    private var mStepSize: Int = 3

    private var isThumbVisible: Boolean = true
    private var isLocked: Boolean = false

    fun getTrackThickness() = mTrackThickness
    fun setTrackThickness(thickness: Float) {
        mTrackThickness = thickness
        initPaints()
        invalidate()
    }

    fun getTrackColor() = mTrackColor
    fun setTrackColor(color: Int) {
        mTrackColor = color
        initPaints()
        invalidate()
    }

    fun getTrackThicknessActive() = mTrackThicknessActive
    fun setTrackThicknessActive(thickness: Float) {
        mTrackThicknessActive = thickness
        initPaints()
        invalidate()
    }

    fun getTrackColorActive() = mTrackColorActive
    fun setTrackColorActive(color: Int) {
        mTrackColorActive = color
        initPaints()
        invalidate()
    }

    fun getThumbThickness() = mThumbThickness
    fun setThumbThickness(thickness: Float) {
        mThumbThickness = thickness
        invalidate()
    }

    fun getThumbColor() = mThumbColor
    fun setThumbColor(color: Int) {
        mThumbColor = color
        initPaints()
        invalidate()
    }

    fun getStartAngle() = mStartAngle
    fun setStartAngle(angle: Float) {
        mStartAngle = angle
        invalidate()
    }

    fun getMaxValue() = mMaxValue
    fun setMaxValue(value: Int) {
        mMaxValue = value
        invalidate()
    }

    fun getStepSize() = mStepSize
    fun setStepSize(value: Int) {
        mStepSize = value
        invalidate()
    }

    fun getValue() = getFixedValue(mValue)
    fun setValue(value: Number) {
        mValue = value.toFloat()
        mOnChangeListener?.onValueChangeDetected(value.toInt())
        invalidate()
    }

    fun getIsThumbVisible() = isThumbVisible
    fun setIsThumbVisible(value: Boolean) {
        isThumbVisible = value
        invalidate()
    }

    fun getIsLocked() = isLocked
    fun setIsLocked(mode: Int) {
        if(mode == MODE_LOCKED) {
            isLocked = true
            return
        }
        if(mode == MODE_UNLOCKED) {
            isLocked = false
            return
        }
        Log.d(TAG, "setMode() - unrecognized mode")
    }

    fun setOnChangeListener(listener: OnChangeListener?) {
        mOnChangeListener = listener
    }


    private var valueAnimator: ValueAnimator = ValueAnimator.ofFloat().apply {
        duration = 180
        addUpdateListener {
            if(mValue == it.animatedValue) {
                return@addUpdateListener
            }
            mValue = it.animatedValue as Float
            updateListeners(mValue)

            invalidate()
        }
        interpolator = LinearInterpolator()
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircularSeekBar,
            0, 0).apply {

            Log.d(TAG, "init")
            val trackThickness = getDimension(R.styleable.CircularSeekBar_trackThickness, 8f)
            setTrackThickness(trackThickness)
            val trackColor = getColor(R.styleable.CircularSeekBar_trackColor, Color.BLUE)
            setTrackColor(trackColor)
            val trackThicknessActive = getDimension(R.styleable.CircularSeekBar_trackThicknessActive, 4f)
            setTrackThicknessActive(trackThicknessActive)
            val trackColorActive = getColor(R.styleable.CircularSeekBar_trackColorActive, Color.CYAN)
            setTrackColorActive(trackColorActive)

            val thumbThickness = getDimension(R.styleable.CircularSeekBar_thumbThickness, 16f)
            setThumbThickness(thumbThickness)
            val thumbColor = getColor(R.styleable.CircularSeekBar_thumbColor, Color.GREEN)
            setThumbColor(thumbColor)

            val startAngle = getFloat(R.styleable.CircularSeekBar_startAngle, 0f)
            setStartAngle(startAngle)

            val thumbVisible = getBoolean(R.styleable.CircularSeekBar_thumbVisibility, true)
            setIsThumbVisible(thumbVisible)
            val stepSize = getInt(R.styleable.CircularSeekBar_stepSize, 5)
            setStepSize(stepSize)
            val maxValue = getInt(R.styleable.CircularSeekBar_maxValue, 100)
            setMaxValue(maxValue)



//                mShowText = getBoolean(R.styleable.PieChart_showText, false)
//                textPos = getInteger(R.styleable.PieChart_labelPosition, 0)
            recycle()
        }
        val sum = paddingBottom + paddingEnd + paddingLeft + paddingRight + paddingStart + paddingTop
        mPadding = sum / 6

        initPaints()
    }

    private lateinit var mTrackPaint: Paint
    private lateinit var mTrackActivePaint: Paint
    private lateinit var mThumbPaint: Paint

    private fun initPaints() {
        mTrackPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = mTrackColor
            strokeWidth = mTrackThickness
            isAntiAlias = true
        }
        mTrackActivePaint = Paint().apply {
            style = Paint.Style.STROKE
            color = mTrackColorActive
            strokeWidth = mTrackThicknessActive
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        mThumbPaint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = mThumbColor
            isAntiAlias = true
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val smallerDim: Float = min(w, h).toFloat()
        mCenterX = (w / 2).toFloat()
        mCenterY = (h / 2).toFloat()
        mRadius = smallerDim / 2 - mTrackThickness / 2 - mPadding
        super.onSizeChanged(w, h, oldw, oldh)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            drawCircle(mCenterX, mCenterY, mRadius, mTrackPaint)
        }

        var angle: Float = getAngleFromValue(mValue)
        if(angle == 0f) angle = 0.1f
        if(angle >= 355f) angle = 355f
        canvas.drawArc(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius, mStartAngle - 90, angle, false, mTrackActivePaint)

        if(isThumbVisible) {
            val thumbPosition = getThumbPositionFromAngle(mStartAngle + angle)
            canvas.drawCircle(thumbPosition.first, thumbPosition.second, mThumbThickness, mThumbPaint)
        }


    }

    private fun moveThumb(x: Float, y: Float) {
        val angle: Float = getAngleFromPosition(x, y)
        var index: Int = getIndexFromAngle(angle)
        val angle1 = getAngleFromIndex(index)
        val angle2 = getAngleFromIndex(index + 1)
        if(distance(getThumbPositionFromAngle(angle1), Pair(x, y)) > distance(getThumbPositionFromAngle(angle2), Pair(x, y))) {
            index ++
        }

        val newValue = getValueFromIndex(index)
        if(abs(mValue - newValue) > mMaxValue / 2)
            return
        if(newValue == mTargetValue)
            return
        mTargetValue = newValue
        initAnimation(mTargetValue)
    }

    private fun distance(p1: Pair<Float, Float>, p2: Pair<Float, Float>): Float {
        return sqrt((p1.first - p2.first) * (p1.first - p2.first) + (p1.second - p2.second) * (p1.second - p2.second))
    }

    private fun getFixedValue(value: Float): Int {
        val index: Int = (value / mStepSize).toInt()
        if(value - index * mStepSize < (index + 1) * mStepSize - value) {
            return index * mStepSize
        }
        else {
            return (index + 1) * mStepSize
        }
    }

    private fun getThumbPositionFromValue(value: Float) = getThumbPositionFromAngle(getAngleFromValue(value))

    private fun getThumbPositionFromAngle(angle: Float): Pair<Float, Float> {
        val x = mCenterX + mRadius * cos(radians(angle - 90))
        val y = mCenterY + mRadius * sin(radians(angle - 90))
        return Pair(x, y)
    }

    private fun radians(degrees: Float): Float = degrees * PI.toFloat() / 180

    private fun stepCount() = (mMaxValue / mStepSize)

    private fun getAngleFromIndex(index: Int): Float {
        return (360 * index / stepCount()).toFloat() // in degrees
    }
    private fun getIndexFromAngle(angle: Float): Int {
        return (stepCount() * angle / 360).toInt()
    }
    private fun getAngleFromValue(value: Float): Float {
        return (360f * (value) / (mMaxValue))
    }
    private fun getIndexFromValue(value: Float): Int {
        return getIndexFromAngle(getAngleFromValue(value))
    }
    private fun getValueFromIndex(index: Int): Float {
        return (index * mStepSize).toFloat()
    }

    /* In degrees */
    private fun getAngleFromPosition(x: Float, y:Float): Float {
        val dx = x - mCenterX
        val dy = mCenterY - y
        var angle = (atan2(dx, dy) * 180 / PI.toFloat())
        if(angle < 0) {
            angle += 360
        }
        return angle
    }

    private fun initAnimation(targetValue: Float) {
        valueAnimator.cancel()
        valueAnimator.setFloatValues(mValue, targetValue)
        valueAnimator.start()
    }

    private var mThumbSelected: Boolean = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(isLocked) {
            return true
        }
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {

                val thumbPosition = getThumbPositionFromValue(mValue)
                if(thumbPosition.first - mThumbThickness <= event.x && event.x <= thumbPosition.first + mThumbThickness &&
                    thumbPosition.second - mThumbThickness <= event.y && event.y <= thumbPosition.second + mThumbThickness) {
                    mThumbSelected = true
                }

            }
            MotionEvent.ACTION_MOVE -> {
                if(mThumbSelected) {
                    moveThumb(event.x, event.y)
                }
            }
            MotionEvent.ACTION_UP -> {
                mThumbSelected = false
            }

        }
        return true

    }

    private var lastListenerValue: Int = -1
    private fun updateListeners(value: Float) {
        val intValue = getFixedValue(value)
        if(lastListenerValue == intValue)
            return
        lastListenerValue = intValue
        Log.d(TAG, "updateListeners() $intValue")
        mOnChangeListener?.onValueChangeDetected(intValue)

    }

    interface OnChangeListener {
        fun onValueChangeDetected(value: Int)
    }

}
