package com.zhuang.likeviewlibrary;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 可以飘起来的一个view，
 * LikeView{@link LikeView}点击时，会创建FloatLikeView
 */
public class FloatLikeView extends View {

    Drawable drawable;
    boolean positionSet;
    private View mAttachedView;
    float top;
    Builder builder;

    public FloatLikeView(Builder builder) {
        super(builder.activity);
        this.builder = builder;
        init();
    }

    public FloatLikeView(Context context) {
        super(context);
        init();
    }

    public FloatLikeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatLikeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        drawable = getResources().getDrawable(R.drawable.ic_messages_like_selected);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(builder.width, builder.height);
        fixPosition();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawable.setBounds(
                builder.paddingLeft + builder.iconSizeAdditional,
                builder.paddingTop,
                (int) builder.iconSize + builder.iconSizeAdditional + builder.paddingLeft,
                (int) builder.iconSize + builder.paddingTop
        );
        drawable.draw(canvas);
    }

    public void setAttachedView(View attachedView) {
        this.mAttachedView = attachedView;
    }

    private void fixPosition() {
        if (!positionSet) {
            Rect rect = new Rect();
            mAttachedView.getGlobalVisibleRect(rect);
            int[] location = new int[2];
            ((ViewGroup) getParent()).getLocationOnScreen(location);
            rect.offset(-location[0], -location[1]);

            int topMargin = rect.top;
            int leftMargin = rect.left;

            FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) getLayoutParams());
            lp.topMargin = topMargin;
            lp.leftMargin = leftMargin;
            setLayoutParams(lp);
        }
        positionSet = true;
    }

    public void start(final ViewGroup floatingTextWrapper) {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, -200f);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, View.ALPHA, 0f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animator1, animator2);
        animSet.setDuration(300);
        animSet.start();

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                floatingTextWrapper.removeView(FloatLikeView.this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public static class Builder {
        float iconSize;
        float textSize;
        int iconGap;
        int paddingLeft;
        int paddingTop;
        int paddingRight;
        int paddingBottom;
        int height;
        int width;
        int iconSizeAdditional;
        Activity activity;

        public Builder iconSize(float iconSize) {
            this.iconSize = iconSize;
            return this;
        }

        public Builder textSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder iconGap(int iconGap) {
            this.iconGap = iconGap;
            return this;
        }

        public Builder paddingLeft(int paddingLeft) {
            this.paddingLeft = paddingLeft;
            return this;
        }

        public Builder paddingTop(int paddingTop) {
            this.paddingTop = paddingTop;
            return this;
        }

        public Builder paddingRight(int paddingRight) {
            this.paddingRight = paddingRight;
            return this;
        }

        public Builder paddingBottom(int paddingBottom) {
            this.paddingBottom = paddingBottom;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder iconSizeAdditional(int iconSizeAdditional) {
            this.iconSizeAdditional = iconSizeAdditional;
            return this;
        }

        public Builder activity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public FloatLikeView build() {
            FloatLikeView floatLikeView = new FloatLikeView(this);
            return floatLikeView;
        }
    }
}
