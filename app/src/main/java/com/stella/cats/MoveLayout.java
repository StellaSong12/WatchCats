package com.stella.cats;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.DataBindingUtil;

import com.stella.cats.databinding.DragSubViewBinding;

public class MoveLayout extends RelativeLayout {

    private int dragDirection = 0;
    private static final int TOP = 0x15;
    private static final int LEFT = 0x16;
    private static final int BOTTOM = 0x17;
    private static final int RIGHT = 0x18;
    private static final int LEFT_TOP = 0x11;
    private static final int RIGHT_TOP = 0x12;
    private static final int LEFT_BOTTOM = 0x13;
    private static final int RIGHT_BOTTOM = 0x14;
    private static final int CENTER = 0x19;

    private int lastX;
    private int lastY;

    private int screenWidth;
    private int screenHeight;

    private int oriLeft;
    private int oriRight;
    private int oriTop;
    private int oriBottom;

    // 标示此类的每个实例的id
    private int identity = 0;

    // 触控区域设定
    private int touchAreaLength = 100;

    private int minHeight = 60;
    private int minWidth = 60;
    private static final String TAG = "MoveLinearLayout";

    private Context mContext;
    public int backgroundColor;
    public int textColor;
    public float testSize = 18 * 3;

    public MoveLayout(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public MoveLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public MoveLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();

    }

    private void init() {
        screenHeight = 500;//getResources().getDisplayMetrics().heightPixels - 40;
        screenWidth = 500;// getResources().getDisplayMetrics().widthPixels;
    }

