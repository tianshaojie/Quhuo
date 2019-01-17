package cn.skyui.module.ugc.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by tiansj on 2017/12/3.
 */

public class SquareRelativeLayout extends RelativeLayout {

    public SquareRelativeLayout(Context context) {
        super(context);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
//
//        int size;
//        if (widthMode == View.MeasureSpec.EXACTLY && widthSize > 0) {
//            size = widthSize;
//        } else if (heightMode == View.MeasureSpec.EXACTLY && heightSize > 0) {
//            size = heightSize;
//        } else {
//            size = widthSize < heightSize ? widthSize : heightSize;
//        }
//
//        int finalMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY);
//        super.onMeasure(finalMeasureSpec, finalMeasureSpec);
    }
}
