package com.zhuang.likeviewlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * TODO: document your custom view class.
 */
public class LikeView extends View implements View.OnClickListener {

    private Activity activity;
    private FrameLayout floatingTextWrapper;
    private String text = "赞";
    private float iconSize;//图标大小
    private float textSize;//文字大小
    private boolean hasLike;//是否已点赞
    private boolean canCancel;//是否能取消点赞
    private int iconGap = 10;//文字与图片之间的距离
    private TextPaint mTextPaint;
    private float mTextWidth;
    private float top;
    private Drawable drawable;
    private OnLikeListeners onLikeListeners;

    public LikeView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public LikeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public LikeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LikeView, defStyle, 0);
        iconSize = a.getDimension(R.styleable.LikeView_like_iconSize, iconSize);
        textSize = a.getDimension(R.styleable.LikeView_like_textSize, textSize);
        canCancel = a.getBoolean(R.styleable.LikeView_like_canCancel, canCancel);
        a.recycle();

        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(textSize);
        mTextWidth = mTextPaint.measureText(text);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        top = fontMetrics.top;

        activity = (Activity) context;
        setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int width = (int) iconSize + (int) mTextWidth + paddingLeft + paddingRight + iconGap;
        int height = (int) iconSize + paddingTop + paddingBottom;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (hasLike) {
            drawable = getResources().getDrawable(R.drawable.ic_like_red);
            mTextPaint.setColor(Color.parseColor("#d81e06"));
        } else {
            drawable = getResources().getDrawable(R.drawable.ic_like_gray);
            mTextPaint.setColor(Color.parseColor("#8a8a8a"));
        }

        drawable.setBounds(paddingLeft, paddingTop, (int) iconSize + paddingLeft, (int) iconSize + paddingTop);
        drawable.draw(canvas);

        canvas.drawText(text, paddingLeft + iconSize + iconGap, -top + paddingTop, mTextPaint);
    }

    /**
     * 创建一个ViewGroup，用于放置“飘起来”的view
     *
     * @see FloatLikeView
     */
    private void createLayoutWrapper() {
        //ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        floatingTextWrapper = (FrameLayout) activity.findViewById(R.id.FloatingText_wrapper);
        if (floatingTextWrapper == null) {
            floatingTextWrapper = new FrameLayout(activity);
            floatingTextWrapper.setId(R.id.FloatingText_wrapper);
            rootView.addView(floatingTextWrapper);
        }
    }

    /**
     * 把“飘起来”的view添加到新建的ViewGroup中
     *
     * @see FloatLikeView
     */
    private void addView2LayoutWrapper() {
        FloatLikeView floatLikeView = new FloatLikeView.Builder()
                .activity(activity)
                .height(getHeight())
                .width(getWidth())
                .iconGap(iconGap)
                .iconSize(iconSize)
                .textSize(textSize)
                .text(canCancel && hasLike ? "-1" : "+1")
                .paddingBottom(getPaddingBottom())
                .paddingTop(getPaddingTop())
                .paddingLeft(getPaddingLeft())
                .paddingRight(getPaddingRight())
                .build();
        floatLikeView.setAttachedView(this);
        floatingTextWrapper.bringToFront();
        floatingTextWrapper.addView(floatLikeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        floatLikeView.start(floatingTextWrapper);
    }

    /**
     * ui改变为已点赞的效果
     */
    private void changeUI() {
        boolean add = canCancel && hasLike ? false : true;//当前是添加一个赞还是减少一个赞
        int textInt = 0;
        if (text.equals("赞")) {
            text = "1";
        } else {
            textInt = Integer.parseInt(text);
            if (add) {
                text = (textInt + 1) + "";
            } else {
                text = (textInt - 1) + "";
            }
        }
        hasLike = canCancel ? !hasLike : true;
        invalidate();
        //字体长度变宽了，需要重新测量
        float newTextWidth = mTextPaint.measureText(text);
        if (newTextWidth > mTextWidth) {
            mTextWidth = newTextWidth;
            requestLayout();
        }
    }

    /**
     * 缩放动画
     */
    public void startAnimal() {
        ScaleAnimation sc = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sc.setDuration(300);
        startAnimation(sc);
    }

    @Override
    public void onClick(View v) {
        if (hasLike && !canCancel) {
            Toast.makeText(activity, "您已经赞过了", Toast.LENGTH_SHORT).show();
            return;
        } else {
            createLayoutWrapper();
            addView2LayoutWrapper();
            changeUI();
            startAnimal();
            if (onLikeListeners != null) {
                onLikeListeners.like(!hasLike);
            }
        }
    }

    public void setText(String text) {
        if (text.equals("0")) {
            this.text = "赞";
        } else {
            this.text = text;
        }
        mTextWidth = mTextPaint.measureText(this.text);
        invalidate();
        requestLayout();
    }

    public void setHasLike(boolean hasLike) {
        this.hasLike = hasLike;
        invalidate();
    }

    public void setOnLikeListeners(OnLikeListeners onLikeListeners) {
        this.onLikeListeners = onLikeListeners;
    }

    public interface OnLikeListeners {
        void like(boolean isCancel);
    }

}