    public void setViewWidthHeight(int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    public void setMinHeight(int height) {
        minHeight = height;
        if (minHeight < touchAreaLength) {
            minHeight = touchAreaLength;
        }
    }

    public void setMinWidth(int width) {
        minWidth = width;
        if (minWidth < touchAreaLength) {
            minWidth = touchAreaLength;
        }
    }

    private boolean mFixedSize = false;

    public void setFixedSize(boolean b) {
        mFixedSize = b;
    }

    public void setMLBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setMLTextColor(int textColor) {
        this.textColor = textColor;
    }

    private int currentPosition = 0; // 当前方向，1：正向；-1：负向
    private int shakeTimes = 0; // 摇晃次数，当前方向改变一次记次+1
    private long doneTime = 0; // 按下时间

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                doneTime = System.currentTimeMillis();
                oriLeft = getLeft();
                oriRight = getRight();
                oriTop = getTop();
                oriBottom = getBottom();

                lastY = (int) event.getRawY();
                lastX = (int) event.getRawX();
                dragDirection = getDirection((int) event.getX(), (int) event.getY());

                shakeTimes = 0;
                currentPosition = 0;
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: up");
                if (System.currentTimeMillis() - doneTime < 300 && mListener != null) {
                    mListener.onClickMoveLayout(identity);
                }
                spotL = false;
                spotT = false;
                spotR = false;
                spotB = false;
                requestLayout();
//                mDeleteView.setVisibility(View.INVISIBLE);
                // invalidate();

                Log.i("~~~", "shakeTimes: " + shakeTimes);
                if (shakeTimes >= 4) {
                    if (mListener != null) {
                        mListener.onDeleteMoveLayout(identity);
                        shakeTimes = 0;
                    }
                }

                break;
//            case MotionEvent.ACTION_CANCEL:
//                Log.d(TAG, "onTouchEvent: cancel");
//                spotL = false;
//                spotT = false;
//                spotR = false;
//                spotB = false;
//                invalidate();
//                break;
            case MotionEvent.ACTION_MOVE:
                // Log.d(TAG, "onTouchEvent: move");
                int tempRawX = (int) event.getRawX();
                int tempRawY = (int) event.getRawY();

                int dx = tempRawX - lastX;
                int dy = tempRawY - lastY;
                lastX = tempRawX;
                lastY = tempRawY;

                if (dragDirection == CENTER) {
                    if (dx > 0 && currentPosition <= 0) {
                        if (dx >= 8) {
                            shakeTimes++;
                        }
                        currentPosition = 1;
                    } else if (dx < 0 && currentPosition >= 0) {
                        if (dx <= -8) {
                            shakeTimes++;
                        }
                        currentPosition = -1;
                    }
                }
                switch (dragDirection) {
//                    case LEFT:
//                        left(dx);
//                        break;
//                    case RIGHT:
//                        right(dx);
//                        break;
//                    case BOTTOM:
//                        bottom(dy);
//                        break;
//                    case TOP:
//                        top(dy);
//                        break;
                    case CENTER:
                        center(dx, dy);
                        break;
//                    case LEFT_BOTTOM:
//                        left( dx);
//                        bottom( dy);
//                        break;
//                    case LEFT_TOP:
//                        left( dx);
//                        top(dy);
//                        break;
                    case RIGHT_BOTTOM:
                        right(dx);
                        bottom(dy);
                        setTextSize();
                        break;
//                    case RIGHT_TOP:
//                        right( dx);
//                        top( dy);
//                        break;
                }

                //new pos l t r b is set into oriLeft, oriTop, oriRight, oriBottom
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(oriRight - oriLeft, oriBottom - oriTop);
                lp.setMargins(oriLeft, oriTop, 0, 0);
                setLayoutParams(lp);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void center(int dx, int dy) {
        int left = getLeft() + dx;
        int top = getTop() + dy;
        int right = getRight() + dx;
        int bottom = getBottom() + dy;

        if (left < 0) {
            left = 0;
            right = left + getWidth();
        }
        if (right > screenWidth) {
            right = screenWidth;
            left = right - getWidth();
        }
        if (top < 0) {
            top = 0;
            bottom = top + getHeight();
        }
        if (bottom > screenHeight) {
            bottom = screenHeight;
            top = bottom - getHeight();
        }

        oriLeft = left;
        oriTop = top;
        oriRight = right;
        oriBottom = bottom;
    }

    private void top(int dy) {
        oriTop += dy;
        if (oriTop < 0) {
            oriTop = 0;
        }
        if (oriBottom - oriTop < minHeight) {
            oriTop = oriBottom - minHeight;
        }
    }

    private void bottom(int dy) {
        oriBottom += dy;
        if (oriBottom > screenHeight) {
            oriBottom = screenHeight;
        }
        if (oriBottom - oriTop < minHeight) {
            oriBottom = minHeight + oriTop;
        }
    }

    private void right(int dx) {
        oriRight += dx;
        if (oriRight > screenWidth) {
            oriRight = screenWidth;
        }
        if (oriRight - oriLeft < minWidth) {
            oriRight = oriLeft + minWidth;
        }
    }

    private void left(int dx) {
        oriLeft += dx;
        if (oriLeft < 0) {
            oriLeft = 0;
        }
        if (oriRight - oriLeft < minWidth) {
            oriLeft = oriRight - minWidth;
        }
    }

    private int getDirection(int x, int y) {
        int left = getLeft();
        int right = getRight();
        int bottom = getBottom();
        int top = getTop();

        if (mFixedSize) {
            return CENTER;
        }

//        if (x < touchAreaLength && y < touchAreaLength) {
//            return LEFT_TOP;
//        }
//        if (y < touchAreaLength && right - left - x < touchAreaLength) {
//            return RIGHT_TOP;
//        }
//        if (x < touchAreaLength && bottom - top - y < touchAreaLength) {
//            return LEFT_BOTTOM;
//        }
        Log.i("~~~", "getDirection: " + (right - left - x) + " " + (bottom - top - y));
        if (right - left - x < touchAreaLength && bottom - top - y < touchAreaLength) {
            return RIGHT_BOTTOM;
        }

//        if (x < touchAreaLength) {
//            spotL = true;
//            requestLayout();
//            return LEFT;
//        }
//        if (y < touchAreaLength) {
//            spotT = true;
//            requestLayout();
//            return TOP;
//        }
//        if (right - left - x < touchAreaLength) {
//            spotR = true;
//            requestLayout();
//            return RIGHT;
//        }
//        if (bottom - top - y < touchAreaLength) {
//            spotB = true;
//            requestLayout();
//            return BOTTOM;
//        }
        return CENTER;
    }

    private void setTextSize() {
        View v = findViewById(R.id.add_your_view_here);
        if (v instanceof LinearLayout && ((LinearLayout) v).getChildAt(0) instanceof LinearLayout) {
            View child = ((LinearLayout) v).getChildAt(0);
            View text = ((LinearLayout) child).getChildAt(1);
            if (text instanceof AppCompatTextView) {
                testSize = ((AppCompatTextView) text).getTextSize();
            }
        }
    }

    private boolean spotL = false;
    private boolean spotT = false;
    private boolean spotR = false;
    private boolean spotB = false;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        RelativeLayout rlt = (RelativeLayout) getChildAt(0);
        int count = rlt.getChildCount();

        for (int a = 0; a < count; a++) {
            if (a == 1) {
                if (spotL)
                    rlt.getChildAt(a).setVisibility(View.VISIBLE);
                else
                    rlt.getChildAt(a).setVisibility(View.INVISIBLE);
            } else if (a == 2) {
                if (spotT)
                    rlt.getChildAt(a).setVisibility(View.VISIBLE);
                else
                    rlt.getChildAt(a).setVisibility(View.INVISIBLE);
            } else if (a == 3) {
                if (spotR)
                    rlt.getChildAt(a).setVisibility(View.VISIBLE);
                else
                    rlt.getChildAt(a).setVisibility(View.INVISIBLE);
            } else if (a == 4) {
                if (spotB)
                    rlt.getChildAt(a).setVisibility(View.VISIBLE);
                else
                    rlt.getChildAt(a).setVisibility(View.INVISIBLE);
            }
        }
    }


