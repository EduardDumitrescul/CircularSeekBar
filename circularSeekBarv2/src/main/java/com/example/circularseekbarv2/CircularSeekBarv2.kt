package com.example.circularseekbarv2

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import kotlin.math.*

private const val TAG = "CircularSeekBarv2"

class CircularSeekBarv2(context: Context, attrs: AttributeSet): FrameLayout(context, attrs) {

    private var mTrackView: View
    private var mActiveTrackView: View
    private var mThumbView: View

    init {
        View.inflate(context, R.layout.circula_seekbar_layout, this)
        mTrackView = findViewById(R.id.trackView)
        mActiveTrackView = findViewById(R.id.activeTrackView)
        mThumbView = findViewById(R.id.thumbView)
    }

    
}