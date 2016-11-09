package transition.chen.nought.com.transitiondemo;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeImageTransform;
import android.view.View;
import android.widget.ImageView;

/**
 * Android 5.0及以上：采用Activity的shared elements transition即可实现。
 * Android 5.0以下：手动使用属性动画实现。
 * <p>
 * 注意：
 * 1.为了坐标计算方便，这里全部采用了全屏的Activity，省得再去计算StatusBar和Toolbar的高度。
 * 2.这里只演示动画的制作，所以把case简化到最低，只去加载本地的图片。
 */
public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    private int mImageResId = R.drawable.biu; // 实际应用中应该在第一个界面里面就把图片缓存好。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageViewMain);
        mImageView.setImageResource(mImageResId);
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.btnLoL:
                transitionOnAndroidLoL();
                break;
            case R.id.btnCompat:
                transitionOnAndroidIceCream();
                break;
        }
    }

    /**
     * shared elements transition
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void transitionOnAndroidLoL() {
        // 把需要共享的元素-ImageView，传给第二个界面
        Intent intent = new Intent(MainActivity.this, DetailActivityLollipop.class);
        String shareElementName = "sharedImageView";
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(this, mImageView, shareElementName);
        getWindow().setSharedElementEnterTransition(new ChangeImageTransform(this, null));
        intent.putExtra(DetailActivityLollipop.SHARED_ELEMENT_KEY, shareElementName);
        intent.putExtra(DetailActivityLollipop.IMAGE_RES_ID, mImageResId);
        // 打开它
        startActivity(intent, activityOptions.toBundle());
    }

    /**
     * 属性动画Animation
     *
     * 在第一个界面需要保存当前ImageView的状态，用于第二个界面中恢复它：
     * 保存当前ImageView在屏幕中的位置
     * 保存当前ImageView自身的大小
     * 保存当前ImageView的ScaleType
     */
    private void transitionOnAndroidIceCream() {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        Rect rect = new Rect();
        mImageView.getGlobalVisibleRect(rect);
        intent.putExtra(DetailActivity.IMAGE_ORIGIN_RECT, rect);
        intent.putExtra(DetailActivity.IMAGE_SCALE_TYPE, mImageView.getScaleType());
        intent.putExtra(DetailActivity.IMAGE_RES_ID, mImageResId);

        // 打开第二个界面，要屏蔽Activity的默认转场效果
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
