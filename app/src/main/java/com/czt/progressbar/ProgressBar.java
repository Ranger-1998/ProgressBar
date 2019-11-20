package com.czt.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class ProgressBar extends View {

    private String mInitialText;    //初始状态显示的文字
    private String mPauseText;      //暂停状态显示的文字
    private String mEndText;        //完成状态显示文字

    private float mViewWidth;    //控件真实宽度
    private float mViewHeight;   //控件真实高度

    private int mMaxValue;  //最大的进度值
    private int mCurValue;  //当前进度值

    private float mTextSize;        //文字大小
    private float mNumberTextSize;  //数字大小

    private int mTextColor;         //文字颜色
    private int mNumberTextColor;   //数字颜色
    private int mProgressBarColor;  //ProgressBar边框和填充颜色

    private ProgressState mProgressState = ProgressState.ON_INITIAL;   //当前状态

    private Paint mNumberTextPaint;  //数字画笔
    private Paint mTextPaint;        //文字画笔
    private Paint mProgressBarFillPaint;     //填充画笔
    private Paint mProgressBarStrokePaint;   //描边画笔

    private float mProgressStrokeWidth;  //描边宽度

    private float mNumberPadding;    //数字的左右padding

    private Path mProgressFillPath;  //填充路径
    private Path mProgressStrokePath;    //描边路径
    private Path mIntersectPath;     //填充路径和描边路径的交集，

    private RectF mProgressRect; //view绘制的边框矩形，用于判断view边界。
    private float mLastOffset = 0;   //用于记录progressFillPath上一个偏移量

    private static final float DEF_NUMBER_PADDING = 10;
    private static final float DEF_STROKE_WIDTH = 2;
    private static final float DEF_TEXT_SIZE = 15;
    private static final float DEF_NUMBER_SIZE = 15;
    private static final int DEF_TEXT_COLOR = 0;
    private static final int DEF_NUMBER_COLOR = 0;
    private static final int DEF_BAR_COLOR = 0;
    private static final int DEF_MAX_VAL = 100;
    private static final int DEF_CUR_VAL = 0;

    private static final float DEF_OFFSET_Y = 0;
    private static final int DEF_RATIO = 100; //百分比的比值

    public ProgressBar(Context context) {
        this(context, null);
    }

    public ProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar);

        mNumberPadding = typedArray.getDimension(R.styleable.ProgressBar_numberPadding,
                DEF_NUMBER_PADDING);

        mProgressStrokeWidth = typedArray.getDimension(R.styleable.ProgressBar_progressBarStrokeWidth,
                DEF_STROKE_WIDTH);

        mInitialText = typedArray.getString(R.styleable.ProgressBar_progressBeginText);
        mPauseText = typedArray.getString(R.styleable.ProgressBar_progressPauseText);
        mEndText = typedArray.getString(R.styleable.ProgressBar_progressEndText);

        mTextSize = typedArray.getDimension(R.styleable.ProgressBar_progressTextSize, DEF_TEXT_SIZE);
        mNumberTextSize = typedArray.getDimension(R.styleable.ProgressBar_progressNumberTextSize,
                DEF_NUMBER_SIZE);

        mTextColor = typedArray.getColor(R.styleable.ProgressBar_progressTextColor, DEF_TEXT_COLOR);
        mNumberTextColor = typedArray.getColor(R.styleable.ProgressBar_progressNumberTextColor, DEF_NUMBER_COLOR);
        mProgressBarColor = typedArray.getColor(R.styleable.ProgressBar_progressBarColor, DEF_BAR_COLOR);

        mMaxValue = typedArray.getInteger(R.styleable.ProgressBar_maxValue, DEF_MAX_VAL);
        mCurValue = typedArray.getInteger(R.styleable.ProgressBar_curValue, DEF_CUR_VAL);

        typedArray.recycle();

        initPaint();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);

        mNumberTextPaint = new Paint();
        mNumberTextPaint.setTextSize(mNumberTextSize);
        mNumberTextPaint.setColor(mNumberTextColor);
        mNumberTextPaint.setAntiAlias(true);
        mNumberTextPaint.setDither(true);
        mNumberTextPaint.setTextAlign(Paint.Align.CENTER);

        mProgressBarFillPaint = new Paint();
        mProgressBarFillPaint.setColor(mProgressBarColor);
        mProgressBarFillPaint.setStyle(Paint.Style.FILL);
        mProgressBarFillPaint.setAntiAlias(true);
        mProgressBarFillPaint.setDither(true);

        mProgressBarStrokePaint = new Paint();
        mProgressBarStrokePaint.setColor(mProgressBarColor);
        mProgressBarStrokePaint.setStyle(Paint.Style.STROKE);
        mProgressBarStrokePaint.setStrokeWidth(mProgressStrokeWidth);
        mProgressBarStrokePaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mProgressState) {
            case ON_INITIAL:
                drawText(canvas, mInitialText);
                break;
            case ON_PROGRESS:
                drawProgress(canvas);
                break;
            case ON_PAUSE:
                drawText(canvas, mPauseText);
                break;
            case ON_END:
                drawText(canvas, mEndText);
                break;
        }
    }

    /**
     * 绘制文字
     * @param canvas 画布
     * @param text 文字
     */
    private void drawText(Canvas canvas, String text) {
        canvas.drawPath(mProgressStrokePath, mProgressBarStrokePaint);
        Paint.FontMetrics metrics = new Paint.FontMetrics();
        mTextPaint.getFontMetrics(metrics);
        float offset = (metrics.descent + metrics.ascent) / 2;
        canvas.drawText(text, mProgressRect.centerX(), mProgressRect.centerY() - offset, mTextPaint);
    }

    /**
     * 绘制进度
     * @param canvas 画布
     */
    private void drawProgress(Canvas canvas) {
        canvas.drawPath(mProgressStrokePath, mProgressBarStrokePaint);

        int curProgress = (mCurValue * DEF_RATIO) / mMaxValue;
        float curWidth = mProgressRect.width() * curProgress / DEF_RATIO;

        mProgressFillPath.offset(curWidth - mLastOffset, DEF_OFFSET_Y);
        mLastOffset = curWidth;

        mIntersectPath.op(mProgressStrokePath, mProgressFillPath, Path.Op.INTERSECT);
        canvas.drawPath(mIntersectPath, mProgressBarFillPaint);



        float numberX, numberY;
        String numberText = curProgress + "%";
        float textWidth = getTextWidth(mNumberTextPaint, numberText);
        float defNumberX = mNumberPadding + getPaddingLeft() + textWidth / 2;
        numberX = curWidth - textWidth / 2 - mNumberPadding;
        if (numberX < defNumberX) numberX = defNumberX;

        Paint.FontMetrics metrics = mNumberTextPaint.getFontMetrics();
        float offset = (metrics.descent + metrics.ascent) / 2;
        numberY = mProgressRect.centerY() - offset;
        canvas.drawText(numberText, numberX, numberY, mNumberTextPaint);
    }

    /**
     * 更新进度
     * @param progress 进度值
     */
    public void updateProgress(int progress) {
        if (progress <= mMaxValue) {
            mCurValue = progress;
            invalidate();
        }
    }

    /**
     * 获取文字宽度
     * @param paint 画笔
     * @param text 文字
     * @return 文字宽度
     */
    private float getTextWidth(Paint paint, String text) {
        return paint.measureText(text);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mProgressRect = new RectF(getPaddingTop() + mProgressStrokeWidth / 2,
                getPaddingLeft() + mProgressStrokeWidth / 2,
                w - getPaddingRight() - mProgressStrokeWidth / 2,
                h - getPaddingBottom() - mProgressStrokeWidth / 2);
        mViewHeight = h;
        mViewWidth = w;
        initPath();
    }

    /**
     * 初始化Path
     */
    private void initPath() {
        mProgressStrokePath = new Path();
        mProgressStrokePath.addRoundRect(mProgressRect, mProgressRect.height() / 2,
                mProgressRect.height() / 2, Path.Direction.CCW);

        mProgressFillPath = new Path(mProgressStrokePath);
        mProgressFillPath.offset(-mProgressRect.width(), DEF_OFFSET_Y);
        mIntersectPath = new Path();

    }

    /**
     * 进度状态
     */
    public enum ProgressState {
        ON_INITIAL,
        ON_PROGRESS,
        ON_PAUSE,
        ON_END
    }

    /**
     * 设置进度状态
     * @param progressState 进度状态
     */
    public void setProgressState(ProgressState progressState) {
        this.mProgressState = progressState;
        invalidate();
    }

    public String getInitialText() {
        return mInitialText;
    }

    public void setInitialText(String mInitialText) {
        this.mInitialText = mInitialText;
        invalidate();
    }

    public String getPauseText() {
        return mPauseText;
    }

    public void setPauseText(String mPauseText) {
        this.mPauseText = mPauseText;
        invalidate();
    }

    public String getEndText() {
        return mEndText;
    }

    public void setEndText(String mEndText) {
        this.mEndText = mEndText;
        invalidate();
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int mMaxValue) {
        this.mMaxValue = mMaxValue;
        invalidate();
    }

    public int getCurValue() {
        return mCurValue;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float mTextSize) {
        this.mTextSize = mTextSize;
        mTextPaint.setTextSize(mTextSize);
        invalidate();
    }

    public float getNumberTextSize() {
        return mNumberTextSize;
    }

    public void setNumberTextSize(float numberTextSize) {
        this.mNumberTextSize = numberTextSize;
        mNumberTextPaint.setTextSize(numberTextSize);
        invalidate();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        mTextPaint.setColor(textColor);
        invalidate();
    }

    public int getNumberTextColor() {
        return mNumberTextColor;
    }

    public void setNumberTextColor(int numberTextColor) {
        this.mNumberTextColor = numberTextColor;
        mNumberTextPaint.setColor(numberTextColor);
        invalidate();
    }

    public int getProgressBarColor() {
        return mProgressBarColor;
    }

    public void setProgressBarColor(int progressBarColor) {
        this.mProgressBarColor = progressBarColor;
        mProgressBarFillPaint.setColor(progressBarColor);
        mProgressBarStrokePaint.setColor(progressBarColor);
        invalidate();
    }

    public ProgressState getProgressState() {
        return mProgressState;
    }


    public float getProgressStrokeWidth() {
        return mProgressStrokeWidth;
    }

    public void setProgressStrokeWidth(float progressStrokeWidth) {
        this.mProgressStrokeWidth = progressStrokeWidth;
        mProgressRect = new RectF(getPaddingTop() + mProgressStrokeWidth / 2,
                getPaddingLeft() + mProgressStrokeWidth / 2,
                mViewWidth - getPaddingRight() - mProgressStrokeWidth / 2,
                mViewHeight - getPaddingBottom() - mProgressStrokeWidth / 2);
        mProgressBarStrokePaint.setStrokeWidth(mProgressStrokeWidth);
        initPath();
        mProgressFillPath.offset(mLastOffset, DEF_OFFSET_Y);
        invalidate();
    }

    public float getNumberPadding() {
        return mNumberPadding;
    }

    public void setNumberPadding(float mNumberPadding) {
        this.mNumberPadding = mNumberPadding;
        invalidate();
    }
}
