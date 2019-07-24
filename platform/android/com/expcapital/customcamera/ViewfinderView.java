package com.expcapital.customcamera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

public class ViewfinderView extends FrameLayout {
    private float RADIUS = 20;
    private float PADDING_HORIZONTAL = 10;
    private float PADDING_VERTICAL = 10;
    private Type type = Type.NONE;

    public enum Type {

        LICENCE(0.62f), A4(1.41f), PASSPORT(0.68f), NONE(1), FACE(1.41f);

        private final float aspectRatio;

        Type(float ratio) {
            aspectRatio = ratio;
        }

        public float getAspectRatio() {
            return aspectRatio;
        }

    }

    private Paint mBackgroundPaint;
    private Paint mStrokePaint;
    private Paint mPassportPaint;

    private int mBackgroundColor = Color.parseColor("#00FFFFFF");
    private int mTutorialColor = Color.parseColor("#4C000000");
    private int mStrokeColor = Color.parseColor("#80FFFFFF");
    private int mPassportColor = Color.parseColor("#26000000");

    private RectF viewFinderRect = new RectF();
    private RectF passportFinderRect = new RectF();

    public ViewfinderView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ViewfinderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewfinderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setViewfinderType(Type t) {
        type = t;
        invalidate();
    }

    private void init(Context context) {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mStrokePaint = new Paint();
        mStrokePaint.setColor(mStrokeColor);
        mStrokePaint.setStyle(Paint.Style.STROKE);

        mPassportPaint = new Paint();
        mPassportPaint.setColor(mPassportColor);

        Resources r = getResources();
        RADIUS = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        PADDING_HORIZONTAL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, r.getDisplayMetrics());
        PADDING_VERTICAL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float width = right - left;
        float height = bottom - top;
        if (height / width > type.getAspectRatio()) {
            computeHorizontallFill(width, height, type);
        } else {
            computeVerticalFill(width, height, type);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (Type.NONE.equals(type)) {
            canvas.drawColor(mBackgroundColor);
        } else {
            canvas.drawColor(mTutorialColor);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Type.PASSPORT.equals(type)) {
                canvas.drawRect(viewFinderRect.left, viewFinderRect.top, viewFinderRect.right, viewFinderRect.bottom, mBackgroundPaint);
                canvas.drawRect(passportFinderRect.left, passportFinderRect.top, passportFinderRect.right, passportFinderRect.bottom, mPassportPaint);
                canvas.drawRect(viewFinderRect.left, viewFinderRect.top, viewFinderRect.right, viewFinderRect.bottom, mStrokePaint);
            } else if (Type.LICENCE.equals(type)) {
                canvas.drawRoundRect(viewFinderRect.left, viewFinderRect.top, viewFinderRect.right, viewFinderRect.bottom, RADIUS, RADIUS, mBackgroundPaint);
                canvas.drawRoundRect(viewFinderRect.left, viewFinderRect.top, viewFinderRect.right, viewFinderRect.bottom, RADIUS, RADIUS, mStrokePaint);
            } else if (Type.A4.equals(type)) {
                canvas.drawRect(viewFinderRect.left, viewFinderRect.top, viewFinderRect.right, viewFinderRect.bottom, mBackgroundPaint);
                canvas.drawRect(viewFinderRect.left, viewFinderRect.top, viewFinderRect.right, viewFinderRect.bottom, mStrokePaint);
            }  else if (Type.FACE.equals(type)) {
                canvas.drawRect(viewFinderRect.left, viewFinderRect.top, viewFinderRect.right, viewFinderRect.bottom, mBackgroundPaint);
                canvas.drawRect(viewFinderRect.left, viewFinderRect.top, viewFinderRect.right, viewFinderRect.bottom, mStrokePaint);
            }
        } else {
            if (Type.PASSPORT.equals(type)) {
                canvas.drawRect(viewFinderRect, mBackgroundPaint);
                canvas.drawRect(passportFinderRect, mPassportPaint);
                canvas.drawRect(viewFinderRect, mStrokePaint);
            } else if (Type.LICENCE.equals(type)) {
                canvas.drawRoundRect(viewFinderRect, RADIUS, RADIUS, mBackgroundPaint);
                canvas.drawRoundRect(viewFinderRect, RADIUS, RADIUS, mStrokePaint);
            } else if (Type.A4.equals(type)) {
                canvas.drawRect(viewFinderRect, mBackgroundPaint);
                canvas.drawRect(viewFinderRect, mStrokePaint);
            } else if (Type.FACE.equals(type)) {
                canvas.drawRect(viewFinderRect, mBackgroundPaint);
                canvas.drawRect(viewFinderRect, mStrokePaint);
            }
        }
    }

    private void computeHorizontallFill(float width, float height, Type type) {
        float widthV = width - PADDING_HORIZONTAL * 2;
        float heightV = widthV * type.getAspectRatio();
        viewFinderRect.left = PADDING_HORIZONTAL;
        viewFinderRect.top = (height - heightV) / 2;
        viewFinderRect.right = widthV + PADDING_HORIZONTAL;
        viewFinderRect.bottom = (height + heightV) / 2;

        passportFinderRect.top = viewFinderRect.top + heightV * 0.7f;
        passportFinderRect.left = viewFinderRect.left;
        passportFinderRect.right = viewFinderRect.right;
        passportFinderRect.bottom = viewFinderRect.bottom;
    }

    private void computeVerticalFill(float width, float height, Type type) {
        float heightV = height - PADDING_VERTICAL * 2;
        float widthV = heightV / type.getAspectRatio();
        viewFinderRect.left = (width - widthV) / 2;
        viewFinderRect.top = PADDING_VERTICAL;
        viewFinderRect.right = (width + widthV) / 2;
        viewFinderRect.bottom = heightV + PADDING_VERTICAL;

        passportFinderRect.top = viewFinderRect.top + heightV * 0.7f;
        passportFinderRect.left = viewFinderRect.left;
        passportFinderRect.right = viewFinderRect.right;
        passportFinderRect.bottom = viewFinderRect.bottom;
    }
}
