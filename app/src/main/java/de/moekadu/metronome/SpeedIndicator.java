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

package de.moekadu.metronome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.Vector;

public class SpeedIndicator extends View {

    private final int defaultHeight = Math.round(Utilities.Companion.dp2px(4));
    private final int defaultWidth = Math.round(Utilities.Companion.dp2px(100));

    private Paint paint = null;

    private int color = Color.BLACK;

    private int positionIndex = 0;
    private float position = 0.0f;
    private final ValueAnimator animatePosition = ValueAnimator.ofFloat(0.0f, 1.0f);

    private final Vector<Float> markPositions = new Vector<>();

    public SpeedIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public SpeedIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SpeedIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

//    public SpeedIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    private void init(Context context, @Nullable AttributeSet attrs) {

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SpeedIndicator);
            color = ta.getColor(R.styleable.SpeedIndicator_normalColor, Color.BLACK);
            ta.recycle();
        }

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);


        animatePosition.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                position = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animatePosition.setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();

        int desiredWidth = defaultWidth + getPaddingStart() + getPaddingEnd();
        int desiredHeight = defaultHeight + getPaddingTop() + getPaddingBottom();

        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //float width = positionStart + (positionEnd-positionStart) * position;
        float secEnd = positionIndex >= markPositions.size() ? 0 : markPositions.get(positionIndex);
        float secStart = positionIndex-1 >= markPositions.size() || positionIndex == 0 ? 0 : markPositions.get(positionIndex-1);
        float barEnd = secStart + (secEnd-secStart) * position;

//        float end = markPositions[positionIndex]

//        Log.v("Metronome", "pos: " +position);

        canvas.drawRect(0,0.0f*getHeight(), barEnd, 1.0f*getHeight(), paint);

//        Log.v("Metronome", "pos: "+ markPositions.size());
        for(Float mark : markPositions){
//            canvas.drawCircle(mark, getHeight()/2.0f, getHeight()/2.0f, markPaint);
            canvas.drawRect(mark, 0, mark+getHeight(), getHeight(), paint);
        }

    }

    public void animate(int positionIndex, float speed){

        this.positionIndex = positionIndex;
        animatePosition.setDuration(Utilities.Companion.speed2dt(speed));
        animatePosition.start();
    }

    public void setMarks(Vector<Float> positions){
        markPositions.clear();
        markPositions.addAll(positions);
        invalidate();
    }

    public void stopPlay(){
        animatePosition.pause();
        position = 0.0f;
        positionIndex = 0;
        invalidate();
    }
}
