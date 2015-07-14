package com.arekaga.shimdance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;
import android.os.Handler;

public class CanvasView extends View {

    //region onSelectedArrowChangeListener
    public interface SelectedArrowChangeListener {
        public void onSelectedArrowChange(Arrow selectedArrow);
    }
    //endregion

    //region Stage
    private enum Stage { init, pause, play, end };
    private static Stage mStage;
    //endregion

    //region Selector
    private final int mSelectorPadding = 4;
    private final float mSelectorStrokeWidth = 3.0f;
    private int mSelectorHeight = 120;
    private int mSelectorColor;
    private int mSelectorShadowColor;

    private ShapeDrawable mSelector;
    private ShapeDrawable mSelectorShadow;
    //endregion

    //region Arrows
    private static final int mArrowSpeed = 10;
    private static Random mRandomGenerator;
    private Arrow mSelectedArrow = null;
    private ArrayList<Arrow> mArrowsOnScreen = new ArrayList<Arrow>();

    private SelectedArrowChangeListener mSelectedArrowChangeListener;
    //endregion

    //region Animation
    private Handler mAnimationHandler = new Handler();
    private Runnable mAcceptAnimation = new Runnable() {
        @Override
        public void run() {
            mSelectorColor = getResources().getColor(android.R.color.white);
            mSelectorShadowColor = getResources().getColor(android.R.color.darker_gray);
        }
    };
    private Runnable mInitAnimation = new Runnable() {
        @Override
        public void run() {
            ++mInitCounter;
        }
    };
    //endregion

    //region Text
    private Paint mTextPaint = new Paint();
    private static final int TEXT_Y = 200;
    private static final int INIT_X = 200;
    private static final int END_X = 90;
    private static final float TEXT_SIZE = 70;
    private static final double INIT_END = 3.5;

    private int mScore;
    private static double mInitCounter = 1.0;
    private static final double INIT_INCREMETN = 0.050;
    //endregion

    public CanvasView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        mRandomGenerator = new Random(System.currentTimeMillis());
        mStage = Stage.end;

        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(TEXT_SIZE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        // create arrows
        Arrow.setUp(getContext(), left, top, right, bottom);

        if (mStage == Stage.play) {
            // manage arrows
            addRemoveArrows();
        }

        // arrows selector
        mSelectorHeight = Arrow.getSize() + 6 * mSelectorPadding;

        mSelector = new ShapeDrawable(new RectShape());
        mSelector.getPaint().setStyle(Paint.Style.STROKE);
        mSelector.getPaint().setStrokeWidth(mSelectorStrokeWidth);
        mSelector.setBounds(mSelectorPadding, mSelectorPadding,
                right - left - mSelectorPadding, mSelectorHeight - mSelectorPadding);

        mSelectorShadow = new ShapeDrawable(new RectShape());
        mSelectorShadow.getPaint().setStyle(Paint.Style.STROKE);
        mSelectorShadow.getPaint().setStrokeWidth(2 * mSelectorStrokeWidth);
        mSelectorShadow.setBounds(0, 0, right - left, mSelectorHeight - mSelectorPadding / 2);

        // call default method
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mStage == Stage.init || (mInitCounter < INIT_END && mStage == Stage.pause)) {
            if (mStage != Stage.pause) {
                mInitCounter += INIT_INCREMETN;
            }
            canvas.drawText((int)mInitCounter + "", INIT_X, TEXT_Y, mTextPaint);
            if (mInitCounter >= INIT_END) {
                mStage = Stage.play;
            }
        }

        if (mStage == Stage.end) {
            canvas.drawText("Score: " + mScore, END_X, TEXT_Y, mTextPaint);
        }

        if ((mStage == Stage.pause || mStage == Stage.play) && mInitCounter > 3.5) {
            int moveArrowBy = (mStage == Stage.pause ? 0 : mArrowSpeed);

            // draw arrows
            for (Arrow a : mArrowsOnScreen)
                a.drawNextPosition(canvas, moveArrowBy);

            // check arrows in range
            setArrowInRange();

            // draw selector
            mSelector.getPaint().setColor(mSelectorColor);
            mSelectorShadow.getPaint().setColor(mSelectorShadowColor);
            mSelectorShadow.draw(canvas);
            mSelector.draw(canvas);
        }

        // animate
        mAnimationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        }, 1000 / 25);
    }

    public void start() {
        if (mInitCounter < INIT_END) {
            mStage = Stage.init;
        } else {
            mStage = Stage.play;
        }
    }

    public void pause() {
        mStage = Stage.pause;
    }

    public void end(int score) {
        mStage = Stage.end;
        mScore = score;
        mInitCounter = 1.0;
        clear();
    }

    public void clear() {
        mArrowsOnScreen.clear();
    }

    public void acceptArrow() {
        if (mStage != Stage.play) {
            return;
        }

        mSelectorColor = getResources().getColor(android.R.color.holo_green_light);
        mSelectorShadowColor = getResources().getColor(android.R.color.holo_green_light);

        mAnimationHandler.removeCallbacks(mAcceptAnimation);
        mAnimationHandler.postDelayed(mAcceptAnimation, 900);
    }

    private void addRemoveArrows() {
        // remove not visible arrows
        for (int i = 0; i < mArrowsOnScreen.size(); i++) {
            if (mArrowsOnScreen.get(i).isVisible() == false) {
                mArrowsOnScreen.remove(i);
                --i;
            }
        }

        // randomly generate new arrow only if there is space fo it
        if (mArrowsOnScreen.size() == 0 || mArrowsOnScreen.get(mArrowsOnScreen.size() -1).getY() < Arrow.getStartY() - Arrow.getSize()) {
            if (mRandomGenerator.nextBoolean()) {
                mArrowsOnScreen.add(new Arrow(Arrow.Type.fromInt(mRandomGenerator.nextInt(4))));
            }
        }
    }

    private void setArrowInRange() {

        if (mSelectorShadowColor != getResources().getColor(android.R.color.holo_green_light)) {
            mSelectorShadowColor = getResources().getColor(android.R.color.darker_gray);
            mSelectorColor = getResources().getColor(android.R.color.white);
        }

        for (Arrow a: mArrowsOnScreen) {
            if (a.getY() < mSelectorHeight && a.getY() > mSelector.getBounds().top) {

                if (mSelectorShadowColor != getResources().getColor(android.R.color.holo_green_light)) {
                    mSelectorShadowColor = getResources().getColor(android.R.color.white);
                }

                if (a != mSelectedArrow) {
                    mSelectedArrow = a;
                    if (mSelectedArrowChangeListener != null) {
                        mSelectedArrowChangeListener.onSelectedArrowChange(mSelectedArrow);
                    }
                }

                return;
            }
        }

        mSelectedArrow = null;
        if (mSelectedArrowChangeListener != null) {
            mSelectedArrowChangeListener.onSelectedArrowChange(mSelectedArrow);
        }
    }
}
