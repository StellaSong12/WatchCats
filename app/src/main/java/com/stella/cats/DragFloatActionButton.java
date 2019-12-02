package com.stella.cats;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DragFloatActionButton extends FloatingActionButton {

    private int parentHeight;
    private int parentWidth;

    public DragFloatActionButton(Context context) {
        super(context);
    }

    public DragFloatActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public DragFloatActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    private int lastX;
    private int lastY;

    private boolean isDrag;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                isDrag = false;
                getParent().requestDisallowInterceptTouchEvent(true);
                lastX = rawX;
                lastY = rawY;
                ViewGroup parent;
                if (getParent() != null) {
                    parent = (ViewGroup) getParent();
                    parentHeight = parent.getHeight();
                    parentWidth = parent.getWidth();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (parentHeight <= 0 || parentWidth == 0) {
                    isDrag = false;
                    break;
                } else {
                    isDrag = true;
                }
                int dx = rawX - lastX;
                int dy = rawY - lastY;

                //这里修复一些华为手机无法触发点击事件
                int distance = (int) Math.sqrt(dx * dx + dy * dy);
                if (distance == 0) {
                    isDrag = false;
                    break;
                }
                float x = getX() + dx;
                float y = getY() + dy;

                //检测是否到达边缘 左上右下
                x = x < 30 ? 30 : x > parentWidth - getWidth() - 30 ? parentWidth - getWidth() - 30 : x;
                y = getY() < 30 ? 30 : getY() > parentHeight - getHeight() - 250 ? parentHeight - getHeight() - 250 : y;
                setX(x);
                setY(y);
                lastX = rawX;
                lastY = rawY;
                Log.i("~~~", "isDrag=" + isDrag + "getX=" + getX() + ";getY=" + getY() + ";parentWidth=" + parentWidth);
                break;
            case MotionEvent.ACTION_UP:
                if (!isNotDrag()) {
                    //恢复按压效果
                    setPressed(false);
                    if (rawX >= parentWidth / 2) {
                        //靠右吸附
                        animate().setInterpolator(new DecelerateInterpolator())
                                .setDuration(500)
                                .xBy(parentWidth - getWidth() - getX() - 30)
                                .start();
                    } else {
                        //靠左吸附
                        ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(), 30);
                        oa.setInterpolator(new DecelerateInterpolator());
                        oa.setDuration(500);
                        oa.start();
                    }
                }
                break;
        }

        // 拖拽则消耗事件，否则正常传递即可
        return !isNotDrag() || super.onTouchEvent(event);
    }

    private boolean isNotDrag() {
        return !isDrag && (getX() == 30 || (getX() == parentWidth - getWidth() - 30));
    }
}