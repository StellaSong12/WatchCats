package com.stella.cats;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SlideColorPicker extends View {

    private Paint paint;
    private Paint strokePaint;
    private Path path;
    private Bitmap bitmap;
    private int viewWidth;
    private int viewHeight;
    private int centerX;
    private float colorPickerRadius;
    private OnColorChangeListener onColorChangeListener;
    private RectF colorPickerBody;
    private float selectorYPos;
    private int borderColor;
    private float borderWidth;
    private int[] colors;
    private boolean cacheBitmap = true;
    private int currentColor;

    public SlideColorPicker(Context context) {
        super(context);
        init();
    }

    public SlideColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.VerticalSlideColorPicker,
                0, 0);

        try {
            borderColor = a.getColor(R.styleable.VerticalSlideColorPicker_borderColor, Color.WHITE);
            borderWidth = a.getDimension(R.styleable.VerticalSlideColorPicker_borderWidth, 10f);
            int colorsResourceId = a.getResourceId(R.styleable.VerticalSlideColorPicker_colors, R.array.default_colors);
            colors = a.getResources().getIntArray(colorsResourceId);
        } finally {
            a.recycle();
        }
        init();
    }

    public SlideColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SlideColorPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        path = new Path();

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(borderColor);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(borderWidth);

        setDrawingCacheEnabled(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        path.addCircle(borderWidth + colorPickerRadius, centerX, colorPickerRadius, Path.Direction.CW);
        path.addRect(colorPickerBody, Path.Direction.CW);
        path.addCircle(viewWidth - (borderWidth + colorPickerRadius), centerX, colorPickerRadius, Path.Direction.CW);

        canvas.drawPath(path, strokePaint);
        canvas.drawPath(path, paint);

        if (cacheBitmap) {
            bitmap = getDrawingCache();
            cacheBitmap = false;
            invalidate();
        } else {
            canvas.drawLine(selectorYPos, colorPickerBody.top, selectorYPos, colorPickerBody.bottom, strokePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float xPos = Math.min(event.getX(), colorPickerBody.right);
        xPos = Math.max(colorPickerBody.left, xPos);

        selectorYPos = xPos;
        int selectedColor = bitmap.getPixel((int) selectorYPos, viewHeight / 2);

        if (onColorChangeListener != null) {
            onColorChangeListener.onColorChange(selectedColor);
            currentColor = selectedColor;
        }

        invalidate();

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = w;
        viewHeight = h;

        centerX = viewHeight / 2;
        colorPickerRadius = (viewHeight / 2) - borderWidth;

        colorPickerBody = new RectF(borderWidth + colorPickerRadius, centerX - colorPickerRadius, viewWidth - (borderWidth + colorPickerRadius), centerX + colorPickerRadius);

        LinearGradient gradient = new LinearGradient(colorPickerBody.left, 0, colorPickerBody.right, 0, colors, null, Shader.TileMode.CLAMP);
        paint.setShader(gradient);

        resetToDefault();
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        invalidate();
    }

    public void setColors(int[] colors) {
        this.colors = colors;
        cacheBitmap = true;
        invalidate();
    }

    public void resetToDefault() {
        selectorYPos = borderWidth + colorPickerRadius;

        if (onColorChangeListener != null) {
            onColorChangeListener.onColorChange(Color.TRANSPARENT);
            currentColor = Color.TRANSPARENT;
        }

        invalidate();
    }

    public void setOnColorChangeListener(OnColorChangeListener onColorChangeListener) {
        this.onColorChangeListener = onColorChangeListener;
    }

    public int getColor() {
        return currentColor;
    }

    public interface OnColorChangeListener {

        void onColorChange(int selectedColor);
    }
}