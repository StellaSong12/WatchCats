package com.stella.cats;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class DragView extends RelativeLayout implements MoveLayout.SetMoveLayout {

    private Context mContext;
    private int mLocalIdentity = 0;
    private List<MoveLayout> mMoveLayoutList;

    private int chooseViewId = -1;
    public ImageView mBackgroundIv;

    public DragView(Context context) {
        super(context);
        init(context, this);
    }

    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, this);
    }

    public DragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, this);
    }

    private void init(Context c, DragView thisp) {
        mContext = c;
        mMoveLayoutList = new ArrayList<>();

        mBackgroundIv = new ImageView(mContext);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mBackgroundIv, rlp);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //  Log.e(TAG, "onDraw: height=" + getHeight());
        int mSelfViewWidth = getWidth();
        int mSelfViewHeight = getHeight();

        if (mMoveLayoutList != null) {
            int count = mMoveLayoutList.size();
            for (int a = 0; a < count; a++) {
                mMoveLayoutList.get(a).setViewWidthHeight(mSelfViewWidth, mSelfViewHeight);
            }
        }
    }

    public void addDragView(View selfView, int left, int top, int right, int bottom, boolean isFixedSize, boolean whitebg) {
        int mMinHeight = 60;
        int mMinWidth = 60;
        addDragView(selfView, left, top, right, bottom, isFixedSize, whitebg, mMinWidth, mMinHeight);
    }

    public void addDragView(View selfView, int left, int top, int right, int bottom, boolean isFixedSize, boolean whitebg, int minwidth, int minheight) {
        MoveLayout moveLayout = new MoveLayout(mContext);

        moveLayout.setClickable(true);
        moveLayout.setMinHeight(minheight);
        moveLayout.setMinWidth(minwidth);
        moveLayout.setMLBackgroundColor(getResources().getColor(R.color.white));
        moveLayout.setMLTextColor(getResources().getColor(R.color.black));
        int tempWidth = right - left;
        int tempHeight = bottom - top;
        if (tempWidth < minwidth) tempWidth = minwidth;
        if (tempHeight < minheight) tempHeight = minheight;

        //set position
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(tempWidth, tempHeight);
        lp.setMargins(left, top, 0, 0);
        moveLayout.setLayoutParams(lp);

        //add sub view (has click indicator)
        moveLayout.setSubView(selfView, whitebg);

        //set fixed size
        moveLayout.setFixedSize(isFixedSize);

        moveLayout.setOnMoveLayout(this);
        moveLayout.setIdentity(mLocalIdentity++);

        addView(moveLayout);
        mMoveLayoutList.add(moveLayout);

//        onClickMoveLayout(moveLayout.getIdentity());
    }


    public void addDragView(int resId, int left, int top, int right, int bottom, boolean isFixedSize, boolean whitebg) {
        LayoutInflater inflater2 = LayoutInflater.from(mContext);
        View selfView = inflater2.inflate(resId, null);
        addDragView(selfView, left, top, right, bottom, isFixedSize, whitebg);
    }

    @Override
    public void onDeleteMoveLayout(int identity) {
        int count = mMoveLayoutList.size();
        for (int a = 0; a < count; a++) {
            if (mMoveLayoutList.get(a).getIdentity() == identity) {
                removeView(mMoveLayoutList.get(a));
            }
        }
    }

    @Override
    public void onClickMoveLayout(int identity) {
        chooseViewId = identity;
        int count = mMoveLayoutList.size();
        for (int a = 0; a < count; a++) {
            if (mMoveLayoutList.get(a).getIdentity() == identity) {
                mMoveLayoutList.get(a).getFocus();
            } else {
                mMoveLayoutList.get(a).removeFocus();
            }
        }
    }

    public void showText() {
        chooseViewId = -1;
        int count = mMoveLayoutList.size();
        for (int a = 0; a < count; a++) {
            mMoveLayoutList.get(a).removeFocus();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                showText();
                break;

            case MotionEvent.ACTION_UP:
        }
        return super.onTouchEvent(event);
    }

    public int getChooseViewId() {
        return chooseViewId;
    }

    public void setViewTextColor(int identity, int color) {
        int count = mMoveLayoutList.size();
        for (int a = 0; a < count; a++) {
            if (mMoveLayoutList.get(a).getIdentity() == identity) {
                mMoveLayoutList.get(a).setTextColor(color);
            }
        }
    }

    public void setViewTextCrisperding(int identity, int color) {
        int count = mMoveLayoutList.size();
        for (int a = 0; a < count; a++) {
            if (mMoveLayoutList.get(a).getIdentity() == identity) {
                mMoveLayoutList.get(a).setTextCrisperding(color);
            }
        }
    }

    public void setViewBackgroundColor(int identity, int color) {
        int count = mMoveLayoutList.size();
        for (int a = 0; a < count; a++) {
            if (mMoveLayoutList.get(a).getIdentity() == identity) {
                mMoveLayoutList.get(a).setBackgroundColor(color);
            }
        }
    }

    public List<MoveLayout> getMoveLayoutList() {
        return mMoveLayoutList;
    }

//
//    // delete interface
//    private DeleteDragView mListener = null;
//    public interface DeleteDragView {
//        void onDeleteDragView();
//    }
//    public void setOnDeleteDragView(DeleteDragView l) {
//        mListener = l;
//    }
}