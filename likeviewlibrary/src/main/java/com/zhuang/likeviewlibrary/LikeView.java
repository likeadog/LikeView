package com.zhuang.likeviewlibrary;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 一个可以点赞的view
 *
 * @author zhuang
 */
public class LikeView extends View implements View.OnClickListener {

    /**
     * 点赞时，图标有一个缩小扩大再回缩的动作，这两个常量表示缩放时的最大和最小倍数
     */
    private static float EXPAND_MULTIPLE = 1.5f;
    private static float SHRINK_MULTIPLE = 0.8f;
    private static int DURATION = 200;//动画持续时间 ms

    private Activity activity;
    private FrameLayout floatingTextWrapper;

    private int likeCount;
    private int countAlphaIn;//数字切换时，新值的透明度。
    private int countAlphaOut = 255;//数字切换时，旧值的透明度。
    private float countFloatOffset;//数字切换时的位移
    private String num1;//数字由两部分组成，num1为前面的部分
    private String num2;//数字由两部分组成，num2为后面的部分
    private String num3;//新值，用于替换旧值
    private boolean hasCut;//是否被切割
    private float num1Width;//数字前半部分长度
    private float iconScaleSelect = 1;//点赞时图标缩放
    private float iconScaleUnSelect = 1;//取消点赞时图标缩放
    private float shiningScale = 1;//闪光图标缩放

    /**
     * 可自定义的属性
     */
    private int iconSize = 20;//图标大小 dp
    private int textSize = 12;//文字大小 sp
    private boolean hasLike;//是否已点赞
    private boolean canCancel;//是否能取消点赞
    private boolean hasFly;//是否有飘出效果

    private int selectColor = Color.parseColor("#d81e06");
    private int normalColor = Color.parseColor("#bdc3c7");
    private int shiningSize;//闪光图标的大小

    private int iconGap = 10;//文字与图片之间的距离 px
    private TextPaint mTextPaint;
    private TextPaint mTextPaintAlphaIn;
    private TextPaint mTextPaintAlphaOut;
    private Rect mBound;
    private float mTextWidth;
    private float mTextHeight;
    private Drawable drawableSelect;
    private Drawable drawableUnSelect;
    private Drawable drawableShining;
    private OnLikeListeners onLikeListeners;
    private boolean animating;//是否正在动画，防止用户狂点
    private int iconSizeAdditional;//点赞时图标扩大需要的补充大小

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
        iconSize = a.getDimensionPixelSize(R.styleable.LikeView_like_iconSize, Util.dip2px(getContext(), iconSize));
        textSize = a.getDimensionPixelSize(R.styleable.LikeView_like_textSize, Util.sp2px(getContext(), textSize));
        canCancel = a.getBoolean(R.styleable.LikeView_like_canCancel, canCancel);
        hasFly = a.getBoolean(R.styleable.LikeView_like_hasFly, hasFly);
        calculateShiningSize();
        a.recycle();

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(textSize);
        mBound = new Rect();

        mTextPaintAlphaOut = new TextPaint();
        mTextPaintAlphaOut.setAntiAlias(true);
        mTextPaintAlphaOut.setTextSize(textSize);

        mTextPaintAlphaIn = new TextPaint();
        mTextPaintAlphaIn.setAntiAlias(true);
        mTextPaintAlphaIn.setTextSize(textSize);

        iconSizeAdditional = (int) ((EXPAND_MULTIPLE - 1) * iconSize);