    public int getIdentity() {
        return identity;
    }

    public void setIdentity(int identity) {
        this.identity = identity;
    }

    private SetMoveLayout mListener = null;

    public interface SetMoveLayout {
        void onDeleteMoveLayout(int identity);

        void onClickMoveLayout(int identity);
    }

    public void setOnMoveLayout(SetMoveLayout l) {
        mListener = l;
    }

    DragSubViewBinding mBinding;

    public void setSubView(View view, boolean whitebg) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.drag_sub_view, this, false);
        LinearLayout.LayoutParams lv = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mBinding.addYourViewHere.addView(view, lv);

        if (whitebg) {
            mBinding.changeBg.setBackgroundResource(R.drawable.corners_bg2);
        }
        this.addView(mBinding.getRoot());
    }

    public void getFocus() {
        View v = findViewById(R.id.add_your_view_here);
        if (v instanceof LinearLayout && ((LinearLayout) v).getChildAt(0) instanceof LinearLayout) {
            View child = ((LinearLayout) v).getChildAt(0);
            View edit = ((LinearLayout) child).getChildAt(0);
            View text = ((LinearLayout) child).getChildAt(1);
            if (edit instanceof EditText && text instanceof AppCompatTextView) {
                ((EditText) edit).setText(((AppCompatTextView) text).getText());
                ((EditText) edit).setTextSize(((AppCompatTextView) text).getTextSize() / 3);
                edit.setVisibility(VISIBLE);
                text.setVisibility(GONE);
                ((EditText) edit).setSelection(((EditText) edit).getText().length());
                GradientDrawable drawable = new GradientDrawable();
                drawable.setCornerRadius(15);
                drawable.setStroke(10, getResources().getColor(R.color.black), 15, 15);
                drawable.setColor(backgroundColor);
                child.setBackground(drawable);
            }
        }
    }

    public void removeFocus() {
        View v = findViewById(R.id.add_your_view_here);
        if (v instanceof LinearLayout && ((LinearLayout) v).getChildAt(0) instanceof LinearLayout) {
            View child = ((LinearLayout) v).getChildAt(0);
            View edit = ((LinearLayout) child).getChildAt(0);
            View text = ((LinearLayout) child).getChildAt(1);
            if (edit instanceof EditText && text instanceof AppCompatTextView) {
                ((AppCompatTextView) text).setText(((EditText) edit).getText());
                ((AppCompatTextView) text).setTextSize(((EditText) edit).getTextSize() * 3);
                edit.setVisibility(GONE);
                text.setVisibility(VISIBLE);
                child.setBackgroundColor(backgroundColor);
            }
        }
    }

    public void setTextColor(int color) {
        textColor = color;
        View v = findViewById(R.id.add_your_view_here);
        if (v instanceof LinearLayout && ((LinearLayout) v).getChildAt(0) instanceof LinearLayout) {
            View child = ((LinearLayout) v).getChildAt(0);
            View edit = ((LinearLayout) child).getChildAt(0);
            View text = ((LinearLayout) child).getChildAt(1);
            if (edit instanceof EditText && text instanceof AppCompatTextView) {
                ((EditText) edit).setTextColor(color);
                ((AppCompatTextView) text).setTextColor(color);
            }
        }
    }

    public void setTextCrisperding(int color) {
        View v = findViewById(R.id.add_your_view_here);
        if (v instanceof LinearLayout && ((LinearLayout) v).getChildAt(0) instanceof LinearLayout) {
            View child = ((LinearLayout) v).getChildAt(0);
            View edit = ((LinearLayout) child).getChildAt(0);
            View text = ((LinearLayout) child).getChildAt(1);
            if (edit instanceof EditText && text instanceof StrokeTextView) {
                ((StrokeTextView) text).setStrokeColor(color);
            }
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        backgroundColor = color;
        View v = findViewById(R.id.add_your_view_here);
        if (v instanceof LinearLayout && ((LinearLayout) v).getChildAt(0) instanceof LinearLayout) {
            View child = ((LinearLayout) v).getChildAt(0);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(15);
            drawable.setStroke(10, getResources().getColor(R.color.black), 15, 15);
            drawable.setColor(backgroundColor);
            child.setBackground(drawable);
        }
    }

    public String getText() {
        View v = findViewById(R.id.add_your_view_here);
        if (v instanceof LinearLayout && ((LinearLayout) v).getChildAt(0) instanceof LinearLayout) {
            View child = ((LinearLayout) v).getChildAt(0);
            View edit = ((LinearLayout) child).getChildAt(0);
            View text = ((LinearLayout) child).getChildAt(1);
            if (edit instanceof EditText && text instanceof AppCompatTextView) {
                if (text.getVisibility() == VISIBLE) {
                    return ((AppCompatTextView) text).getText().toString();
                } else {
                    return ((EditText) edit).getText().toString();
                }
            }
            return null;
        }
        return null;
    }
}
