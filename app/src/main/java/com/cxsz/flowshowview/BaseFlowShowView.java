package com.cxsz.flowshowview;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/**
 * 流量显示视图基类
 */
public abstract class BaseFlowShowView extends View {

    //控件宽高
    protected int mWidth;
    protected int mHeight;
    //边距
    protected float mPadding;
    //仪表盘盘半径
    protected int mRadius;
    //圆环最小值和最大值
    private int mMin;
    private int mMax;
    //圆环角度
    public float mArcStartAngle;
    protected float mArcSweepAngle;
    //大小刻度的数量
    protected int mLargeCalibrationNumber;
    protected int mLargeBetweenCalibrationNumber;
    //刻度总数
    protected int mCalibrationTotalNumber;
    //每个刻度之间的角度
    protected float mLargeCalibrationBetweenAngle;
    protected float mSmallCalibrationBetweenAngle;
    //刻度文本
    protected int[] mCalibrationNumberText;
    protected String[] mCalibrationBetweenText;
    //当前值
    private int mValue;
    private String unitInfo;
    //当前值所在区域
    private String mValueLevel;
    //设置的时间
    private String mDateStr;
    //时间字符串显示模板
    private String mDateStrPattern;
    //数值等级显示模板
    private String mValueLevelPattern;
    //动画时长
    private long mProgressAnimTime;
    //当前进度的角度
    public float mProgressSweepAngle;
    //文字之间的间距
    protected int mTextSpacing;
    //数值画笔
    protected Paint mPaintValue;
    //时间画笔
    protected Paint mPaintDate;

    // 圆环起始角度
    private final static float DEFAULT_ARC_START_ANGLE = 165f;
    // 圆环范围大小
    private final static float DEFAULT_ARC_SWEEP_ANGLE = 210f;
    // 进度动画时长
    private final static long DEFAULT_PROGRESS_ANIM_TIME = 2500;
    // 默认边距
    private final static int DEFAULT_PADDING = 30;
    // 默认刻度文字
    private final static int[] DEFAULT_CALIBRATION_NUMBER = new int[]{0, 400, 600, 800};
    private final static String[] DEFAULT_CALIBRATION_BETWEEN = new String[]{};
    // 默认刻度数量
    private final static int DEFAULT_LARGE_BETWEEN_CALIBRATION_NUMBER = 3;
    // 默认控件大小
    private final static int DEFAULT_SIZE = 250;
    //中间文字之间的间距
    private static final int DEFAULT_TEXT_SPACING = 7;
    // 数值画笔属性
    private final static float DEFAULT_VALUE_TEXT_SIZE = 22f;
    private final static int DEFAULT_VALUE_TEXT_COLOR = Color.WHITE;
    // 时间画笔属性
    private final static float DEFAULT_DATE_TEXT_SIZE = 12f;
    private final static int DEFAULT_DATE_TEXT_COLOR = Color.WHITE;


    public BaseFlowShowView(Context context) {
        this(context, null);
    }

    public BaseFlowShowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseFlowShowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //初始化
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        //默认数值
        mTextSpacing = dp2px(DEFAULT_TEXT_SPACING);
        mCalibrationNumberText = DEFAULT_CALIBRATION_NUMBER;
        mCalibrationBetweenText = DEFAULT_CALIBRATION_BETWEEN;
        mMin = mCalibrationNumberText[0];
        mMax = mCalibrationNumberText[mCalibrationNumberText.length - 1];
        mArcStartAngle = DEFAULT_ARC_START_ANGLE;
        mArcSweepAngle = DEFAULT_ARC_SWEEP_ANGLE;
        mProgressAnimTime = DEFAULT_PROGRESS_ANIM_TIME;
        mLargeCalibrationNumber = mCalibrationNumberText.length;
        mLargeBetweenCalibrationNumber = DEFAULT_LARGE_BETWEEN_CALIBRATION_NUMBER;
        //计算刻度的相关数据
        resetCalibrationData();

        //初始化画笔
        //数值画笔
        mPaintValue = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintValue.setTextAlign(Paint.Align.CENTER);
        mPaintValue.setTextSize(sp2px(DEFAULT_VALUE_TEXT_SIZE));
        mPaintValue.setColor(DEFAULT_VALUE_TEXT_COLOR);

