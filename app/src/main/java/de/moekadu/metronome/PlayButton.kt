/*
 * Copyright 2019 Michael Moessner
 *
 * This file is part of Metronome.
 *
 * Metronome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Metronome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Metronome.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.moekadu.metronome

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import kotlin.math.*

class PlayButton(context : Context, attrs : AttributeSet?, defStyleAttr: Int)
    : ControlPanel(context, attrs, defStyleAttr) {
    companion object {
        const val STATUS_PLAYING = 1
        const val STATUS_PAUSED = 2
    }

    constructor(context : Context, attrs : AttributeSet? = null) : this(context, attrs, R.attr.controlPanelStyle)

    private var buttonStatus = STATUS_PAUSED

    private val circlePaint = Paint()

    private val pathPlayButton = Path()
    private val pathOuterCircle = Path()

    private var clickInitiated = false

    private var playPercentage = 0.0

    interface ButtonClickedListener {
        //void onButtonClicked();
        fun onPlay()
        fun onPause()
    }

    var buttonClickedListener : ButtonClickedListener? = null

    private val animateToPlay = ValueAnimator.ofFloat(0.0f, 1.0f)
    private val animateToPause = ValueAnimator.ofFloat(1.0f, 0.0f)

    init {
        circlePaint.isAntiAlias = true

        buttonClickedListener = null

        animateToPause.duration = 200
        animateToPlay.duration = 200

        animateToPause.addUpdateListener { animation ->
            playPercentage = (animation.animatedValue as Float).toDouble()
            invalidate()
        }

        animateToPlay.addUpdateListener { animation ->
            playPercentage = (animation.animatedValue as Float).toDouble()
            invalidate()
        }

        val outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view : View, outline : Outline) {
                val radius = innerRadius
                outline.setOval(centerX - radius, centerY - radius,
                        centerX + radius, centerY + radius)
            }
        }
        setOutlineProvider(outlineProvider)
    }

    @Override
    override fun onDraw(canvas : Canvas) {
        super.onDraw(canvas)

        circlePaint.color = backgroundTintList?.defaultColor ?: Color.WHITE

        circlePaint.style = Paint.Style.FILL

        pathOuterCircle.fillType = Path.FillType.EVEN_ODD
        pathOuterCircle.rewind()
        pathOuterCircle.addCircle(centerX.toFloat(), centerY.toFloat(), innerRadius.toFloat(), Path.Direction.CW)
        canvas.drawPath(pathOuterCircle, circlePaint)

        val triRad = innerRadius * 0.7f

        val xShift = innerRadius * 0.1f
        val rectWidth = innerRadius * 0.4f
        val rectHeight = innerRadius * 1f

        val phiPauseOuter = atan((0.5f * rectHeight) / (xShift + rectWidth))
        val phiPauseInner = atan((0.5f * rectHeight) / xShift)
        val radPauseOuter = sqrt((xShift + rectWidth).pow(2) + (0.5f * rectHeight).pow(2))
        val radPauseInner = sqrt(xShift.pow(2) + (0.5f * rectHeight).pow(2))

        circlePaint.style = Paint.Style.FILL
        if(clickInitiated) {
            circlePaint.color = highlightColor
        }
        else {
            circlePaint.color = labelColor
        }

        pathPlayButton.fillType = Path.FillType.EVEN_ODD

        var phi = playPercentage * phiPauseOuter + (1 - playPercentage) * 2.0 * PI
        var r = playPercentage * radPauseOuter + (1 - playPercentage) * triRad
        pathPlayButton.moveTo(pTX(phi, r), pTY(phi, r))

        phi = playPercentage * phiPauseInner + (1 - playPercentage) * 2.0 * PI
        r = playPercentage * radPauseInner + (1 - playPercentage) * triRad
        pathPlayButton.lineTo(pTX(phi, r), pTY(phi, r))

        phi = playPercentage * (-phiPauseInner) + (1 - playPercentage) * 2.0 * PI / 2.0
        r = playPercentage * radPauseInner + (1 - playPercentage) * triRad * cos(2.0 * PI / 3.0 + PI)
        pathPlayButton.lineTo(pTX(phi, r), pTY(phi, r))

        phi = playPercentage * (-phiPauseOuter) + (1 - playPercentage) * 4.0 * PI / 3.0
        r = playPercentage * radPauseOuter + (1 - playPercentage) * triRad
        pathPlayButton.lineTo(pTX(phi, r), pTY(phi, r))

        pathPlayButton.close()

        phi = playPercentage * (PI - phiPauseOuter) + (1 - playPercentage) * 2.0 * PI
        r = playPercentage * radPauseOuter + (1 - playPercentage) * triRad
        pathPlayButton.moveTo(pTX(phi, r), pTY(phi, r))

        phi = playPercentage * (PI - phiPauseInner) + (1 - playPercentage) * 2.0 * PI
        r = playPercentage * radPauseInner + (1 - playPercentage) * triRad
        pathPlayButton.lineTo(pTX(phi, r), pTY(phi, r))

        phi = playPercentage * (phiPauseInner - PI) + (1 - playPercentage) * 4.0 * PI / 4.0
        r = playPercentage * radPauseInner + (1 - playPercentage) * triRad * cos(4.0 * PI / 3.0 + PI)
        pathPlayButton.lineTo(pTX(phi, r), pTY(phi, r))

        phi = playPercentage * (phiPauseOuter - PI) + (1 - playPercentage) * 2.0 * PI / 3.0
        r = playPercentage * radPauseOuter + (1 - playPercentage) * triRad
        pathPlayButton.lineTo(pTX(phi, r), pTY(phi, r))

        pathPlayButton.close()

        canvas.drawPath(pathPlayButton, circlePaint)
        pathPlayButton.rewind()
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {

        val action = event.actionMasked
        val x = event.x - centerX
        val y = event.y - centerY

        val radiusXY = sqrt(x*x + y*y).roundToInt()

        when(action) {
            MotionEvent.ACTION_DOWN -> {
                if (radiusXY < innerRadius) {
                    clickInitiated = true
                } else {
                    return false
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (clickInitiated && radiusXY > innerRadius) {
                    clickInitiated = false
                    invalidate()
                    return false
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (clickInitiated && radiusXY < innerRadius) {
                    performClick()
                }
                clickInitiated = false
                invalidate()
            }
        }

        return true
    }

    override fun performClick() : Boolean {
        if (buttonStatus == STATUS_PAUSED) {
            // Log.v("Metronome", "PlayButton:GestureTap:onSingleTapConfirmed() : trigger onPlay");
            buttonClickedListener?.onPlay()
        }
        else {
            // Log.v("Metronome", "PlayButton:GestureTap:onSingleTapConfirmed() : trigger onPause");
            buttonClickedListener?.onPause()
         }
         return super.performClick()
    }

    fun changeStatus(status : Int, animate : Boolean){
        if(buttonStatus == status)
            return

        // Log.v("Metronome", "changeStatus: changing button status");
        buttonStatus = status
        if(status == STATUS_PAUSED) {
            //playPercentage = 0.0
            if(animate) {
                animateToPause.start()
            }
            else {
                playPercentage = 0.0
            }
        }
        else if(status == STATUS_PLAYING) {
            // playPercentage = 1.0
            if(animate) {
                animateToPlay.start()
            }
            else {
                playPercentage = 1.0
            }
        }
        invalidate()
    }
}