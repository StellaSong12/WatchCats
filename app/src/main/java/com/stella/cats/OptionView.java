package com.stella.cats;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;

import com.stella.cats.databinding.ItemViewOptionBinding;

public class OptionView extends RelativeLayout {

    ItemViewOptionBinding mBinding = null;

    public OptionView(Context context) {
        super(context);
    }

    public OptionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_view_option, this, false);
        addView(mBinding.getRoot());

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OptionView, 0, 0);
        int iconResId = typedArray.getResourceId(R.styleable.OptionView_so_icon, -1);
        int backResId = typedArray.getResourceId(R.styleable.OptionView_so_ic_back, -1);
        String title = typedArray.getString(R.styleable.OptionView_so_text);
        typedArray.recycle();

        mBinding.ivIcon.setVisibility(iconResId > 0 ? VISIBLE : GONE);
        if (iconResId > 0) {
            mBinding.ivIcon.setImageResource(iconResId);
        }
        if (backResId > 0) {
            mBinding.ivIcon.setBackgroundColor(getResources().getColor(backResId));
        }
        mBinding.tvTitle.setText(title);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
