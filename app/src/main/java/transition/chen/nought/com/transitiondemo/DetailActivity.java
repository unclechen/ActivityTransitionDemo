package transition.chen.nought.com.transitiondemo;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 兼容4.x的实现。
 */
public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    public static final String IMAGE_RES_ID = "imageResId";
    public static final String IMAGE_ORIGIN_RECT = "originRect";
    public static final String IMAGE_SCALE_TYPE = "scaleType";
    private static final AccelerateDecelerateInterpolator DEFAULT_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    protected static final long IMAGE_TRANSLATION_DURATION = 500;

    private Rect mRect; // 传过来的第一个界面ImageView的位置和大小
    private ImageView mSourceImageView; // 这个页面里用于复原第一个界面的ImageView
    private ImageView mTargetImageView; // 这个页面最终要展示图片的ImageView
    private LinearLayout mContainer;
    private int mResId;
    private float[] mInitImageMatrixValues = new float[9];
    private AnimatorSet mEnteringAnimation;
    private AnimatorSet mExitingAnimation;
    private int mTargetLeft, mTargetTop, mTargetWidth, mTargetHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail); // 布局代码和5.0共用一个
        mTargetImageView = (ImageView) findViewById(R.id.imageDetail);
        mContainer = (LinearLayout) findViewById(R.id.activityContanierDetail);
        initSourceImageView();
        initImageEnterAnimation();
    }

    /** 图片进入的转场动画 */
    private void initImageEnterAnimation() {
        mTargetImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                mTargetImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                final int[] finalLocationOnTheScreen = new int[2];
                mTargetImageView.getLocationOnScreen(finalLocationOnTheScreen);
                mTargetLeft = finalLocationOnTheScreen[0];
                mTargetTop = finalLocationOnTheScreen[1];
                mTargetWidth = mTargetImageView.getWidth();
                mTargetHeight = mTargetImageView.getHeight();
                playEnteringAnimation(mTargetLeft, mTargetTop, mTargetWidth, mTargetHeight);
                return true;
            }
        });
    }

    // 属性动画走起，为了将sourceImageView变换到targetImageView，需要用到：位移、比例、渐变
    private void playEnteringAnimation(final int left, final int top, final int width, final int height) {
        // 1.改变ImageView的位置、宽高
        PropertyValuesHolder propertyLeft = PropertyValuesHolder.ofInt("left", mSourceImageView.getLeft(), left);
        PropertyValuesHolder propertyTop = PropertyValuesHolder.ofInt("top", mSourceImageView.getTop(), top);
        PropertyValuesHolder propertyRight = PropertyValuesHolder.ofInt("right", mSourceImageView.getRight(), left + width);
        PropertyValuesHolder propertyBottom = PropertyValuesHolder.ofInt("bottom", mSourceImageView.getBottom(), top + height);

        ObjectAnimator positionAnimator = ObjectAnimator.ofPropertyValuesHolder(mSourceImageView,
                propertyLeft, propertyTop, propertyRight, propertyBottom);
        positionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // 为了退出动画，需要把sourceImageView的LayoutParams改成targetImageView
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSourceImageView.getLayoutParams();
                layoutParams.height = height;
                layoutParams.width = width;
                layoutParams.setMargins(left, top, 0, 0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        // 2.ImageView的矩阵动画
        Matrix initMatrix = getImageMatrix(mSourceImageView);
        initMatrix.getValues(mInitImageMatrixValues);
        final Matrix endMatrix = getImageMatrix(mTargetImageView);
        mSourceImageView.setScaleType(ImageView.ScaleType.MATRIX);
        // ofObject()用法：传入自定义Property和Evaluator的用法
        ObjectAnimator matrixAnimator = ObjectAnimator.ofObject(mSourceImageView, ANIMATED_TRANSFORM_PROPERTY, new MatrixEvaluator(), initMatrix, endMatrix);

        // 3.顺便加个渐变动画
        ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(mContainer, "alpha", 0.0f, 1.0f);

        // 4.一起播放上面的动画
        mEnteringAnimation = new AnimatorSet();
        mEnteringAnimation.setDuration(IMAGE_TRANSLATION_DURATION);
        mEnteringAnimation.setInterpolator(DEFAULT_INTERPOLATOR);
        mEnteringAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(Animator animation) {
                mEnteringAnimation = null;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mEnteringAnimation != null) {
                    mEnteringAnimation = null;
                    mTargetImageView.setVisibility(View.VISIBLE);
                    mSourceImageView.setVisibility(View.INVISIBLE);
                }
            }
        });

        mEnteringAnimation.playTogether(positionAnimator, matrixAnimator, fadeInAnimator);
        mEnteringAnimation.start();
    }

    // 自定义的计算器
    static class MatrixEvaluator implements TypeEvaluator<Matrix> {

        public static TypeEvaluator<Matrix> NULL_MATRIX_EVALUATOR = new TypeEvaluator<Matrix>() {
            @Override
            public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
                return null;
            }
        };

        float[] mTempStartValues = new float[9];

        float[] mTempEndValues = new float[9];

        Matrix mTempMatrix = new Matrix();

        @Override
        public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
            startValue.getValues(mTempStartValues);
            endValue.getValues(mTempEndValues);
            for (int i = 0; i < 9; i++) {
                float diff = mTempEndValues[i] - mTempStartValues[i];
                mTempEndValues[i] = mTempStartValues[i] + (fraction * diff);
            }
            mTempMatrix.setValues(mTempEndValues);

            return mTempMatrix;
        }
    }

    /**
     * 自定义属性，当Evaluator返回了新的值时，会调用这里的setter方法，因此我们需要实现set方法。
     * 而getter方法只会在ofObject()方法中的可变长参数为1个时，才会调用，这里不存在这种情况，所以get直接返回null。
     */
    private static final Property<ImageView, Matrix> ANIMATED_TRANSFORM_PROPERTY = new Property<ImageView, Matrix>(Matrix.class,
            "animatedTransform") {

        @Override
        public void set(ImageView imageView, Matrix matrix) {
            Drawable drawable = imageView.getDrawable();
            if (drawable == null) {
                return;
            }
            if (matrix == null) {
                drawable.setBounds(0, 0, imageView.getWidth(), imageView.getHeight());
            } else {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                Matrix drawMatrix = imageView.getImageMatrix();
                if (drawMatrix == null) {
                    drawMatrix = new Matrix();
                    imageView.setImageMatrix(drawMatrix);
                }
                imageView.setImageMatrix(matrix);
            }
            imageView.invalidate();
        }

        @Override
        public Matrix get(ImageView object) {
            return null;
        }
    };

    public static Matrix getImageMatrix(ImageView imageView) {
        Log.d(TAG, "getImageMatrix, imageView " + imageView);

        int left = imageView.getLeft();
        int top = imageView.getTop();
        int right = imageView.getRight();
        int bottom = imageView.getBottom();

        Rect bounds = new Rect(left, top, right, bottom);

        Drawable drawable = imageView.getDrawable();

        Matrix matrix;
        ImageView.ScaleType scaleType = imageView.getScaleType();
        Log.d(TAG, "getImageMatrix, scaleType " + scaleType);

        if (scaleType == ImageView.ScaleType.FIT_XY) {
            matrix = imageView.getImageMatrix();
            if (!matrix.isIdentity()) {
                matrix = new Matrix(matrix);
            } else {
                int drawableWidth = drawable.getIntrinsicWidth();
                int drawableHeight = drawable.getIntrinsicHeight();
                if (drawableWidth > 0 && drawableHeight > 0) {
                    float scaleX = ((float) bounds.width()) / drawableWidth;
                    float scaleY = ((float) bounds.height()) / drawableHeight;
                    matrix = new Matrix();
                    matrix.setScale(scaleX, scaleY);
                } else {
                    matrix = null;
                }
            }
        } else {
            matrix = new Matrix(imageView.getImageMatrix());
        }

        return matrix;
    }


    // 创建一个和第一个界面一模一样的ImageView，作为这个界面的sourceImageView
    private void initSourceImageView() {
        // 先动态创建出这个sourceImageView，把它添加到第二个界面的ContentView中。
        FrameLayout contentView = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);
        mSourceImageView = new ImageView(this);
        contentView.addView(mSourceImageView);

        // 读取第一个界面传过来的信息
        Bundle bundle = getIntent().getExtras();
        mRect = (Rect) getIntent().getParcelableExtra(IMAGE_ORIGIN_RECT);
        ImageView.ScaleType scaleType = (ImageView.ScaleType) bundle.getSerializable(IMAGE_SCALE_TYPE);
        mResId = bundle.getInt(IMAGE_RES_ID);

        // 设置为和第一个界面一样的图片
        mSourceImageView.setImageResource(mResId);
        mTargetImageView.setImageResource(mResId);

        // 设为和原来一样的裁剪模式
        mSourceImageView.setScaleType(scaleType);

        // 设置为和原来一样的位置
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSourceImageView.getLayoutParams();
        layoutParams.width = mRect.width();
        layoutParams.height = mRect.height();
        layoutParams.setMargins(mRect.left, mRect.top, 0, 0);
    }

    @Override
    public void onBackPressed() {
        if (mEnteringAnimation != null) {
            mEnteringAnimation.cancel();
        }

        playExitAnimations(mRect.left, mRect.top, mRect.width(), mRect.height(), mInitImageMatrixValues);
    }

    /** 图片退出的转场动画：完全是和之前相反的过程 */
    private void playExitAnimations(int sourceImageViewLeft, int sourceImageViewTop, int sourceImageViewWidth, int sourceImageViewHeight, float[] imageMatrixValues) {
        mSourceImageView.setVisibility(View.VISIBLE);
        mTargetImageView.setVisibility(View.INVISIBLE);

        // 改变SourceView的位置、宽高属性
        int[] locationOnScreen = new int[2];
        mSourceImageView.getLocationOnScreen(locationOnScreen);
        PropertyValuesHolder propertyLeft = PropertyValuesHolder.ofInt("left", locationOnScreen[0], sourceImageViewLeft);
        PropertyValuesHolder propertyTop = PropertyValuesHolder.ofInt("top", locationOnScreen[1], sourceImageViewTop);
        PropertyValuesHolder propertyRight = PropertyValuesHolder.ofInt("right", locationOnScreen[0] + mSourceImageView.getWidth(), sourceImageViewLeft + sourceImageViewWidth);
        PropertyValuesHolder propertyBottom = PropertyValuesHolder.ofInt("bottom", mSourceImageView.getBottom(), sourceImageViewTop + sourceImageViewHeight);
        ObjectAnimator positionAnimator = ObjectAnimator.ofPropertyValuesHolder(mSourceImageView, propertyLeft, propertyTop, propertyRight, propertyBottom);

        // ImageView的矩阵动画
        Matrix initialMatrix = getImageMatrix(mSourceImageView);

        Matrix endMatrix = new Matrix();
        endMatrix.setValues(imageMatrixValues);
        mSourceImageView.setScaleType(ImageView.ScaleType.MATRIX);
        ObjectAnimator matrixAnimator = ObjectAnimator.ofObject(mSourceImageView, ANIMATED_TRANSFORM_PROPERTY, new MatrixEvaluator(), initialMatrix, endMatrix);

        // 渐变动画
        ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(mContainer, "alpha", 1.0f, 0.0f);

        mExitingAnimation = new AnimatorSet();
        mExitingAnimation.setDuration(IMAGE_TRANSLATION_DURATION);
        mExitingAnimation.setInterpolator(new AccelerateInterpolator());
        mExitingAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mExitingAnimation != null) {
                    mExitingAnimation = null;
                }
                // 关闭第二个界面
                Activity activity = (Activity) mSourceImageView.getContext();
                activity.finish();
                activity.overridePendingTransition(0, 0); // 同样去掉默认的转场动画
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mExitingAnimation.playTogether(positionAnimator, matrixAnimator, fadeInAnimator);
        mExitingAnimation.start();
    }
}
