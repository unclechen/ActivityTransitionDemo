package transition.chen.nought.com.transitiondemo;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Android 5.0开始提供原生的实现，这里全部用Java代码实现，没有写xml。
 *
 * 更多说明可参考：https://developer.android.com/training/material/animations.html#Transitions
 */
public class DetailActivityLollipop extends AppCompatActivity {

    public static final String SHARED_ELEMENT_KEY = "SHARED_ELEMENT_KEY";
    public static final String IMAGE_RES_ID = "imageResId";
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail); // layout代码和4.x是一样的
        imageView = (ImageView) findViewById(R.id.imageDetail);
        initImageEnterTransition();
    }

    private void initImageEnterTransition() {
        imageView.setVisibility(View.VISIBLE);
        String imageTransitionName = getIntent().getStringExtra(SHARED_ELEMENT_KEY);
        ViewCompat.setTransitionName(imageView, imageTransitionName);

        View mainContainer = findViewById(R.id.activityContanierDetail);
        mainContainer.setAlpha(1.0f);
        int resId = getIntent().getExtras().getInt(IMAGE_RES_ID);
        imageView.setImageResource(resId);
    }
}