        //时间画笔
        mPaintDate = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintDate.setTextAlign(Paint.Align.CENTER);
        mPaintDate.setTextSize(sp2px(DEFAULT_DATE_TEXT_SIZE));
        mPaintDate.setColor(DEFAULT_DATE_TEXT_COLOR);

        //初始化
        initView();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //屏幕宽高
        mWidth = w;
        mHeight = h;
        //半径
        mRadius = mWidth / 2;

        //初始化圆环
        initArcRect(mPadding, mPadding, mWidth - mPadding, mWidth - mPadding);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = dp2px(DEFAULT_SIZE);
        mPadding = Math.max(Math.max(getPaddingLeft(), getPaddingTop()),
                Math.max(getPaddingRight(), getPaddingBottom()));
        mPadding = Math.max(dp2px(DEFAULT_PADDING), mPadding);

        setMeasuredDimension(measureSize(widthMeasureSpec, size), measureSize(heightMeasureSpec, size));
    }

    /**
     * 判断当前控件宽高类型
     */
    private int measureSize(int measureSpec, int defaultSize) {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                defaultSize = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        return defaultSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制圆环
        drawArc(canvas, mArcStartAngle, mArcSweepAngle);

        //绘制进度圆环
        drawProgressArc(canvas, mArcStartAngle, mProgressSweepAngle);
        //绘制文本
        drawText(canvas, mValue, mValueLevel, mDateStr);
    }

    /**
     * 设置当前值
     */
    public void setValue(int value) {
        setValue(value, false, false);
    }

    /**
     * 设置当前值
     *
     * @param value  当前数字
     * @param isAnim 是否开启动画
     * @param reset  true:从min开始进行动画 / false:从当前值开始绘制
     */
    public void setValue(int value, boolean isAnim, boolean reset) {
        value = value < mMin ? mMin : value > mMax ? mMax : value;
        //计算进度需要转动的角度
        float progressSweepAngle = computeProgressSweepAngle(value);
        mDateStr = getCurrentTime();
        //如果开启动画
        if (isAnim) {
            startProgressAnim(value, progressSweepAngle, reset);
        } else {
            mValue = value;
            mProgressSweepAngle = progressSweepAngle;
            postInvalidate();
        }
    }

    public String getUnitInfo() {
        return unitInfo;
    }

    public void setUnitInfo(String unitInfo) {
        this.unitInfo = unitInfo;
    }

    /**
     * 计算需要进度条需要转动的角度
     */
    private float computeProgressSweepAngle(int value) {
        mValueLevel = "";
        //如果小于最小值
        if (value <= mMin) {
            return 0;
        }
        //如果大于最大值
        if (value >= mMax) {
            return mArcSweepAngle;
        }
        //计算其他情况
        int index = findValueInterval(value);
        //如果不在范围内的
        if (index == -1) {
            return ((mValue - mMin) / (mMax - mMin)) * mArcSweepAngle;
        }
        index--;
        float angle = mLargeCalibrationBetweenAngle * index;
        float intervalMin = mCalibrationNumberText[index];
        float intervalMax = mCalibrationNumberText[index + 1];
        return angle + ((value - intervalMin) / (intervalMax - intervalMin)) * mLargeCalibrationBetweenAngle;
    }

    /**
     * 寻找value所在区间
     */
    private int findValueInterval(int value) {
        int i = -1;
        //是否有区间数据
        if (mCalibrationNumberText != null && mCalibrationNumberText.length > 0) {
            for (int j = 0; j < mCalibrationNumberText.length; j++) {
                if (mCalibrationNumberText[j] > value) {
                    return j;
                }
            }
        }
        return i;
    }

    /**
     * 启动进度动画
     */
    private void startProgressAnim(int value, float progressSweepAngle, boolean reset) {
        //启动角度变动动画
        float angle = reset ? 0 : mProgressSweepAngle;
        ValueAnimator angleAnim = ValueAnimator.ofFloat(angle, progressSweepAngle);
        angleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        angleAnim.setDuration(mProgressAnimTime);
        angleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //设置当进度
                mProgressSweepAngle = (float) valueAnimator.getAnimatedValue();
            }
        });
        angleAnim.start();
        int start = reset ? mMin : mValue;
        //启动文字变动动画
        ValueAnimator valueAnimator = ValueAnimator.ofInt(start, value);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(mProgressAnimTime);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //设置当进度
                mValue = (int) valueAnimator.getAnimatedValue();

                postInvalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 重置角度信息
     */
    private void resetCalibrationData() {
        //计算总共有多少个刻度
        mCalibrationTotalNumber = (mLargeCalibrationNumber - 1) * mLargeBetweenCalibrationNumber + mLargeCalibrationNumber;
        //计算每个大刻度之间的角度
        mLargeCalibrationBetweenAngle = mArcSweepAngle / (mLargeCalibrationNumber - 1);
        //计算小刻度之间的角度
        mSmallCalibrationBetweenAngle = mArcSweepAngle / (mCalibrationTotalNumber - 1);
    }

    /**
     * 获取当前时间
     */
    protected String getCurrentTime() {
        return getInfo();
    }

    private String info;

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    /**
     * 设置进度条动画时长
     */
    public void setProgressAnimTime(long time) {
        mProgressAnimTime = time;
    }

    /**
     * 设置圆环角度
     */
    public void setArcAngle(float arcStartAngle, float arcSweepAngle) {
        mArcStartAngle = arcStartAngle;
        mArcSweepAngle = arcSweepAngle;

        resetCalibrationData();

        postInvalidate();
    }

    /**
     * 设置刻度信息
     */
    public void setCalibration(int[] calibrationNumberText, String[] calibrationBetweenText, int largeCalibrationBetweenNumber) {
        if (calibrationNumberText == null || calibrationNumberText.length < 2 ||
                calibrationNumberText[0] >= calibrationNumberText[calibrationNumberText.length - 1]) {
            return;
        }
        //设置刻度数量数据
        mLargeCalibrationNumber = calibrationNumberText.length;
        mLargeBetweenCalibrationNumber = largeCalibrationBetweenNumber;
        //计算刻度相关数据
        resetCalibrationData();
        //设置刻度对应的显示数据
        mCalibrationNumberText = calibrationNumberText;
        mCalibrationBetweenText = calibrationBetweenText;
        mMin = mCalibrationNumberText[0];
        mMax = mCalibrationNumberText[mCalibrationNumberText.length - 1];

        postInvalidate();
    }

    /**
     * 设置文字之间的间隔
     */
    public void setTextSpacing(int spacingDp) {
        mTextSpacing = dp2px(spacingDp);

        postInvalidate();
    }

    /**
     * 设置数值画笔
     */
    public void setValuePaint(float spSize, @ColorInt int color) {
        mPaintValue.setTextSize(sp2px(spSize));
        mPaintValue.setColor(color);

        postInvalidate();
    }

    /**
     * 设置时间画笔
     */
    public void setDatePaint(float spSize, @ColorInt int color) {
        mPaintDate.setTextSize(sp2px(spSize));
        mPaintDate.setColor(color);

        postInvalidate();
    }

    /**
     * 设置时间的显示格式
     *
     * @param pattern 格式(如: 评估时间：{date}) {date}为占位符
     */
    public void setDateStrPattern(String pattern) {
        mDateStrPattern = pattern;
    }

    /**
     * 设置数值等级的模板
     *
     * @param pattern 格式(如: 信用{level}) {level}为占位符
     */
    public void setValueLevelPattern(String pattern) {
        mValueLevelPattern = pattern;
    }

    /**
     * 获取最大值
     */
    public int getMax() {
        return mMax;
    }

    /**
     * 获取最小值
     */
    public int getMin() {
        return mMin;
    }

    /**
     * 获取当前值
     */
    public int getValue() {
        return mValue;
    }

    /**
     * dp2px
     */
    protected int dp2px(float dpValue) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * sp2px
     */
    protected int sp2px(float spValue) {
        float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 测量画笔高度
     */
    protected float getPaintHeight(Paint paint, String text) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.height();
    }

    protected abstract void initView();

    protected abstract void initArcRect(float left, float top, float right, float bottom);

    protected abstract void drawArc(Canvas canvas, float arcStartAngle, float arcSweepAngle);

    protected abstract void drawProgressArc(Canvas canvas, float arcStartAngle, float progressSweepAngle);

    protected abstract void drawText(Canvas canvas, int value, String valueLevel, String currentTime);

}