        activity = (Activity) context;
        setOnClickListener(this);
    }

    /**
     * likeView的大小由iconSize与textSize确定。
     * 这里并没有考虑layout_width，layout_height值的设置
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        String text = likeCountToString();
        mTextPaint.getTextBounds(text, 0, text.length(), mBound);
        mTextWidth = mTextPaint.measureText(text);
        mTextHeight = mBound.height();
        if (hasCut && !num1.equals("0")) {
            num1Width = mTextPaint.measureText(num1);
        } else {
            num1Width = 0;
        }
        //防止图标扩大时有部分显示不全，在测量宽度时，需要把扩大图标时需要的空间计算进去
        int width = (int) (EXPAND_MULTIPLE * iconSize) + (int) mTextWidth + paddingLeft + paddingRight + iconGap;
        int height = paddingTop + paddingBottom + iconSize + shiningSize;
        setMeasuredDimension(width, height);

        //不同的状态会显示不同的drawable和paint的颜色，我没有一开始就加载所有资源，
        //是因为有些资源不会用到，例如被点赞的likeView需要用到红色的图标，却不需要用到灰色的图标,
        //但是如果状态切换时，就要用到灰色图标，所以要寻找一个地方可以让其状态改变时，加载其他资源。
        //把drawable和paint的设置放在这里，是考虑到如果放在init()中，时机不对,不能与hasLike的状态改变对应起来
        //如果放在onDraw()中，则运行动画效果时，会导致多次调用
        //综合考虑，放在onMeasure()性能会好一些
        prepareSource();
    }

    /**
     * 根据不同状态，加载设置不同资源
     */
    private void prepareSource() {
        if (hasLike) {
            if (drawableShining == null) {
                drawableShining = getResources().getDrawable(R.drawable.ic_messages_like_selected_shining);
            }
            if (drawableSelect == null) {
                drawableSelect = getResources().getDrawable(R.drawable.ic_messages_like_selected);
            }
            mTextPaint.setColor(selectColor);
            mTextPaintAlphaOut.setColor(selectColor);
        } else {
            if (drawableUnSelect == null) {
                drawableUnSelect = getResources().getDrawable(R.drawable.ic_messages_like_unselected);
            }
            mTextPaint.setColor(normalColor);
            mTextPaintAlphaIn.setColor(normalColor);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        //画拇指
        if (hasLike) {
            int scaleSize = (int) (iconSize * (1 - iconScaleSelect) / 2);
            drawableSelect.setBounds(
                    paddingLeft + scaleSize + iconSizeAdditional,
                    paddingTop + shiningSize / 2 + scaleSize,
                    iconSize + paddingLeft - scaleSize + iconSizeAdditional,
                    paddingTop + shiningSize / 2 + iconSize - scaleSize
            );
            drawableSelect.draw(canvas);
        } else {
            int scaleSize = (int) (iconSize * (1 - iconScaleUnSelect) / 2);
            drawableUnSelect.setBounds(
                    paddingLeft + scaleSize + iconSizeAdditional,
                    paddingTop + shiningSize / 2 + scaleSize,
                    iconSize + paddingLeft - scaleSize + iconSizeAdditional,
                    paddingTop + shiningSize / 2 + iconSize - scaleSize
            );
            drawableUnSelect.draw(canvas);
        }

        //画闪光
        if (hasLike) {
            int shiningScaleSize = (int) (shiningSize * (1 - shiningScale) / 2);
            drawableShining.setBounds(
                    paddingLeft + (iconSize - shiningSize) / 2 + shiningScaleSize + iconSizeAdditional,
                    paddingTop + shiningScaleSize,
                    paddingLeft + (iconSize - shiningSize) / 2 + shiningSize - shiningScaleSize + iconSizeAdditional,
                    paddingTop + shiningSize - shiningScaleSize
            );
            drawableShining.draw(canvas);
        }

        //画文本
        if (!hasCut) {
            canvas.drawText(
                    likeCountToString(),
                    paddingLeft + iconSize + iconGap + iconSizeAdditional,
                    paddingTop + (iconSize + shiningSize + mTextHeight) / 2,
                    mTextPaint
            );
        } else {
            mTextPaintAlphaOut.setAlpha(countAlphaOut);
            mTextPaintAlphaIn.setAlpha(countAlphaIn);
            if (!num1.equals("0")) {
                //画不变的文本
                canvas.drawText(
                        num1,
                        paddingLeft + iconSize + iconGap + iconSizeAdditional,
                        paddingTop + (iconSize + shiningSize + mTextHeight) / 2,
                        mTextPaint
                );
            }
            if (!hasLike) {
                //画即将被替换掉的文本，会从中间移到下方
                canvas.drawText(
                        num2,
                        paddingLeft + iconSize + iconGap + num1Width + iconSizeAdditional,
                        paddingTop + (iconSize + shiningSize + mTextHeight) / 2 + countFloatOffset,
                        mTextPaintAlphaOut
                );
                //画即将替换掉别人的文本，会从上方移到中间，替换掉旧文本
                canvas.drawText(
                        num3,
                        paddingLeft + iconSize + iconGap + num1Width + iconSizeAdditional,
                        paddingTop + mTextHeight + countFloatOffset,
                        mTextPaintAlphaIn
                );
            } else {
                //画即将被替换掉的文本，会从中间移到上方
                canvas.drawText(
                        num2,
                        paddingLeft + iconSize + iconGap + num1Width + iconSizeAdditional,
                        paddingTop + (iconSize + shiningSize + mTextHeight) / 2 - countFloatOffset,
                        mTextPaintAlphaOut
                );
                //画即将替换掉别人的文本，会从下方移到中间，替换掉旧文本
                canvas.drawText(
                        num3,
                        paddingLeft + iconSize + iconGap + num1Width + iconSizeAdditional,
                        getHeight() - getPaddingBottom() - countFloatOffset,
                        mTextPaintAlphaIn
                );
            }
        }
    }

    /**
     * 计算闪光的大小
     * 原始资源中，闪光图片大小为64*64px;拇指图片大小为80*80px
     * 我们已经设置了iconSize,iconSize对应拇指，这里通过该比例计算出闪光的大小
     */
    private void calculateShiningSize() {
        shiningSize = (int) (0.8 * iconSize);
    }

    /**
     * 点赞数字转换为文本
     *
     * @return
     */
    private String likeCountToString() {
        return likeCount + "";
    }

    /**
     * 切割数字
     */
    private void cutNum() {
        //点赞过，那么再次点击将会减少点赞数，就按减少点赞来切割
        //没有点赞过，则按增加点赞来切割点赞数
        if (hasLike) {
            String[] arr = Util.cutNumDel(likeCount);
            num1 = arr[0];
            num2 = arr[1];
            num3 = arr[2];
        } else {
            String[] arr = Util.cutNumAdd(likeCount);
            num1 = arr[0];
            num2 = arr[1];
            num3 = arr[2];
        }
        hasCut = true;
    }

    /**
     * ui改变为已点赞的效果
     */
    private void changeUI() {
        boolean add = canCancel && hasLike ? false : true;//当前是添加一个赞还是减少一个赞
        if (add) {
            likeCount++;
        } else {
            likeCount--;
        }
        hasLike = canCancel ? !hasLike : true;
        invalidate();
        requestLayout();
    }

    /**
     * 状态回滚到初始状态
     */
    private void resetStatus() {
        hasCut = false;
        animating = false;
    }

    /**
     * LikeView被点击时的动画
     */
    public void startAnimal() {
        animating = true;
        //点赞时拇指图标缩放动画
        ObjectAnimator animator0 = ObjectAnimator.ofFloat(this, "iconScaleSelect", SHRINK_MULTIPLE, EXPAND_MULTIPLE, 1f);
        //取消点赞时，拇指图标缩放动画
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, "iconScaleUnSelect", SHRINK_MULTIPLE, 1f);
        //点赞时，闪光缩放动画（取消点赞时，闪光没有动画，直接消失）
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, "shiningScale", 0, 1f);
        //飘出字体的透明度，由不透明逐渐到完全透明
        ObjectAnimator animator3 = ObjectAnimator.ofInt(this, "countAlphaOut", 255, 0);
        //进入字体的透明度，由完全透明到不透明
        ObjectAnimator animator4 = ObjectAnimator.ofInt(this, "countAlphaIn", 0, 255);
        //字体替换时的位移动画
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(this, "countFloatOffset", 0, (iconSize + shiningSize - mTextHeight) / 2);
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(DURATION);
        //点赞时动画与取消点赞时动画稍微不一样，主要表现在拇指与闪光的动画上。
        if (hasLike) {
            animSet.playTogether(animator0, animator2, animator3, animator4, animator5);
        } else {
            animSet.playTogether(animator1, animator3, animator4, animator5);
        }
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                resetStatus();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSet.start();
    }

    @Override
    public void onClick(View v) {
        if (hasLike && !canCancel) {
            Toast.makeText(activity, "您已经赞过了", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (animating) return;
            //点赞时，飘出一个大拇指
            if (!hasLike && hasFly) {
                createLayoutWrapper();
                addView2LayoutWrapper();
            }
            cutNum();
            changeUI();
            startAnimal();
            if (onLikeListeners != null) {
                onLikeListeners.like(!hasLike);
            }
        }
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
                .paddingBottom(getPaddingBottom())
                .paddingTop(getPaddingTop())
                .paddingLeft(getPaddingLeft())
                .paddingRight(getPaddingRight())
                .iconSizeAdditional(iconSizeAdditional)
                .build();
        floatLikeView.setAttachedView(this);
        floatingTextWrapper.bringToFront();
        floatingTextWrapper.addView(floatLikeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        floatLikeView.start(floatingTextWrapper);
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
        invalidate();
        requestLayout();
    }

    public void setHasLike(boolean hasLike) {
        this.hasLike = hasLike;
        invalidate();
    }

    public float getIconScaleSelect() {
        return iconScaleSelect;
    }

    public void setIconScaleSelect(float iconScaleSelect) {
        this.iconScaleSelect = iconScaleSelect;
    }

    public float getIconScaleUnSelect() {
        return iconScaleUnSelect;
    }

    public void setIconScaleUnSelect(float iconScaleUnSelect) {
        this.iconScaleUnSelect = iconScaleUnSelect;
    }

    public float getShiningScale() {
        return shiningScale;
    }

    public void setShiningScale(float shiningScale) {
        this.shiningScale = shiningScale;
    }

    public int getCountAlphaOut() {
        return countAlphaOut;
    }

    public void setCountAlphaOut(int countAlphaOut) {
        this.countAlphaOut = countAlphaOut;
    }

    public int getCountAlphaIn() {
        return countAlphaIn;
    }

    public void setCountAlphaIn(int countAlphaIn) {
        this.countAlphaIn = countAlphaIn;
    }

    public float getCountFloatOffset() {
        return countFloatOffset;
    }

    public void setCountFloatOffset(float countFloatOffset) {
        this.countFloatOffset = countFloatOffset;
        invalidate();
    }

    public void setOnLikeListeners(OnLikeListeners onLikeListeners) {
        this.onLikeListeners = onLikeListeners;
    }

    public interface OnLikeListeners {
        void like(boolean isCancel);
    }

}
