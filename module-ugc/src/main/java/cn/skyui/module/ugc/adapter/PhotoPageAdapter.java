package cn.skyui.module.ugc.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.image.picker.widget.photoview.OnPhotoTapListener;
import cn.skyui.library.image.picker.widget.photoview.PhotoView;
import cn.skyui.library.utils.ScreenUtils;
import cn.skyui.module.ugc.R;

import java.util.ArrayList;

/**
 * Created by tiansj on 2018/4/11.
 */

public class PhotoPageAdapter extends PagerAdapter {

    private Activity mActivity;
    private ArrayList<String> images = new ArrayList<>();
    private PhotoClickListener listener;

    public PhotoPageAdapter(Activity activity, ArrayList<String> images) {
        this.mActivity = activity;
        this.images = images;
    }

    public void setData(ArrayList<String> images) {
        this.images = images;
    }

    public void setPhotoViewClickListener(PhotoClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(mActivity);
        GlideApp.with(mActivity)
                .load(ImageConstants.getOriginUrl(images.get(position)))
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_default_image)
                        .error(R.drawable.ic_default_image)
                        .override(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight()) //指定图片大小(像素)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(photoView);
        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                if (listener != null) {
                    listener.OnPhotoClickListener(view, x, y);
                }
            }
        });
        container.addView(photoView);
        return photoView;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface PhotoClickListener {
        void OnPhotoClickListener(View view, float v, float v1);
    }
}