package com.arekaga.shimdance;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

public class CanvasView extends View {

    //region Selector
    private final int mSelectorPadding = 4;
    private final int mSelectorHeight = 120;
    private final float mSelectorStrokeWidth = 3.0f;

    private ShapeDrawable mSelector;
    private ShapeDrawable mSelectorShadow;
    //endregion

    //region Arrows
    private final int mArrowSize = 80;

    private Drawable mArrowUp;
    private Drawable mArrowDown;
    private Drawable mArrowRight;
    private Drawable mArrowLeft;
    //endregion

    public CanvasView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        // arrows selector
        mSelector = new ShapeDrawable(new RectShape());
        mSelector.getPaint().setColor(getResources().getColor(android.R.color.white));
        mSelector.getPaint().setStyle(Paint.Style.STROKE);
        mSelector.getPaint().setStrokeWidth(mSelectorStrokeWidth);
        mSelector.setBounds(mSelectorPadding, mSelectorPadding,
                right - left - mSelectorPadding, mSelectorHeight - mSelectorPadding);

        mSelectorShadow = new ShapeDrawable(new RectShape());
        mSelectorShadow.getPaint().setColor(getResources().getColor(android.R.color.darker_gray));
        mSelectorShadow.getPaint().setStyle(Paint.Style.STROKE);
        mSelectorShadow.getPaint().setStrokeWidth(2*mSelectorStrokeWidth);
        mSelectorShadow.setBounds(0, 0, right - left, mSelectorHeight - mSelectorPadding);

        // create arrows
        Resources res = getContext().getResources();
        mArrowUp = res.getDrawable(R.drawable.arrow_up);
        mArrowDown = res.getDrawable(R.drawable.arrow_down);
        mArrowRight = res.getDrawable(R.drawable.arrow_right);
        mArrowLeft = res.getDrawable(R.drawable.arrow_left);

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw selector
        mSelectorShadow.draw(canvas);
        mSelector.draw(canvas);

        int padding = mSelectorPadding * 5;
        int spacesBetween = (this.getWidth() - mArrowSize*4)/5;

        // draw arrows
        drawArrow(mArrowUp, canvas, spacesBetween, padding);
        drawArrow(mArrowDown, canvas, 2*spacesBetween+mArrowSize, padding);
        drawArrow(mArrowRight, canvas, 3*spacesBetween+2*mArrowSize, padding);
        drawArrow(mArrowLeft, canvas, 4*spacesBetween+3*mArrowSize, padding);
    }

    private void drawArrow(Drawable arrow, Canvas canvas, int x, int y) {
        arrow.setBounds(x, y, x + mArrowSize, y + mArrowSize);
        arrow.draw(canvas);
    }
}
