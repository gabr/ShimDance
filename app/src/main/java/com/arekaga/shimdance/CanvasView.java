package com.arekaga.shimdance;

import android.content.Context;
import android.graphics.Canvas;
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
    private boolean mPaused = true;
    private Handler mAnimationHandler = new Handler();
    private Runnable mAcceptAnimation = new Runnable() {
        @Override
        public void run() {
            mSelectorColor = getResources().getColor(android.R.color.white);
            mSelectorShadowColor = getResources().getColor(android.R.color.darker_gray);
        }
    };
    //endregion

    public CanvasView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        mRandomGenerator = new Random(System.currentTimeMillis());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        // create arrows
        Arrow.setUp(getContext(), left, top, right, bottom);

        // manage arrows
        if (!mPaused) {
            addRemoveArrows();
        }

        // arrows selector
        mSelectorHeight = Arrow.getSize() + 6*mSelectorPadding;

        mSelector = new ShapeDrawable(new RectShape());
        mSelector.getPaint().setStyle(Paint.Style.STROKE);
        mSelector.getPaint().setStrokeWidth(mSelectorStrokeWidth);
        mSelector.setBounds(mSelectorPadding, mSelectorPadding,
                right - left - mSelectorPadding, mSelectorHeight - mSelectorPadding);

        mSelectorShadow = new ShapeDrawable(new RectShape());
        mSelectorShadow.getPaint().setStyle(Paint.Style.STROKE);
        mSelectorShadow.getPaint().setStrokeWidth(2*mSelectorStrokeWidth);
        mSelectorShadow.setBounds(0, 0, right - left, mSelectorHeight - mSelectorPadding/2);

        // call default method
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int moveArrowBy = (mPaused ? 0 : mArrowSpeed);

        // draw arrows
        for (Arrow a: mArrowsOnScreen)
            a.drawNextPosition(canvas, moveArrowBy);

        // check arrows in range
        setArrowInRange();

        // draw selector
        mSelector.getPaint().setColor(mSelectorColor);
        mSelectorShadow.getPaint().setColor(mSelectorShadowColor);
        mSelectorShadow.draw(canvas);
        mSelector.draw(canvas);

        // animate
        mAnimationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        }, 1000 / 25);
    }

    public void start() {
        mPaused = false;
        mSelectorColor = getResources().getColor(android.R.color.white);
    }

    public void pause() {
        mPaused = false;
    }

    public void end(int score) {
        clear();
    }

    public void clear() {
        mPaused = true;
        mArrowsOnScreen.clear();
    }

    public void acceptArrow() {
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
