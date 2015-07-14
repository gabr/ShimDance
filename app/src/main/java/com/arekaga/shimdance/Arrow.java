package com.arekaga.shimdance;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.util.Hashtable;

public class Arrow {

    //region Static part
    public enum Type {
        up,
        down,
        left,
        right;

        public static Type fromInt(int x) {
            switch (x) {
                case 0: return up;
                case 1: return down;
                case 2: return left;
                case 3: return right;
            }

            return null;
        }
    }

    private static final float mArrowPercentageWidth = 0.2f;
    private static final float mArrowPercentageSpace = 0.04f;

    private static int mWidth;
    private static int mSpace;

    private static int mYStart;
    private static int mYEnd;

    private static Hashtable<Type, Drawable> mArrows;
    private static Hashtable<Type, Integer> mXPosition;

    public static void setUp(Context context, int left, int top, int right, int bottom) {
        mWidth = (int)(mArrowPercentageWidth * (right - left));
        mSpace = (int)(mArrowPercentageSpace * (right - left));

        mYStart = bottom - top;
        mYEnd = -mWidth;

        mArrows = new Hashtable<Type, Drawable>();
        mXPosition = new Hashtable<Type, Integer>();

        mXPosition.put(Type.left, mSpace);
        mXPosition.put(Type.up, 2 * mSpace + mWidth);
        mXPosition.put(Type.down, 3 * mSpace + 2 * mWidth);
        mXPosition.put(Type.right, 4 * mSpace + 3 * mWidth);

        Resources res = context.getResources();
        mArrows.put(Type.left, res.getDrawable(R.drawable.arrow_left));
        mArrows.put(Type.up, res.getDrawable(R.drawable.arrow_up));
        mArrows.put(Type.down, res.getDrawable(R.drawable.arrow_down));
        mArrows.put(Type.right, res.getDrawable(R.drawable.arrow_right));
    }

    public static int getSize() {
        return mWidth;
    }

    public static int getStartY() {
        return mYStart;
    }
    //endregion

    private Type mType;
    private Drawable mArrow;

    private int mX = 0;
    private int mY = 0;

    public Arrow(Type type) {
        mType = type;
        mArrow = mArrows.get(type);

        mY = mYStart;
        mX = mXPosition.get(type);
    }

    public Type getType() {
        return mType;
    }

    public int getY() {
        return mY;
    }

    public boolean isVisible() {
        if (mY > mYEnd) {
            return true;
        }

        return false;
    }

    public void drawNextPosition(Canvas canvas, int moveBy) {
        mY -= moveBy;
        draw(canvas, mX, mY);
    }

    private void draw(Canvas canvas, int x, int y) {
        mArrow.setBounds(x, y, x + mWidth, y + mWidth);
        mArrow.draw(canvas);
    }
}